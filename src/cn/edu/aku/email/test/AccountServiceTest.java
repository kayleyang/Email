package cn.edu.aku.email.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.test.AndroidTestCase;
import android.util.Log;
import cn.edu.aku.email.account.Account;
import cn.edu.aku.email.db.DBHelper;
import cn.edu.aku.email.service.AccountService;

public class AccountServiceTest extends AndroidTestCase{
	public final static String TAG = "AccountServiceTest";
	
	public void testCreateDB() {
		DBHelper dbOpenHelper = new DBHelper(this.getContext());
		dbOpenHelper.getWritableDatabase();
	}
	
	String account_sohu = "yangkai_test@sohu.com";
	String account_sina = "yangkai_test@sina.cn";
	String password = "a123456";
	int store_port = 110;
	Account sina = new Account(account_sina, password,
			"smtp", "smtp.sina.cn", 25,
//			"imap", "imap.sina.cn", 110);
			"pop3", "pop.sina.cn", 110);
	Account sohu = new Account(account_sohu, password,
			"smtp", "smtp.sohu.com", 25,
//			"imap", "mail.sohu.com", 110);
			"pop3", "pop3.sohu.com", 110);
	public void testSave() {
		AccountService service = new AccountService(this.getContext());
		service.save(sina);
		service.save(sohu);
	}
	
	public void testDelete() {
		AccountService service = new AccountService(this.getContext());
		service.delete(account_sohu);
//		service.delete(account_sina);
//		AccountService.delete("yangkai_test@163.com");
	}
	
	public void testUpdate() {
		AccountService service = new AccountService(this.getContext());
		sina.setStore_protocol("imap");
		sina.setStore_host("imap.sina.cn");
		AccountService.update(sina);
		
	}
	
	public void testFind() {
		AccountService service = new AccountService(this.getContext());
		Account account1 = AccountService.find(account_sina);
		Log.e(TAG, account1.toString());
	}
	
	public void testFindAll() {
		AccountService service = new AccountService(this.getContext());
		List<Account> accounts = new ArrayList<Account>();
		accounts = AccountService.findAll();
		for (Iterator iterator = accounts.iterator(); iterator.hasNext();) {
			Account account = (Account) iterator.next();
			Log.e(TAG, account.toString());
		}
	}
	public void testFindAllAccount() {
		AccountService service = new AccountService(this.getContext());
		List<String> accounts = service.findAllAccount();
		for (String account : accounts) {
			Log.e(TAG, account);
		}
	}
}
