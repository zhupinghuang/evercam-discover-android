package io.evercam.connect.discover.ipscan;

import io.evercam.connect.helper.Constants;
import io.evercam.connect.net.NetInfo;

public class Host
{
    public int deviceType;
    public String ipAddress;
    public String hardwareAddress;
    public String vendor;

    public Host()
    {
        deviceType = Constants.TYPE_OTHERS;
        ipAddress = null;
        hardwareAddress = NetInfo.EMPTY_MAC;
        vendor = "Unknown";
    }

    public int getDeviceType()
    {
        return deviceType;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }

    public String getHardwareAddress()
    {
        return hardwareAddress;
    }

    public String getVendor()
    {
        return vendor;
    }

    public void setDeviceType(int deviceType)
    {
        this.deviceType = deviceType;
    }

    public void setIpAddress(String ipAddress)
    {
        this.ipAddress = ipAddress;
    }

    public void setHardwareAddress(String hardwareAddress)
    {
        this.hardwareAddress = hardwareAddress;
    }

    public void setVendor(String vendor)
    {
        this.vendor = vendor;
    }

}
