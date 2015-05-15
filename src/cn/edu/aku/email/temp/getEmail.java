package cn.edu.aku.email.temp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

public class getEmail {
	/**
	 * 以pop3方式读取邮件，此方法不能读取邮件是否为已读，已经通过测试
	 * */
	private void getPop3Email() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		try {
			Properties props = System.getProperties();
			props.put("mail.smtp.host", "smtp.163.com");
			props.put("mail.smtp.auth", "true");
			Session session = Session.getDefaultInstance(props, null);
			URLName urln = new URLName("pop3", "pop3.163.com", 110, null,
					"邮箱名（没有@163.com）", "密码");
			// 邮件协议为pop3，邮件服务器是pop3.163.com，端口为110，用户名/密码为abcw111222/123456w
			Store store = session.getStore(urln);
			store.connect();
			Folder folder = store.getFolder("INBOX");
			folder.open(Folder.READ_WRITE);
			Message message[] = folder.getMessages();
			if (message.length > 0) {
				Map<String, Object> map;
				System.out.println("Messages's length: " + message.length);
				ReciveOneMail pmm = null;
				for (int i = 0; i < message.length; i++) {
					System.out.println("======================");
					pmm = new ReciveOneMail((MimeMessage) message[i]);
					System.out.println("Message " + i + " subject: "
							+ pmm.getSubject());
					System.out.println("Message " + i + " sentdate: "
							+ pmm.getSentDate());
					System.out.println("Message " + i + " replysign: "
							+ pmm.getReplySign());

					boolean isRead = pmm.isNew();// 判断邮件是否为已读
					System.out.println("Message " + i + " hasRead: " + isRead);
					System.out.println("Message " + i + "  containAttachment: "
							+ pmm.isContainAttach(message[i]));
					System.out.println("Message " + i + " form: "
							+ pmm.getFrom());
					System.out.println("Message " + i + " to: "
							+ pmm.getMailAddress("to"));
					System.out.println("Message " + i + " cc: "
							+ pmm.getMailAddress("cc"));
					System.out.println("Message " + i + " bcc: "
							+ pmm.getMailAddress("bcc"));
					pmm.setDateFormat("yy年MM月dd日 HH:mm");
					System.out.println("Message " + i + " sentdate: "
							+ pmm.getSentDate());
					System.out.println("Message " + i + " Message-ID: "
							+ pmm.getMessageId());
					// 获得邮件内容===============
					pmm.getMailContent(message[i]);
					System.out.println("Message " + i + " bodycontent: \r\n"
							+ pmm.getBodyText());
					String file_path = File.separator + "mnt" + File.separator
							+ "sdcard" + File.separator;
					System.out.println(file_path);
					pmm.setAttachPath(file_path);
					pmm.saveAttachMent(message[i]);

				}

			} else {
			}
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 以imap方式读取邮件，可以判定读取邮件是否为已读
	 * */
	private void getImapEmail() {
		String user = "abcw111222@163.com";// 邮箱的用户名
		String password = "123456w"; // 邮箱的密码

		Properties prop = System.getProperties();
		prop.put("mail.store.protocol", "imap");
		prop.put("mail.imap.host", "imap.163.com");

		Session session = Session.getInstance(prop);

		int total = 0;
		IMAPStore store;
		try {
			store = (IMAPStore) session.getStore("imap"); // 使用imap会话机制，连接服务器

			store.connect(user, password);

			IMAPFolder folder = (IMAPFolder) store.getFolder("INBOX"); // 收件箱
			folder.open(Folder.READ_WRITE);
			// 获取总邮件数
			total = folder.getMessageCount();
			System.out.println("---共有邮件：" + total + " 封---");
			// 得到收件箱文件夹信息，获取邮件列表
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			System.out.println("未读邮件数：" + folder.getUnreadMessageCount());
			Message[] messages = folder.getMessages();
			if (messages.length > 0) {
				Map<String, Object> map;
				System.out.println("Messages's length: " + messages.length);
				ReciveOneMail pmm = null;
				for (int i = 0; i < messages.length; i++) {
					System.out.println("======================");
					pmm = new ReciveOneMail((MimeMessage) messages[i]);
					System.out.println("Message " + i + " subject: "
							+ pmm.getSubject());
					try {
						System.out.println("Message " + i + " sentdate: "
								+ pmm.getSentDate());
						System.out.println("Message " + i + " replysign: "
								+ pmm.getReplySign());

						boolean isRead;// 用来判断该邮件是否为已读
						String read;
						Flags flags = messages[i].getFlags();
						if (flags.contains(Flags.Flag.SEEN)) {
							System.out.println("这是一封已读邮件");
							isRead = true;
							read = "已读";
						} else {
							System.out.println("未读邮件");
							isRead = false;
							read = "未读";
						}
						System.out.println("Message " + i + " hasRead: "
								+ isRead);
						System.out.println("Message " + i
								+ "  containAttachment: "
								+ pmm.isContainAttach(messages[i]));
						System.out.println("Message " + i + " form: "
								+ pmm.getFrom());
						System.out.println("Message " + i + " to: "
								+ pmm.getMailAddress("to"));
						System.out.println("Message " + i + " cc: "
								+ pmm.getMailAddress("cc"));
						System.out.println("Message " + i + " bcc: "
								+ pmm.getMailAddress("bcc"));
						pmm.setDateFormat("yy年MM月dd日 HH:mm");
						System.out.println("Message " + i + " sentdate: "
								+ pmm.getSentDate());
						System.out.println("Message " + i + " Message-ID: "
								+ pmm.getMessageId());
						// 获得邮件内容===============
						pmm.getMailContent(messages[i]);
						System.out.println("Message " + i
								+ " bodycontent: \r\n" + pmm.getBodyText());
						String file_path = File.separator + "mnt"
								+ File.separator + "sdcard" + File.separator;
						System.out.println(file_path);
						pmm.setAttachPath(file_path);
						pmm.saveAttachMent(messages[i]);

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		} catch (javax.mail.NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
