package extrace.ui.main;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import zxing.util.CaptureActivity;
import extrace.misc.model.UserInfo;
import extrace.ui.main.MainActivity;
import extrace.ui.main.baidumap.BaiduActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import extrace.ui.main.ExTraceApplication;

public class LoginActivity extends Activity {

	private Button login_button;
	private EditText nameText;
	private EditText pwdText;
	private ExTraceApplication app;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		login_button = (Button) findViewById(R.id.signin_button);
		nameText = (EditText) findViewById(R.id.username_edit);
		pwdText = (EditText) findViewById(R.id.password_edit);

		login_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				String name = nameText.getText().toString();
				String pwd = pwdText.getText().toString();
				GetThread getThread = new GetThread(name, pwd);
				getThread.start();

			}
		});

	}

	class GetThread extends Thread {

		String username;
		String pwd;

		public GetThread(String name, String pwd) {
			this.username = name;
			this.pwd = pwd;
		}

		@Override
		public void run() {
			// ��HttpClient�������󣬷�Ϊ�岽
			// ��һ��������HttpClient����
			HttpClient httpClient = new DefaultHttpClient();
			// ע�⣬������һ���У���֮ǰ�������е�"test"��д����"text"�����µ�BUG���˰���ûŪ�����������˷�ʱ�䰡    ExTraceWebService_war/
			String url = "http://192.168.1.124:8080/ExTraceWebService_war/REST/Misc/doLogin/" + username
					+ "/" + pwd;
			// �ڶ�����������������Ķ���,�����Ƿ��ʵķ�������ַ
			HttpGet httpGet = new HttpGet(url);
			try {
				// ��������ִ�����󣬻�ȡ��������������Ӧ����
				HttpResponse response = httpClient.execute(httpGet);
				// ���Ĳ��������Ӧ��״̬�Ƿ����������״̬���ֵ��200��ʾ����
				if (response.getStatusLine().getStatusCode() == 200) {
					// ���岽������Ӧ������ȡ�����ݣ��ŵ�entity����
					HttpEntity entity = response.getEntity();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(entity.getContent()));
					String result = reader.readLine();
					if(result.equals("1")||result.equals("2")||result.equals("3"))
					{	
						Log.i("zhagn","asjdfjsd");
						app = (ExTraceApplication) getApplication();
						switch(Integer.parseInt(result)){
						case 1: String DelivePkgid=Result("http://192.168.1.124:8080/ExTraceWebService_war/REST/Misc/getReceivePackageID/uid/"+username);
								Log.i("tangfei", DelivePkgid);
								UserInfo post=new UserInfo();
								//post.setID(Integer.parseInt(username));
								post.setID(11);
								post.setDelivePackageID("8002");
								
								//app.getLoginUser().setDelivePackageID(DelivePkgid);
								String ReceivePkgid=Result("http://192.168.1.124:8080/ExTraceWebService_war/REST/Misc/getDelivePackageID/uid/"+username);
								post.setReceivePackageID("8001");
								post.setTransPackageID("8002");
								app.setLoginUser(post);
								String Id=app.getLoginUser().getDelivePackageID();
								Log.i("idddddd",Id);
								break;
						case 3:	app.getLoginUser().setReceivePackageID("");
								break;
						case 2:break;
						}
						Intent LoginIntent=new Intent();
						LoginIntent.putExtra("role",username);
						LoginIntent.setClass(LoginActivity.this,MainActivity.class);
						startActivity(LoginIntent);
					}else{
						showToastByRunnable(LoginActivity.this, "�˺Ż����������", 3000);
					
						
					}
					Log.d("HTTP", "GET:" + result);
				}
				else{
					showToastByRunnable(LoginActivity.this, "�˺Ż����������", 3000);
				
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		private String Result(String url){
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			try {
				// ��������ִ�����󣬻�ȡ��������������Ӧ����
				HttpResponse response = httpClient.execute(httpGet);
				// ���Ĳ��������Ӧ��״̬�Ƿ����������״̬���ֵ��200��ʾ����
				if (response.getStatusLine().getStatusCode() == 200) {
					// ���岽������Ӧ������ȡ�����ݣ��ŵ�entity����
					HttpEntity entity = response.getEntity();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(entity.getContent()));
					String result = reader.readLine();
					return result;
				}
				else{
					showToastByRunnable(LoginActivity.this, "�Һ�˧��", 3000);
				
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}
		
		
		
		private void showToastByRunnable(final Context context, final CharSequence text, final int duration) {
		    Handler handler = new Handler(Looper.getMainLooper());
		    handler.post(new Runnable() {
		        @Override
		        public void run() {
		            Toast.makeText(context, text, duration).show();
		        }
		    });
		}
	}
}