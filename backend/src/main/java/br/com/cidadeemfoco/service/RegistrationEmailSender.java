package br.com.cidadeemfoco.service;

public interface RegistrationEmailSender {

    void sendRegistrationCompleted(String recipientEmail, String citizenName);
}
