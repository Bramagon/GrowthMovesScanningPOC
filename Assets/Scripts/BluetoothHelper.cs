using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class BluetoothHelper
{
    const string pluginName = "com.growthmoves.bluetoothmanager.BluetoothPlugin";

    static AndroidJavaClass pluginClass;
    static AndroidJavaObject pluginInstance;


    [System.Serializable]
    public class BtConnection {
        public string name;
        public string rssi;

        public override string ToString()
        {
            return "Name: " + name + " | RSSI: " + rssi;
        }
    }

    [System.Serializable]
    public class BtConnections
    {
        public List<BtConnection> connections;
    }

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

    public BtConnections GetDiscoveredBluetoothDevices()
    {
        if (Application.platform == RuntimePlatform.Android)
        {
            string result = pluginInstance.Call<string>("getDiscoveredBluetoothDevices");
            BtConnections connections = JsonUtility.FromJson<BtConnections>(result);

            return connections;
        }
        Debug.LogWarning("Wrong platform");
        return new BtConnections();
    }

    public string GetDiscoveredBluetoothDevicesString()
    {
        if (Application.platform == RuntimePlatform.Android)
        {
            return pluginInstance.Call<string>("getDiscoveredBluetoothDevices");

        }
        Debug.LogWarning("Wrong platform");
        return "";
    }
}
