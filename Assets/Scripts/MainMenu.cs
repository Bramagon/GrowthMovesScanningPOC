using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class MainMenu : MonoBehaviour
{
    private QRManager qrManager;
    private NFCManager nfcManager;
    private BTManager btManager;
    private GPSManager gpsManager;

    private void Start()
    {
        qrManager = new QRManager();
        nfcManager = new NFCManager();
        btManager = new BTManager();
        gpsManager = new GPSManager(); 
    }

    public void ScanQR()
    {
        Debug.Log("Scanning QR code");
    }

    public void NFC()
    {
        Debug.Log("Using NFC");
    }

    public void Bluetooth()
    {
        Debug.Log("Scanning for bluetooth device");
    }

    public void GPS()
    {
        Debug.Log("Comparing gps locations");
        StartCoroutine(gpsManager.StartGpsMonitoring());
    }
}
