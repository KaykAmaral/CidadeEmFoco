package br.com.cidadeemfoco.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SmtpRegistrationEmailSenderTest {

    @Test
    void shouldSendRegistrationCompletedMessage() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        SmtpRegistrationEmailSender sender = new SmtpRegistrationEmailSender(
                mailSender,
                "no-reply@cidadeemfoco.com",
                "Cadastro concluído — Cidade em Foco",
                "Redefinição de senha — Cidade em Foco"
        );

        sender.sendRegistrationCompleted("ana@example.com", "Ana");

        verify(mailSender).send(mimeMessage);
        assertThat(mimeMessage.getFrom()[0].toString()).contains("Cidade em Foco", "no-reply@cidadeemfoco.com");
        assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo("ana@example.com");
        assertThat(mimeMessage.getSubject()).isEqualTo("Cadastro concluído — Cidade em Foco");
    }

    @Test
    void shouldNotFailRegistrationFlowWhenSmtpIsUnavailable() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("SMTP indisponivel"))
                .when(mailSender).send(any(MimeMessage.class));
        SmtpRegistrationEmailSender sender = new SmtpRegistrationEmailSender(
                mailSender,
                "no-reply@cidadeemfoco.com",
                "Cadastro concluído",
                "Redefinição de senha"
        );

        assertThatCode(() -> sender.sendRegistrationCompleted("ana@example.com", "Ana"))
                .doesNotThrowAnyException();
    }
}
