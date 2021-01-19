package com.tanaguru.service.impl;

import com.tanaguru.service.MailService;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
    
    public boolean sendMimeMessage(String to, String subject, String text) {
    	boolean emailSent = true;
    	MimeMessage message = javaMailSender.createMimeMessage();
    	MimeMessageHelper helper = new MimeMessageHelper(message,"UTF-8");
    	try {
			helper.setTo(to);
			helper.setReplyTo(from);
			helper.setFrom(from);
			helper.setSubject(subject);
			String body = "<!DOCTYPE html><html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/></head><body><p>"+text+"</p></body></html>";
			helper.setText(body, true); //true indicate html support with mime message
			javaMailSender.send(message);
			LOGGER.info("Send [{}] email to {}", subject, to);
		} catch (MessagingException e) {
			LOGGER.error("Send [{}] email to {} failed", subject, to);
			emailSent=false;
		}
    	return emailSent;
    	
    }
}
