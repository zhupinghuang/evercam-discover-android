package io.evercam.connect.discover.bonjour;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.Enumeration;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import io.evercam.connect.DiscoverMainActivity;
import io.evercam.connect.db.Camera;
import io.evercam.connect.db.CameraOperation;
import io.evercam.connect.net.NetInfo;

public class JmdnsDiscover
{

    JmDNS jmdns;
    private String axisVideoService = "_axis-video._tcp.local.";
    // for Axis camera only
    private Camera camera;
    private ServiceInfo info;
    private CameraOperation cameraOperation;
    public NetInfo netInfo;

    public JmdnsDiscover(NetInfo netInfo, Context ctxt)
    {
        this.netInfo = netInfo;
        cameraOperation = new CameraOperation(ctxt);
    }

    public void startJmdnsDiscovery()
    {
        new JmdnsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private Camera getCameraFromBonjour(String type, String name)
    {
        info = jmdns.getServiceInfo(type, name);
        int httpport = info.getPort();
        String ip = String.valueOf(info.getHostAddress());
        Camera bonjourCamera = new Camera(ip);
        for(Enumeration<String> names = info.getPropertyNames(); names.hasMoreElements(); )
        {
            String prop = names.nextElement();
            String mac = info.getPropertyString(prop);
            bonjourCamera.setMAC(mac);
        }
        String model = name.substring(0, name.indexOf("-") - 1);
        bonjourCamera.setHttp(httpport);
        bonjourCamera.setVendor("AXIS");
        bonjourCamera.setModel(model);
        bonjourCamera.setFlag(1);
        bonjourCamera.setBonjour(1);
        bonjourCamera.setActive(1);
        bonjourCamera.setFirstSeen(DiscoverMainActivity.getSystemTime());
        bonjourCamera.setLastSeen(DiscoverMainActivity.getSystemTime());
        return bonjourCamera;
    }

    private class JmdnsTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            try
            {
                jmdns = JmDNS.create();
                jmdns.addServiceListener(axisVideoService, new ServiceListener()
                {

                    @Override
                    public void serviceAdded(ServiceEvent e)
                    {
                        final String serviceName = e.getName();
                        camera = getCameraFromBonjour(axisVideoService, serviceName);
                        if(cameraOperation.isExisting(camera.getIP(), netInfo.getSsid()))
                        {
                            camera.setLastSeen(DiscoverMainActivity.getSystemTime());
                            cameraOperation.updateBonjourCamera(camera, netInfo.getSsid());
                        }
                        else
                        {
                            cameraOperation.insertCamera(camera, netInfo.getSsid());
                        }
                    }

                    @Override
                    public void serviceRemoved(ServiceEvent e)
                    {
                    }

                    @Override
                    public void serviceResolved(ServiceEvent arg0)
                    {

                    }
                });
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }

    }
}
