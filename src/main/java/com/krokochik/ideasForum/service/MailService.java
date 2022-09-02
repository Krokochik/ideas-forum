package com.krokochik.ideasForum.service;


import com.krokochik.ideasForum.model.Mail;
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

@Component
public class MailService {

    @Autowired
    JavaMailSender javaMailSender;

    @Value("spring.mail.sender.email")
    String senderEmail;
    @Value("spring.mail.sender.text")
    String senderText;

    public void sendEmail(Mail mail, String name, String htmlName) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(new InternetAddress(senderEmail, "Ideas-forum"));
            helper.setTo(mail.getReceiver());
            helper.setSubject(mail.getTheme());
            Scanner scanner = new Scanner(new ClassPathResource("templates/" + htmlName).getInputStream());
            StringBuilder html = new StringBuilder();
            while (scanner.hasNextLine()) html.append(scanner.nextLine());
            helper.setText(html.toString().replace("{LINK}", mail.getLink()).replace("{NAME}", name), true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void sendActiveMail(Mail mail, String name)  {
        sendEmail(mail, name, "confirm.html");
    }
}

