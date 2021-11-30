using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class BluetoothHelper
{
    const string pluginName = "com.growthmoves.bluetoothmanager.BluetoothPlugin";

    static AndroidJavaClass pluginClass;
    static AndroidJavaObject pluginInstance;

    public BluetoothHelper()
    {
        pluginClass = new AndroidJavaClass(pluginName);
        pluginInstance = pluginClass.CallStatic<AndroidJavaObject>("getInstance");
    }

    public double GetElapsedTime()
    {
        if (Application.platform == RuntimePlatform.Android)
        {
            return pluginInstance.Call<double>("getElapsedTime");
        }
        Debug.LogWarning("Wrong platform");
        return 0;
    }

    public bool GetBluetoothEnabled()
    {
        if (Application.platform == RuntimePlatform.Android)
        {
            return pluginInstance.Call<bool>("getBluetoothEnabled");
        }
        Debug.LogWarning("Wrong platform");
        return false;
    }

    public string GetDiscoveredBluetoothDevices()
    {
        if (Application.platform == RuntimePlatform.Android)
        {
            
            
           return pluginInstance.Call<string>("getDiscoveredBluetoothDevices");
            
        }
        Debug.LogWarning("Wrong platform");
        return "";
    }
}
