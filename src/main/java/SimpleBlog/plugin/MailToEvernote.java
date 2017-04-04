package SimpleBlog.plugin;

import java.io.IOException;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sendgrid.*;

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
	private String sendgridApiKey;

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

	public void setSendgridApiKey(String sendgridApiKey) {
		this.sendgridApiKey = sendgridApiKey;
	}

	public boolean send(Blog blog) {
		logger.entry();
		System.setProperty("mail.mime.charset", "UTF-8");
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", true);
		props.put("mail.smtp.port", port);

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {

			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

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

	public boolean sendByAPI(Blog blog) {
		logger.entry();

		Email from = new Email(username);
		String subject = blog.getSubject() + " " + blog.getTags();
		Email to = new Email(mailbox);
		Content content = new Content("text/plain", blog.getContent());

		Mail mail = new Mail(from, subject, to, content);
		SendGrid sg = new SendGrid(sendgridApiKey);
		Request request = new Request();
		try {
			request.method = Method.POST;
			request.endpoint = "mail/send";
			request.body = mail.build();
			Response response = sg.api(request);
			System.out.println(response.statusCode);
			System.out.println(response.body);
			System.out.println(response.headers);
		} catch (IOException e) {
			logger.catching(e);
			logger.error("Sendgrid API send mail to Evernote server failed:", e);
			return logger.exit(false);
		}
		return logger.exit(true);
	}
}
