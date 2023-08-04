package com.krokochik.ideasforum.service;


import com.krokochik.ideasforum.model.service.Mail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Scanner;

@Slf4j
@Component
public class MailService {

    @Autowired
    JavaMailSender javaMailSender;

    @Value("${spring.mail.sender.email}")
    String senderEmail;

    public void sendEmail(Mail mail, String name, String content, String htmlName) {
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

    public void sendConfirmationMail(Mail mail, String name, String content) {
        sendEmail(mail, name, content,"email-confirmation.html");
    }
}

