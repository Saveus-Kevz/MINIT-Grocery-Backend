package com.minimartph.Minit.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  private final JavaMailSender mailSender;

  EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void sendCredentials(String to, String username, String rawPassword, String firstName) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject("Your Minit Staff Account");
    message.setText(
        String.format(
            "Dear %s,\n\n"
                + "Your account has been created successfully.\n\n"
                + "Username: %s\n"
                + "Password: %s\n\n"
                + "Please log in at http://localhost:3000\n\n"
                + "Regards,\nMinit Team",
            firstName, username, rawPassword));
    mailSender.send(message);
  }
}
