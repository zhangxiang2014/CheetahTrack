package extrace.ui.misc;

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
import extrace.misc.model.CustomerInfo;
import extrace.misc.model.ExpressSheet;
import extrace.ui.domain.ExpressListAdapter;
import extrace.ui.main.*;

public class CustomerListActivity extends ActionBarActivity {
	
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		list_fg.onActivityResult(requestCode,resultCode,data);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends ListFragment {
		private CustomerListAdapter mAdapter;
		private CustomerListLoader mLoader;

		private CustomerInfo selectItem;
		private int selectPosition;

		Intent mIntent;  

		public PlaceholderFragment() {
		}
		
		//�ο��õ�
//		@Override public void onActivityCreated(Bundle savedInstanceState) {
//	        super.onActivityCreated(savedInstanceState);
//
//	        if (getArguments() != null) {	//��һ�ֶ�����������ķ�ʽ
//				mExType = getArguments().getString(ARG_EX_TYPE);
//			}
//	        // Give some text to display if there is no data.  In a real
//	        // application this would come from a resource.
//	        setEmptyText("����б�յ�!");
//	        
//	        mAdapter = new ExpressListAdapter(new ArrayList<ExpressSheet>(), this.getActivity(), mExType);
//	        setListAdapter(mAdapter);
//
//	        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE); 
//	        registerForContextMenu(getListView());
//	        
//	        RefreshList();
//		}
	//�ο��õ�	
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
            setEmptyText("���ҿͻ�!");

            // We have a menu item to show in action bar.
            setHasOptionsMenu(true);

            // Create an empty adapter we will use to display the loaded data.
            mAdapter = new CustomerListAdapter(new ArrayList<CustomerInfo>(), this.getActivity());
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
    				RefreshCustomerList();
    			}
    			else if(mIntent.getStringExtra("Action").equals("Edit")){
    				if (mIntent.hasExtra("CustomerInfo")) {
    					selectItem = (CustomerInfo) mIntent.getSerializableExtra("CustomerInfo");
    					RefreshList(selectItem.getName());
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
									RefreshList(query);
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
				SelectOk();	//���ظ��ϲ�
				return true;
			case R.id.action_edit:
				EditItem();
				return true;
			case R.id.action_new:
				NewItem();
				return true;
			case R.id.action_search:
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
		
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		    super.onCreateContextMenu(menu, v, menuInfo);
		    AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

		    //onListItemClick(null,null,info.position,0);

		    selectItem = mAdapter.getItem(info.position);
		    selectPosition = info.position;
        	this.getActivity().setTitle(selectItem.getName());
		    menu.setHeaderTitle("�ͻ�: "+selectItem.getName());
		    menu.add(info.position, 1, 0, "ѡ��");
		    menu.add(info.position, 2, 1, "�޸�");
		    menu.add(info.position, 3, 2, "ɾ��");
		}

		@Override
		public boolean onContextItemSelected(MenuItem item) {
		    //AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		    if (item.getTitle().equals("ѡ��")) {
				SelectOk();	//���ظ��ϲ�
		   } else if (item.getTitle().equals("�޸�")) {
				EditItem();	//�༭�ͻ�
		   } else if (item.getTitle().equals("ɾ��")) {
				DeleteItem();	//ɾ���ͻ�
		   }
		   return super.onContextItemSelected(item);
		}	   
		
        @Override public void onListItemClick(ListView l, View v, int position, long id) {
        	selectItem = mAdapter.getItem(position);
        	selectPosition = position; 
        	this.getActivity().setTitle(selectItem.getName());
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {	//�༭����Ļص�
    		switch (resultCode) {
    		case RESULT_OK:
    			CustomerInfo customer;
    			if (data.hasExtra("CustomerInfo")) {
    				customer = (CustomerInfo) data.getSerializableExtra("CustomerInfo");
	    			if(requestCode == CustomerEditActivity.INTENT_NEW){
	    				mAdapter.getData().add(customer);
	    			}
	    			else if(requestCode == CustomerEditActivity.INTENT_EDIT){
	    				mAdapter.setItem(customer, selectPosition);
	    			}
	    			mAdapter.notifyDataSetChanged();
    			}
//    			if (data.hasExtra("CustomerInfo")) {
//    				customer = (CustomerInfo) data.getSerializableExtra("CustomerInfo");
//   					RefreshList(customer.getName());
//    			}
    			break;
    		default:
    			break;
    		}
    	}
        
		private void SelectOk()
		{
	        mIntent.putExtra("CustomerInfo",selectItem);				
			this.getActivity().setResult(RESULT_OK, mIntent);
			this.getActivity().finish();
		}

		private void EditItem()
		{
	        Intent intent = new Intent();
			intent.setClass(this.getActivity(),CustomerEditActivity.class);
			intent.putExtra("Action","Edit"); 
			intent.putExtra("CustomerInfo",selectItem); 
			this.getActivity().startActivityForResult(intent, CustomerEditActivity.INTENT_EDIT);	//�����²�༭
		}

		private void NewItem()
		{
	        Intent intent = new Intent();
			intent.setClass(this.getActivity(),CustomerEditActivity.class);
			intent.putExtra("Action","New"); 
			this.getActivity().startActivityForResult(intent, CustomerEditActivity.INTENT_NEW);	//�����²�༭-�����µ�
		}

		private void DeleteItem()
		{
			try {
				mLoader = new CustomerListLoader(mAdapter, this.getActivity());
				mLoader.DeleteCustomer(selectItem.getID());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void RefreshList(String name)
		{
			this.getActivity().setTitle("");
			if(checkMobile(name)){	//���ϵ绰����Ĺ���,���绰�����ѯ
				try {
					mLoader = new CustomerListLoader(mAdapter, this.getActivity());
					mLoader.LoadCustomerListByTelCode(name);//�������ݿ�Ĳ�ѯ
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else
			{
				try {
					mLoader = new CustomerListLoader(mAdapter, this.getActivity());
					mLoader.LoadCustomerListByName(name);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		private void RefreshCustomerList()
		{
			this.getActivity().setTitle("");
		
				try {
					mLoader = new CustomerListLoader(mAdapter, this.getActivity());
					mLoader.LoadCustomerList();//�������ݿ�Ĳ�ѯ
				} catch (Exception e) {
					e.printStackTrace();
				}
		
		}
		
		public boolean checkMobile(String str){ 
			String regex_mb = "(\\+\\d+)?1[34578]\\d{9}$";  //�ƶ��绰��������ʽ
	        String regex_ph = "(\\+\\d+)?(\\d{3,4}\\-?)?\\d{7,8}$";  //�̶��绰��������ʽ
		    Pattern pattern_mb = Pattern.compile(regex_mb); 
		    Pattern pattern_ph = Pattern.compile(regex_ph); 
		    return pattern_mb.matcher(str).matches() || pattern_ph.matcher(str).matches();    
		} 
	}
}
