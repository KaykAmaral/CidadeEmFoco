package br.com.cidadeemfoco.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@ConditionalOnProperty(name = "app.email.enabled", havingValue = "true")
public class SmtpRegistrationEmailSender implements RegistrationEmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpRegistrationEmailSender.class);

    private final JavaMailSender mailSender;
    private final String from;
    private final String registrationSubject;
    private final String passwordResetSubject;

    public SmtpRegistrationEmailSender(
            JavaMailSender mailSender,
            @Value("${app.email.from}") String from,
            @Value("${app.email.registration-subject}") String registrationSubject,
            @Value("${app.email.password-reset-subject}") String passwordResetSubject
    ) {
        this.mailSender = mailSender;
        this.from = from;
        this.registrationSubject = registrationSubject;
        this.passwordResetSubject = passwordResetSubject;
    }

    @Async
    @Override
    public void sendRegistrationCompleted(String recipientEmail, String citizenName) {
        String safeName = escapeHtml(citizenName);
        String text = "Olá, " + citizenName + "!\n\n"
                + "Seu cadastro no Cidade em Foco foi concluído com sucesso.\n"
                + "Agora você já pode registrar ocorrências e acompanhar os alertas da sua cidade.\n\n"
                + "Nossa cidade, nosso olhar.";
        String html = emailLayout(
                "Cadastro concluído",
                "Olá, " + safeName + "!",
                "Seu cadastro no <strong>Cidade em Foco</strong> foi concluído com sucesso. "
                        + "Agora você já pode registrar ocorrências e acompanhar os alertas da sua cidade.",
                null,
                null
        );
        send(recipientEmail, registrationSubject, text, html, "cadastro");
    }

    @Async
    @Override
    public void sendPasswordReset(String recipientEmail, String citizenName, String resetUrl, long validMinutes) {
        String safeName = escapeHtml(citizenName);
        String safeUrl = escapeHtml(resetUrl);
        String text = "Olá, " + citizenName + "!\n\n"
                + "Recebemos uma solicitação para redefinir sua senha.\n"
                + "Acesse o link abaixo em até " + validMinutes + " minutos:\n\n"
                + resetUrl + "\n\n"
                + "Se você não fez essa solicitação, ignore este e-mail.";
        String html = emailLayout(
                "Redefinição de senha",
                "Olá, " + safeName + "!",
                "Recebemos uma solicitação para redefinir sua senha. O link é válido por <strong>"
                        + validMinutes + " minutos</strong> e só pode ser usado uma vez.",
                "Redefinir minha senha",
                safeUrl
        );
        send(recipientEmail, passwordResetSubject, text, html, "recuperacao de senha");
    }

    private void send(String recipientEmail, String subject, String text, String html, String purpose) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from, "Cidade em Foco");
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(text, html);
            mailSender.send(message);
            LOGGER.info("Email de {} enviado com sucesso para {}", purpose, recipientEmail);
        } catch (MessagingException | MailException | UnsupportedEncodingException exception) {
            LOGGER.warn("Nao foi possivel enviar o email de {} para {}: {}",
                    purpose, recipientEmail, exception.getMessage());
        }
    }

    private String emailLayout(String eyebrow, String title, String content, String buttonText, String buttonUrl) {
        String button = buttonText == null ? "" : """
                <p style="margin:28px 0 22px">
                  <a href="%s" style="display:inline-block;padding:13px 20px;border-radius:9px;background:#0878c9;color:#ffffff;text-decoration:none;font-weight:700">%s</a>
                </p>
                """.formatted(buttonUrl, buttonText);
        return """
                <!doctype html>
                <html lang="pt-BR"><body style="margin:0;background:#f2f7fb;font-family:Arial,sans-serif;color:#17324d">
                  <div style="padding:32px 16px">
                    <div style="max-width:560px;margin:auto;overflow:hidden;border:1px solid #dbe7f0;border-radius:16px;background:#ffffff">
                      <div style="padding:22px 28px;background:#06345f;color:#ffffff">
                        <strong style="font-size:20px">Cidade em Foco</strong>
                        <div style="margin-top:4px;color:#a9d9f7;font-size:13px">Nossa cidade, nosso olhar.</div>
                      </div>
                      <div style="padding:30px 28px">
                        <div style="color:#0878c9;font-size:12px;font-weight:700;text-transform:uppercase;letter-spacing:.08em">%s</div>
                        <h1 style="margin:8px 0 16px;font-size:26px;color:#06345f">%s</h1>
                        <p style="margin:0;color:#48657d;font-size:16px;line-height:1.65">%s</p>
                        %s
                        <p style="margin:24px 0 0;color:#71879a;font-size:13px;line-height:1.5">Se você não reconhece esta ação, nenhuma outra providência é necessária.</p>
                      </div>
                    </div>
                  </div>
                </body></html>
                """.formatted(eyebrow, title, content, button);
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
