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
    BluetoothHelper.BtConnections connectionObject;

    [SerializeField] Image background;
    [SerializeField] GameObject popupDialog;
    [SerializeField] GameObject scrollableContent;
    [SerializeField] TMP_Text debugText;
    [SerializeField] Button deviceButtonPrefab;

    private void Start()
    {
        bluetoothHelper = new BluetoothHelper();
    }

    public IEnumerator StartMonitoring()
    {
        monitoring = true;

        originalBackgroundColor = Color.white;
        scrollableContent.transform.parent.parent.gameObject.SetActive(true);

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
            connectionObject = bluetoothHelper.GetDiscoveredBluetoothDevices();

            if (elapsedTime >= 1f)
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


                elapsedTime = 0;
            }
        }
    }
}
