package extrace.ui.mypackage;

import java.util.ArrayList;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SearchViewCompat.OnCloseListenerCompat;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SearchView;
import extrace.loader.CustomerListLoader;
import extrace.loader.ExpressListLoader;
import extrace.loader.TransPackageListLoader;
import extrace.misc.model.CustomerInfo;
import extrace.misc.model.ExpressSheet;
import extrace.misc.model.TransPackage;
import extrace.ui.domain.ExpressListAdapter;
import extrace.ui.main.*;

public class PackageListActivity extends ActionBarActivity {
	
	PlaceholderFragment list_fg;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentManager fm = getSupportFragmentManager();

        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
        	list_fg = new PlaceholderFragment();
            fm.beginTransaction().add(android.R.id.content, list_fg).commit();
        }
	}
	

	public static class PlaceholderFragment extends ListFragment {
		private PackageListAdapter mAdapter;
		private TransPackageListLoader mLoader;

		private TransPackage selectItem;
		private int selectPosition;

		Intent mIntent;  

		public PlaceholderFragment() {
		}
		
		//参考用的
//		@Override public void onActivityCreated(Bundle savedInstanceState) {
//	        super.onActivityCreated(savedInstanceState);
//
//	        if (getArguments() != null) {	//另一种读出传入参数的方式
//				mExType = getArguments().getString(ARG_EX_TYPE);
//			}
//	        // Give some text to display if there is no data.  In a real
//	        // application this would come from a resource.
//	        setEmptyText("快递列表空的!");
//	        
//	        mAdapter = new ExpressListAdapter(new ArrayList<ExpressSheet>(), this.getActivity(), mExType);
//	        setListAdapter(mAdapter);
//
//	        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE); 
//	        registerForContextMenu(getListView());
//	        
//	        RefreshList();
//		}
	//参考用的	
//		private void RefreshList()
//		{
//			String pkgId = null;
//			switch(mExType){
//			case "ExDLV":
//				pkgId = ((ExTraceApplication)this.getActivity().getApplication()).getLoginUser().getDelivePackageID();
//				break;
//			case "ExRCV":
//				pkgId = ((ExTraceApplication)this.getActivity().getApplication()).getLoginUser().getReceivePackageID();
//				break;
//			case "ExTAN":
//				pkgId = ((ExTraceApplication)this.getActivity().getApplication()).getLoginUser().geTransPackageID();
//				break;
//			}
//			mLoader = new ExpressListLoader(mAdapter, this.getActivity());
//			mLoader.LoadExpressListInPackage(pkgId);
//			//mLoader.LoadExpressList();
//		}
		
		@Override public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // Give some text to display if there is no data.  In a real
            // application this would come from a resource.
            setEmptyText("查找包裹!");

            // We have a menu item to show in action bar.
            setHasOptionsMenu(true);

            // Create an empty adapter we will use to display the loaded data.
            mAdapter = new PackageListAdapter(new ArrayList<TransPackage>(), this.getActivity());
            setListAdapter(mAdapter);

            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE); 

            registerForContextMenu(getListView());	//getListView().setLongClickable(true);

            // Start out with a progress indicator.
            //setListShown(false);
            
    		if (mIntent.hasExtra("Action")) {
    			if(mIntent.getStringExtra("Action").equals("New")){
    				//mItem = new CustomerInfo();
    			}
    			else if(mIntent.getStringExtra("Action").equals("None")){
    				RefreshPackageList();
    			}
    			else if(mIntent.getStringExtra("Action").equals("Edit")){
    				if (mIntent.hasExtra("CustomerInfo")) {
    					selectItem = (TransPackage) mIntent.getSerializableExtra("CustomerInfo");
    				} else {
    					this.getActivity().setResult(RESULT_CANCELED, mIntent);
    					this.getActivity().finish();
    				}
    			}
    			else{
    				this.getActivity().setResult(RESULT_CANCELED, mIntent);
    				this.getActivity().finish();
    			}
    		}
    		else{
    			this.getActivity().setResult(RESULT_CANCELED, mIntent);
    			this.getActivity().finish();
    		}
        }

	    @Override  
	    public void onAttach(Activity activity) {  
	        super.onAttach(activity);  
	        mIntent = activity.getIntent();
	    }

	    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			// Inflate the menu; this adds items to the action bar if it is present.
			inflater.inflate(R.menu.customer_list, menu);
			
			MenuItem item = menu.findItem(R.id.action_search);
			final SearchView searchView = (SearchView) item.getActionView();
			if (searchView != null) {

				SearchViewCompat.setOnQueryTextListener(searchView,
						new OnQueryTextListenerCompat() {
							@Override
							public boolean onQueryTextChange(String newText) {
								// Called when the action bar search text has
								// changed. Since this
								// is a simple array adapter, we can just have
								// it do the filtering.
								return true;
							}
							
							@Override
							public boolean onQueryTextSubmit(String query) {
								if (!TextUtils.isEmpty(query)) {
									SearchViewCompat.setQuery(searchView, null,true);
								}
								return true;
							}
						});
				SearchViewCompat.setOnCloseListener(searchView,
						new OnCloseListenerCompat() {
							@Override
							public boolean onClose() {
								if (!TextUtils.isEmpty(SearchViewCompat.getQuery(searchView))) {
									SearchViewCompat.setQuery(searchView, null,true);
								}
								return true;
							}

						});
				MenuItemCompat.setActionView(item, searchView);
			}
	    }
	    
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			switch(id){
			case android.R.id.home:
			case R.id.action_ok:
				ChangeStatus();	//返回给上层
				return true;
			case R.id.action_edit:

				return true;
			case R.id.action_new:

				return true;
			case R.id.action_search:
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
		
	   
		private void ChangeStatus() {
			
			// TODO Auto-generated method stub
			
		}

		private void RefreshPackageList()
		{
			this.getActivity().setTitle("");
		
				try {
					mLoader = new TransPackageListLoader(mAdapter, this.getActivity());
					mLoader.LoadPackageList("22");//激发数据库的查询
				} catch (Exception e) {
					e.printStackTrace();
				}
		
		}

	}
}
