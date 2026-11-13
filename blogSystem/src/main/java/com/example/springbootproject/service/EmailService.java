package com.example.springbootproject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮箱验证码发送服务（QQ 邮箱 SMTP）。
 *
 * <p>需要在配置中提供：
 * spring.mail.username / spring.mail.password（QQ 邮箱授权码）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${mail.from:}")
    private String from;

    public void sendCode(String email, String code) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalStateException("邮件服务未配置：请设置 MAIL_USERNAME / MAIL_PASSWORD");
        }
        String sender = (from == null || from.trim().isEmpty()) ? username : from.trim();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(email);
        message.setSubject("AIOT Nexus 登录验证码");
        message.setText("您好：\n\n"
                + "您的 AIOT Nexus 验证码是：" + code + "\n"
                + "验证码 5 分钟内有效，请勿泄露给他人。\n\n"
                + "如非本人操作，请忽略此邮件。");
        mailSender.send(message);
        log.info("邮箱验证码已发送，email={}", maskEmail(email));
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***" + (at >= 0 ? email.substring(at) : "");
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
