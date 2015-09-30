package SimpleBlog.plugin;

import SimpleBlog.entity.Blog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Created by lyoo on 9/27/2015.
 */
public class MailToEvernote {
    private static Logger logger = LogManager.getLogger(MailToEvernote.class.getName());

    private String host;
    private String port;
    private String mailbox;
    private String username;

    public void setPassword(String password) {
        this.password = password;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setMailbox(String mailbox) {
        this.mailbox = mailbox;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private String password;

    public boolean send(Blog blog) {
        logger.entry();
        System.setProperty("mail.mime.charset", "UTF-8");
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                }
        );

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailbox));
            message.setSubject(blog.getSubject() + " " + blog.getTags());
            message.setText(blog.getContent());
            Transport.send(message);
        } catch (MessagingException e) {
            logger.catching(e);
            logger.error("Send mail to Evernote server failed:", e);
            return logger.exit(false);
        }
        return logger.exit(true);
    }
}
