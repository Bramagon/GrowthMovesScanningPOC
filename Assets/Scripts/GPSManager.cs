using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class GPSManager : MonoBehaviour
{
    private float gpsPingTimeout = 2f;
    private bool monitoring;

    public IEnumerator StartGpsMonitoring()
    {
        if (!Input.location.isEnabledByUser)
        {
            Debug.Log("Error, location not enabled by user");
            yield break;

        }

        Input.location.Start();

        int maxWait = 20;
        while(Input.location.status == LocationServiceStatus.Initializing && maxWait > 0)
        {
            yield return new WaitForSeconds(1);
            maxWait--;
        }

        if (maxWait < 1)
        {
            Debug.Log("GPS timed out");
            yield break;
        }

        if (Input.location.status == LocationServiceStatus.Failed)
        {
            Debug.Log("Unable to determine device location");
            yield break;
        }
        else
        {
            Debug.Log("GPS connection succeeded, monitoring gps location...");
        }

        monitoring = true;
    }

    void Update()
    {
        if (monitoring)
        {
            if (gpsPingTimeout < 0)
            {
                SendCurrentLocation();
                gpsPingTimeout = 2f;
            }

            gpsPingTimeout -= Time.deltaTime;
        }
    }

    private void SendCurrentLocation()
    {
        Debug.Log("Sending current location: " + Input.location.lastData.latitude + " " + Input.location.lastData.longitude + " " + Input.location.lastData.altitude + " " + Input.location.lastData.horizontalAccuracy + " " + Input.location.lastData.timestamp);
        throw new NotImplementedException();
    }
}
