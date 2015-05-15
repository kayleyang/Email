package cn.edu.aku.email;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import cn.edu.aku.email.account.Account;
import cn.edu.aku.email.account.AddAccountActivity;
import cn.edu.aku.email.main.MainActivity;
import cn.edu.aku.email.patternpassword.PatternPasswordActivity;
import cn.edu.aku.email.service.AccountService;
import cn.edu.aku.email.service.ReceiveService;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

public class SplashActivity extends Activity {
	
	private static final String TAG = "SplashActivity";
	private static final int NetWorkDialog = 1; 
	
	private SharedPreferences mPreferences ;
	private boolean needVerify ;
	private ReceiveService receiveService;
	private AccountService accountService;
	private Timer timer;
	private Long startTime;
	private Thread mThread;
	private final Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				
				mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()) ;
				needVerify = mPreferences.getBoolean("NeedVerify", false) ;
				System.out.println("NeedVerify" + needVerify);
				if (needVerify) {
					Intent intent = new Intent(SplashActivity.this, PatternPasswordActivity.class);
					SplashActivity.this.startActivity(intent);
				}
				if (accountService.findAllAccount().isEmpty()) {
					Intent intent = new Intent(SplashActivity.this, AddAccountActivity.class);
					SplashActivity.this.startActivity(intent); //启动 添加账户界面
				} else {
					Intent intent = new Intent(SplashActivity.this, MainActivity.class);
					SplashActivity.this.startActivity(intent);    //启动 主账户界面
				}
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);   
				SplashActivity.this.finish();    //关闭自己这个开场屏
				break;
			default:
				break;
			}
		}
	};
	private final TimerTask task = new TimerTask() {
		
		@Override
		public void run() {
			if (task.scheduledExecutionTime() - startTime > 2000) {
				Message message = new Message();
				message.what = 0;
				mHandler.sendMessage(message);
				
				Log.i(TAG, "结束计时");
				timer.cancel();
				this.cancel();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		accountService = new AccountService(getApplicationContext());
		receiveService = new ReceiveService(getApplicationContext());
		if (isConnectedNetwork()) {
			Log.i(TAG, "开始计时");
			timer = new Timer(true);
			startTime = System.currentTimeMillis();
			timer.schedule(task, 0, 1);
			if (mThread == null) {
				mThread = new Thread(receiveRunnable);
			}
		} else {
			Log.e(TAG, "网络连接失败");
		}
		
	}
	
	Runnable receiveRunnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			ArrayList<Account> accounts = accountService.findAll();
			for (Account account : accounts) {
				receiveService.ReceiveEml(account);
			}
		}
	};
	
	/**
	 * 检查网络是否连接成功
	 * @return
	 */
	private boolean isConnectedNetwork() {
		ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (conn != null) {
			NetworkInfo [] infos = conn.getAllNetworkInfo();
			if (infos != null) {
				for (int i = 0; i < infos.length; i++) {
					if (infos[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		showDialog(NetWorkDialog);
		return false;
	}
	
	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case NetWorkDialog:
			return buildNetWorkDialog(SplashActivity.this);
		}
		return null;
	}

	private Dialog buildNetWorkDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setTitle("网络连接失败，请检查网络连接");
		
		builder.setNeutralButton("设置网络", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent("android.settings.WIRELESS_SETTINGS");
	            startActivity(intent);
	            android.os.Process.killProcess(android.os.Process.myPid());
			}
		});
		builder.setPositiveButton("忽略", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Message message = new Message();
				message.what = 0;
				mHandler.sendMessage(message);
			}
		});
		builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//先让程序到Home界面，然后再将process杀死
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		});
		return builder.create();
	}
	
}
