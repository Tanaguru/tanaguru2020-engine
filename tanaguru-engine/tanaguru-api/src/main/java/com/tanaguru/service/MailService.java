package com.tanaguru.service;

public interface MailService {

    /**
     * @param to      The email address to send mail to
     * @param subject The subject of the mail
     * @param text    The mail content
     */
    void sendSimpleMessage(String to, String subject, String text);
    
    boolean sendMimeMessage(String to, String subject, String text);
}
