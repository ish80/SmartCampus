/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.exception;

/**
 *
 * @author ishamhasmin
 */
public class SensorUnavailableException
        extends RuntimeException {

    private String sensorId;
    private String currentStatus;

    public SensorUnavailableException(
            String sensorId, String currentStatus) {
        super("Sensor " + sensorId
              + " is currently " + currentStatus
              + " and cannot accept readings");
        this.sensorId = sensorId;
        this.currentStatus = currentStatus;
    }

    public String getSensorId() { return sensorId; }
    public String getCurrentStatus() { return currentStatus; }
}
