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
			// 用HttpClient发送请求，分为五步
			// 第一步：创建HttpClient对象
			HttpClient httpClient = new DefaultHttpClient();
			// 注意，下面这一行中，我之前把链接中的"test"误写成了"text"，导致调BUG调了半天没弄出来，真是浪费时间啊    ExTraceWebService_war/
			String url = "http://192.168.1.124:8080/ExTraceWebService_war/REST/Misc/doLogin/" + username
					+ "/" + pwd;
			// 第二步：创建代表请求的对象,参数是访问的服务器地址
			HttpGet httpGet = new HttpGet(url);
			try {
				// 第三步：执行请求，获取服务器发还的相应对象
				HttpResponse response = httpClient.execute(httpGet);
				// 第四步：检查相应的状态是否正常：检查状态码的值是200表示正常
				if (response.getStatusLine().getStatusCode() == 200) {
					// 第五步：从相应对象当中取出数据，放到entity当中
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
						showToastByRunnable(LoginActivity.this, "账号或者密码错误", 3000);
					
						
					}
					Log.d("HTTP", "GET:" + result);
				}
				else{
					showToastByRunnable(LoginActivity.this, "账号或者密码错误", 3000);
				
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		private String Result(String url){
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			try {
				// 第三步：执行请求，获取服务器发还的相应对象
				HttpResponse response = httpClient.execute(httpGet);
				// 第四步：检查相应的状态是否正常：检查状态码的值是200表示正常
				if (response.getStatusLine().getStatusCode() == 200) {
					// 第五步：从相应对象当中取出数据，放到entity当中
					HttpEntity entity = response.getEntity();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(entity.getContent()));
					String result = reader.readLine();
					return result;
				}
				else{
					showToastByRunnable(LoginActivity.this, "我好帅啊", 3000);
				
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