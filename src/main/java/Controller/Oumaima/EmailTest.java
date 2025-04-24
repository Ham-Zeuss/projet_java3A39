package Controller.Oumaima;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailTest {
    public static void main(String[] args) {
        final String fromEmail = "oumaima.boulila@esprit.tn";
        final String password = "jmis uuzk emig hccz";
        final String toEmail = "66damm.mmad66@gmail.com";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });
        session.setDebug(true);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Test Email");
            message.setText("This is a test email.");

            Transport.send(message);
            System.out.println("Test email sent successfully!");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
