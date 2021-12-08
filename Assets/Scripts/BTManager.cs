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
    BluetoothHelper.BtConnection activeConnection;

    [SerializeField] Image background;
    [SerializeField] GameObject popupDialog;
    [SerializeField] GameObject scrollableContent;
    [SerializeField] TMP_Text debugText;
    [SerializeField] GameObject deviceButtonPrefab;

    private Dictionary<string, GameObject> connectionButtons = new Dictionary<string, GameObject>();

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
        scrollableContent.SetActive(true);
        debugText.gameObject.SetActive(true);
        yield return null;
    }

    public void StopMonitoring()
    {

        monitoring = false;
        background.color = Color.white;
        scrollableContent.SetActive(false);
        activeConnection = null;
        debugText.gameObject.SetActive(false);
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
                        "Bluetooth enabled: " + bluetoothHelper.GetBluetoothEnabled();

                    foreach (BluetoothHelper.BtConnection conn in connectionObject.connections)
                    {
                        if (!connectionButtons.ContainsKey(conn.address))
                        {

                            GameObject btn = Instantiate(deviceButtonPrefab, scrollableContent.GetComponentInChildren<ContentSizeFitter>().gameObject.transform);
                            btn.GetComponentInChildren<TMP_Text>().fontWeight = FontWeight.Bold;
                            btn.GetComponentInChildren<TMP_Text>().text = conn.name.ToString();
                            btn.GetComponent<Button>().onClick.AddListener(OnBtnClick);

                            void OnBtnClick()
                            {
                                activeConnection = conn;
                                scrollableContent.SetActive(false);
                                debugText.text = conn.ToString();
                                monitoring = false;

                            };

                            connectionButtons.Add(conn.address, btn);
                        }
                    }

  

                    debugText.text = scrollableText;

                }
                elapsedTime = 0;
            }
        } else if (activeConnection != null)
        {
            activeConnection = bluetoothHelper.GetBluetoothDeviceByAddress(activeConnection.address);
            debugText.text = activeConnection.ToString();
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
