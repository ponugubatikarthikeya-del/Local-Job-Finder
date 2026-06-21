package com.localjobfinder.bakend.sendMailPackage;


import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class ForgetEmailSend {

    private JavaMailSender javaMailSender;

    public ForgetEmailSend(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void mailSender(String email, int otp,String text) {


//        String resetLink =
//                "http://PersonalChatApp.com/reset-password?token=" + token;

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject("Reset your password");
        mailMessage.setText(text);

        javaMailSender.send(mailMessage);

    }
}