package com.nonu1l.msgboxservice;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGeneratorTest {


    public static void main(String[] args) {

    }
    /**
     * 此测试用于生成BCrypt加密的密码字符串。
     * 运行此测试，然后从控制台复制输出的密码哈希。
     * 您可以将这个哈希值手动更新到数据库中'super'用户的password字段。
     * 获取密码后，建议删除此文件。
     */
    public void generatePassword() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String rawPassword = "super@ex";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        System.out.println("==================================================");
        System.out.println("原始密码: " + rawPassword);
        System.out.println("BCrypt 加密后密码: " + encodedPassword);
        System.out.println("==================================================");
    }
}