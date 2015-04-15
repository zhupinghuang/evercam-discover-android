package io.evercam.connect.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.evercam.connect.helper.Constants;

public class CurrentNetworkInterface
{
    private String interfaceName = null;
    private NetworkInterface networkInterface;
    private Context ctxt;
    private SharedPreferences prefs;
    private int cidr = 24;

    private final static int BUF = 8 * 1024;
    private static final String CMD_IP = " -f inet addr show %s";
    private static final String PTN_IP1 = "\\s*inet [0-9\\.]+\\/([0-9]+) brd [0-9\\.]+ scope " +
            "global %s$";
    private static final String PTN_IP2 = "\\s*inet [0-9\\.]+ peer [0-9\\.]+\\/([0-9]+) scope " +
            "global %s$";
    private static final String PTN_IF = "^%s: ip [0-9\\.]+ mask ([0-9\\.]+) flags.*";

    public CurrentNetworkInterface(Context ctxt)
    {
        this.ctxt = ctxt;
        initializeInterfaceName();
        networkInterface = getInterfaceByName(interfaceName);

        cidr = getCidrFromInterfaceName(interfaceName);
    }

    public String getIpAddress()
    {
        return getIPFromInterface(networkInterface);
    }

    private void initializeInterfaceName()
    {
        prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
        interfaceName = prefs.getString(Constants.KEY_NETWORK_INTERFACE, null);

        if(getInterfaceByName(interfaceName) == null)
        {
            chooseDefaultInterfaceName();
        }
    }

    private NetworkInterface getInterfaceByName(String name)
    {
        if(name != null)
        {
            try
            {
                return NetworkInterface.getByName(name);
            }
            catch(SocketException e)
            {
                e.printStackTrace();
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    public String getMacAddress()
    {
        try
        {
            byte[] mac = networkInterface.getHardwareAddress();
            if(mac != null)
            {
                StringBuilder buf = new StringBuilder();
                for(int idx = 0; idx < mac.length; idx++)
                {
                    buf.append(String.format("%02X:", mac[idx]));
                }
                if(buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
                return buf.toString();
            }
        }
        catch(SocketException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isWiFiInterface()
    {
        WifiManager wifi = (WifiManager) ctxt.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifi.getConnectionInfo();
        String wifiMac = wifiInfo.getMacAddress();

        if(getMacAddress() != null)
        {
            if(getMacAddress().equalsIgnoreCase(wifiMac))
            {
                return true;
            }

        }
        return false;
    }

    private String getIPFromInterface(NetworkInterface networkInterface)
    {
        for(Enumeration<InetAddress> nis = networkInterface.getInetAddresses(); nis
                .hasMoreElements(); )
        {
            InetAddress ia = nis.nextElement();
            if(!ia.isLoopbackAddress())
            {
                if(ia instanceof Inet6Address)
                {
                    continue;
                }
                else
                {
                    return ia.getHostAddress();
                }

            }
        }
        return null;
    }

    private void chooseDefaultInterfaceName()
    {
        try
        {
            for(Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                    .hasMoreElements(); )
            {
                NetworkInterface ni = en.nextElement();
                String name = ni.getName();
                if(name.equals("lo"))
                {
                    continue;
                }
                else
                {

                    String ip = getIPFromInterface(ni);
                    if(ip == null)
                    {
                        continue;
                    }
                    else
                    {
                        interfaceName = name;
                        break;
                    }
                }
            }
        }
        catch(SocketException e)
        {
            e.printStackTrace();
        }
    }

    public String getInterfaceName()
    {
        return interfaceName;
    }

    private int getCidrFromInterfaceName(String intf)
    {
        int cidr = 24;
        String match;
        // Running ip tools

        if(intf != null)
        {
            try
            {
                if((match = matchFromCommand("/system/xbin/ip", String.format(CMD_IP, intf),
                        String.format(PTN_IP1, intf))) != null)
                {
                    cidr = Integer.parseInt(match);

                }
                else if((match = matchFromCommand("/system/xbin/ip", String.format(CMD_IP, intf),
                        String.format(PTN_IP2, intf))) != null)
                {
                    cidr = Integer.parseInt(match);

                }
                else if((match = matchFromCommand("/system/bin/ifconfig", " " + intf, String
                        .format(PTN_IF, intf))) != null)
                {
                    cidr = maskIpToCidr(match);

                }
                else
                {
                    Log.i("Network", "cannot find cidr");
                }
            }
            catch(NumberFormatException e)
            {
                Log.i("Network", e.getMessage() + " -> cannot find cidr");
            }
        }
        return cidr;
    }

    private int maskIpToCidr(String ip)
    {
        double sum = -2;
        String[] part = ip.split("\\.");
        for(String p : part)
        {
            sum += 256D - Double.parseDouble(p);
        }
        return 32 - (int) (Math.log(sum) / Math.log(2d));
    }

    private String matchFromCommand(String path, String cmd, String ptn)
    {
        try
        {
            if(new File(path).exists() == true)
            {
                String line;
                Matcher matcher;
                Pattern ptrn = Pattern.compile(ptn);
                Process p = Runtime.getRuntime().exec(path + cmd);
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()), BUF);
                while((line = r.readLine()) != null)
                {
                    matcher = ptrn.matcher(line);
                    if(matcher.matches())
                    {
                        return matcher.group(1);
                    }
                }
            }
        }
        catch(Exception e)
        {
            Log.e("Network", "Can't use native command: " + e.getMessage());
            return null;
        }
        return null;
    }

    public int getCidr()
    {
        return cidr;
    }
}
