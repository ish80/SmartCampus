/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

/**
 *
 * @author ishamhasmin
 */
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Map<String, Object> getApiInfo() {

        Map<String, Object> info = new HashMap<>();
        info.put("apiName", "Smart Campus API");
        info.put("version", "1.0");
        info.put("description",
                "University Smart Campus Sensor and Room Management");

        Map<String, String> contact = new HashMap<>();
        contact.put("name", "Campus IT Department");
        contact.put("email", "smartcampus@university.ac.uk");
        info.put("contact", contact);

        Map<String, String> resources = new HashMap<>();
        resources.put("rooms", "/SmartCampus/api/v1/rooms");
        resources.put("sensors", "/SmartCampus/api/v1/sensors");
        info.put("resources", resources);

        return info;
    }
}
