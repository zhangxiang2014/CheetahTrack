/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zxing.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import zxing.util.camera.CameraManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;

import extrace.misc.model.ExpressSheet;
import extrace.ui.main.LoginActivity;
import extrace.ui.main.R;
import extrace.ui.main.MainActivity;
import extrace.ui.main.baidumap.BaiduActivity;
 

/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a viewfinder to help the user place the barcode correctly,
 * shows feedback as the image processing is happening, and then overlays the
 * results when a scan is successful.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends Activity implements
		SurfaceHolder.Callback {

	private static final String TAG = CaptureActivity.class.getSimpleName();

//	private static final long DEFAULT_INTENT_RESULT_DURATION_MS = 1500L;
//	private static final long BULK_MODE_SCAN_DELAY_MS = 1000L;

	private CameraManager cameraManager;
	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private TextView statusView;
	private View resultView;
	private String lastResult;
	private boolean hasSurface;
	private Collection<BarcodeFormat> decodeFormats;
	private Map<DecodeHintType, ?> decodeHints;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private BeepManager beepManager;
	private AmbientLightManager ambientLightManager;
	private Button backButton;

	ViewfinderView getViewfinderView() {
		return viewfinderView;
	} 

	public Handler getHandler() {
		return handler;
	}

	CameraManager getCameraManager() {
		return cameraManager;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.capture);

		backButton=(Button)findViewById(R.id.cancel_capture);
		backButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CaptureActivity.this.finish();
				Intent backIntent = new Intent(CaptureActivity.this,
						MainActivity.class);
				startActivity(backIntent);
			}
			
		});

		hasSurface = false;
		// historyManager = new HistoryManager(this);
		// historyManager.trimHistory();
		inactivityTimer = new InactivityTimer(this);
		beepManager = new BeepManager(this);
		ambientLightManager = new AmbientLightManager(this);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// CameraManager must be initialized here, not in onCreate(). This is
		// necessary because we don't
		// want to open the camera driver and measure the screen size if we're
		// going to show the help on
		// first launch. That led to bugs where the scanning rectangle was the
		// wrong size and partially
		// off screen.
		cameraManager = new CameraManager(getApplication());

		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);

		resultView = findViewById(R.id.result_view);
		statusView = (TextView) findViewById(R.id.status_view);

		handler = null;
//		lastResult = null;

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		if (prefs.getBoolean(PreferencesActivity.KEY_DISABLE_AUTO_ORIENTATION,
				true)) {
			setRequestedOrientation(getCurrentOrientation());
		} else {
			// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		}

		resetStatusView();

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(surfaceHolder);
		} else {
			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			surfaceHolder.addCallback(this);
		}

		beepManager.updatePrefs();
		ambientLightManager.start(cameraManager);

		inactivityTimer.onResume();

		Intent intent = getIntent();

		decodeFormats = null;
		characterSet = null;

		if (intent != null) {

			//String action = intent.getAction();
			//String dataString = intent.getDataString();
			//characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);

		}
	}

	private int getCurrentOrientation() {
		int rotation = getWindowManager().getDefaultDisplay().getRotation();
		switch (rotation) {
		case Surface.ROTATION_0:
			return ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
		case Surface.ROTATION_90:
			return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		default:
			return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
		}
	}

	@Override
	protected void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		ambientLightManager.stop();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			Intent intent = new Intent();
			if (lastResult != null) {
				intent.putExtra("BarCode",lastResult);
				setResult(RESULT_OK,intent);
				finish();
				return true;
			}
			else
			{
				restartPreviewAfterDelay(0L);
				return true;
			}
//			if ((source == IntentSource.NONE || source == IntentSource.ZXING_LINK)
//					&& lastResult != null) {
//				restartPreviewAfterDelay(0L);
//				return true;
//			}
//			break;
		case KeyEvent.KEYCODE_FOCUS:
		case KeyEvent.KEYCODE_CAMERA:
			// Handle these events so they don't launch the Camera app
			return true;
			// Use volume up/down to turn on light
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			cameraManager.setTorch(false);
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			cameraManager.setTorch(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			Log.e(TAG,
					"*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	/**
	 * A valid barcode has been found, so give an indication of success and show
	 * the results.
	 * 
	 * @param rawResult
	 *            The contents of the barcode.
	 * @param scaleFactor
	 *            amount by which thumbnail was scaled
	 * @param barcode
	 *            A greyscale bitmap of the camera data which was decoded.
	 */
	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
		inactivityTimer.onActivity();
		//lastResult = rawResult;
		String url = "http://192.168.1.105:8080/ExTraceWebService_war/REST/Domain";
		beepManager.playBeepSoundAndVibrate();
		String resultString = rawResult.getText();
		Intent mIntent = getIntent();
		if(mIntent.getStringExtra("Action").equals("Pkg")){
			switch(Integer.parseInt(mIntent.getStringExtra("role").toString().substring(0,1))){
			//�Բ�ͬ��Ա��ɨ��(��������зֱ�Ĵ���
			//ֱ��ɨ�裬������½�������ת��Ϊ������������������������ת������ѡ���Ƿ�������������ڶԻ�����ѡ��ȷ����������������
			case 1:
				Log.i("teasdfjsadf",mIntent.getStringExtra("role").toString());
				if(resultString.startsWith("1")){

				url=url+"/receiveExpressSheetId/id/"+resultString+"/uid/"+mIntent.getStringExtra("role").toString();
				startOnePkgExp(url);//���½�״̬�Ŀ���������հ���
//				Intent intentTest=new Intent(this,BaiduActivity.class);
//				startActivity(intentTest);
				break;
				}else if(resultString.startsWith("8")){
				url=url+"/dispatchExpressSheetId/id/"+resultString+"/uid/"+mIntent.getStringExtra("role").toString();
				changePkgToStatus4(url);//������״̬�İ������ߴ��״̬�İ����������״̬
				break;
				}else{
					break;
				}
			//ֱ��ɨ�裨ɨ���ǰ��������жϰ����Ƿ�Ϊ���״̬������Ǵ��״̬���ı������״̬Ϊת��״̬
			case 2:
				if(resultString.startsWith("8")){

					url=url+"/receiveExpressSheetId/id/"+resultString+"/uid/"+mIntent.getStringExtra("role").toString();
					startOnePkgExp(url);//���½�״̬�Ŀ���������հ���
//					Intent intentTest=new Intent(this,BaiduActivity.class);
//					startActivity(intentTest);
					break;
				}
				
				break;
			//����Ա����Ҫ�½����������ڴ��״̬��Ȼ��һ��һ��ɨ����ѡ�����½��İ������롣
			case 3:
				if(resultString.startsWith("1")){
					url=url+"/packTransPackage/PackageID/"+"8001"+"/ExpressID/"+resultString;
//					Intent intentTest=new Intent(this,BaiduActivity.class);
//					startActivity(intentTest);
					break;
					}else if(resultString.startsWith("8")){
					url=url+"/unpackTransPackage/"+resultString+"/uid/"+mIntent.getStringExtra("role").toString();
					break;
					}else{
						break;
					}
				
			}
		}else if(mIntent.getStringExtra("Action").equals("exp")){
			//�Բ�ͬ��Ա��ɨ�裨��������зֱ���
			switch(Integer.parseInt(mIntent.getStringExtra("role"))){
			case 1:
				//finsh();
				break;
			case 2:
				if(resultString.startsWith("8")){
					url=url+"/unpackTransPackage/"+"8001";
//					Intent intentTest=new Intent(this,BaiduActivity.class);
//					startActivity(intentTest);
					break;
					}else{
						
					}
				//finsh();
				break;
			//�������յİ�����ת�˵İ������ܲ��
			case 3:
				
			
			}
		}
		

		
		drawResultPoints(barcode, scaleFactor, rawResult);
		statusView.setVisibility(View.GONE);
		viewfinderView.setVisibility(View.GONE);
		resultView.setVisibility(View.VISIBLE);
		
		
		
		

		ImageView barcodeImageView = (ImageView) findViewById(R.id.barcode_image_view);
		if (barcode == null) {
			barcodeImageView.setImageBitmap(BitmapFactory.decodeResource(
					getResources(), R.drawable.launcher_icon));
		} else {
			barcodeImageView.setImageBitmap(barcode);
		}
		
		ParsedResult result = ResultParser.parseResult(rawResult);
		String contents = result.getDisplayResult().replace("\r", "");
		CharSequence displayContents = contents;
		lastResult = contents;
		TextView contentsTextView = (TextView) findViewById(R.id.contents_text_view);
		contentsTextView.setText(displayContents);
		int scaledSize = Math.max(22, 32 - displayContents.length() / 4);
		contentsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize);
	}
	
	private void changePkgToStatus4(String url) {
		Log.i("teasdfjsadf",url);
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
				if(!(result==null))
				{
					Log.i("startOnepkg","���Գɹ�");
					CaptureActivity.this.finish();
					showToastByRunnable(CaptureActivity.this, "�����������״̬��", 3000);
					
				}else{
					showToastByRunnable(CaptureActivity.this, "����������", 3000);
				}
				Log.d("HTTP", "GET:" + result);
			}
			else{
				showToastByRunnable(CaptureActivity.this, "�������Ӵ���", 3000);
		
			}
		} catch (Exception e) {
			e.printStackTrace();
		}// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		
	}

	private void startOnePkgExp(String url) {
		Log.i("teasdfjsadf",url);
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
				if(!(result==null))
				{
					Log.i("startOnepkg","���Գɹ�");
					CaptureActivity.this.finish();
					showToastByRunnable(CaptureActivity.this, "������ճɹ���", 3000);
					
				}else{
					showToastByRunnable(CaptureActivity.this, "����������", 3000);
				}
				Log.d("HTTP", "GET:" + result);
			}
			else{
				showToastByRunnable(CaptureActivity.this, "�������Ӵ���", 3000);
		
			}
		} catch (Exception e) {
			e.printStackTrace();
		}// TODO Auto-generated method stub
		
	}

	//ɾ�������ĺ���
	void deletePackage(String resultString) {
		// TODO Auto-generated method stub
			// ��HttpClient�������󣬷�Ϊ�岽
			// ��һ��������HttpClient����
			HttpClient httpClient = new DefaultHttpClient();
			// ע�⣬������һ���У���֮ǰ�������е�"test"��д����"text"�����µ�BUG���˰���ûŪ�����������˷�ʱ�䰡
//			String url = "http://192.168.1.119:8080/ExTraceWebService_war/REST/Misc/getTransPackage/" + resultString;
			String url = "http://192.168.1.119:8080/ExTraceWebService_war/REST/Domain/unpackTransPackage/" + resultString;
		
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
					if(result.equals("true"))
					{
//						Intent loginIntent = new Intent(CaptureActivity.this,
//								LoginActivity.class);
//						startActivity(loginIntent);
						
						CaptureActivity.this.finish();
						showToastByRunnable(CaptureActivity.this, "����ɾ���ɹ�", 3000);
						
					}else{
						showToastByRunnable(CaptureActivity.this, "����������", 3000);
					}
					Log.d("HTTP", "GET:" + result);
				}
				else{
					showToastByRunnable(CaptureActivity.this, "�������Ӵ���", 3000);
			
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

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
		
	

	/**
	 * Superimpose a line for 1D or dots for 2D to highlight the key features of
	 * the barcode.
	 * 
	 * @param barcode
	 *            A bitmap of the captured image.
	 * @param scaleFactor
	 *            amount by which thumbnail was scaled
	 * @param rawResult
	 *            The decoded results which contains the points to draw.
	 */
	private void drawResultPoints(Bitmap barcode, float scaleFactor,
			Result rawResult) {
		ResultPoint[] points = rawResult.getResultPoints();
		if (points != null && points.length > 0) {
			Canvas canvas = new Canvas(barcode);
			Paint paint = new Paint();
			paint.setColor(getResources().getColor(R.color.result_points));
			if (points.length == 2) {
				paint.setStrokeWidth(4.0f);
				drawLine(canvas, paint, points[0], points[1], scaleFactor);
			} else if (points.length == 4
					&& (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A || rawResult
							.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
				// Hacky special case -- draw two lines, for the barcode and
				// metadata
				drawLine(canvas, paint, points[0], points[1], scaleFactor);
				drawLine(canvas, paint, points[2], points[3], scaleFactor);
			} else {
				paint.setStrokeWidth(10.0f);
				for (ResultPoint point : points) {
					if (point != null) {
						canvas.drawPoint(scaleFactor * point.getX(),
								scaleFactor * point.getY(), paint);
					}
				}
			}
		}
	}

	private static void drawLine(Canvas canvas, Paint paint, ResultPoint a,
			ResultPoint b, float scaleFactor) {
		if (a != null && b != null) {
			canvas.drawLine(scaleFactor * a.getX(), scaleFactor * a.getY(),
					scaleFactor * b.getX(), scaleFactor * b.getY(), paint);
		}
	}

	private void sendReplyMessage(int id, Object arg, long delayMS) {
		if (handler != null) {
			Message message = Message.obtain(handler, id, arg);
			if (delayMS > 0L) {
				handler.sendMessageDelayed(message, delayMS);
			} else {
				handler.sendMessage(message);
			}
		}
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) {
			Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);
			// Creating the handler starts the preview, which can also throw a
			// RuntimeException.
			if (handler == null) {
				handler = new CaptureActivityHandler(this, decodeFormats,
						decodeHints, characterSet, cameraManager);
			}
//			decodeOrStoreSavedBitmap(null, null);
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
			displayFrameworkBugMessageAndExit();
		}
	}

	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.app_name));
		builder.setMessage(getString(R.string.msg_camera_framework_bug));
		builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}

	public void restartPreviewAfterDelay(long delayMS) {
		if (handler != null) {
			handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
		}
		resetStatusView();
	}

	private void resetStatusView() {
		resultView.setVisibility(View.GONE);
		statusView.setText(R.string.msg_default_status);
		statusView.setVisibility(View.VISIBLE);
		viewfinderView.setVisibility(View.VISIBLE);
		lastResult = null;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}
}
