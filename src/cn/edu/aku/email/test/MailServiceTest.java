package cn.edu.aku.email.test;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import android.test.AndroidTestCase;
import android.util.Log;
import cn.edu.aku.email.account.Account;
import cn.edu.aku.email.service.AccountService;
import cn.edu.aku.email.service.MailService;
import cn.edu.aku.email.service.MimeMessageService;

public class MailServiceTest extends AndroidTestCase {
	public final static String TAG = "MailServiceTest";
	
	public void receive() throws Exception{
		AccountService service = new AccountService(getContext());
		Account account = AccountService.find("yangkai_test@sohu.com");
		Session session = MailService.createSession(account);
		Store store = session.getStore();
		store.connect();
		Folder folder = store.getFolder("INBOX");
		folder.open(Folder.READ_WRITE);
		Message[] messages = folder.getMessages();
		
		FetchProfile fp = new FetchProfile();
		fp.add(FetchProfile.Item.ENVELOPE); //fetch subject, from, etc.
		fp.add("X-mailer"); //fetch x-mailer
		folder.fetch(messages, fp);	
		
//		for (Message message : messages) {
//			String subject = message.getSubject();
//			String from = message.getFrom()[0].toString();
//			Log.e(TAG, "主题：" + subject + "\t发件人:" + from);
//			message.writeTo(System.out);
//		}
		System.out.println("Messagess length: "+messages.length);
		MimeMessageService pmm = null;
		for(int i=0;i<messages.length;i++){
			pmm = new MimeMessageService();
			pmm.setMimeMessage((MimeMessage)messages[i]);
			System.out.println("Message "+i+" subject: "+pmm.getSubject());
			System.out.println("Message "+i+" sentdate: "+pmm.getSendDate());
			System.out.println("Message "+i+" replysign: "+pmm.getReplySign());
			System.out.println("Message "+i+" hasRead: "+pmm.isNew());
			System.out.println("Message "+i+" form: "+pmm.getFrom());
			System.out.println("Message "+i+" to: "+pmm.getMailAddress("to"));
			System.out.println("Message "+i+" cc: "+pmm.getMailAddress("cc"));
			System.out.println("Message "+i+" bcc: "+pmm.getMailAddress("bcc"));
			pmm.setDateFormat("yy年MM月dd日 HH:mm");
			System.out.println("Message "+i+" sentdate: "+pmm.getSendDate());
			System.out.println("Message "+i+" Message-ID: "+pmm.getMessageId());
			pmm.getMailContent(messages[i]);
//			pmm.readPart((Part)messages[i]);
			System.out.println("Message "+i+" bodycontent: \r\n"+pmm.getBodyText());
			pmm.setAttachDir("c:\\tmp\\coffeecat1124");
			pmm.saveAttachMent(messages[i]);
			System.out.println("Message "+i+" containAttachment: "+pmm.isContainAttach(messages[i]));
		}
	}
	public void verifyTest() {
		String account = "yangkai_test@sina.cn";
		String password = "a123456";
		MailService mailService = new MailService();
		try {
			MailService.verify(account, password);
			Log.i(TAG, "验证通过");
		} catch (MessagingException e) {
			e.printStackTrace();
			Log.e(TAG, "验证未通过，用户名或密码错误");
		}
	}
}
