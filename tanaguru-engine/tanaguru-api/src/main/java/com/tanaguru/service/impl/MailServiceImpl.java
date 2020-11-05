package com.tanaguru.service.impl;

import com.tanaguru.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * @author rcharre
 */
@Service
public class MailServiceImpl implements MailService {
    private final Logger LOGGER = LoggerFactory.getLogger(MailServiceImpl.class);

    @Value("${mail.from}")
    private String from;

    private final JavaMailSender javaMailSender;

    @Autowired
    public MailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setReplyTo(from);
        message.setFrom(from);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
        LOGGER.info("Send [{}] email to {}", subject, to);
    }
}
