package io.evercam.connect;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class DeviceListAdapter extends SimpleAdapter
{
	private final String TAG = "evercamdiscover-DeviceListAdapter";
	
	public DeviceListAdapter(Context context, List<? extends Map<String, ?>> data, int resource,
			String[] from, int[] to)
	{
		super(context, data, resource, from, to);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View superView = super.getView(position, convertView, parent);
		
		TextView ipTextView = (TextView) superView.findViewById(R.id.device_name);
		TextView macTextView = (TextView) superView.findViewById(R.id.device_mac);
		TextView vendorTextView = (TextView) superView.findViewById(R.id.device_vendor);
		TextView activeTextView = (TextView) superView.findViewById(R.id.device_active);
		String activeText = activeTextView.getText().toString();
		
		if(activeText.isEmpty())
		{
			ipTextView.setTextColor(Color.GRAY);
			macTextView.setTextColor(Color.GRAY);
			vendorTextView.setTextColor(Color.GRAY);
		}
		else
		{
			ipTextView.setTextColor(Color.BLACK);
			macTextView.setTextColor(Color.BLACK);
			vendorTextView.setTextColor(Color.BLACK);
		}

		return superView;
	}
}