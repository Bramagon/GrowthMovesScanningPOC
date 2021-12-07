using System;
using System.Collections;
using System.Collections.Generic;
using System.Threading;
using TMPro;
using UnityEngine;
using UnityEngine.UI;

public class BTManager : MonoBehaviour, IManager
{
    private BluetoothHelper bluetoothHelper;
    private float elapsedTime = 0;
    private bool monitoring;

    private Color originalBackgroundColor;
    BluetoothHelper.BtConnections connectionObject;

    [SerializeField] Image background;
    [SerializeField] GameObject popupDialog;
    [SerializeField] GameObject scrollableContent;
    [SerializeField] TMP_Text debugText;
    [SerializeField] Button deviceButtonPrefab;

    private Thread getDevicesThread;
    private bool threadRunning = false;

    private void Start()
    {
        bluetoothHelper = new BluetoothHelper();
    }

    public IEnumerator StartMonitoring()
    {
        monitoring = true;

        originalBackgroundColor = Color.white;
        scrollableContent.transform.parent.parent.gameObject.SetActive(true);
        /*getDevicesThread = new Thread(() => GetDevices());
        getDevicesThread.IsBackground = true;
        getDevicesThread.Start();*/

        yield return null;
    }

    public void StopMonitoring()
    {

        monitoring = false;
        background.color = Color.white;
        scrollableContent.transform.parent.parent.gameObject.SetActive(false);
    }

    private void Update()
    {
        if (monitoring)
        {

            elapsedTime += Time.deltaTime;


            if (elapsedTime >= 1f && !threadRunning)
            {
                connectionObject = bluetoothHelper.GetDiscoveredBluetoothDevices();
                if (connectionObject != null)
                {
                    string scrollableText =
                        "Bluetooth enabled: " + bluetoothHelper.GetBluetoothEnabled()
                        + "\nElapsed Time: " + bluetoothHelper.GetElapsedTime()
                        + "\nDevices:" + connectionObject.ToString();



 /*               foreach (BluetoothHelper.BtConnection conn in connectionObject.connections)
                {
                    Button btn = Instantiate(deviceButtonPrefab);
                    btn.GetComponentInChildren<TMP_Text>().text = conn.ToString();
                    btn.transform.SetParent(scrollableContent.transform);
                }*/

                debugText.text = scrollableText;

                }
                elapsedTime = 0;
            }
        }
    }


    public void GetDevices()
    {
        float interval = 0.5f;
        while (monitoring)
        {

            Debug.Log("TEST " + interval);
            interval -= 0.01f;
            if (interval <= 0)
            {
                threadRunning = true;
                connectionObject = bluetoothHelper.GetDiscoveredBluetoothDevices();
                interval = 0.5f;
            }
            threadRunning = false;
        }

    }
        
}
