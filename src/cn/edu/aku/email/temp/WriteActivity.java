package cn.edu.aku.email.temp;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import cn.edu.aku.email.R;
import cn.edu.aku.email.SplashActivity;
import cn.edu.aku.email.main.MainActivity;
import cn.edu.aku.email.service.MailService;
import android.R.integer;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
	private Uri originalUri;
	//保存标记
	private Boolean saveFlag = false;
	private Thread mThread;
	private MimeMessage mimeMessage;
	private static final int SaveDialog = 1;
	
	//requestCode
	private static final int IMAGE_SELECT_CODE = 1;
	private static final int FILE_SELECT_CODE = 2;
	
	private HashMap<String, Object> imageMap = new HashMap<String, Object>();
	
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
		String charset = "utf-8"; 
		try {
			mimeMessage.setFrom(new InternetAddress(fromString));
			//给定序列的地址以逗号解析成InternetAddress对象， strict为false则不严格检测地址是否非法
			mimeMessage.setRecipients(Message.RecipientType.TO, 
					InternetAddress.parse(toString, false));
			mimeMessage.setRecipients(Message.RecipientType.CC, 
					InternetAddress.parse(ccString, false));
			mimeMessage.setSentDate(new Date());
			mimeMessage.setSubject(subjectString);
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
		if (resultCode == RESULT_OK) {
			if (requestCode == IMAGE_SELECT_CODE) {
				Bitmap bitmap = null;
				InputStream in = null;
				String imageName = "";
				originalUri = intent.getData();
				try {
					in = resolver.openInputStream(originalUri);
					Bitmap originalBitmap = BitmapFactory.decodeStream(in);
					bitmap = resizeImage(originalBitmap, 200 , 200);
					String[] proj = {MediaStore.Images.Media.DATA};
					Cursor cursor = managedQuery(originalUri, proj, null, null, null); 
					//获得用户选择的图片的索引值
					int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					//将光标移至开头 ，这个很重要，不小心很容易引起越界
					cursor.moveToFirst();
					//最后根据索引值获取图片路径
					String path = cursor.getString(column_index);
					
					imageName = path.substring(path.lastIndexOf("/") + 1, path.indexOf("."));
//					System.out.println(path + "#####" + imageName);
					imageMap.put(imageName, bitmap);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				if (bitmap != null) {
					insertIntoEditTextAsDrable(imageName);
//					insertIntoEditTextAsSpannableString(getBitmapMime(bitmap, originalUri));
				} else {
					Toast.makeText(WriteActivity.this, "获取图片失败",
							Toast.LENGTH_SHORT).show();
				}
			}
			if (requestCode == FILE_SELECT_CODE) {
				Uri uri = intent.getData();  
	            String[] proj = { MediaStore.Images.Media.DATA };  
	            Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);  
	            int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);  
	            actualimagecursor.moveToFirst();  
	            String path = actualimagecursor.getString(actual_image_column_index);// 获取选择文件的路径  
	            String imageName = path.substring(path.lastIndexOf("/") + 1, path.indexOf("."));
				System.out.println(path + "#####" + imageName);
//				imageMap.put(imageName, bitmap);
			}
		super.onActivityResult(requestCode, resultCode, intent);  
		}
	}
	/**
	 * 重新调整bitmap大小
	 * @param bitmap Bitmap对象
	 * @param newWidth 新的宽度
	 * @param newHeight 新的高度
	 * @return 新的Bitmap对象
	 */
	private Bitmap resizeImage(Bitmap bitmap, int newWidth, int newHeight) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		
/*		DisplayMetrics dm = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(dm);
	    int newWidth = dm.widthPixels;    //得到宽度
	    int newHeight = dm.heightPixels;  //得到高度
		*/
		// 计算缩放比例
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		// 得到新的图片
		Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		return newBitmap;
	}
	/**
	 * 首先建立一个SpannableString,r然后我们要给它附一张图片
	 * @param pic
	 * @param uri
	 * @return
	 */
	private SpannableString getBitmapMime(Bitmap pic, Uri uri) {
		String path = uri.getPath();
		SpannableString ss = new SpannableString(path);
		ImageSpan span = new ImageSpan(this, pic);
		// setSpan()函数里4个参数，分别是要插入的对象，起始位置，终止位置，标记
		ss.setSpan(span, 0, path.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return ss;
	}
	/**
	 * 用到Editable,然后先把ss添加到et里，然后在放到editText里，最后在设置一下光标的位置为最后
	 * @param ss
	 */
	private void insertIntoEditTextAsSpannableString(SpannableString ss) {
		Editable et = textEditText.getText();// 先获取Edittext中的内容
		int start = textEditText.getSelectionStart();
		et.insert(start, ss);// 设置ss要添加的位置
		textEditText.setText(et);// 把et添加到Edittext中
		textEditText.setSelection(start + ss.length());// 设置Edittext中光标在最后面显示
		System.out.println(textEditText.getText().toString());
	}
	
	private void insertIntoEditTextAsDrable(String imageName) {
		Editable editable = textEditText.getText();// 先获取Edittext中的内容
		editable.append(Html.fromHtml("<img src='" + imageName +"'/>", imageGetter, null));
		textEditText.setText(editable);// 把et添加到Edittext中
		System.out.println(textEditText.getText().toString());
	}
	ImageGetter imageGetter = new ImageGetter(){
		
		@Override
		public Drawable getDrawable(String source)
		{
			Bitmap bitmap = (Bitmap) imageMap.get(source);
			BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
			bitmapDrawable.setBounds(0, 0, 800,600);
//					bitmapDrawable.getIntrinsicWidth(),
//					bitmapDrawable.getIntrinsicHeight());

			return bitmapDrawable;
		}
	};


}
