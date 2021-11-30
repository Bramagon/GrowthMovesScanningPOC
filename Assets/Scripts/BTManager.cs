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
    [SerializeField] TMP_Text debugText;

    private void Start()
    {
        bluetoothHelper = new BluetoothHelper();
    }

    public IEnumerator StartMonitoring()
    {
        monitoring = true;

        originalBackgroundColor = Color.white;
        background.color = Color.red;
        debugText.gameObject.SetActive(true);

        yield return null;
    }

    public void StopMonitoring()
    {
        monitoring = false;
        background.color = Color.white;
        debugText.gameObject.SetActive(false);
    }

    private void Update()
    {
        if (monitoring)
        {
            elapsedTime += Time.deltaTime;

            if (elapsedTime >= 1)
            {
                debugText.text = "Bluetooth enabled: " + bluetoothHelper.GetBluetoothEnabled() + "\nElapsed Time: " + bluetoothHelper.GetElapsedTime() + "\nDevices: \n" + bluetoothHelper.GetDiscoveredBluetoothDevices();
                elapsedTime = 0;
            }
        }
    }
}
