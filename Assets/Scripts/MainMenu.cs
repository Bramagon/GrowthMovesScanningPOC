using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class MainMenu : MonoBehaviour
{
    [SerializeField] private QRManager qrManager;
    [SerializeField] private NFCManager nfcManager;
    [SerializeField] private BTManager btManager;
    [SerializeField] private GPSManager gpsManager;

    [SerializeField] Button[] mainMenuButtons;
    [SerializeField] Button backButton;

    private void Start()
    {
        qrManager = new QRManager();
        nfcManager = new NFCManager();
        btManager = new BTManager();
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
        foreach (Button btn in mainMenuButtons)
        {
            btn.gameObject.SetActive(false);
        }

        backButton.gameObject.SetActive(true);

        Debug.Log("Comparing gps locations");
        StartCoroutine(gpsManager.StartGpsMonitoring());
    }

    public void Back()
    {
        foreach (Button btn in mainMenuButtons)
        {
            btn.gameObject.SetActive(true);
        }

        backButton.gameObject.SetActive(false);

        StopAllCoroutines();
        gpsManager.Stop();
    }
}
