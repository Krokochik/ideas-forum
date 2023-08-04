package com.krokochik.ideasforum.service;


import com.krokochik.ideasforum.model.service.Mail;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Scanner;

@Slf4j
@Component
public class MailService {

    @Autowired
    JavaMailSender javaMailSender;

    @Value("${spring.mail.sender.email}")
    String senderEmail;

    public void sendEmail(Mail mail, String name, String content, String htmlName) throws MessagingException {
        try
        {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setFrom(new InternetAddress(senderEmail, "Ideas-forum"));
            helper.setTo(mail.getReceiver());
            helper.setSubject(mail.getTheme());

            Scanner scanner = new Scanner(new ClassPathResource("templates/mails/" + htmlName).getInputStream());
            StringBuilder html = new StringBuilder();

            while (scanner.hasNextLine())
                html.append(scanner.nextLine());

            helper.setText(html.toString().replace("{LINK}", mail.getLink()).replace("{NAME}", name).replace("{CONTENT}", content), true);
            javaMailSender.send(message);
        }
        catch (MessagingException | IOException exc) {
            log.error("An error occurred", exc);
        }
    }

    public void sendConfirmationMail(Mail mail, String name, String content) throws MessagingException {
        sendEmail(mail, name, content,"email-confirmation.html");
    }
}

