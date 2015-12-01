//自己写的异步接收程序
package extrace.net;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import extrace.net.HttpResponseParam.RETURN_STATUS;

//HTTP应答返回参数

public abstract class HttpAsyncTask extends
		AsyncTask<String, Integer, HttpResponseParam> {
	private static final int REGISTRATION_TIMEOUT = 3 * 1000;//##登记注册时间
	private static final int WAIT_TIMEOUT = 5 * 1000;//##等待时间

	private static final String TAG = "ExTraceHttpUtils";
	private static final String USER_AGENT = "Mozilla/4.5";//##用户代理

	//private Activity context;
	private final ProgressDialog dialog;

	public HttpAsyncTask(Activity context) {
		//this.context = context;
		dialog = new ProgressDialog(context);
	}

	protected String retrieveInputStream(HttpEntity httpEntity) {//##检索输入流
		int length = (int) httpEntity.getContentLength();//##得到实体长度
		if (length < 0)
			length = 10000;//##为什么会小于0
		StringBuffer stringBuffer = new StringBuffer(length);//##一个一万长度的stringBuffer
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(
					httpEntity.getContent(), HTTP.UTF_8);//##创建一个输入流将http实体中的信息放进去
			char buffer[] = new char[length];
			int count;
			while ((count = inputStreamReader.read(buffer, 0, length - 1)) > 0) {
				stringBuffer.append(buffer, 0, count);//##把得到的字符串全部放到stringBuffer中
			}
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getMessage());
		} catch (IllegalStateException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
		return stringBuffer.toString();
	}

	@Override
	protected HttpResponseParam doInBackground(String... params) {//##就是网络传来的东西
		String http_uri;

		HttpResponseParam responseObj = new HttpResponseParam();
		boolean isPost = (params[1] == "POST") ? true : false; //判断是 GET/POST

		try {
			http_uri = params[0];
			HttpClient client = new DefaultHttpClient();//##HttpClient代理
			final HttpParams http_params = client.getParams();
			HttpConnectionParams.setConnectionTimeout(http_params,
					REGISTRATION_TIMEOUT);
			HttpConnectionParams.setSoTimeout(http_params, WAIT_TIMEOUT);
			ConnManagerParams.setTimeout(http_params, WAIT_TIMEOUT);

			publishProgress(30);

			HttpResponse response;//##回应

			if (!isPost) {
				HttpGet getMethod = new HttpGet(http_uri);//##一个HttpGet实例，里面有很多方法，用get方式得到url中的东西
				getMethod.setHeader("User-Agent", USER_AGENT);
				getMethod.setHeader("Accept", "application/json");
				// HttpParams params = new HttpParams();

				// 添加用户密码验证信息
				// client.getCredentialsProvider().setCredentials(
				// new AuthScope(null, -1),
				// new UsernamePasswordCredentials(mUsername, mPassword));
				response = client.execute(getMethod);//##执行getMethod方法返回
			} else {
				String str_json = params[2];//##得到json数据
				HttpPost postMethod = new HttpPost(http_uri);//##HttpPost实例（需要一个url)
				postMethod.setHeader("User-Agent", USER_AGENT);
				postMethod.addHeader("Content-Type", "application/json");
				postMethod.setHeader("Accept", "application/json");
				System.out.println(str_json); // ============================================================
				StringEntity entity = new StringEntity(str_json, "UTF-8");
				entity.setContentType("application/json");
				postMethod.setEntity(entity);

//				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//				if(params.length > 2){
//					for(int i=2; i<params.length; i++){
//						nameValuePairs.add(new BasicNameValuePair(params[i],params[i+1]));
//						System.out.println(nameValuePairs); // ============================================================
//					}
//					postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
//				}
				
				// List<NameValuePair> nameValuePairs = new
				// ArrayList<NameValuePair>(6);
				// nameValuePairs.add(new BasicNameValuePair("uid",params[1]));
				// nameValuePairs.add(new BasicNameValuePair("upw",params[2]));
				// nameValuePairs.add(new BasicNameValuePair("ver6",params[3]));
				// nameValuePairs.add(new BasicNameValuePair("vid6",params[4]));
				// nameValuePairs.add(new BasicNameValuePair("mid6",params[5]));
				// nameValuePairs.add(new
				// BasicNameValuePair("smbtn",params[6]));
				// post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				response = client.execute(postMethod);//##执行postMethod来返回httpResponse方法
			}
			publishProgress(50);
			HttpEntity entity;
			switch (response.getStatusLine().getStatusCode()) {
			case 200:
				entity = response.getEntity();
				Header[] head = response.getHeaders("EntityClass");
				if (head.length > 0) {
					responseObj.responseClassName = head[0].getValue(); // 当有多种返回的对象可能时,用这个名字来区分
				} else {
					responseObj.responseClassName = "";
				}

				responseObj.responseString = EntityUtils.toString(entity,
						"utf8");
				responseObj.statusCode = RETURN_STATUS.Ok;
				break;
			case 201:
				entity = response.getEntity();
				responseObj.responseString = EntityUtils.toString(entity,
						"utf8");
				responseObj.statusCode = RETURN_STATUS.Saved;
				break;
			case 400:
				responseObj.responseString = "服务器未能识别请求。"
						+ response.getStatusLine().toString();
				responseObj.statusCode = RETURN_STATUS.ResponseException;
				break;
			case 404:
				responseObj.responseString = "服务器拒绝满足请求。: "
						+ response.getStatusLine().toString();
				responseObj.statusCode = RETURN_STATUS.ResponseException;
				break;
			case 500:
				entity = response.getEntity();
				responseObj.responseString = EntityUtils.toString(entity,
						"utf8");
				responseObj.statusCode = RETURN_STATUS.ServerException;
				break;
			default:
				responseObj.responseString = "服务器错误: "
						+ response.getStatusLine().toString();
				responseObj.statusCode = RETURN_STATUS.RequestException;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
			responseObj.responseString = "网络连接错误: " + e.getMessage();
			responseObj.statusCode = RETURN_STATUS.NetworkException;
		} catch (Exception e) {
			e.printStackTrace();
			responseObj.responseString = "运行时错误: " + e.getMessage();
			responseObj.statusCode = RETURN_STATUS.Unknown;
		}
		publishProgress(100);
		return responseObj;
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		dialog.dismiss();
	}

	@Override
	protected void onPostExecute(HttpResponseParam result) {
		if (result.statusCode == RETURN_STATUS.Ok) {
			onDataReceive(result.responseClassName, result.responseString);
			dialog.dismiss();
		} else {
			// Toast.makeText(context.getApplicationContext(),
			// "服务请求失败!"+result.responseString, Toast.LENGTH_SHORT).show();
			dialog.setMessage("服务请求失败!" + result.responseString);
			onStatusNotify(result.statusCode, result.responseString);
		}
		// dialog.dismiss();
	}

	@Override
	protected void onPreExecute() {
		dialog.setMessage("正在更新数据...");
		dialog.show();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		// 更新进度
	}

	public abstract void onDataReceive(String class_name, String json_data);

	public abstract void onStatusNotify(RETURN_STATUS status,
			String str_response);
}
