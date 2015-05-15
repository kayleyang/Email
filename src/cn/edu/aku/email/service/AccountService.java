package cn.edu.aku.email.service;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import cn.edu.aku.email.account.Account;
import cn.edu.aku.email.account.AccountColumn;
import cn.edu.aku.email.db.DBHelper;

public class AccountService {
	private static  DBHelper dbOpenHelper;
	public AccountService(Context context){
		dbOpenHelper = new DBHelper(context);
	}
	
	public static long save(Account account){
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(AccountColumn.ACCOUNT, account.getAccount());
		values.put(AccountColumn.PASSWORD, account.getPassword());
		
		values.put(AccountColumn.TRANSPORT_PROTOCOL, account.getTransport_protocol());
		values.put(AccountColumn.TRANSPORT_HOST, account.getTransport_host());
		values.put(AccountColumn.TRANSPORT_PORT, account.getTransport_port());
		
		values.put(AccountColumn.STORE_PROTOCOL, account.getStore_protocol());
		values.put(AccountColumn.STORE_HOST, account.getStore_host());
		values.put(AccountColumn.STORE_PORT, account.getStore_port());
		return db.insert("account", null, values );
	}
	
	public static int delete(String account){
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		return db.delete("account", "account = ?", new String[]{account});
	}
	
	public static int update(Account account){
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(AccountColumn.ACCOUNT, account.getAccount());
		values.put(AccountColumn.PASSWORD, account.getPassword());
		
		values.put(AccountColumn.TRANSPORT_PROTOCOL, account.getTransport_protocol());
		values.put(AccountColumn.TRANSPORT_HOST, account.getTransport_host());
		values.put(AccountColumn.TRANSPORT_PORT, account.getTransport_port());
		
		values.put(AccountColumn.STORE_PROTOCOL, account.getStore_protocol());
		values.put(AccountColumn.STORE_HOST, account.getStore_host());
		values.put(AccountColumn.STORE_PORT, account.getStore_port());
		return db.update("account", values, "account = ?", new String[]{account.getAccount()});
	}
	
	public static Account find(String account){
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		Cursor cursor = db.query("account", AccountColumn.COLUMNS, "account = ?", new String[]{account}, 
				null, null, null, null);
		if (cursor.moveToFirst()) {
			String password = cursor.getString(AccountColumn.PASSWORD_COLUMN);
			String transport_protocol = cursor.getString(AccountColumn.TRANSPORT_PROTOCOL_COLUMN);
			String transport_host = cursor.getString(AccountColumn.TRANSPORT_HOST_COLUMN);
			int transport_port = cursor.getInt(AccountColumn.TRANSPORT_PORT_COLUMN);
			String store_protocol = cursor.getString(AccountColumn.STORE_PROTOCOL_COLUMN);
			String store_host = cursor.getString(AccountColumn.STORE_HOST_COLUMN);
			int store_port = cursor.getInt(AccountColumn.STORE_PORT_COLUMN);
			return new Account(account, password,
					transport_protocol, transport_host, transport_port,
					store_protocol, store_host, store_port);
		}
		cursor.close();
		return null;
	}
	
	public static ArrayList<Account> findAll(){
		ArrayList<Account> accounts = new ArrayList<Account>();
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		Cursor cursor = db.query("account", null, null,	null, null, null, null);
		while (cursor.moveToNext()) {
			String account = cursor.getString(AccountColumn.ACCOUNT_COLUMN);
			String password = cursor.getString(AccountColumn.PASSWORD_COLUMN);
			String transport_protocol = cursor.getString(AccountColumn.TRANSPORT_PROTOCOL_COLUMN);
			String transport_host = cursor.getString(AccountColumn.TRANSPORT_HOST_COLUMN);
			int transport_port = cursor.getInt(AccountColumn.TRANSPORT_PORT_COLUMN);
			String store_protocol = cursor.getString(AccountColumn.STORE_PROTOCOL_COLUMN);
			String store_host = cursor.getString(AccountColumn.STORE_HOST_COLUMN);
			int store_port = cursor.getInt(AccountColumn.STORE_PORT_COLUMN);
			accounts.add(new Account(account, password,
					transport_protocol, transport_host, transport_port,
					store_protocol, store_host, store_port));
		}
		cursor.close();
		return accounts;
	}
	
	public List<String> findAllAccount() {
		List<String> accounts = new ArrayList<String>();
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		Cursor cursor = db.query("account", null, null,	null, null, null, null);
		while (cursor.moveToNext()) {
			String account = cursor.getString(AccountColumn.ACCOUNT_COLUMN);
			accounts.add(account);
		}
		accounts.add("添加帐户");
		cursor.close();
		return accounts;
	}
}
