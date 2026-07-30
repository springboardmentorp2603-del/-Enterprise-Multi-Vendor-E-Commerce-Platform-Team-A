package com.shopstack.modules.notification.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.mail.javamail.*;
@Configuration
public class EmailConfig {
    @Bean
    public JavaMailSender javaMailSender(@Value("${spring.mail.host:smtp.gmail.com}") String host, @Value("${spring.mail.port:587}") int port, @Value("${spring.mail.username:}") String username, @Value("${spring.mail.password:}") String password) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl(); sender.setHost(host); sender.setPort(port); sender.setUsername(username); sender.setPassword(password); return sender;
    }
}
