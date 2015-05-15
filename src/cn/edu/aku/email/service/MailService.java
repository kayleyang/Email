package cn.edu.aku.email.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import cn.edu.aku.email.account.Account;

import android.util.Log;

public class MailService {
	private static final String TAG = "MailService";
	
	//是否打开JavaMail调试功能
	private static boolean debug = false;
	
	/**
	 * 检测邮箱名格式是否合法
	 * @param account
	 * @return
	 */
	public static boolean CheckAccount(String account) {
		Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}");
		Matcher m = p.matcher(account);
		return m.matches();
	}
	
	/**
	 * 连接SMTP服务器验证帐号和密码
	 * @param account 用户账户
	 * @param password 用户密码
	 * @throws MessagingException Messaging异常
	 */
	public static void verify(String account , String password) throws MessagingException{
		final String accountString = account;
		final String passwordString = password;

		String username = account.substring(0, account.indexOf("@"));
		String domain = account.substring(account.indexOf("@") + 1,	account.length());
		String smtpHost = "smtp." + domain;

		Properties props = new Properties();
		props.put("mail.smtp.host", smtpHost);
		props.put("mail.smtp.auth", "true");
		props.put("mail.transport.protocol", "smtp");

		Session session = Session.getDefaultInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(accountString, passwordString);
			}
		});
		session.setDebug(debug);

		Transport trans = session.getTransport();
		trans.connect(smtpHost, username, password);
		trans.close();
	}
	
	public static void verify(String account , String password,
			String transport_protocol, String transport_host, int transport_port) throws MessagingException{
		
		final String accountString = account;
		final String passwordString = password;
		String username = account.substring(0, account.indexOf("@"));
		
		Properties props = new Properties();
		props.put("mail.smtp.host", transport_host);
		props.put("mail.smtp.port", transport_port);
		props.put("mail.smtp.auth", "true");
		props.put("mail.transport.protocol", "smtp");
		
		Session session = Session.getDefaultInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(accountString, passwordString);
			}
		});
		session.setDebug(debug);
		
		Transport trans = session.getTransport();
		trans.connect(transport_host, username, password);
		trans.close();
	}
	
	/**
	 * 根据账户创建Session
	 * @param account 账户
	 * @return Session
	 */
	public static Session createSession(Account account) {
		final String accountString = account.getAccount();
		final String passwordString = account.getPassword();
		final String transportHost = account.getTransport_host();
		final String storeHost = account.getStore_host();
		Log.i(TAG, accountString + "........");
		Log.i(TAG, transportHost + "........" + storeHost);
		Properties props = new Properties();
		
		props.setProperty("mail.transport.protocol", account.getTransport_protocol());
		props.setProperty("mail." + account.getTransport_protocol() + ".host", transportHost);
		props.setProperty("mail." + account.getTransport_protocol() + ".port", "" + account.getTransport_port());
		
		props.setProperty("mail.store.protocol", account.getStore_protocol());
		props.setProperty("mail." + account.getStore_protocol() + ".host", storeHost);
		props.setProperty("mail." + account.getStore_protocol() + ".port", "" + account.getStore_port());
		
		props.setProperty("mail.smtp.auth", "true");
		//遍历输出props里面的键值对
		/*for (Iterator<Entry<Object, Object>> iterator = props.entrySet().iterator(); iterator.hasNext();) {
			Entry<Object, Object> entry = iterator.next();  
            Object key = entry.getKey();  
            Object value = entry.getValue();  
            System.out.println(key + " : " + value);  
		}*/
		//程序中只有一个Authenticator验证对象，所有使用第二个帐号发送邮件的时候，Authenticator还是保存的第一个帐号的信息，所以不能使用getDefaultInstance()
		Session session = Session.getInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(accountString, passwordString);
			}
		});
		session.setDebug(debug);
		return session;
	}
	/**
	 * 根据账户帐号创建Session
	 * @param account 账户帐号
	 * @return Session
	 */
	public static Session createSession(String account) {
		Account a = AccountService.find(account);
		return createSession(a);
	}
	
	/**
	 * 遍历folders获取邮件
	 * @param folders
	 * @return
	 */
	public static ArrayList<ArrayList<MimeMessageService>> getData(Folder[] folders){
		ArrayList<ArrayList<MimeMessageService>> dataList = new ArrayList<ArrayList<MimeMessageService>>();
		for (int i = 0; i < folders.length; i++) {
			System.out.println(folders[i].toString());
			ArrayList<MimeMessageService> data = getData(folders[i]);
			dataList.add(i, data);
			
		}
		return dataList;
		
	}
	/**
	 * 根据Folder获取邮件
	 * @param folder
	 * @return
	 */
	public static ArrayList<MimeMessageService> getData(Folder folder){
		ArrayList<MimeMessageService> mmsList = new ArrayList<MimeMessageService>();
		try {
			folder.open(Folder.READ_WRITE);
			Message[] messages = folder.getMessages();
			FetchProfile fp = new FetchProfile();
			fp.add(FetchProfile.Item.ENVELOPE); //fetch subject, from, etc.
			fp.add("X-mailer"); //fetch x-mailer
			folder.fetch(messages, fp);	
			MimeMessageService mms = null;
			for (int i = 0; i < messages.length; i++) {
				mms = new MimeMessageService();
				mms.setMimeMessage((MimeMessage) messages[i]);
				mmsList.add(i, mms);
				System.out.println(mms.getSubject());
			}
			return mmsList;
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "MessagingException");
			e.printStackTrace();
			return null;
		} 
/*		finally {
			try {
				if (folder != null)
					folder.close(true); //expunge the deleted message
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}*/
	}
	
}
