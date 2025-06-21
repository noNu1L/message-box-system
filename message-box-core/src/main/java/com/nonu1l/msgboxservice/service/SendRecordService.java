package com.nonu1l.msgboxservice.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nonu1l.commons.utils.Result;
import com.nonu1l.commons.utils.ValidateUtils;
import com.nonu1l.exdataext.service.BaseService;
import com.nonu1l.msgboxservice.dto.SendRecordViewDto;
import com.nonu1l.msgboxservice.entity.SendRecord;
import com.nonu1l.msgboxservice.entity.UserExt;
import com.nonu1l.msgboxservice.mapper.SendRecordMapper;
import com.nonu1l.msgboxservice.utils.ResourceUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * 发送记录服务
 *
 * @author zhonghanbo
 * @date 2025年01月05日 23:49
 */

@Service
public class SendRecordService extends BaseService<SendRecordMapper, SendRecord> {

    public Result<HashMap<String, List<SendRecordViewDto>>> getSendRecord() {
        UserExt sessionUser = ResourceUtil.getSessionUser();
        String userId = sessionUser.getUserId();
        return Result.ok(buildSendRecord(userId));
    }


    private SendRecordViewDto copyDto(SendRecordViewDto original) {
        SendRecordViewDto copy = new SendRecordViewDto();
        copy.setTitle(original.getTitle());
        copy.setBackgroundColor(original.getBackgroundColor());
        copy.setReceiveMode(original.getReceiveMode());
        copy.setR1c1_label(original.getR1c1_label());
        copy.setR1c1_value(original.getR1c1_value());
        copy.setR1c2_label(original.getR1c2_label());
        copy.setR1c2_value(original.getR1c2_value());
        copy.setR2c1_label(original.getR2c1_label());
        copy.setR2c1_value(original.getR2c1_value());
        copy.setR2c2_label(original.getR2c2_label());
        copy.setR2c2_value(original.getR2c2_value());
        copy.setR3c1_label(original.getR3c1_label());
        copy.setR3c1_value(original.getR3c1_value());
        copy.setR4c1_label(original.getR4c1_label());
        copy.setR4c1_value(original.getR4c1_value());
        return copy;
    }

    private  HashMap<String, List<SendRecordViewDto>> buildSendRecord(String userId) {
        HashMap<String, List<SendRecordViewDto>> sendRecordData = new HashMap<>();

        LambdaQueryWrapper<SendRecord> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SendRecord::getUserId, userId);
        lambdaQueryWrapper.orderByDesc(SendRecord::getCreatedAt);
        lambdaQueryWrapper.last("limit 100");
        List<SendRecord> sendRecords = this.list(lambdaQueryWrapper);

        List<SendRecordViewDto> allDtos = new java.util.ArrayList<>();
        List<SendRecordViewDto> emailDtos = new java.util.ArrayList<>();
        List<SendRecordViewDto> clientDtos = new java.util.ArrayList<>();

        for (SendRecord item : sendRecords) {
            SendRecordViewDto sendRecordViewDto = new SendRecordViewDto();
            sendRecordViewDto.setTitle(item.getSubject());

            if (ValidateUtils.isNotEmpty(item.getChannelCode())) {
                sendRecordViewDto.setR1c1_label("ChannelCode:");
                sendRecordViewDto.setR1c1_value(item.getChannelCode());
            }

            if (ValidateUtils.isNotEmpty(item.getGroupName())) {
                sendRecordViewDto.setR1c2_label("接收组名称:");
                sendRecordViewDto.setR1c2_value(item.getGroupName());
            }

            if (ValidateUtils.isNotEmpty(item.getFailMsg())) {
                sendRecordViewDto.setBackgroundColor("red");
            }

            sendRecordViewDto.setR2c1_label("记录时间:");
            sendRecordViewDto.setR2c1_value(DateUtil.format(item.getCreatedAt(), DatePattern.NORM_DATETIME_PATTERN));
            sendRecordViewDto.setR2c2_label("接收方式:");
            sendRecordViewDto.setReceiveMode(item.getReceiveMode());
            sendRecordViewDto.setR2c2_value(item.getReceiveMode()
                    .replace("email", "邮箱")
                    .replace("client", "客户端"));

            sendRecordViewDto.setR3c1_label("消息接收方:");
            StringBuilder r3c1_value = new StringBuilder();
            if (ValidateUtils.isNotEmpty(item.getReceiveEmails())) {
                r3c1_value.append(item.getReceiveEmails().replace(",", ",  "));
            }

            if (ValidateUtils.isNotEmpty(item.getReceiveMachineId())) {
                if (!r3c1_value.isEmpty()) {
                    r3c1_value.append("  ");
                }
                r3c1_value.append(item.getReceiveMachineId().replace(",", ",  "));
            }
            sendRecordViewDto.setR3c1_value(r3c1_value.toString());

            if (ValidateUtils.isNotEmpty(item.getFailMsg())) {
                sendRecordViewDto.setR4c1_label("错误信息:");
                sendRecordViewDto.setR4c1_value(item.getFailMsg());
            }

            allDtos.add(sendRecordViewDto);
            if (item.getReceiveMode().contains("email")) {
                emailDtos.add(copyDto(sendRecordViewDto));
            }
            if (item.getReceiveMode().contains("client")) {
                clientDtos.add(copyDto(sendRecordViewDto));
            }
        }

        sendRecordData.put("all", allDtos);
        sendRecordData.put("email", emailDtos);
        sendRecordData.put("client", clientDtos);

        return sendRecordData;
    }

}
