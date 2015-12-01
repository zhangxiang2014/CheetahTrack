package extrace.ui.main;

import java.lang.reflect.Field;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import extrace.ui.domain.ExpressEditActivity;
import extrace.ui.domain.ExpressListFragment;
import extrace.ui.domain.ExpressListFragment.OnFragmentInteractionListener;
import extrace.ui.main.baidumap.BaiduActivity;
import extrace.ui.main.LoginActivity;
import extrace.ui.misc.CustomerListActivity;
import extrace.ui.mypackage.PackageListActivity;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener,OnFragmentInteractionListener{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    private double mLatitude;
	private double mLongtitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        setOverflowShowingAlways();
//        sendMyLocation();
   }
    
    //发送自己位置的经纬度,由于接口没写，先放着。。。。。
    private void sendMyLocation() {
		// TODO Auto-generated method stub
    	LocationManager loctionManager;
    	String contextService=Context.LOCATION_SERVICE;
    	//通过系统服务，取得LocationManager对象
    	loctionManager=(LocationManager) getSystemService(contextService);
    	String provider=LocationManager.GPS_PROVIDER;
    	Location location = loctionManager.getLastKnownLocation(provider);
    	mLatitude = location.getLatitude();
		mLongtitude = location.getLongitude();
//		loctionManager.requestLocationUpdates(provider, 2000, 10, locationListener);
//		//位置监听器
//		private final LocationListener locationListener = new LocationListener() {
//		@Override
//		public void onStatusChanged(String provider, int status, Bundle extras) {
//		}
//
//		@Override
//		public void onProviderEnabled(String provider) {
//		}
//		@Override
//		public void onProviderDisabled(String provider) {
//		}
//		//当位置变化时触发
//		@Override
//		public void onLocationChanged(Location location) {
//		//使用新的location更新TextView显示
//		updateWithNewLocation(location);
//		}
//
//		};
		
	}

	private void setOverflowShowingAlways() {  
	    try {  
	        ViewConfiguration config = ViewConfiguration.get(this);  
	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");  
	        menuKeyField.setAccessible(true);  
	        menuKeyField.setBoolean(config, false);  
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }  
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
        case R.id.action_search:
        	return true;
        case R.id.action_new:
        	StartReceiveExpress();
        	break;
        case R.id.action_my_package:
        	StartMyPackage();
        	break;
        case R.id.action_my_message:
        	return true;
        case R.id.action_my_location:
        	StartBaiduMap();
        	break;
//        case R.id.action_login:
//            return true;
        case R.id.action_logout:
            backToLogin();
            break;
        case R.id.action_settings:
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    void StartMyPackage() {
		// TODO Auto-generated method stub
    	Intent intent = new Intent();
		intent.putExtra("Action","None");
		intent.setClass(this, PackageListActivity.class);
		startActivityForResult(intent, 0);	
	}

	void StartReceiveExpress()
    {
		Intent intent = new Intent();
		intent.putExtra("Action","New");//给一个状态，当跳转至那个activity时便可以根据状态调整页面
		intent.setClass(this, ExpressEditActivity.class);
		startActivityForResult(intent, 0);  	
    }

   
    void StartBaiduMap()
    {
		Intent intent = new Intent(this,BaiduActivity.class);
		startActivity(intent);  	
    }
    
    void StartLogin()
    {
		Intent intent = new Intent(this,LoginActivity.class);
		startActivity(intent);  	
    }
    
    void backToLogin()
    {
    	AlertDialog isback=new AlertDialog.Builder(this).create(); 
    	isback.setTitle("确认");
    	isback.setMessage("确定退出吗？");
    	isback.setButton("是", listener);
    	isback.setButton2("否", listener);
    	isback.show();
    }
    
    DialogInterface.OnClickListener  listener=new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			switch(which)
			{
			case AlertDialog.BUTTON_POSITIVE://确认
				StartLogin();
				break;
			case AlertDialog.BUTTON_NEGATIVE://取消
				break;
			default:
				break;
			
			}
			
		}
	};

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {//##就是Tab的4个功能选项
        	switch(position){
        	case 0:
        		Intent mIntent = getIntent();
        		MainFragment MF=MainFragment.newInstance();
        		if (mIntent.hasExtra("role")) {
        			switch(Integer.parseInt(mIntent.getStringExtra("role").toString().substring(0,1))){
        			case 1:
        				Bundle data = new Bundle();
        				data.putString("role",mIntent.getStringExtra("role").toString() );
        				MF.setArguments(data);
        	        break;
        			case 2:
            			Bundle data2 = new Bundle();
            	        data2.putString("role", mIntent.getStringExtra("role").toString());
            	        MF.setArguments(data2);
            	        break;
        			case 3:
            			Bundle data3 = new Bundle();
            	        data3.putString("role", mIntent.getStringExtra("role").toString());
            	        MF.setArguments(data3);
            	        break;
        				
        			}
        		}
                return MF;
        	case 1:
                return ExpressListFragment.newInstance("ExDLV");	//派送快件,点击时刻就转入那个Fragment
        	case 2:
                return ExpressListFragment.newInstance("ExRCV");	//揽收快件
        	case 3:
                return ExpressListFragment.newInstance("ExTAN");	//转运快件
        	}
        	return null;
        }

        @Override
        public int getCount() {
            // 总共4页.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
                case 3:
                    return getString(R.string.title_section4).toUpperCase(l);
            }
            return null;
        }
    }

	@Override
	public void onFragmentInteraction(String id) {
		// TODO Auto-generated method stub
		
	}

}
