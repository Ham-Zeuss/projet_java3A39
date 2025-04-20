package org.example.mailtrap;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;


public class Main {
    public static void main(String[] args) {

        // provide recipient's email ID
        String to = "daliihamroun@gmail.com";
        // provide sender's email ID
        String from = "john.doe@your.domain";

        // provide account credentials
        final String username = "YOUR_USERNAME";
        final String password = "YOUR_PASSWORD";

        // provide host address
        String host = "YOUR_HOST_ADDRESS";

        // configure SMTP details
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        // create the mail Session object
        Session session = Session.getInstance(props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            // create a MimeMessage object
            Message message = new MimeMessage(session);
            // set From email field
            message.setFrom(new InternetAddress(from));
            // set To email field
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            // set email subject field
            message.setSubject("Hello from the Mailtrap team");
            // set the content of the email message
            message.setText("Enjoy sending emails from Jakarta Mail!");

            // send the email message
            Transport.send(message);

            System.out.println("Email Message Sent Successfully!");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
