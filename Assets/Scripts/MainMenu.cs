using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class MainMenu : MonoBehaviour
{
    List<IManager> managers = new List<IManager>();

    [SerializeField] private QRManager qrManager;
    [SerializeField] private NFCManager nfcManager;
    [SerializeField] private BTManager btManager;
    [SerializeField] private GPSManager gpsManager;

    [SerializeField] Button[] mainMenuButtons;
    [SerializeField] Button backButton;

    private void Start()
    {
        managers.Add(gpsManager);
        managers.Add(btManager);
    }

    public void ScanQR()
    {
        DisableMenuButtons();
        Debug.Log("Scanning QR code");
    }

    public void NFC()
    {
        DisableMenuButtons();
        Debug.Log("Using NFC");
    }

    public void Bluetooth()
    {
        DisableMenuButtons();

        Debug.Log("Starting bluetooth");
        StartCoroutine(btManager.StartMonitoring());
    }

    public void GPS()
    {
        DisableMenuButtons();

        Debug.Log("Comparing gps locations");
        StartCoroutine(gpsManager.StartMonitoring());
    }

    private void DisableMenuButtons()
    {
        foreach (Button btn in mainMenuButtons)
        {
            btn.gameObject.SetActive(false);
        }

        backButton.gameObject.SetActive(true);
    }

    public void Back()
    {
        foreach (Button btn in mainMenuButtons)
        {
            btn.gameObject.SetActive(true);
        }

        foreach(IManager manager in managers)
        {
            manager.StopMonitoring();
        }

        backButton.gameObject.SetActive(false);

        StopAllCoroutines();

    }
}
