/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

/**
 *
 * @author ishamhasmin
 */
import com.smartcampus.data.DataStore;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.smartcampus.exception.SensorUnavailableException;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // Returns all readings for this sensor
    @GET
    public Response getReadings() {

        // check if the sensor exists
        Sensor sensor = DataStore.getSensors().get(sensorId);
        if (sensor == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sensor not found");
            error.put("sensorId", sensorId);
            return Response.status(Response.Status.NOT_FOUND)
                          .entity(error).build();
        }

        // Get the readings list for this sensor
        List<SensorReading> sensorReadings =
                DataStore.getReadings()
                        .getOrDefault(sensorId, new ArrayList<>());

        return Response.ok(sensorReadings).build();
    }

    // Adds a new reading for this sensor
    @POST
    public Response addReading(SensorReading reading) {

        // Check if the sensor exists
        Sensor sensor = DataStore.getSensors().get(sensorId);
        if (sensor == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sensor not found");
            error.put("sensorId", sensorId);
            return Response.status(Response.Status.NOT_FOUND)
                          .entity(error).build();
        }

        // Check if the sensor is in MAINTENANCE mode
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    sensorId, sensor.getStatus());
        }


        // Generate a unique ID for the reading if not provided
        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }

        // Set timestamp to current time if not provided
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Add the reading to the readings list for this sensor
        DataStore.getReadings()
                .computeIfAbsent(sensorId, k -> new ArrayList<>())
                .add(reading);

        // SIDE EFFECT: Update the parent sensor's currentValue
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED)
                      .entity(reading).build();
    }
}
