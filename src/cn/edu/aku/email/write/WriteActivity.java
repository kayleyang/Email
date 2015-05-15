package cn.edu.aku.email.write;

import java.util.Date;
import java.util.HashMap;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.sun.mail.dsn.message_deliverystatus;

import cn.edu.aku.email.R;
import cn.edu.aku.email.service.MailService;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class WriteActivity extends Activity {
	
	private static final String TAG = "WriteAction";
	
	private TextView fromTextView;
	private EditText toEditText;
	private EditText subjectEditText;
	private EditText ccEditText;
	private EditText textEditText;
	
	private String fromString;
	private String toString;
	private String subjectString;
	private String ccString;
	private String textString;
	
	private ActionBar actionBar;
	//保存标记
	private Boolean saveFlag = false;
	private Thread mThread;
	private MimeMessage mimeMessage;
	private static final int SaveDialog = 1;
	
	//requestCode
	private static final int IMAGE_SELECT_CODE = 1;
	private static final int FILE_SELECT_CODE = 2;
	
	private HashMap<String, Object> attachMap = new HashMap<String, Object>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_write);
		setTitle("写邮件");
		initView();
		initData();
	}

	@SuppressLint("NewApi")
	private void initView() {
		// TODO Auto-generated method stub
		actionBar = getActionBar();
		
		fromTextView = (TextView) findViewById(R.id.write_tv_form);
		toEditText = (EditText) findViewById(R.id.write_edt_to);
		subjectEditText = (EditText) findViewById(R.id.write_edt_subject);
		ccEditText = (EditText) findViewById(R.id.write_edt_cc);
		textEditText = (EditText) findViewById(R.id.write_edt_text);
	}
	
	private void initData() {
		// TODO Auto-generated method stub
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		Intent intent = getIntent();
		fromString = intent.getStringExtra("Tag");
		fromTextView.setText(fromString);
		toEditText.setHint("群发邮件请用逗号隔开");
		ccEditText.setHint("群发邮件请用逗号隔开");
		toEditText.addTextChangedListener(mTextWatcher);
		Session session = MailService.createSession(fromString);
		mimeMessage = new MimeMessage(session);
	}
	
	private void initMimeMessage(){
		toString = toEditText.getText().toString();
		subjectString = subjectEditText.getText().toString();
		ccString = ccEditText.getText().toString();
		textString = textEditText.getText().toString();
		Log.d(TAG, textString);
		String charset = "gb2312"; 
		try {
			mimeMessage.setFrom(new InternetAddress(fromString));
			//给定序列的地址以逗号解析成InternetAddress对象， strict为false则不严格检测地址是否非法
			mimeMessage.setRecipients(Message.RecipientType.TO, 
					InternetAddress.parse(toString, false));
			mimeMessage.setRecipients(Message.RecipientType.CC, 
					InternetAddress.parse(ccString, false));
			mimeMessage.setSentDate(new Date());
			mimeMessage.setSubject(subjectString);
			mimeMessage.setText(textString);
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.write, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home://取消
			if (saveFlag) {
				showDialog(SaveDialog);
			}
			return true;
		case R.id.write_menu_insert:// 插入图片
			insertImg();
			return true;
		case R.id.write_menu_attach://添加附件
			addAttach();
			return true;
		case R.id.write_menu_save://保存
			if (saveFlag) {
				saveEml();
			} else {
				Toast.makeText(getApplication(), 
						"发件人和收件人不能为空", Toast.LENGTH_LONG).show();
			}
			return true;
		case R.id.write_menu_send://发送
			if (saveFlag) {
				sendEml();
				goBack();
			} else {
				Toast.makeText(getApplication(), 
						"发件人和收件人不能为空", Toast.LENGTH_LONG).show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	/**
	 * 返回到MainActivity
	 */
	private void goBack(){
		this.finish();
	}
	
	private void addAttach() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);  
        intent.setType("*/*");  
        intent.addCategory(Intent.CATEGORY_OPENABLE);  
        try {  
            startActivityForResult(Intent.createChooser(intent, "请选择一个要上传的文件"), FILE_SELECT_CODE);  
        } catch (ActivityNotFoundException e) {  
            Toast.makeText(this, "系统没有文件管理器", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }  
	}
	
	private void insertImg(){
		Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);
        getImage.addCategory(Intent.CATEGORY_OPENABLE);
        getImage.setType("image/*");
        startActivityForResult(getImage, IMAGE_SELECT_CODE);
	}
	
	private void saveEml(){
		try {
			initMimeMessage();
			//保存并生成最终的邮件内容
			mimeMessage.saveChanges();
			
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	private void sendEml() {
		initMimeMessage();
		//保存并生成最终的邮件内容
		if (mThread == null) {
			mThread = new Thread(){
				@Override
				public void run() {
					try {
						mimeMessage.saveChanges();
						Transport.send(mimeMessage);
					} catch (MessagingException e) {
						e.printStackTrace();
					}
				}
			};
			mThread.start();// 线程启动
		} else {
			// TODO 线程创建失败，提醒
			saveEml();
			Toast.makeText(getApplication(), "发送失败，请稍候再试！", Toast.LENGTH_LONG).show();
			Log.e(TAG, "创建接收线程失败");
		}
	}
	
	TextWatcher mTextWatcher = new TextWatcher(){
		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
//			Log.d(TAG, "=" + accountEditText.getText().toString() + "-" + passwordEditText.getText().toString() + "=");
			if ((toEditText.getText().toString()).isEmpty()) {
				saveFlag = false;
			} else {
				saveFlag = true;
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
	
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case SaveDialog:
			return buildSaveDialog(WriteActivity.this);
		}
		return null;
	}

	private Dialog buildSaveDialog(Context context) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setTitle("是否保存邮件？");
		
		builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				saveEml();
			}
		});
		builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				goBack();
			}
		});
		return builder.create();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, intent);
		ContentResolver resolver = getContentResolver();
		if (resultCode == RESULT_OK  && requestCode == FILE_SELECT_CODE) {
			Uri uri = intent.getData();  
			String[] proj = { MediaStore.Images.Media.DATA };  
			Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);  
			int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);  
			actualimagecursor.moveToFirst();  
			String path = actualimagecursor.getString(actual_image_column_index);// 获取选择文件的路径  
			String filename = path.substring(path.lastIndexOf("/") + 1, path.length());
			System.out.println(path + "#####" + filename);
			
			MimeBodyPart attach = new MimeBodyPart();
			DataSource ds = new FileDataSource(path);
			DataHandler dh = new DataHandler(ds);
			try {
				attach.setFileName(filename);
				attach.setDataHandler(dh);
				MimeMultipart mailContent = new MimeMultipart("mixed");  
				mailContent.addBodyPart(attach);
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			super.onActivityResult(requestCode, resultCode, intent);  
		}
	}
}
