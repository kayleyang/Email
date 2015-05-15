package cn.edu.aku.email.temp;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 *
 * JavaMail examples: send and fetch mail messages
 *  
 * Copyright (c) 2011 ljs (http://blog.csdn.net/ljsspace/)
 * Licensed under GPL (http://www.opensource.org/licenses/gpl-license.php)
 *
 * @author ljs
 * 2011-10-10
 *
 */
public class JavaMail {
	/**
	 * send a message to a mailing list (e.g. java-list@ljsspace.org). The actual receivers (e.g. toAddr)
	 * are specified at the last moment when transport.send is called.
	 */
	public void sendMail(String host, final String user,
			final String password,
			String fromAddr,String toAddr,String subject,
			String msgText,File attachment,String filename) {
		//		Transport transport = null;
		try {
			Properties props = System.getProperties(); //use system properties
			props.put("mail.debug", "true"); //or: session.setDebug(true)
			props.put("mail.transport.protocol", "smtp"); 			
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.user", user);	

			//if SMTP authentication is required, we must set this property
			props.put("mail.smtp.auth", "true");  


			Authenticator auth = new Authenticator() {
				@Override
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(user, password);
				}
			};
			Session session = Session.getInstance(props, auth);//use authentication
			// session = Session.getDefaultInstance(props, null);

			MimeMessage msg = new MimeMessage(session); 

			msg.setFrom(new InternetAddress(fromAddr));
			//InternetAddress[] toAddress = {new InternetAddress(toAddr)};
			//msg.setRecipients(Message.RecipientType.TO, toAddress);
			String mailingListAddr = "java-list@ljsspace.org";
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(mailingListAddr,"Java List"));
			msg.setSubject(subject);


			msg.setHeader("X-Mailer", "ljsspace-javamail");
			msg.setSentDate(new Date());

			//create a multipart as the content
			MimeMultipart multipart = new MimeMultipart();

			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(msgText); 
			multipart.addBodyPart(mbp1);

			//attachment
			DataSource fileDS = new FileDataSource(attachment);
			DataHandler fileDH = new DataHandler(fileDS);
			MimeBodyPart file_attachment = new MimeBodyPart();
			file_attachment.setDataHandler(fileDH);   
			file_attachment.setFileName(filename);				
			multipart.addBodyPart(file_attachment);

			//set content for the message; the content is a multipart object
			msg.setContent(multipart);
			//msg.saveChanges(); //unnecessary: Transport.send will call saveChanges

			//transport = session.getTransport(); 
			//transport.connect();
			//transport.send(msg);

			InternetAddress[] toAddress = {new InternetAddress(toAddr)};
			//Transport.send(msg); //only send to message's getRecipients()
			Transport.send(msg,toAddress); //ignore message's getRecipients()
		} catch (Exception e) {
			e.printStackTrace();
		} 
		//		finally{	
		//			try {
		//				transport.close();
		//			} catch (Exception ex) {
		//				ex.printStackTrace();
		//			}
		//		}
	}

	/**
	 * Fetch messages from the INBOX store
	 * @param preview If true, display all messages' profiles; otherwise, display the contents of the first two new messages 
	 */
	public void fetchMail(boolean preview,String host, final String user,
			final String password) {
		Store store = null;
		Folder inbox = null;
		try {
			Properties props = System.getProperties();			
			props.put("mail.debug", "true");
			props.put("mail.store.protocol", "pop3"); //or store like imap
			props.put("mail.pop3.user", user); //no password!
			props.put("mail.pop3.host", host); 

			Authenticator auth = new Authenticator() {
				@Override
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(user, password);
				}
			};
			Session session = Session.getDefaultInstance(props, auth);

			// Get hold of a POP3 message store using session (a factory object)
			store = session.getStore();  //the same as session.getStore("pop3"). Since we've set mail.store.protocol property, we just call session.getStore().
			// store.connect(host, user, password); //use authenticator instead
			store.connect(); //connect using session's authenticator

			// inbox = store.getDefaultFolder(); //the root folder in the default namespace
			inbox = store.getFolder("INBOX"); //the only folder in POP3
			inbox.open(Folder.READ_WRITE);

			// Get the messages (a light-weight operation)
			Message[] msgs = inbox.getMessages();

			if(preview){
				FetchProfile fp = new FetchProfile();
				fp.add(FetchProfile.Item.ENVELOPE); //fetch subject, from, etc.
				fp.add("X-mailer"); //fetch x-mailer
				inbox.fetch(msgs, fp);	

				for (int i = 0; i < inbox.getMessageCount(); i++) {
					displayMessageProfile(msgs[i]);
				}
			}else{
				//read the first 2 messages only (from the newest to the oldest)
				for (int i = inbox.getMessageCount()-1; i>=0 && i > inbox.getMessageCount() - 3; i--) {
					processMessage(msgs[i]);
					//msgs[i].setFlag(Flags.Flag.DELETED, true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (inbox != null)
					inbox.close(true); //expunge the deleted message
				if (store != null)
					store.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	// display a light-weight message's profile
	private void displayMessageProfile(Message message) {
		try {
			System.out.println(message.getFrom()[0] + "\t" + message.getSubject());
		} catch (MessagingException e) {			
			e.printStackTrace();
		}		
	}
	private void processMessage(Message message) {
		try {
			System.out.println("*******" + message.getSubject() + "*******");
			readPart(message);
		} catch (MessagingException e) {			
			e.printStackTrace();
		}	
	}

	private void readPart(Part p) {
		try {
			Object content = p.getContent();
			if(content instanceof String){
				System.out.println(content);
			}else if (content instanceof Multipart) {
				Multipart mp = (Multipart) content;
				int count = mp.getCount();
				for (int i = 0; i < count; i++) {
					Part bodyPart = mp.getBodyPart(i); //or check: boyPart.getDisposition()
					readPart(bodyPart); //recursion
				}
			}else if (content instanceof InputStream) {
				System.out.println("****found inputstream as a part's content****");
			}else{
				System.out.println("****unknown type****");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {		
		//test store: pop3
		String pop3host="pop3.sohu.com";
		String pop3user="yangkai_test"; //test account
		String pop3pass="a123456";
		JavaMail mailer = new JavaMail();
		mailer.fetchMail(true,pop3host, pop3user, pop3pass);

		//test transport: smtp
		String smtphost="smtp.sohu.com";
		String smtpuser="yangkai_test"; //test account
		String smtppass="a123456";
		String fromAddr="abc@126.com";
		String toAddr="someone@somewhere.com";
		String subject="A Test Message sent via JavaMail";
		String msgText = "Please see the attachment for details.";
		File attachment = new File("/tmp/test.pdf");
		String filename = "test.pdf";
		mailer.sendMail(smtphost, smtpuser,smtppass,
				fromAddr,toAddr,subject,
				msgText,attachment,filename);
	}

}
