package cn.edu.aku.email.account;

import javax.mail.MessagingException;

import cn.edu.aku.email.R;
import cn.edu.aku.email.main.MainActivity;
import cn.edu.aku.email.service.AccountService;
import cn.edu.aku.email.service.MailService;
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
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class AddAccountActivity extends Activity{
	
	private static final String TAG = "AddAccountActivity";

	private EditText accountEditText;
	private EditText passwordEditText;
	
	private CheckBox isShow;
	private Button nextButton;
	private Button setupButton;
	
	private static String account;
	private static String password;
	
	private static AccountService accountService;
	
	// 声明弹出对话框
	private static final int CONERRORDLG = 1;
	private static final int INFERRORDLG = 2;
	static ProgressDialog loginDlg;
	// 登录成功标识
	static boolean loginSuccess = false;

	// 监听消息
	private static final int MSG_SUCCESS = 1002;// 成功的标识
	private static final int MSG_FAILURE = 1000;// 失败的标识
	// 监听句柄
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {// 此方法在ui线程运行
			switch (msg.what) {
			case MSG_SUCCESS:
				loginSuccess = true;
				Intent intent = new Intent();
				intent.setClass(AddAccountActivity.this, MainActivity.class);
				startActivity(intent);
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
		setContentView(R.layout.activity_add);
		setTitle("添加电子邮箱");
		initView();
		initData();
	}


	private void initView() {
		// TODO Auto-generated method stub
		accountEditText = (EditText) findViewById(R.id.editAccount);
		passwordEditText = (EditText) findViewById(R.id.editPassword);

		isShow = (CheckBox) findViewById(R.id.isShowPassword);
		nextButton = (Button) findViewById(R.id.btnNext);
		setupButton = (Button) findViewById(R.id.btnSetup);
		
	}

	private void initData() {
		// TODO Auto-generated method stub
		accountEditText.addTextChangedListener(mTextWatcher);
		passwordEditText.addTextChangedListener(mTextWatcher);
		isShow.setOnCheckedChangeListener(new showPasswordOnCheckedChangeListener());
		nextButton.setOnClickListener(new loginButtonListener());
		setupButton.setOnClickListener(new setUpButtonListener());
	}
	/**
	 * activity实现TextWatcher接口
	 * 当页面所有edittext不为空时按钮才可点击
	 * 根据页面edittext的内容控制按钮可点击状态 
	 */
	TextWatcher mTextWatcher = new TextWatcher(){
		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
//			Log.d(TAG, "=" + accountEditText.getText().toString() + "-" + passwordEditText.getText().toString() + "=");
			if ((accountEditText.getText().toString()).isEmpty() 
					|| (passwordEditText.getText().toString()).isEmpty()) {
				nextButton.setEnabled(false);
				setupButton.setEnabled(false);
			} else {  
				nextButton.setEnabled(true);
				setupButton.setEnabled(true);
			} 
		}


		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub

		}


		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			// TODO Auto-generated method stub

		}
	};
	
	//监听是否显示密码
	private final class showPasswordOnCheckedChangeListener implements OnCheckedChangeListener  {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isShow.isChecked()) {  
				// 显示密码  
				passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);   
			} 
			else {  
				// 隐藏密码  
				passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);  
			}
		}
	}
	/**
	 * 监听登录按钮
	 */
	private final class loginButtonListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			account = accountEditText.getText().toString();
			password = passwordEditText.getText().toString();
			if (account.length() == 0 || password.length() == 0) {
				showDialog(INFERRORDLG);
			} else {
				// 显示进度条
				loginDlg = new ProgressDialog(AddAccountActivity.this);
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
	/**
	 *监听设置按钮
	 */
	private final class setUpButtonListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(AddAccountActivity.this, SetUpAccountActivity.class);
			
			intent.putExtra("account", accountEditText.getText().toString());
			intent.putExtra("password", passwordEditText.getText().toString());

			AddAccountActivity.this.startActivity(intent);
			overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			AddAccountActivity.this.finish();
		}
	}
	private class myThread implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				
				//验证用户名和密码
				if (MailService.CheckAccount(account)) {
					MailService.verify(account, password);
					
					Account a = new Account(account, password);
					accountService = new AccountService(getApplicationContext());
					AccountService.save(a);
					loginDlg.cancel();
					handler.sendEmptyMessageDelayed(MSG_SUCCESS, 0);
				} else {
					loginDlg.cancel();
					handler.sendEmptyMessageDelayed(MSG_FAILURE, 0);
				}				
			} catch (MessagingException e) {
				loginDlg.cancel();
				e.printStackTrace();
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
			return conErrorDlg(AddAccountActivity.this);

		case INFERRORDLG:
			return infErrorDlg(AddAccountActivity.this);
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
			}
		});
		return builder.create();
	}
	/**
	 * 账户错误Dialog
	 * @param context
	 * @return
	 */
	private Dialog infErrorDlg(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		// builder.setIcon(R.drawable.img1);
		builder.setTitle("提示: ");
		builder.setMessage("请输入正确的账户信息！");

		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});
		return builder.create();
	}
	
}
