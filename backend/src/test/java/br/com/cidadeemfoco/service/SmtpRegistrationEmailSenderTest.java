package br.com.cidadeemfoco.service;

import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SmtpRegistrationEmailSenderTest {

    @Test
    void shouldSendRegistrationCompletedMessage() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        SmtpRegistrationEmailSender sender = new SmtpRegistrationEmailSender(
                mailSender,
                "no-reply@cidadeemfoco.com",
                "Teste - inscrição concluída no Cidade em Foco"
        );

        sender.sendRegistrationCompleted("ana@example.com", "Ana");

        var captor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertThat(message.getFrom()).isEqualTo("no-reply@cidadeemfoco.com");
        assertThat(message.getTo()).containsExactly("ana@example.com");
        assertThat(message.getSubject()).contains("inscrição concluída");
        assertThat(message.getText()).contains("Parabéns, Ana", "cadastro foi concluído");
    }

    @Test
    void shouldNotFailRegistrationFlowWhenSmtpIsUnavailable() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        doThrow(new MailSendException("SMTP indisponivel"))
                .when(mailSender).send(any(SimpleMailMessage.class));
        SmtpRegistrationEmailSender sender = new SmtpRegistrationEmailSender(
                mailSender,
                "no-reply@cidadeemfoco.com",
                "Cadastro concluído"
        );

        assertThatCode(() -> sender.sendRegistrationCompleted("ana@example.com", "Ana"))
                .doesNotThrowAnyException();
    }
}
