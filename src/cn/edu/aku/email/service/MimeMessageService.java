package cn.edu.aku.email.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import android.os.Parcel;
import android.os.Parcelable;

public class MimeMessageService  {
	private MimeMessage mimeMessage = null;
	private String attachDir = ""; // 附件下载后的存放目录
	private StringBuffer bodytext = new StringBuffer(); // 存放邮件内容的StringBuffer对象
	private String dateformat = "yy-MM-dd HH:mm"; // 默认的日前显示格式

	public MimeMessageService() {
	}

	public MimeMessageService(MimeMessage mimeMessage) {
		this.mimeMessage = mimeMessage;
	}
	
	public void setMimeMessage(MimeMessage mimeMessage) {
		this.mimeMessage = mimeMessage;
	}
	public MimeMessage getMimeMessage() {
		return this.mimeMessage;
	}

	
	/**
	 * 　获得发件人的地址和姓名
	 */
	public String getFrom() {
		InternetAddress address[];
		try {
			address = (InternetAddress[]) mimeMessage.getFrom();
			String from = address[0].getAddress();
			if (from == null) {
				from = "";
			}

			String personal = address[0].getPersonal();
			if (personal == null) {
				personal = "";
			}
			String fromaddr = personal + "<" + from + ">";
			return fromaddr;
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 获得邮件的收件人，抄送，和密送的地址和姓名，根据所传递的参数的不同 TO --- 收件地址 CC --- 抄送地址 BCC --- 密送地址
	 * 
	 * @param type
	 * @return
	 */
	public String getMailAddress(String type) {
		String mailaddr = "";
		try {
			String addtype = type.toUpperCase();
			InternetAddress[] address = null;
			if (addtype.equals("TO") || addtype.equals("CC")
					|| addtype.equals("BBC")) {
				if (addtype.equals("TO")) {
					address = (InternetAddress[]) mimeMessage
							.getRecipients(Message.RecipientType.TO);
				} else if (addtype.equals("CC")) {
					address = (InternetAddress[]) mimeMessage
							.getRecipients(Message.RecipientType.CC);
				} else {
					address = (InternetAddress[]) mimeMessage
							.getRecipients(Message.RecipientType.BCC);
				}
				if (address != null) {
					for (int i = 0; i < address.length; i++) {
						String email = address[i].getAddress();
						if (email == null)
							email = "";
						else {
							email = MimeUtility.decodeText(email);
						}
						String personal = address[i].getPersonal();
						if (personal == null)
							personal = "";
						else {
							personal = MimeUtility.decodeText(personal);
						}
						String compositeto = personal + "<" + email + ">";
						mailaddr += "," + compositeto;
					}
					mailaddr = mailaddr.substring(1);
				}
			} else {
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return mailaddr;
	}

	/**
	 * 　获得邮件主题 　　
	 */
	public String getSubject() {
		String subject = "";
		try {
			subject = MimeUtility.decodeText(mimeMessage.getSubject());
			if (subject == null)
				subject = "";
		} catch (Exception e) {
			// TODO: handle exception
		}
		return subject;
	}

	/**
	 * 　获得邮件发送日期
	 */
	public String getSendDate() {
		Date senddate;
		try {
			senddate = mimeMessage.getSentDate();
			SimpleDateFormat format = new SimpleDateFormat(dateformat);
			return format.format(senddate);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 　解析邮件，把得到的邮件内容保存到一个StringBuffer对象中，解析邮件
	 * 　主要是根据MimeType类型的不同执行不同的操作，一步一步的解析
	 */
	public void getMailContent(Part part) {
		String contenttype;
		try {
			contenttype = part.getContentType();
			int nameindex = contenttype.indexOf("name");
			boolean conname = false;
			if (nameindex != -1)
				conname = true;
			if (part.isMimeType("text/plain") && !conname) {
				System.out.println("text/plain");
				System.out.println((String) part.getContent());
				bodytext.append((String) part.getContent());
			} else if (part.isMimeType("text/html") && !conname) {
				System.out.println("text/html");
				System.out.println((String) part.getContent());
				bodytext.append((String) part.getContent());
			} else if (part.isMimeType("multipart/*")) {
				System.out.println("multipart/*");
				Multipart multipart = (Multipart) part.getContent();
				int counts = multipart.getCount();
				for (int i = 0; i < counts; i++) {
					getMailContent(multipart.getBodyPart(i));
				}
			} else if (part.isMimeType("message/rfc822")) {
				System.out.println("message/rfc822");
				getMailContent((Part) part.getContent());
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readPart(Part p) {
		try {
			Object content = p.getContent();
			if (content instanceof String) {
				System.out.println(content);
				bodytext.append(content);
			} else if (content instanceof Multipart) {
				Multipart mp = (Multipart) content;
				int count = mp.getCount();
				for (int i = 0; i < count; i++) {
					Part bodyPart = mp.getBodyPart(i); // or check:
														// boyPart.getDisposition()
					readPart(bodyPart); // recursion
				}
			} else if (content instanceof InputStream) {
				System.out.println("****found inputstream as a part's content****");
			} else {
				System.out.println("****unknown type****");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 　获得邮件正文内容
	 */
	public String getBodyText() {
//		readPart((Part)mimeMessage);
		getMailContent((Part)mimeMessage);
		
		return bodytext.toString();
	}

	/**
	 * 　判断此邮件是否需要回执，如果需要回执返回"true",否则返回"false"
	 * 
	 * @throws MessagingException
	 */
	public boolean getReplySign() throws MessagingException {
		boolean replysign = false;
		String needreply[] = mimeMessage
				.getHeader("Disposition-Notification-To");
		if (needreply != null) {
			replysign = true;
		}
		return replysign;
	}

	/**
	 * 　获得此邮件的Message-ID 　　
	 * 
	 * @throws MessagingException
	 */
	public String getMessageId() throws MessagingException {
		return mimeMessage.getMessageID();
	}

	/**
	 * 　【判断此邮件是否已读，如果未读返回返回false,反之返回true】 　　
	 * 
	 * @throws MessagingException
	 */
	public boolean isNew() throws MessagingException {
		boolean isnew = false;
		Flags flags = ((Message) mimeMessage).getFlags();
		Flags.Flag[] flag = flags.getSystemFlags();
		for (int i = 0; i < flag.length; i++) {
			if (flag[i] == Flags.Flag.SEEN) {
				isnew = true;
				break;
			}
		}
		return isnew;
	}

	/**
	 * 　判断此邮件是否包含附件 　
	 * 
	 * @throws MessagingException
	 */
	public boolean isContainAttach(Part part) throws Exception {
		boolean attachflag = false;
		if (part.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) part.getContent();
			// 获取附件名称可能包含多个附件
			for (int j = 0; j < mp.getCount(); j++) {
				BodyPart mpart = mp.getBodyPart(j);
				String disposition = mpart.getDescription();
				if ((disposition != null)
						&& ((disposition.equals(Part.ATTACHMENT)) || (disposition
								.equals(Part.INLINE)))) {
					attachflag = true;
				} else if (mpart.isMimeType("multipart/*")) {
					attachflag = isContainAttach(mpart);
				} else {
					String contype = mpart.getContentType();
					if (contype.toLowerCase().indexOf("application") != -1)
						attachflag = true;
					if (contype.toLowerCase().indexOf("name") != -1)
						attachflag = true;
				}
			}
		} else if (part.isMimeType("message/rfc822")) {
			attachflag = isContainAttach((Part) part.getContent());
		}
		return attachflag;
	}

	/**
	 * 　【保存附件】 　
	 * 
	 * @throws Exception
	 * @throws IOException
	 * @throws MessagingException
	 * @throws Exception
	 */
	public void saveAttachMent(Part part) throws Exception {
		String fileName = "";
		if (part.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) part.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				BodyPart mpart = mp.getBodyPart(i);
				String disposition = mpart.getDisposition();
				if ((disposition != null)
						&& ((disposition.equals(Part.ATTACHMENT)) || (disposition
								.equals(Part.INLINE)))) {
					fileName = mpart.getFileName();
					if (fileName.toLowerCase().indexOf("gbk") != -1) {
						fileName = MimeUtility.decodeText(fileName);
					}
					saveFile(fileName, mpart.getInputStream());
				} else if (mpart.isMimeType("multipart/*")) {
					saveAttachMent(mpart);
				} else {
					fileName = mpart.getFileName();
					if ((fileName != null)
							&& (fileName.toLowerCase().indexOf("GB2312") != -1)) {
						fileName = MimeUtility.decodeText(fileName);
						saveFile(fileName, mpart.getInputStream());
					}
				}
			}
		} else if (part.isMimeType("message/rfc822")) {
			saveAttachMent((Part) part.getContent());
		}
	}

	/**
	 * 　【设置日期显示格式】
	 */
	public void setDateFormat(String format) {
		this.dateformat = format;
	}

	/**
	 * 　【设置附件存放路径】
	 */
	public void setAttachDir(String attachDir) {
		this.attachDir = attachDir;
	}

	/**
	 * 　【获得附件存放路径】
	 */

	public String getAttachDir() {
		return attachDir;
	}

	/**
	 * 　【真正的保存附件到指定目录里】
	 */
	private void saveFile(String fileName, InputStream in) throws Exception {
		String attachDir = getAttachDir();
		File filePath = new File(attachDir + fileName);
		System.out.println("storefile's path: " + filePath.toString());
		FileOutputStream fos = new FileOutputStream(filePath);

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] data = new byte[4096];
		int count = -1;
		while ((count = in.read(data, 0, 4096)) != -1)
			outStream.write(data, 0, count);
		data = null;
		fos.write(outStream.toByteArray());
		fos.close();
	}
}
