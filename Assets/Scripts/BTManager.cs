using System;
using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;
using UnityEngine.UI;

public class BTManager : MonoBehaviour, IManager
{
    private BluetoothHelper bluetoothHelper;
    private float elapsedTime = 0;
    private bool monitoring;

    private Color originalBackgroundColor;
    [SerializeField] Image background;
    [SerializeField] GameObject popupDialog;
    [SerializeField] GameObject scrollableContent;

    private void Start()
    {
        bluetoothHelper = new BluetoothHelper();
    }

    public IEnumerator StartMonitoring()
    {
        monitoring = true;

        originalBackgroundColor = Color.white;
        scrollableContent.gameObject.SetActive(true);

        yield return null;
    }

    public void StopMonitoring()
    {
        monitoring = false;
        background.color = Color.white;
        scrollableContent.gameObject.SetActive(false);
    }

    private void Update()
    {
        if (monitoring)
        {
            elapsedTime += Time.deltaTime;

            if (elapsedTime >= 1)
            {
                string scrollableText =
                    "Bluetooth enabled: " + bluetoothHelper.GetBluetoothEnabled()
                    + "\nElapsed Time: " + bluetoothHelper.GetElapsedTime()
                    + "\nDevices:" + bluetoothHelper.GetDiscoveredBluetoothDevices();

                BluetoothHelper.BtConnections connectionObject = bluetoothHelper.GetDiscoveredBluetoothDevices();

                foreach (BluetoothHelper.BtConnection conn in connectionObject.connections)
                {
                    Debug.Log(conn);
                    scrollableText += "\n" + conn.ToString();
                }

                scrollableContent.GetComponentInChildren<TMP_Text>().text = scrollableText;


                elapsedTime = 0;
            }
        }
    }
}
