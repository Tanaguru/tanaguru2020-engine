package com.tanaguru.service;

public interface MailService {

    /**
     * @param to      The email address to send mail to
     * @param subject The subject of the mail
     * @param text    The mail content
     */
    void sendSimpleMessage(String to, String subject, String text);
    
    /**
     * Send email with html in content
     * @param to The email adress to send mail to
     * @param subject The subject of the mail
     * @param text The mail content
     * @return true if sent false otherwise
     */
    boolean sendMimeMessage(String to, String subject, String text);
}
