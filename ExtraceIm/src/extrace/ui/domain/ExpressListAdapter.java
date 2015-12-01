package extrace.ui.domain;

import java.text.SimpleDateFormat;
import java.util.List;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import extrace.misc.model.ExpressSheet;
import extrace.net.IDataAdapter;
import extrace.ui.main.R;

public class ExpressListAdapter extends ArrayAdapter<ExpressSheet> implements IDataAdapter<List<ExpressSheet>>{
	
	private List<ExpressSheet> itemList;
	private Context context;
	private String ex_type;
	
	public ExpressListAdapter(List<ExpressSheet> itemList, Context ctx, String ex_type) {
		super(ctx, R.layout.express_list_item, itemList);
		
		this.itemList = itemList;
		this.context = ctx;	
		this.ex_type = ex_type;
	}
	
	public int getCount() {
		if (itemList != null)
			return itemList.size();
		return 0;
	}

	public ExpressSheet getItem(int position) {
		if (itemList != null)
			return itemList.get(position);
		return null;
	}

	public long getItemId(int position) {
		if (itemList != null)
			return itemList.get(position).hashCode();
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;
		hold hd = null;
		if(v==null){
			hd = new hold();
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.express_list_item, null);
			hd.name = (TextView)v.findViewById(R.id.name);
			hd.telCode = (TextView)v.findViewById(R.id.tel);
			hd.address = (TextView)v.findViewById(R.id.addr);
			hd.time = (TextView)v.findViewById(R.id.time);
			hd.status = (TextView)v.findViewById(R.id.st);
			
			v.setTag(hd);
		}else{
			hd = (hold)v.getTag();
		}

		ExpressSheet es = getItem(position);
		switch(ex_type){
		case "ExDLV":	//����
			if(es.getRecever() != null){
				hd.name.setText(es.getRecever().getName());			//����������
				hd.telCode.setText(es.getRecever().getTelCode());	//�����ߵ绰
				hd.address.setText(es.getRecever().getAddress());	//������
			}
			if(es.getAccepterTime() != null){
				//SimpleDateFormat myFmt=new SimpleDateFormat("MM��dd�� HH:mm");
				hd.time.setText(DateFormat.format("MM��dd�� HH:mm",es.getAccepterTime()));
			}
			break;
		case "ExRCV":	//����
			if(es.getSender() != null){
				hd.name.setText(es.getSender().getName());			//����������
				hd.telCode.setText(es.getSender().getTelCode());
				hd.address.setText(es.getSender().getAddress());
			}
			if(es.getAccepterTime() != null){
				//SimpleDateFormat myFmt=new SimpleDateFormat("MM��dd�� hh:mm");
				hd.time.setText(DateFormat.format("MM��dd�� hh:mm",es.getAccepterTime()));
			}
			break;
		case "ExTAN":	//�����Ҫ��
			if(es.getRecever() != null){
				hd.name.setText(es.getRecever().getName());			//����������
				hd.telCode.setText(es.getRecever().getTelCode());	//�����ߵ绰
				hd.address.setText(es.getRecever().getAddress());	//������
			}
			if(es.getAccepterTime() != null){
				SimpleDateFormat myFmt=new SimpleDateFormat("MM��dd�� hh:mm");
				hd.time.setText(myFmt.format(es.getAccepterTime()));
			}
			break;
		}

		String stText = "";
		switch(es.getStatus()){
		case ExpressSheet.STATUS.STATUS_CREATED:
			stText = "����";
			break;
		case ExpressSheet.STATUS.STATUS_RECEIVED:
			stText = "�ռ�";
			break;
		case ExpressSheet.STATUS.STATUS_DELIVERIED:
			stText = "����";
			break;
		}
		hd.status.setText(stText);
		return v;		
	}

	@Override
	public List<ExpressSheet> getData() {
		return itemList;
	}

	@Override
	public void setData(List<ExpressSheet> data) {
		this.itemList = data;
	}	
	
	private class hold{
		TextView name;
		TextView telCode;
		TextView address;
		TextView time;
		TextView status;
	}
}