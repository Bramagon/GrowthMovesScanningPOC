using System;
using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;
using UnityEngine.UI;

public class GPSManager : MonoBehaviour
{
    private float gpsPingTimeout = 2f;
    private bool monitoring;

    private LocationInfo testTargetLocation;
    private LocationInfo currentDeviceLocation;

    [SerializeField] Image background;
    [SerializeField] GameObject popupDialog;
    [SerializeField] TMP_Text distanceFromTargetText;


    struct LocationInfo 
    {
        public float latitude;
        public float longitude;
        public float altitude;
        public float horizontalAccuracy;
    }


    public IEnumerator StartGpsMonitoring()
    { 

        if (!Input.location.isEnabledByUser)
        {
            
            Debug.Log("Error, location not enabled by user...");
            ShowDialog();

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

        testTargetLocation.latitude = Input.location.lastData.latitude;
        testTargetLocation.longitude = Input.location.lastData.altitude;
        testTargetLocation.altitude = Input.location.lastData.altitude;
        testTargetLocation.horizontalAccuracy = Input.location.lastData.horizontalAccuracy;

        background.color = Color.red;
        distanceFromTargetText.gameObject.SetActive(true);

        monitoring = true;
    }

    private void ShowDialog()
    {
        popupDialog.GetComponent<TextContainer>().textObject.text = "Please enable location on your device.";
        popupDialog.SetActive(true);
    }

    internal void Stop()
    {
        monitoring = false;
    }

    public void StartTestDrive()
    { 

        currentDeviceLocation.latitude = 38.8951f;
        currentDeviceLocation.longitude = -77.0363f;

        testTargetLocation.latitude = 38.8952f;
        testTargetLocation.longitude = -77.0363f;

        if (CheckIfCloseToTarget())
        {
            // endgame
            Debug.Log("Target reached");
        }
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

            if (CheckIfCloseToTarget())
            {
                // endgame
                background.color = Color.green;
                Debug.Log("Target reached");
            }

            gpsPingTimeout -= Time.deltaTime;
        }
    }

    private bool CheckIfCloseToTarget()
    {
        // Get target location from server;


        if (GetDistanceInMeters(currentDeviceLocation.latitude, currentDeviceLocation.longitude, testTargetLocation.latitude, testTargetLocation.longitude) <= 1)
        {

            return true;
        }
        else
        {
            return false;
        }
    }

    private double GetDistanceInMeters(double lat1, double lng1, double lat2, double lng2)
    {

        double earthRadius = 6371000; // in meters

        double dLat = ToRadians(lat2 - lat1);
        double dLng = ToRadians(lng2 - lng1);

        double sindLat = Math.Sin(dLat / 2);
        double sindLng = Math.Sin(dLng / 2);

        double a = Math.Pow(sindLat, 2) + Math.Pow(sindLng, 2)
            * Math.Cos(ToRadians(lat1)) * Math.Cos(ToRadians(lat2));

        double c = 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));

        double dist = earthRadius * c;
        distanceFromTargetText.text = dist.ToString();

        return dist; // output distance, in meters
    }

    private double ToRadians(double angle) // Math extension method
    {
        return (Math.PI / 180) * angle;
    }

    private void SendCurrentLocation()
    {
        Debug.Log("Sending current location: " + Input.location.lastData.latitude + " " + Input.location.lastData.longitude + " " + Input.location.lastData.altitude + " " + Input.location.lastData.horizontalAccuracy + " " + Input.location.lastData.timestamp);
        
        currentDeviceLocation.latitude = Input.location.lastData.latitude;
        currentDeviceLocation.longitude = Input.location.lastData.altitude;
        currentDeviceLocation.altitude = Input.location.lastData.altitude;
        currentDeviceLocation.horizontalAccuracy = Input.location.lastData.horizontalAccuracy;

        throw new NotImplementedException();
    }
}
