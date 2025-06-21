package com.nonu1l.msgboxcore.controller;

import com.nonu1l.commons.utils.MapUtils;
import com.nonu1l.commons.utils.Result;
import com.nonu1l.msgboxcore.service.ClientWebSocketHandler;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ws")
public class WebSocketPushController {

    private final ClientWebSocketHandler clientWebSocketHandler;

    public WebSocketPushController(ClientWebSocketHandler clientWebSocketHandler) {
        this.clientWebSocketHandler = clientWebSocketHandler;
    }


    @PostMapping("/push")
    public Result<String> push(@RequestBody Map<String, Object> requestBody) {
        String channelCode = MapUtils.getStringValueIgnoreCase(requestBody, "channelCode");
        String title = MapUtils.getStringValueIgnoreCase(requestBody, "subject");
        String body = MapUtils.getStringValueIgnoreCase(requestBody, "body");

        clientWebSocketHandler.sendMessageToChannel(channelCode, title, body);
        return Result.ok();
    }
}