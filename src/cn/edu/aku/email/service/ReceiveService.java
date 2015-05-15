package cn.edu.aku.email.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import cn.edu.aku.email.account.Account;
import cn.edu.aku.email.account.AccountColumn;
import cn.edu.aku.email.db.DBHelper;
import cn.edu.aku.email.receive.ReceiveColumn;

public class ReceiveService {
	private static final String TAG = "ReceiveService";
	private static  DBHelper dbOpenHelper;

	private static String STATUS;
	private static String SDCARD_DIR = 
			Environment.getExternalStorageDirectory().getPath() + "/email/eml";
	private static String NOSDCARD_DIR = 
			Environment.getDataDirectory().getPath() + "/data/cn.edu.aku.email/eml";
	private static String saveDir;
	
	public ReceiveService(Context context){
		dbOpenHelper = new DBHelper(context);
		setSaveDir();
	}
	
	public static Boolean isHasSDcard() {
		STATUS = Environment.getExternalStorageState();
		if (STATUS.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}
	public static void setSaveDir(){
		if (isHasSDcard()) {
			saveDir = SDCARD_DIR;
		} else {
			saveDir = NOSDCARD_DIR;
		}
	}
	
	public static void ReceiveEml(Account account) {
		setSaveDir();
		//创建账号文件夹
		String accountDir = saveDir + "/" + account.getAccount();
		System.out.println(accountDir);
		File accountFile = new File(accountDir);
		if (!accountFile.exists()) {
			accountFile.mkdirs();
		}
		Session session = MailService.createSession(account);
		Store store;
		try {
			store = session.getStore();
			store.connect();
			Folder[] folders = store.getDefaultFolder().list();
			for (int i = 0; i < folders.length; i++) {
				String folderDir = accountDir + "/" + folders[i].getName();
				System.out.println(folderDir);
				File folderFile = new File(folderDir);
				if (!folderFile.exists()) {
					folderFile.mkdirs();
				}
				ArrayList<MimeMessageService> data = MailService.getData(folders[i]); 
				for (int j = 0; j < data.size(); j++) {
					MimeMessageService mms = data.get(j);
					String emlDir = folderDir + "/" + mms.getMessageId() + ".eml";
					System.out.println(emlDir);
					if (mms.getMessageId() != null){
						if(!isHasMessageId(mms.getMessageId())) {
							FileOutputStream fos = new FileOutputStream(emlDir, false);
							mms.getMimeMessage().writeTo(fos);
							insert(account.getAccount()
									, folders[i].getName()
									, mms.getMessageId()
									, mms.getSendDate()
									, mms.getSubject());
						} else {
							update(account.getAccount()
									, folders[i].getName()
									, mms.getMessageId()
									, mms.getSendDate()
									, mms.getSubject());
						}
					}
				}
			}
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public static long insert(String account, String folder,
			String message_id, String send_date, String subjece) {
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(ReceiveColumn.ACCOUNT, account);
		values.put(ReceiveColumn.FOLDER, folder);
		values.put(ReceiveColumn.MESSAGE_ID, message_id);
		values.put(ReceiveColumn.SEND_DATE, send_date);
		values.put(ReceiveColumn.SUBJECT, subjece);
		return db.insert("receive", null, values );
	}
	
	private static int update(String account, String folder, String message_id,
			String send_date, String subject) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(ReceiveColumn.ACCOUNT, account);
		values.put(ReceiveColumn.FOLDER, folder);
		values.put(ReceiveColumn.MESSAGE_ID, message_id);
		values.put(ReceiveColumn.SEND_DATE, send_date);
		values.put(ReceiveColumn.SUBJECT, subject);
		return db.update("receive", values, "message_id = ?", new String[]{message_id});
	}
	public static int deleteAccount(String account){
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		String fileDir = saveDir + "/" +account;
		File file = new File(fileDir);
		deleteFile(file);
		Log.i(TAG, "删除" +fileDir + "成功" );
		return db.delete("receive", "account = ?", new String[]{account});
	}
	public static int deleteFolder(String account, String folder){
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		String fileDir = saveDir + "/" +account + "/" + folder;
		File file = new File(fileDir);
		deleteFile(file);
		Log.i(TAG, "删除" +fileDir + "成功" );
		return db.delete("receive", "account = ? AND folder = ?", new String[]{account, folder});
	}
	public static int deleteEml(String message_id){
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		Cursor cursor = db.query("receive",ReceiveColumn.COLUMNS, "message_id = ?", new String[]{message_id},
				null, null, null);
		while (cursor.moveToNext()) {
			String account = cursor.getString(ReceiveColumn.ACCOUNT_COLUMN);
			String folder = cursor.getString(ReceiveColumn.FOLDER_COLUMN);
			String fileDir = saveDir + "/" + account + "/" + folder + "/" + message_id + ".eml";
			File file = new File(fileDir);
			deleteFile(file);
			
		}
		return db.delete("receive", "message_id = ?", new String[]{message_id});
	}
	public static Boolean isHasMessageId(String message_id) {
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		Cursor cursor = db.query("receive", ReceiveColumn.COLUMNS, "message_id = ?", new String[]{message_id}, 
				null, null, null, null);
		if (cursor.moveToFirst()) {
			return true;
		}
		cursor.close();
		return false;
	}
	
	public static ArrayList<String> findMessageIds(String account, String folder) {
		ArrayList<String> messageIdList = new ArrayList<String>();
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		Cursor cursor = db.query("receive", null, "account = ? AND folder = ? ", new String[]{account, folder}, 
				null, null, null, null);
		while (cursor.moveToNext()) {
			String messageId = cursor.getString(ReceiveColumn.MESSAGE_ID_COLUMN);
			messageIdList.add(messageId);
		}
		return messageIdList;
	}
	public static ArrayList<String> findFolders(String account) {
		ArrayList<String> folders = new ArrayList<String>();
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		Cursor cursor = db.query(true, "receive", new String[]{ReceiveColumn.FOLDER},
				"account = ? ", new String[]{account}, null, null, null, null);
		while (cursor.moveToNext()) {
			String folder = cursor.getString(0);
			folders.add(folder);
		}
		cursor.close();
		return folders;
	}
	
	public static ArrayList<ArrayList<MimeMessageService>> getData(String account){
		ArrayList<ArrayList<MimeMessageService>> dataList = new ArrayList<ArrayList<MimeMessageService>>();
		System.out.println(account);
		List<String> folders = findFolders(account);
		System.out.println(account + "fasdfadsf");
		for (int i = 0; i < folders.size(); i++) {
			ArrayList<MimeMessageService> data = getData(account, folders.get(i));
			System.out.println(account);
			dataList.add(i, data);
		}
		return dataList;
	}
	public static ArrayList<MimeMessageService> getData(String account, String folder){
		ArrayList<MimeMessageService> mmsList = new ArrayList<MimeMessageService>();
		ArrayList<String> messageIdList = findMessageIds(account, folder);
		MimeMessageService mms = null;
		for (int i = 0; i < messageIdList.size(); i++) {
			String emlPath = saveDir + "/" + account + "/" + folder + "/" + messageIdList.get(i) + ".eml";
			try {
				InputStream fis = new FileInputStream(emlPath);
				Object emlObj = (Object)fis;
				Session session = Session.getDefaultInstance(System.getProperties(), null);
				mms = new MimeMessageService();
				mms.setMimeMessage(new MimeMessage(session,fis));
				mmsList.add(i, mms);
				System.out.println(mms.getSubject());
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return mmsList;
	}
	public static void deleteFile(File file) {  
		if (file.isFile()) {  
			file.delete();  
			return;  
		}  
		if(file.isDirectory()){  
			File[] childFiles = file.listFiles();  
			if (childFiles == null || childFiles.length == 0) {  
				file.delete();  
				return;  
			}  
			for (int i = 0; i < childFiles.length; i++) {  
				deleteFile(childFiles[i]);  
			}  
			file.delete();  
		}  
	} 
	
}
