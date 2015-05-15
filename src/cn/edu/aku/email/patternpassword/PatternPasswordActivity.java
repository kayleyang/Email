package cn.edu.aku.email.patternpassword;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import cn.edu.aku.email.R;
import cn.edu.aku.email.main.MainActivity;
import cn.edu.aku.email.patternpassword.PatternPasswordView.OnBeginListener;
import cn.edu.aku.email.patternpassword.PatternPasswordView.OnCompleteListener;
import cn.edu.aku.email.patternpassword.PatternPasswordView.OnErrorListener;

public class PatternPasswordActivity extends Activity {
	private TextView mTextView ;
	private PatternPasswordView mPatternPasswordView ;
	private Button mButton_reset , mButton_next ;
	private String password ;
	private SharedPreferences mPreferences ;
	private boolean needVerify ;
	

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState) ;
		requestWindowFeature(Window.FEATURE_NO_TITLE) ;
		// 只能在HVGA 和 WVGA下使用该布局 否则出问题
		this.isNeedVerify();
		if (needVerify) {
			VerifyPassword() ;
		} else {
			NoNeedToVerify() ;
		}
	}
	private void NoNeedToVerify() {
		setContentView(R.layout.setpattern_layout) ;
		this.setLayout() ;
		this.addListenerToComponent() ;
	}
	public void isNeedVerify() {
		mPreferences = getApplicationContext().getSharedPreferences("email.preference", Context.MODE_PRIVATE);
//		mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()) ;
		needVerify = mPreferences.getBoolean("NeedVerify", false) ;
		System.out.println(needVerify);
	}
	private void restoreBoolean() {
		mPreferences.edit().putBoolean("NeedVerify", true).commit() ;
	}
	/**
	 * 	(non-Javadoc)
	 * 	@see android.app.Activity#onResume()
	 */
	protected void onResume() {
		super.onResume() ;
	}
	private void VerifyPassword() {
		setContentView(R.layout.login_pattern_activity) ;
		mPatternPasswordView = (PatternPasswordView) this.findViewById(R.id.PatternPasswordView_PatternLogin);
		mTextView = (TextView) findViewById(R.id.TextView_PatternLoginTitle) ;
		mPatternPasswordView.setOnBeginListener(new OnBeginListener() {
			public void onBegin() {
				mTextView.setText("请拖动4个点以上") ;
			}
		}) ;
		mPatternPasswordView.setOnErrorListener(new OnErrorListener() {
			public void onError() {
				mTextView.setText("绘制错误，请重绘") ;
			}
		}) ;
		mPatternPasswordView.setOnCompleteListener(new OnCompleteListener() {
			public void onComplete(String mPassword) {
				//	如果密码正确,则进入主页面
				if (mPatternPasswordView.verifyPassword(mPassword)) {
					validIdentityLogin() ;
				} else {
					invalidIdentityRetry() ;
				}
			}
		});
	}
	private void validIdentityLogin() {
		Toast.makeText(this , "验证成功" , Toast.LENGTH_SHORT).show() ;
		NoNeedToVerify() ;
	}
	private void invalidIdentityRetry() {
		Toast.makeText(this , "密码输入错误,请重新输入" , Toast.LENGTH_SHORT).show() ;
		mPatternPasswordView.clearPassword() ;
	}
	/**
	 * 	setLayout:(这里用一句话描述这个方法的作用)
	 * 	@param  	    
	 * 	@return 	void   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	private void setLayout() {
		mTextView = (TextView) findViewById(R.id.TextView_SetPatternPassword_HelpInfo) ;
		mTextView.setText("请绘制图案密码") ;
		mPatternPasswordView = (PatternPasswordView) findViewById(R.id.PatternPasswordView_SetPatternPassword) ;
		mButton_reset = (Button) findViewById(R.id.Button_SetPatternPassword_Reset) ;
		mButton_reset.setText("取消") ;
		mButton_next = (Button) findViewById(R.id.Button_SetPatternPassword_Next) ;
		mButton_next.setText("继续") ;
		mButton_next.setEnabled(false) ;
	}
	/**
	 * 	addListenerToComponent:(这里用一句话描述这个方法的作用)
	 * 	@param  	    
	 * 	@return 	void   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	private void addListenerToComponent() {
		this.setCancelListener() ;
		mPatternPasswordView.setOnBeginListener(new OnBeginListener() {
			public void onBegin() {
				beginToDrawPattern() ;
			}
		}) ;
		mPatternPasswordView.setOnErrorListener(new OnErrorListener() {
			public void onError() {
				mTextView.setText("绘制错误，请重绘") ;
				onErrorHappened() ;
			}
		}) ;
		mPatternPasswordView.setOnCompleteListener(new OnCompleteListener() {
			public void onComplete(String mPassword) {
				//					if (mPatternPasswordView.verifyPassword(mPassword)) {
				//						ToastMaker.makeToast(getApplicationContext() , "密码输入正确,请输入新密码!") ;
				//						mPatternPasswordView.clearPassword(200) ;
				//						needverify = false ;
				//					} else {
				//						ToastMaker.makeToast(getApplicationContext() , "错误的密码,请重新输入!") ;
				//						mPatternPasswordView.clearPassword() ;
				//						password = "" ;
				//					}
				if (password == null) {
					if (StringUtil.isNotEmpty(mPassword)) {
						tempPasswordCompleted(mPassword) ;
//					} else {
//						mPatternPasswordView.clearPassword() ;
//						return ;
					}
				} else {
					if (password.equals(mPassword)) {
						onSetPasswordCompleted() ;
					} else {
						passwordNotMatch() ;
					}
				}
			}
		}) ;
	}
	private void passwordNotMatch() {
		mTextView.setText("对不起，密码不一致，请重试") ;
		mPatternPasswordView.clearPassword(800) ;
	}
	private void beginToDrawPattern() {
		mTextView.setText("请拖动4个点以上") ;
		mButton_reset.setEnabled(false) ;
	}
	private void onErrorHappened() {
		mButton_reset.setText("取消") ;
		mButton_reset.setEnabled(true) ;
	}
	private void setResetListener(final long cleartime , boolean flag){
		if (flag) {
			mButton_reset.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					mPatternPasswordView.clearPassword(cleartime) ;
					mTextView.setText("请绘制图案密码") ;
					mButton_reset.setText("取消") ;
					setCancelListener() ;
					mButton_next.setEnabled(false) ;
				}
			}) ;
		} else {
			mButton_reset.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					mPatternPasswordView.clearPassword(cleartime) ;
					mTextView.setText("请绘制图案密码") ;
				}
			}) ;
		}
	}
	private void setCancelListener(){
		mButton_reset.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish() ;
			}
		}) ;
	}
	private void tempPasswordCompleted(final String password) {
		mTextView.setText("图案已经记录，请继续") ;
		mButton_reset.setText("重试") ;
		mButton_reset.setEnabled(true) ;
		setResetListener(200 , true) ;
		mButton_next.setEnabled(true) ;
		mButton_next.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				tryAgainToConfirm(password) ;
			}
		}) ;
	}
	/**
	 * 	@param  	    
	 * 	@throws 
	 */
	private void onSetPasswordCompleted() {
		mTextView.setText("您的图案密码如下，请保存") ;
		mButton_reset.setText("取消") ;
		mButton_reset.setEnabled(true) ;
		setCancelListener() ;
		mButton_next.setEnabled(true) ;
		mButton_next.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				restorePassword() ;
				startNextActivity() ;
			}
		}) ;
	}
	private void startNextActivity() {
		Intent intent = new Intent(this , MainActivity.class);
		startActivity(intent) ;
		finish() ;
		
	}
	/**
	 * 	tryAgainToConfirm:(这里用一句话描述这个方法的作用)
	 * 	@param  	    
	 * 	@return 	void   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	private void tryAgainToConfirm(final String temppassword) {
		mTextView.setText("请重新绘制来确认") ;
		mButton_reset.setText("取消") ;
		mButton_reset.setEnabled(true) ;
		setCancelListener() ;
		mButton_next.setText("保存") ;
		mButton_next.setEnabled(false) ;
		password = temppassword ;
		mPatternPasswordView.clearPassword(200) ;
	}
	/**
	 * 	restorePassword:(这里用一句话描述这个方法的作用)
	 * 	@param  	
	 * 	@return 	void   
	 * 	@throws 	
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	private void restorePassword() {
		mPatternPasswordView.resetPassWord(password) ;
		restoreBoolean() ;
	}
}