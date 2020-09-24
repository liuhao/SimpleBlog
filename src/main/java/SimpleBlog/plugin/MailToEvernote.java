package SimpleBlog.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.auth.AuthScope;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import SimpleBlog.entity.Blog;

/**
 * Post note to Evernote by sending email
 * Created by lyoo on 9/27/2015.
 */
public class MailToEvernote {

    private static final Logger logger = LogManager.getLogger(MailToEvernote.class.getName());

    private String host;
    private String port;
    private String mailbox;
    private String username;
    private String password;
    private String mailgunApiKey;
    private String mailgunApiHost;
    private String mailgunApiUrl;
    private String mailgunApiSender;

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

    public void setMailgunApiKey(String mailgunApiKey) {
        this.mailgunApiKey = mailgunApiKey;
    }

    public void setMailgunApiHost(String mailgunApiHost) {
        this.mailgunApiHost = mailgunApiHost;
    }

    public void setMailgunApiUrl(String mailgunApiUrl) {
        this.mailgunApiUrl = mailgunApiUrl;
    }

    public void setMailgunApiSender(String mailgunApiSender) {
        this.mailgunApiSender = mailgunApiSender;
    }

    public boolean send(Blog blog) {
        logger.entry();
        System.setProperty("mail.mime.charset", "UTF-8");
        Properties props = new Properties();
        props.put("mail.smtp.host", System.getenv(host));
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.port", System.getenv(port));
        String user = System.getenv(username);
        String pwd = System.getenv(password);
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pwd);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(System.getenv(mailbox)));
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

    public boolean sendByAPI(Blog blog) {
        logger.entry();

        String from = System.getenv(mailgunApiSender);
        String subject = blog.getSubject() + " " + blog.getTags();
        String to = System.getenv(mailbox);
        String content = blog.getContent();

        final BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(System.getenv(mailgunApiHost), 443),
                new UsernamePasswordCredentials("api", System.getenv(mailgunApiKey).toCharArray()));

        try (final CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build()) {
            final HttpPost httpPost = new HttpPost(System.getenv(mailgunApiUrl));
            List<NameValuePair> formData = new ArrayList<>();
            formData.add(new BasicNameValuePair("from", from));
            formData.add(new BasicNameValuePair("to", to));
            formData.add(new BasicNameValuePair("subject", subject));
            formData.add(new BasicNameValuePair("text", content));
            httpPost.setEntity(new UrlEncodedFormEntity(formData));
            try (final CloseableHttpResponse response = httpClient.execute(httpPost)) {
                System.out.println(response.getCode() + " " + response.getHeaders() + " " + response.getReasonPhrase());
                System.out.println(EntityUtils.toString(response.getEntity()));
            } catch (ParseException e) {
                System.out.println("cannot parse the response's Entity");
            }
        } catch (IOException e) {
            logger.catching(e);
            logger.error("Sendgrid API send mail to Evernote server failed:", e);
            return logger.exit(false);
        }
        return logger.exit(true);
    }
}
