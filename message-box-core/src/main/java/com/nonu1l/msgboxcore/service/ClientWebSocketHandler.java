package com.nonu1l.msgboxcore.service;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nonu1l.commons.utils.JsonUtils;
import com.nonu1l.commons.utils.MapUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@Slf4j
public class ClientWebSocketHandler extends TextWebSocketHandler {

    // --- Core Data Structures ---
    // 1. channelCode -> Set<machineId>
    private final Map<String, Set<String>> channelToMachineIds = new ConcurrentHashMap<>();
    // 2. machineId -> WebSocketSession
    private final Map<String, WebSocketSession> machineIdToSession = new ConcurrentHashMap<>();
    // 3. machineId -> Set<channelCode>
    private final Map<String, Set<String>> machineIdToChannelCodes = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String MACHINE_ID_ATTRIBUTE = "MACHINE_ID";


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("客户端 WebSocket 已连接: session_id={}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        String machineIdFromAttr = (String) session.getAttributes().get(MACHINE_ID_ATTRIBUTE);
        log.info("收到客户端消息: session_id={}, machine_id={}, payload={}", session.getId(),
            Objects.toString(machineIdFromAttr, "N/A"), payload);

        try {
            Map<String, Object> messageJson = objectMapper.readValue(payload, new TypeReference<>() {
            });

            String type = MapUtils.getStringValueIgnoreCase(messageJson, "type");
            // 对于ping和register消息，machineId必须从payload中获取，因为此时可能还未注册
            String machineIdFromPayload = MapUtils.getStringValueIgnoreCase(messageJson, "machineId");

            switch (type) {
                case "register":
                    List<String> channelCodes = MapUtils.getListValueIgnoreCase(messageJson, "channelCodes", String.class);
                    if (StringUtils.hasText(machineIdFromPayload) && !channelCodes.isEmpty()) {
                        register(session, machineIdFromPayload, channelCodes);
                    } else {
                        log.warn("无效的注册消息，machineId 或 channelCodes 为空。 payload={}", payload);
                    }
                    break;
                case "ping":
                    if (isMachineConnected(machineIdFromPayload)) {
                        session.sendMessage(new TextMessage(JsonUtils.toString(Collections.singletonMap("type", "pong"))));
                    } else {
                        log.warn("收到来自未知或未注册 machineId 的 Ping: {}", machineIdFromPayload);
                        // 可以选择性地要求客户端重新注册
                        // session.sendMessage(new TextMessage("{\"type\":\"re-register\"}"));
                    }
                    break;
                default:
                    log.warn("收到未知的消息类型: {}", type);
                    break;
            }

        } catch (IOException e) {
            log.error("解析消息时出错: {}", payload, e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String machineId = (String) session.getAttributes().get(MACHINE_ID_ATTRIBUTE);
        if (machineId != null) {
            unregister(machineId);
            log.info("客户端 WebSocket 连接断开: session_id={}, machine_id={}, status={}", session.getId(), machineId, status);
        } else {
            log.info("未识别的客户端 WebSocket 连接断开: session_id={}, status={}", session.getId(), status);
        }
    }

    private void register(WebSocketSession session, String machineId, List<String> channelCodes) {
        // 如果此 machineId 已存在，先注销旧的连接信息
        if (machineIdToSession.containsKey(machineId)) {
            log.warn("machineId {} 正在重新注册，清理旧的 session 中...", machineId);
            unregister(machineId);
        }

        // 关联 machineId 和 session
        session.getAttributes().put(MACHINE_ID_ATTRIBUTE, machineId);
        machineIdToSession.put(machineId, session);

        // 存储 machineId -> channelCodes 的映射
        Set<String> newCodesForMachine = new CopyOnWriteArraySet<>(channelCodes);
        machineIdToChannelCodes.put(machineId, newCodesForMachine);

        // 存储 channelCode -> machineIds 的映射
        for (String channelCode : channelCodes) {
            channelToMachineIds.computeIfAbsent(channelCode, k -> new CopyOnWriteArraySet<>()).add(machineId);
        }

        log.info("machineId {} (session_id={}) 已注册到通道: {}", machineId, session.getId(), channelCodes);
    }

    private void unregister(String machineId) {
        // 从 machineId -> session 中移除
        WebSocketSession session = machineIdToSession.remove(machineId);
        if (session != null) {
            // 从 session 属性中移除，以防万一
            session.getAttributes().remove(MACHINE_ID_ATTRIBUTE);
        }

        // 从 machineId -> channelCodes 中移除
        Set<String> codes = machineIdToChannelCodes.remove(machineId);

        // 从 channelCode -> machineIds 中移除
        if (codes != null) {
            for (String code : codes) {
                Set<String> machineIds = channelToMachineIds.get(code);
                if (machineIds != null) {
                    machineIds.remove(machineId);
                    if (machineIds.isEmpty()) {
                        channelToMachineIds.remove(code);
                    }
                }
            }
        }
        log.info("machineId {} 已注销。", machineId);
    }

    /**
     * 向指定 ChannelCode 的所有客户端发送消息
     */
    public String sendMessageToChannel(String channelCode, String title, String body) {
        Set<String> machineIds = getMachineIdsByChannelCode(channelCode);

        if (CollUtil.isNotEmpty(machineIds)) {
            log.info("正在向通道 [{}] 的 {} 个客户端发送消息: {}", channelCode, machineIds.size(), title);
            for (String machineId : machineIds) {
                sendMessageToMachine(machineId, title, body, channelCode);
            }

            return CollUtil.join(machineIds,",");
        } else {
            log.warn("通道 [{}] 没有已注册的客户端", channelCode);
        }
        return null;
    }

    /**
     * 向指定 machineId 发送消息
     */
    public boolean sendMessageToMachine(String machineId, String title, String body, String channelCode) {
        WebSocketSession session = machineIdToSession.get(machineId);
        if (session != null && session.isOpen()) {
            try {
                HashMap<String, String> dataMap = new HashMap<>();
                dataMap.put("channelCode", channelCode);
                dataMap.put("title", title);
                dataMap.put("body", body);
                dataMap.put("id", UUID.randomUUID().toString());
                TextMessage textMessage = new TextMessage(JsonUtils.toString(dataMap));
                session.sendMessage(textMessage);
                return true;
            } catch (IOException e) {
                log.error("向 machineId {} 发送消息失败: {}", machineId, e.getMessage());
                return false;
            }
        }
        return false;
    }


    // --- Requested Utility Methods ---

    /**
     * 1. 根据 channelCode，找出所有连接的 machineId
     * @param channelCode The channel code.
     * @return A set of machineIds, or an empty set if none.
     */
    public Set<String> getMachineIdsByChannelCode(String channelCode) {
        return channelToMachineIds.getOrDefault(channelCode, Collections.emptySet());
    }

    /**
     * 2. 根据 machineId，找出其关联的所有 channelCode
     * @param machineId The machine ID.
     * @return A set of channelCodes, or an empty set if not found.
     */
    public Set<String> getChannelCodesByMachineId(String machineId) {
        return machineIdToChannelCodes.getOrDefault(machineId, Collections.emptySet());
    }

    /**
     * 3. 获取所有正在连接的 machineId
     * @return A set of all connected machineIds.
     */
    public Set<String> getAllConnectedMachineIds() {
        return machineIdToSession.keySet();
    }

    // --- Additional Useful Methods ---

    /**
     * 检查指定的 machineId 是否在线
     * @param machineId The machine ID.
     * @return true if connected, false otherwise.
     */
    public boolean isMachineConnected(String machineId) {
        WebSocketSession session = machineIdToSession.get(machineId);
        return session != null && session.isOpen();
    }

    /**
     * 断开指定 machineId 的连接
     * @param machineId The machine ID to disconnect.
     * @return true if the machine was found and disconnection was attempted, false otherwise.
     */
    public boolean disconnectByMachineId(String machineId) {
        WebSocketSession session = machineIdToSession.get(machineId);
        if (session != null) {
            try {
                log.info("正在强制断开 machineId: {}", machineId);
                session.close(CloseStatus.NORMAL); // This will trigger afterConnectionClosed
                return true;
            } catch (IOException e) {
                log.error("断开 machineId {} 时出错: {}", machineId, e.getMessage());
                // Even if closing fails, proceed with unregistering to clean up maps
                unregister(machineId);
                return false;
            }
        }
        return false;
    }

    /**
     * 向所有已连接的客户端广播消息
     * @param title 消息标题
     * @param body 消息内容
     */
    public void broadcastMessage(String title, String body) {
        if (machineIdToSession.isEmpty()) {
            log.info("没有客户端连接，跳过广播。");
            return;
        }
        log.info("正在向 {} 个客户端广播消息: {}", machineIdToSession.size(), title);
        int successCount = 0;
        for (String machineId : machineIdToSession.keySet()) {
            // 对于广播，channelCode 字段可以为空或设为通用值
            if (sendMessageToMachine(machineId, title, body, "broadcast")) {
                successCount++;
            }
        }
        log.info("广播成功发送至 {}/{} 个客户端。", successCount, machineIdToSession.size());
    }

    /**
     * 获取在线客户端总数
     * @return Total number of connected clients.
     */
    public int getConnectedClientsCount() {
        return machineIdToSession.size();
    }
} 