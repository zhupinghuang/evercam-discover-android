package io.evercam.connect.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.InetAddress;
import java.net.UnknownHostException;

import io.evercam.network.discovery.IpTranslator;

public class NetInfo
{
    private Context ctxt;
    private WifiInfo wifiInfo;
    private String ssid = "unknown";
    private String macAddress = EMPTY_MAC;
    private String netmaskIp = EMPTY_IP;
    private String gatewayIp = EMPTY_IP;

    private CurrentNetworkInterface currentNetworkInterface = null;

    public static final String EMPTY_MAC = "00:00:00:00:00:00";
    public static final String EMPTY_IP = "0.0.0.0";

    public NetInfo(Context ctxt)
    {
        this.ctxt = ctxt;
        currentNetworkInterface = new CurrentNetworkInterface(ctxt);
        if(hasActiveNetwork())
        {
            if(isWifiConnected(ctxt))
            {
                if(currentNetworkInterface.isWiFiInterface())
                {
                    setWifiInfo();
                }
                else
                {
                    setNotWifiNetwork();
                }
            }
            else
            {
                setNotWifiNetwork();
            }
        }
    }

    // get wifi information
    private void setWifiInfo()
    {
        WifiManager wifi = (WifiManager) ctxt.getSystemService(Context.WIFI_SERVICE);
        if(isWifiConnected(ctxt))
        {
            wifiInfo = wifi.getConnectionInfo();
            ssid = wifiInfo.getSSID();
            macAddress = wifiInfo.getMacAddress();
            gatewayIp = IpTranslator.getIpFromIntSigned(wifi.getDhcpInfo().gateway);
            netmaskIp = IpTranslator.getIpFromIntSigned(wifi.getDhcpInfo().netmask);
        }
    }

    private void setNotWifiNetwork()
    {
        ssid = currentNetworkInterface.getInterfaceName();
        macAddress = currentNetworkInterface.getMacAddress();
        netmaskIp = cidrToMask(currentNetworkInterface.getCidr());
    }

    public boolean hasActiveNetwork()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctxt.getSystemService
                (Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getActiveNetworkInfo() != null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    // check wifi connection
    public boolean isWifiConnected(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService
                (Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager
                .TYPE_WIFI);
        if(wifiNetworkInfo.isConnected())
        {
            return true;
        }

        return false;
    }

    public String getSsid()
    {
        return ssid;
    }

    public String getMacAddress()
    {
        return macAddress;
    }

    public String getNetmaskIp()
    {
        return netmaskIp;
    }

    public String getGatewayIp()
    {
        return gatewayIp;
    }

    public String getLocalIp()
    {
        return currentNetworkInterface.getIpAddress();
    }

    public String getInterfaceName()
    {
        return currentNetworkInterface.getInterfaceName();
    }

    public int getCidr()
    {
        return currentNetworkInterface.getCidr();
    }

    public boolean isEthernetConnected()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctxt.getSystemService
                (Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null)
        {
            if(networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET)
            {
                return true;
            }
        }
        return false;
    }

    private String cidrToMask(int cidr)
    {
        int value = 0xffffffff << (32 - cidr);
        byte[] bytes = new byte[]{(byte) (value >>> 24), (byte) (value >> 16 & 0xff), (byte) (value >> 8 & 0xff), (byte) (value & 0xff)};

        InetAddress netAddr;
        try
        {
            netAddr = InetAddress.getByAddress(bytes);
            return netAddr.getHostAddress();
        }
        catch(UnknownHostException e)
        {
            e.printStackTrace();
        }

        return EMPTY_IP;
    }

}
