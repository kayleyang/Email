package cn.edu.aku.email.account;

import javax.mail.MessagingException;

import cn.edu.aku.email.R;
import cn.edu.aku.email.main.MainActivity;
import cn.edu.aku.email.service.AccountService;
import cn.edu.aku.email.service.MailService;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SetUpAccountActivity extends Activity {
	
	private static final String TAG = "SetUpAccountActivity";
	
	private static EditText accountEditText;
	private static EditText passwordtEditText;
	private static EditText transProtocolEditText;
	private static EditText transHostEditText;
	private static EditText transPortEditText;
	private static EditText storeProtocolEditText;
	private static EditText storeHostEditText;
	private static EditText storePortEditText;
	private static Button setUptButton;
	
	private static String account;
	private static String password;
	private static String transport_protocol;
	private static String transport_host;
	private static int transport_port;
	private static String store_protocol;
	private static String store_host;
	private static int store_port;
	
	private static boolean flag = false;
	// 声明弹出对话框
	private static final int CONERRORDLG = 1;
	static ProgressDialog loginDlg;
	// 登录成功标识
	static boolean loginSuccess = false;

	// 监听消息
	private static final int MSG_SUCCESS = 1000;// 成功的标识
	private static final int MSG_FAILURE = 1001;// 失败的标识
	// 监听句柄
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@SuppressWarnings("deprecation")
		@Override
		public void handleMessage(Message msg) {// 此方法在ui线程运行
			switch (msg.what) {
			case MSG_SUCCESS:
				loginSuccess = true;
				Intent intent = new Intent();
				intent.setClass(SetUpAccountActivity.this, MainActivity.class);
				startActivity(intent);
				SetUpAccountActivity.this.finish();
				break;
			case MSG_FAILURE:
				showDialog(CONERRORDLG);
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);
		setTitle("自定义电子邮箱");
		initView();
		initData();
	}

	private void initView() {
		// TODO Auto-generated method stub
		accountEditText = (EditText) findViewById(R.id.edtTxt_account);
		passwordtEditText = (EditText) findViewById(R.id.edtTxt_password);
		transProtocolEditText = (EditText) findViewById(R.id.edtTxt_transport_protocol);
		transHostEditText = (EditText) findViewById(R.id.edtTxt_transport_host);
		transPortEditText = (EditText) findViewById(R.id.edtTxt_transport_port);
		storeProtocolEditText = (EditText) findViewById(R.id.edtTxt_store_protocol);
		storeHostEditText = (EditText) findViewById(R.id.edtTxt_store_host);
		storePortEditText = (EditText) findViewById(R.id.edtTxt_store_port);
		setUptButton = (Button) findViewById(R.id.btn_setUp);
	}
	
	private void initData() {
		// TODO Auto-generated method stub
		Intent intent = this.getIntent();
		String account = intent.getStringExtra("account");
		String password = intent.getStringExtra("password");
		String domain = account.substring(account.indexOf("@") + 1,	account.length());
		
		accountEditText.setText(account);
		passwordtEditText.setText(password);
		transProtocolEditText.setText("smtp");
		transHostEditText.setText("smtp." + domain);
		transPortEditText.setText("25");
		storeProtocolEditText.setText("pop3");
		storeHostEditText.setText("pop3." + domain);
		storePortEditText.setText("110");
		
		setUptButton.setOnClickListener(new setUpBtnListener());
	}
	
	private class setUpBtnListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			account = accountEditText.getText().toString();
			password = passwordtEditText.getText().toString();

			transport_protocol = transProtocolEditText.getText().toString();
			store_protocol = storeProtocolEditText.getText().toString();
			
			transport_host = transHostEditText.getText().toString();
			store_host = storeHostEditText.getText().toString();
			
			String transport_portString = transPortEditText.getText().toString();
			String store_portString = storePortEditText.getText().toString();
			if (account.length() == 0) {
				Toast.makeText(getApplicationContext(), "用户名不能为空", Toast.LENGTH_SHORT).show();
			}
			if (!MailService.CheckAccount(account)) {
				Toast.makeText(getApplicationContext(), "邮箱格式不正确", Toast.LENGTH_SHORT).show();
			}
			if (password.length() == 0) {
				Toast.makeText(getApplicationContext(), "密码不能为空", Toast.LENGTH_SHORT).show();
			}
			if (transport_protocol.length() == 0) {
				Toast.makeText(getApplicationContext(), "发送协议不能为空", Toast.LENGTH_SHORT).show();
			}
			if (store_protocol.length() == 0) {
				Toast.makeText(getApplicationContext(), "接收协议不能为空", Toast.LENGTH_SHORT).show();
			}
			if (transport_portString.length() == 0) {
				Toast.makeText(getApplicationContext(), "发送端口不能为空", Toast.LENGTH_SHORT).show();
			} else {
				transport_port = Integer.parseInt(transport_portString);
			}
			if (store_portString.length() == 0) {
				Toast.makeText(getApplicationContext(), "接收端口不能为空", Toast.LENGTH_SHORT).show();
			} else {
				store_port = Integer.parseInt(store_portString);
				flag = true;
			}
			if (flag) {
				loginDlg = new ProgressDialog(SetUpAccountActivity.this);
				loginDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				loginDlg.setTitle("提示:");
				loginDlg.setMessage("正在登录....");
				// loginDlg.setIcon(R.drawable.img2);
				loginDlg.setIndeterminate(false);
				loginDlg.setCancelable(true);
				// 为读取邮件显示一个提示信息的线程.......
				loginDlg.show();
				new Thread(new myThread()).start();
			}
		}
		
	}
	private class myThread implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				//验证用户名和密码
				if (MailService.CheckAccount(account)) {
					MailService.verify(account, password, transport_protocol, transport_host, transport_port);
					
					Account a = new Account(account, password,
							transport_protocol, transport_host, transport_port,
							store_protocol, store_host, store_port);
					new AccountService(SetUpAccountActivity.this);
					AccountService.save(a);
					loginDlg.cancel();
					handler.sendEmptyMessageDelayed(MSG_SUCCESS, 0);
				} else {
					loginDlg.cancel();
					Log.e(TAG, "用户名格式错误");
					handler.sendEmptyMessageDelayed(MSG_FAILURE, 0);
				}
			} catch (MessagingException e) {
				Log.e(TAG, "验证失败");
				e.printStackTrace();
				loginDlg.cancel();
				handler.sendEmptyMessageDelayed(MSG_FAILURE, 0);
			}
		}
	}

	/**
	 * 错误窗口
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case CONERRORDLG:
			return conErrorDlg(SetUpAccountActivity.this);
		}
		return null;
	}

	/**
	 * 连接失败Dialog
	 * @param context
	 * @return
	 */
	private Dialog conErrorDlg(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		// builder.setIcon(R.drawable.img2);
		builder.setTitle("提示: ");
		builder.setMessage("连接服务器失败!\n您是否要重新填写登录信息 ?");
		builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// 确定按钮的响应====重新填写
				loginDlg.cancel();
			}
		});
		builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// 取消按钮的响应=====空账号进入 (下个Activity)
				loginSuccess = false;
				// 跳转
				/*
				 * Intent intent = new Intent();
				 * intent.setClass(ActivityMain.this, ActivityChoose.class);
				 * startActivity(intent); ActivityMain.this.finish();
				 */
			}
		});
		return builder.create();
	}
}
