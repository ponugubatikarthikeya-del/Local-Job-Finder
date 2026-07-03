package com.localjobfinder.bakend.sendMailPackage;


import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ForgetEmailSend {

    private JavaMailSender javaMailSender;

    public ForgetEmailSend(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Async
    public void mailSender(String email,String text) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject("Reset your password");
        mailMessage.setText(text);

        javaMailSender.send(mailMessage);

    }
}