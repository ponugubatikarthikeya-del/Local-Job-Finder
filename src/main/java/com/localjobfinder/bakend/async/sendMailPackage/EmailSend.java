package com.localjobfinder.bakend.async.sendMailPackage;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import org.springframework.scheduling.annotation.Async;

@Service
public class EmailSend {

    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailSend(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Async(value = "taskExecutor")
    public void mailSender(String email,String text,String type) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setTo(email);

        if(type.equals("password reset")){
            mailMessage.setSubject("Reset your password");
        } else {
            mailMessage.setSubject("Signup Verification");
        }

        mailMessage.setText(text);

        javaMailSender.send(mailMessage);

    }

}