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
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.smartcampus.exception.LinkedResourceNotFoundException;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // Returns all sensors, optionally filtered by type
    @GET
    public List<Sensor> getAllSensors(
            @QueryParam("type") String type) {

        List<Sensor> allSensors =
                new ArrayList<>(DataStore.getSensors().values());

        // If no type filter provided, return all sensors
        if (type == null || type.isEmpty()) {
            return allSensors;
        }

        // Filter by type
        return allSensors.stream()
                .filter(s -> s.getType() != null
                        && s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensorById(
            @PathParam("sensorId") String sensorId) {

        Sensor sensor = DataStore.getSensors().get(sensorId);

        if (sensor == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sensor not found");
            error.put("sensorId", sensorId);
            return Response.status(Response.Status.NOT_FOUND)
                          .entity(error).build();
        }

        return Response.ok(sensor).build();
    }

    // Registers a new sensor, validates that roomId exists
    @POST
    public Response createSensor(Sensor sensor) {

        // Validate that roomId is provided
        if (sensor.getRoomId() == null
                || sensor.getRoomId().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "roomId is required");
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity(error).build();
        }

        // Validate that the room actually exists
        Room room = DataStore.getRooms().get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                    "Room", sensor.getRoomId());
        }


        // Validate sensor ID
        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sensor ID is required");
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity(error).build();
        }

        // Store the sensor
        DataStore.getSensors().put(sensor.getId(), sensor);

        // Add the sensor ID to the room's sensor list
        room.getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED)
                      .entity(sensor).build();
    }
    
    
    
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsSubResource(
            @PathParam("sensorId") String sensorId) {

        // Pass the sensorId to the sub-resource
        return new SensorReadingResource(sensorId);
    }

}

