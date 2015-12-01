package extrace.ui.main;

import zxing.util.CaptureActivity;
import extrace.ui.domain.ExpressEditActivity;
import extrace.ui.misc.CustomerListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainFragment  extends Fragment {
	
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MainFragment newInstance() {
    	MainFragment fragment = new MainFragment();
//        Bundle args = new Bundle();
//        //args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//        fragment.setArguments(args);
        return fragment;
    }

//    public MainFragment() {
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        //fragment_main中的操作激发

        rootView.findViewById(R.id.action_ex_receive_icon).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						StartReceiveExpress();
					}
				});
        rootView.findViewById(R.id.action_ex_receive).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						StartReceiveExpress();
					}
				});
        rootView.findViewById(R.id.action_ex_transfer_icon).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						StartQueryExpress();
					}
				});
        rootView.findViewById(R.id.action_ex_transfer).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						StartQueryExpress();
					}
				});
//        android:id="@+id/action_pk_pkg"
//                android:layout_width="35dp"
//                android:layout_height="35dp"
//                android:scaleType="fitCenter"
//                android:src="@drawable/find_more_friend_shake"
//        

        //包裹拆包和打包在这里,以下4个一旦点击以后就会开启另一个Activity,老师这里写的很好，因为他把打开另一个activity写成了一个方法。 
        rootView.findViewById(R.id.action_pk_pkg).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						StartCapturePkg();
					}
				});
//         rootView.findViewById(R.id.action_pk_pkg_icon).setOnclickListener(
//        		 new View.OnClickListener(){
//        			 public void onClick(View view){
//        				 StartCapturePkg();
//        			 }
//        		});
         rootView.findViewById(R.id.action_pk_exp).setOnClickListener(
 				new View.OnClickListener() {
 					@Override
 					public void onClick(View view) {
 						StartCaptureExp();
 					}
 				});
         rootView.findViewById(R.id.action_pk_exp_icon).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						StartCaptureExp();
					}
				});



        rootView.findViewById(R.id.action_cu_mng_icon).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						StartCustomerList();
					}
				});
        rootView.findViewById(R.id.action_cu_mng).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						StartCustomerList();
					}
				});

        rootView.findViewById(R.id.action_ex_qur_icon).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						StartQueryExpress();
					}
				});
        rootView.findViewById(R.id.action_ex_qur).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						StartQueryExpress();
					}
				});

        return rootView;
    }
    
    void StartCapturePkg() {
		// TODO Auto-generated method stub
    	
    	Bundle data = getArguments();
    	Log.i("test",data.getString("role"));
    	Intent intent=new Intent();
    	intent.putExtra("Action","Pkg");
    	intent.putExtra("role",data.getString("role"));
    	intent.setClass(this.getActivity(),CaptureActivity.class);
    	startActivityForResult(intent, 100);  
		
	}
    void StartCaptureExp() {
		// TODO Auto-generated method stub
    	
    	Bundle data = getArguments();
    	Intent intent=new Intent();
    	intent.putExtra("Action","Exp");
    	intent.putExtra("role",data.getString("role"));
    	intent.setClass(this.getActivity(),CaptureActivity.class);
    	startActivityForResult(intent, 100);  
		
	}

	void StartReceiveExpress()
    {
		Intent intent = new Intent();
		intent.putExtra("Action","New");//给一个状态，当跳转至那个activity时便可以根据状态调整页面
		intent.setClass(this.getActivity(), ExpressEditActivity.class);
		startActivityForResult(intent, 0);  	
    }

    void StartQueryExpress()
    {
		Intent intent = new Intent();
		intent.putExtra("Action","Query");
		intent.setClass(this.getActivity(), ExpressEditActivity.class);
		startActivityForResult(intent, 0);  	
    }

    void StartCustomerList()
    {
		Intent intent = new Intent();
		intent.putExtra("Action","None");
		intent.setClass(this.getActivity(), CustomerListActivity.class);
		startActivityForResult(intent, 0);
    }

}
