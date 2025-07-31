package com.example.homecontrol;

public class Device {
    public String label;
    public String macAddress;
    public String ipAddress;
    public String OS;
    public boolean isOnline;

    public Device(String label, String macAddress, String ipAddress) {
        this.label = label;
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
        this.isOnline = false;
        this.OS = "Unknown";
    }

    public String getLabel() {
        return label;
    }

    public String getMac() {
        return macAddress;
    }

    public String getIp() {
        return ipAddress;
    }
}
