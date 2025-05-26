package org.example.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Envía un correo electrónico con un archivo adjunto
     * 
     * @param to          Dirección de correo del destinatario
     * @param subject     Asunto del correo
     * @param text        Cuerpo del correo (HTML permitido)
     * @param attachment  Datos del archivo adjunto
     * @param filename    Nombre del archivo adjunto
     */
    public void sendEmailWithAttachment(String to, String subject, String text, byte[] attachment, String filename) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true); // true indica que el texto es HTML
            
            if (attachment != null && attachment.length > 0) {
                helper.addAttachment(filename, new ByteArrayResource(attachment));
            }
            
            mailSender.send(message);
            log.info("Correo enviado con éxito a {}", to);
        } catch (MessagingException e) {
            log.error("Error al enviar correo a {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Error al enviar correo electrónico", e);
        }
    }
} 