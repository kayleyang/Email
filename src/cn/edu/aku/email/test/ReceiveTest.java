package cn.edu.aku.email.test;

import java.util.ArrayList;

import cn.edu.aku.email.account.Account;
import cn.edu.aku.email.receive.Receive;
import cn.edu.aku.email.service.AccountService;
import cn.edu.aku.email.service.ReceiveService;
import android.os.Environment;
import android.test.AndroidTestCase;

public class ReceiveTest extends AndroidTestCase{
	public void testEnvironment() {
		System.out.println(Environment.getExternalStorageState());
		System.out.println(Environment.getDataDirectory().getPath());
		System.out.println(Environment.getDownloadCacheDirectory().getPath());
		System.out.println(Environment.getExternalStorageDirectory().getPath());
		System.out.println(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
		System.out.println(Environment.getRootDirectory().getPath());
	}
	public void testReceive(){
//		String account = "yangkai_test@sina.cn";
		String account = "yangkai_test@sohu.com";
//		String account = "yangkai_test@163.com";
		AccountService accountService = new AccountService(this.getContext());
		ReceiveService receiveService = new ReceiveService(this.getContext());
		Account a = accountService.find(account);
		receiveService.ReceiveEml(a);
	}
	public void testGetFolders(){
		String account = "yangkai_test@sina.cn";
//		String account = "yangkai_test@163.com";
		ReceiveService receiveService = new ReceiveService(this.getContext());
		ArrayList<String> folders = ReceiveService.findFolders(account);
		for (String folder : folders) {
			System.out.println(folder);
		}
	}
	public void testGetData(){
		
	}
	
}
