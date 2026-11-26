package br.com.cidadeemfoco.service;

public interface RegistrationEmailSender {

    void sendRegistrationCompleted(String recipientEmail, String citizenName);

    void sendPasswordReset(String recipientEmail, String citizenName, String resetUrl, long validMinutes);
}
