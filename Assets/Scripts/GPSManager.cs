using System;
using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;
using UnityEngine.UI;

public class GPSManager : MonoBehaviour, IManager
{
    private float gpsPingTimeout = 1f;
    private bool monitoring;

    private LocationInfo testTargetLocation;
    private LocationInfo currentDeviceLocation;
    private Color originalBackgroundColor;

    [SerializeField] Image background;
    [SerializeField] GameObject popupDialog;
    [SerializeField] GameObject scrollableContent;
    [SerializeField] TMP_Text debugText;


    struct LocationInfo 
    {
        public float latitude;
        public float longitude;
        public float altitude;
        public float horizontalAccuracy;
    }


    public IEnumerator StartMonitoring()
    { 

        if (!Input.location.isEnabledByUser)
        {
            
            Debug.Log("Error, location not enabled by user...");
            ShowDialog();

            yield break;

        }

        Input.location.Start(1f, 1f);

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
        testTargetLocation.longitude = Input.location.lastData.longitude;
        testTargetLocation.altitude = Input.location.lastData.altitude;
        testTargetLocation.horizontalAccuracy = Input.location.lastData.horizontalAccuracy;

        originalBackgroundColor = Color.white;
        background.color = Color.red;
        scrollableContent.SetActive(true);

        monitoring = true;
    }

    private void ShowDialog()
    {
        popupDialog.GetComponent<TextContainer>().textObject.text = "Please enable location on your device.";
        popupDialog.SetActive(true);
    }

    void Update()
    {
        try
        {
            if (gpsPingTimeout < 0 && monitoring)
            {

                //SendCurrentLocation();

                if (CheckIfCloseToTarget())
                {
                    // endgame
                    background.color = Color.green;
                    Debug.Log("Target reached");
                }
                else
                {
                    background.color = Color.red;
                }

                gpsPingTimeout = 1f;
            }
            gpsPingTimeout -= Time.deltaTime;
        } catch(Exception e)
        {
            debugText.text = e.Message;
        }
    }

    private bool CheckIfCloseToTarget()
    {
        // Get target location from server;

        if (GetDistanceInMeters(Input.location.lastData.latitude, Input.location.lastData.longitude, testTargetLocation.latitude, testTargetLocation.longitude) <= 5d)
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

        double earthRadius = 6371d; // in km

        double dLat = ToRadians(lat2 - lat1);
        double dLng = ToRadians(lng2 - lng1);

        double sindLat = Math.Sin(dLat / 2d);
        double sindLng = Math.Sin(dLng / 2d);

        double a = Math.Pow(sindLat, 2d) + Math.Pow(sindLng, 2d)
            * Math.Cos(ToRadians(lat1)) * Math.Cos(ToRadians(lat2));

        double c = 2d * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1d - a));

        double dist = earthRadius * c;
        double distMeters = dist * 1000d;

        debugText.text = "Distance from target: \n" + distMeters.ToString() + "\nCurrentLocation: \nLatitude: " + Input.location.lastData.latitude + "\nLongitude: " + Input.location.lastData.longitude;

        return distMeters; // output distance, in meters
    }

    private double ToRadians(double angle) // Math extension method
    {
        return (Math.PI / 180d) * angle;
    }

    private void SendCurrentLocation()
    {
        Debug.Log("Sending current location: " 
            + Input.location.lastData.latitude + " " 
            + Input.location.lastData.longitude + " " 
            + Input.location.lastData.altitude + " " 
            + Input.location.lastData.horizontalAccuracy 
            + " " + Input.location.lastData.timestamp);
        
        currentDeviceLocation.latitude = Input.location.lastData.latitude;
        currentDeviceLocation.longitude = Input.location.lastData.longitude;
        currentDeviceLocation.altitude = Input.location.lastData.altitude;
        currentDeviceLocation.horizontalAccuracy = Input.location.lastData.horizontalAccuracy;

    }

    public void StopMonitoring()
    {
        monitoring = false;
        background.color = Color.white;
        scrollableContent.SetActive(false);
    }
}
