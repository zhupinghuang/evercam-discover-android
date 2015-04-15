package io.evercam.connect;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import io.evercam.connect.net.NetInfo;

public class DeviceListAdapter extends SimpleAdapter
{
    private final String TAG = "evercamdiscover-DeviceListAdapter";
    private Context context;
    private String routerIp;

    public DeviceListAdapter(Context context, List<? extends Map<String, ?>> data, int resource,
                             String[] from, int[] to)
    {
        super(context, data, resource, from, to);
        this.context = context;
        routerIp = new NetInfo(context).getGatewayIp();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View superView = super.getView(position, convertView, parent);

        TextView ipTextView = (TextView) superView.findViewById(R.id.device_name);
        TextView macTextView = (TextView) superView.findViewById(R.id.device_mac);
        TextView vendorTextView = (TextView) superView.findViewById(R.id.device_vendor);
        TextView activeTextView = (TextView) superView.findViewById(R.id.device_active);
        ImageView imageView = (ImageView) superView.findViewById(R.id.device_img);
        String activeText = activeTextView.getText().toString();
        String cameraIp = ipTextView.getText().toString();

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

        if(DiscoverMainActivity.thumbnailMap.containsKey(cameraIp))
        {
            imageView.setBackgroundResource(android.R.color.transparent);
            imageView.setImageBitmap(DiscoverMainActivity.thumbnailMap.get(cameraIp));
        }
        else if(cameraIp.equals(routerIp))
        {
            imageView.setBackgroundResource(R.drawable.tplink_trans);
        }
        else
        {
            imageView.setBackgroundResource(R.drawable.question_img_trans);
        }
        return superView;
    }
}