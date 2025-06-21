package com.nonu1l.msgboxservice.service;

import com.nonu1l.msgboxservice.utils.RandomStringGeneratorUtils2;
import org.springframework.stereotype.Service;

/**
 * 通道代码 Service
 *
 * @author zhonghanbo
 * @date 2025年01月05日 18:06
 */

@Service
public class ChannelCodeService {

    /**
     * 重新获取通道代码
     *
     * @return {@link String }
     */
    public String getNewChannelCode() {
        return RandomStringGeneratorUtils2.generateRandomString(8);
    }
}
