package extrace.ui.main;

import extrace.misc.model.CustomerInfo;
import extrace.misc.model.UserInfo;
import android.app.Application;
import android.content.SharedPreferences;

public class ExTraceApplication extends Application {
	private static final String PREFS_NAME = "ExTrace.cfg";
	String mServerUrl;
	String mMiscService,mDomainService;
    private UserInfo userInfo;
   // private String LoginUser;
    CustomerInfo customerInfo;
    
    public String getServerUrl() {  
        return mServerUrl;  
    }  
    public String getMiscServiceUrl() {  
        return mServerUrl+mMiscService;  
    }  
    public String getDomainServiceUrl() {  
        return mServerUrl+mDomainService;  
    }  
  
    public void setLoginUser(UserInfo userInfo){
    	userInfo=this.userInfo;
    }
    public UserInfo getLoginUser(){
    	return userInfo;
    }
    
    public CustomerInfo getCustomer(){
    	return customerInfo;
    }
    
    public void setServerUrl(String url) {  
        mServerUrl = url;  
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();  
		editor.putString("ServerUrl", mServerUrl);
		editor.commit();
    }  
  
    @Override  
    public void onCreate() {  
        super.onCreate();  

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
       // mServerUrl = settings.getString("ServerUrl", ""); ExTraceWebService_war
        mServerUrl = "http://192.168.1.124:8080/ExTraceWebService_war";
        mMiscService = settings.getString("MiscService", "/REST/Misc/"); 
        mDomainService = settings.getString("DomainService", "/REST/Domain/"); 
		if(mServerUrl == null || mServerUrl.length() == 0)
		{
//			mServerUrl = "http://192.168.7.100:8080/TestCxfHibernate";
			mServerUrl = "http://192.168.1.119:8080";
			mMiscService = "/REST/Misc/";
			mDomainService = "/REST/Domain/";
			SharedPreferences.Editor editor = settings.edit();  
			editor.putString("ServerUrl", mServerUrl);
			editor.putString("MiscService", mMiscService);
			editor.putString("DomainService", mDomainService);
			editor.commit();
		}
		setLoginUser(userInfo);
		//临时造一个用户
//		userInfo = new UserInfo();
//		userInfo.setID(11);
//		userInfo.setReceivePackageID("8001");
////		//userInfo.setTransPackageID("8002");
//		userInfo.setDelivePackageID("8002");
    }  
      
    public void onTerminate() {  
        super.onTerminate();  
          
        //save data of the map  
    }  
}
