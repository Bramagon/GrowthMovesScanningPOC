using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public interface IManager
{
    IEnumerator StartMonitoring();
    void StopMonitoring();
}
