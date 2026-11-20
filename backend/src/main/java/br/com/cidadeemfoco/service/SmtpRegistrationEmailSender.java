package br.com.cidadeemfoco.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.email.enabled", havingValue = "true")
public class SmtpRegistrationEmailSender implements RegistrationEmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpRegistrationEmailSender.class);

    private final JavaMailSender mailSender;
    private final String from;
    private final String subject;

    public SmtpRegistrationEmailSender(
            JavaMailSender mailSender,
            @Value("${app.email.from}") String from,
            @Value("${app.email.registration-subject}") String subject
    ) {
        this.mailSender = mailSender;
        this.from = from;
        this.subject = subject;
    }

    @Async
    @Override
    public void sendRegistrationCompleted(String recipientEmail, String citizenName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipientEmail);
        message.setSubject(subject);
        message.setText("Parabéns, " + citizenName + "!\n\n"
                + "Você se inscreveu no Cidade em Foco. Seu cadastro foi concluído com sucesso.\n\n"
                + "Nossa cidade, nosso olhar.");

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            LOGGER.warn(
                    "Nao foi possivel enviar o email de cadastro para {}: {}",
                    recipientEmail,
                    exception.getMessage()
            );
        }
    }
}
