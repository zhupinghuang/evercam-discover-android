package io.evercam.connect.net;

import io.evercam.connect.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

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

	// 0x1 is HW Type: Ethernet (10Mb) [JBP]
	// 0x2 is ARP Flag: completed entry (ha valid)
	private final static String MAC_RE = "^%s\\s+0x1\\s+0x2\\s+([:0-9a-fA-F]+)\\s+\\*\\s+\\w+$";
	private final static int BUF = 8 * 1024;

	public NetInfo(Context ctxt)
	{
		this.ctxt = ctxt;
		currentNetworkInterface = new CurrentNetworkInterface(ctxt);
		if (hasActiveNetwork())
		{
			if (isWifiConnected(ctxt))
			{
				if (currentNetworkInterface.isWiFiInterface())
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

	// int ip to string(16885952 to 192.168.1.1)
	public String getIpFromIntSigned(int ip_int)
	{
		String ip = "";
		for (int k = 0; k < 4; k++)
		{
			ip = ip + ((ip_int >> k * 8) & 0xFF) + ".";
		}
		return ip.substring(0, ip.length() - 1);
	}

	public static String getIpFromLongUnsigned(long ip_long)
	{
		String ip = "";
		for (int k = 3; k > -1; k--)
		{
			ip = ip + ((ip_long >> k * 8) & 0xFF) + ".";
		}
		return ip.substring(0, ip.length() - 1);
	}

	public static long getUnsignedLongFromIp(String ip_addr)
	{

		String[] a = ip_addr.split("\\.");
		return (Integer.parseInt(a[0]) * 16777216 + Integer.parseInt(a[1])
				* 65536 + Integer.parseInt(a[2]) * 256 + Integer.parseInt(a[3]));
	}

	// get wifi information
	private void setWifiInfo()
	{
		WifiManager wifi = (WifiManager) ctxt
				.getSystemService(Context.WIFI_SERVICE);
		if (isWifiConnected(ctxt))
		{
			wifiInfo = wifi.getConnectionInfo();
			ssid = wifiInfo.getSSID();
			macAddress = wifiInfo.getMacAddress();
			gatewayIp = getIpFromIntSigned(wifi.getDhcpInfo().gateway);
			netmaskIp = getIpFromIntSigned(wifi.getDhcpInfo().netmask);
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
		ConnectivityManager connectivityManager = (ConnectivityManager) ctxt
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager.getActiveNetworkInfo() != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public CharSequence[] getNetworkInterfaceNames()
	{
		Enumeration<NetworkInterface> networkInterfaces = null;
		ArrayList<String> interfaceNameArrayList = new ArrayList<String>();
		try
		{
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
			for (Enumeration<NetworkInterface> networkInterfaceEnum = networkInterfaces; networkInterfaces
					.hasMoreElements();)
			{
				NetworkInterface networkInterface = networkInterfaceEnum
						.nextElement();
				for (Enumeration<InetAddress> nis = networkInterface
						.getInetAddresses(); nis.hasMoreElements();)
				{
					InetAddress thisInetAddress = nis.nextElement();
					if (!thisInetAddress.isLoopbackAddress())
					{
						if (thisInetAddress instanceof Inet6Address)
						{
							continue;
						}
						else
						{
							interfaceNameArrayList.add(networkInterface
									.getName());
						}
					}
				}
			}
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
		CharSequence[] chars = interfaceNameArrayList
				.toArray(new CharSequence[interfaceNameArrayList.size()]);
		return chars;
	}

	// check wifi connection
	public boolean isWifiConnected(Context context)
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetworkInfo.isConnected())
		{
			return true;
		}

		return false;
	}

	public static String getExternalIP()
	{
		String extIP = null;
		HttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT,
				2000);
		httpclient.getParams().setIntParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 2000);
		try
		{

			HttpGet httpget = new HttpGet(Constants.URL_GET_EXTERNAL_ADDR);
			HttpResponse response;

			response = httpclient.execute(httpget);

			HttpEntity entity = response.getEntity();
			if (entity != null)
			{
				long len = entity.getContentLength();
				if (len != -1 && len < 1024)
				{
					extIP = EntityUtils.toString(entity);
				}
			}
		}
		catch (ConnectTimeoutException e)
		{
			Log.e("Network", "External IP address not responding");

		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		} finally
		{
			httpclient.getConnectionManager().shutdown();
		}
		return extIP;
	}

	public static String getHardwareAddress(String ip)
	{
		String hw = EMPTY_MAC;
		try
		{
			if (ip != null)
			{
				String ptrn = String.format(MAC_RE, ip.replace(".", "\\."));
				Pattern pattern = Pattern.compile(ptrn);
				BufferedReader bufferedReader = new BufferedReader(
						new FileReader("/proc/net/arp"), BUF);
				String line;

				Matcher matcher;
				while ((line = bufferedReader.readLine()) != null)
				{
					matcher = pattern.matcher(line);
					if (matcher.matches())
					{
						hw = matcher.group(1);
						break;
					}
				}
				bufferedReader.close();
			}
		}
		catch (IOException e)
		{
			Log.e("Network", "Can't open/read file ARP: " + e.getMessage());
			return hw;
		}
		return hw;
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
		ConnectivityManager connectivityManager = (ConnectivityManager) ctxt
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null)
		{
			if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET)
			{
				return true;
			}
		}
		return false;
	}

	private String cidrToMask(int cidr)
	{
		int value = 0xffffffff << (32 - cidr);
		byte[] bytes = new byte[] { (byte) (value >>> 24),
				(byte) (value >> 16 & 0xff), (byte) (value >> 8 & 0xff),
				(byte) (value & 0xff) };

		InetAddress netAddr;
		try
		{
			netAddr = InetAddress.getByAddress(bytes);
			return netAddr.getHostAddress();
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}

		return EMPTY_IP;
	}

}