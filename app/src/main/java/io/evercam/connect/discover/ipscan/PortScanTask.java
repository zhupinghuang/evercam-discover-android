package io.evercam.connect.discover.ipscan;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import io.evercam.connect.db.CameraOperation;
import io.evercam.network.discovery.Port;
import io.evercam.network.discovery.PortScan;
import io.evercam.network.discovery.PortScanCallback;

public class PortScanTask extends AsyncTask<Void, Void, Void>
{
    private final String TAG = "evercamdiscover-PortScanTask";
    private String ssid;
    private String ip;
    private CameraOperation cameraOperation;

    public PortScanTask(final String ip, final String ssid, Context ctxt)
    {
        this.ip = ip;
        this.ssid = ssid;
        cameraOperation = new CameraOperation(ctxt);
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        try
        {
            PortScan portScan = new PortScan(new PortScanCallback()
            {
                @Override
                public void onActivePort(Port port)
                {
                    int portInt = port.getValue();
                    String portType = port.getType();

                    if(portType.equals(Port.TYPE_HTTP))
                    {
                        cameraOperation.updateAttributeInt(ip, ssid, "http", portInt);
                    }
                    if(portType.equals(Port.TYPE_RTSP))
                    {
                        cameraOperation.updateAttributeInt(ip, ssid, "rtsp", portInt);
                    }
                }
            });

            portScan.start(ip);
        }
        catch(Exception e)
        {
            Log.e(TAG, e.toString());
        }
        return null;
    }
}
