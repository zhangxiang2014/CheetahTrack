package extrace.ui.main.baidumap;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import extrace.ui.main.R;
import extrace.ui.main.baidumap.MyOrientationListener.OnOrientationListener;

public class BaiduActivity extends Activity
{
	private MapView mMapView;
	private BaiduMap mBaiduMap;

	private Context context;

	// 定位相关
	private LocationClient mLocationClient;
	private MyLocationListener mLocationListener;
	private boolean isFirstIn = true;
	private double mLatitude;
	private double mLongtitude;
	// 自定义定位图标
	private BitmapDescriptor mIconLocation;
	private MyOrientationListener myOrientationListener;
	private float mCurrentX;
	private LocationMode mLocationMode;

	// 覆盖物相关
	private BitmapDescriptor mMarker;
	private RelativeLayout mMarkerLy;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_NEEDS_MENU_KEY,
			//	WindowManager.LayoutParams.FLAG_NEEDS_MENU_KEY);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.baiduactivity_main);

		this.context = this;

		initView();
		// 初始化定位
		initLocation();
		initMarker();

		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener()
		{
			@Override
			public boolean onMarkerClick(Marker marker)
			{
				Bundle extraInfo = marker.getExtraInfo();
				Info info = (Info) extraInfo.getSerializable("info");
				ImageView iv = (ImageView) mMarkerLy
						.findViewById(R.id.id_info_img);
				TextView distance = (TextView) mMarkerLy
						.findViewById(R.id.id_info_distance);
				TextView name = (TextView) mMarkerLy
						.findViewById(R.id.id_info_name);
				TextView zan = (TextView) mMarkerLy
						.findViewById(R.id.id_info_zan);
				iv.setImageResource(info.getImgId());
				distance.setText(info.getDistance());
				name.setText(info.getName());
				zan.setText(info.getZan() + "");

				InfoWindow infoWindow;
				TextView tv = new TextView(context);
				tv.setBackgroundResource(R.drawable.location_tips);
				tv.setPadding(30, 20, 30, 50);
				tv.setText(info.getName());
				tv.setTextColor(Color.parseColor("#ffffff"));

				final LatLng latLng = marker.getPosition();
				Point p = mBaiduMap.getProjection().toScreenLocation(latLng);
				p.y -= 47;
				LatLng ll = mBaiduMap.getProjection().fromScreenLocation(p);

				infoWindow = new InfoWindow(tv, ll,
						new OnInfoWindowClickListener()
						{
							@Override
							public void onInfoWindowClick()
							{
								mBaiduMap.hideInfoWindow();
							}
						});
				mBaiduMap.showInfoWindow(infoWindow);
				mMarkerLy.setVisibility(View.VISIBLE);
				return true;
			}
		});
		mBaiduMap.setOnMapClickListener(new OnMapClickListener()
		{

			@Override
			public boolean onMapPoiClick(MapPoi arg0)
			{
				return false;
			}

			@Override
			public void onMapClick(LatLng arg0)
			{
				mMarkerLy.setVisibility(View.GONE);
				mBaiduMap.hideInfoWindow();
			}
		});
	}

	private void initMarker()
	{
		mMarker = BitmapDescriptorFactory.fromResource(R.drawable.maker);
		mMarkerLy = (RelativeLayout) findViewById(R.id.id_maker_ly);
	}

	private void initLocation()
	{

		mLocationMode = LocationMode.NORMAL;
		mLocationClient = new LocationClient(this);
		mLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mLocationListener);

		LocationClientOption option = new LocationClientOption();
		option.setCoorType("bd09ll");
		option.setIsNeedAddress(true);
		option.setOpenGps(true);
		option.setScanSpan(1000);
		mLocationClient.setLocOption(option);
		// 初始化图标
		mIconLocation = BitmapDescriptorFactory
				.fromResource(R.drawable.navi_map_gps_locked);
		myOrientationListener = new MyOrientationListener(context);

		myOrientationListener
				.setOnOrientationListener(new OnOrientationListener()
				{
					@Override
					public void onOrientationChanged(float x)
					{
						mCurrentX = x;
					}
				});

	}

	private void initView()
	{
		mMapView = (MapView) findViewById(R.id.id_bmapView);
		mBaiduMap = mMapView.getMap();
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
		mBaiduMap.setMapStatus(msu);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		// 开启定位
		mBaiduMap.setMyLocationEnabled(true);
		if (!mLocationClient.isStarted())
			mLocationClient.start();
		// 开启方向传感器
		myOrientationListener.start();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onPause();
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		// 停止定位
		mBaiduMap.setMyLocationEnabled(false);
		mLocationClient.stop();
		// 停止方向传感器
		myOrientationListener.stop();

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.baidumain, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.id_map_common:
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
			break;

		case R.id.id_map_site:
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
			break;

		case R.id.id_map_traffic:
			if (mBaiduMap.isTrafficEnabled())
			{
				mBaiduMap.setTrafficEnabled(false);
				item.setTitle("实时交通(off)");
			} else
			{
				mBaiduMap.setTrafficEnabled(true);
				item.setTitle("实时交通(on)");
			}
			break;
		case R.id.id_map_location:
			centerToMyLocation();
			break;
		case R.id.id_map_mode_common:
			mLocationMode = LocationMode.NORMAL;
			break;
		case R.id.id_map_mode_following:
			mLocationMode = LocationMode.FOLLOWING;
			break;
		case R.id.id_map_mode_compass:
			mLocationMode = LocationMode.COMPASS;
			break;
		case R.id.id_add_overlay:
			addOverlays(Info.infos);
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * 添加覆盖物
	 * 
	 * @param infos
	 */
	private void addOverlays(List<Info> infos)
	{
		mBaiduMap.clear();
		LatLng latLng = null;
		Marker marker = null;
		OverlayOptions options;
		for (Info info : infos)
		{
			// 经纬度
			latLng = new LatLng(info.getLatitude(), info.getLongitude());
			// 图标
			options = new MarkerOptions().position(latLng).icon(mMarker)
					.zIndex(5);
			marker = (Marker) mBaiduMap.addOverlay(options);
			Bundle arg0 = new Bundle();
			arg0.putSerializable("info", info);
			marker.setExtraInfo(arg0);
		}

		MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
		mBaiduMap.setMapStatus(msu);

	}

	/**
	 * 定位到我的位置
	 */
	private void centerToMyLocation()
	{
		LatLng latLng = new LatLng(mLatitude, mLongtitude);
		MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
		mBaiduMap.animateMapStatus(msu);
	}

	private class MyLocationListener implements BDLocationListener
	{
		@Override
		public void onReceiveLocation(BDLocation location)
		{

			MyLocationData data = new MyLocationData.Builder()//
					.direction(mCurrentX)//
					.accuracy(location.getRadius())//
					.latitude(location.getLatitude())//
					.longitude(location.getLongitude())//
					.build();
			mBaiduMap.setMyLocationData(data);
			// 设置自定义图标
			MyLocationConfiguration config = new MyLocationConfiguration(
					mLocationMode, true, mIconLocation);
			mBaiduMap.setMyLocationConfigeration(config);

			// 更新经纬度
			mLatitude = location.getLatitude();
			mLongtitude = location.getLongitude();

			if (isFirstIn)
			{
				LatLng latLng = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
				mBaiduMap.animateMapStatus(msu);
				isFirstIn = false;

				Toast.makeText(context, location.getAddrStr(),
						Toast.LENGTH_SHORT).show();
			}

		}
	}

}
