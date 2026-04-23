/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus;

/**
 *
 * @author ishamhasmin
 */
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
    
        classes.add(
            com.smartcampus.resource.DiscoveryResource.class);
        classes.add(
            com.smartcampus.resource.RoomResource.class);
        classes.add(
            com.smartcampus.resource.SensorResource.class);

        classes.add(
            com.smartcampus.mapper.RoomNotEmptyExceptionMapper.class);
        classes.add(
            com.smartcampus.mapper.LinkedResourceNotFoundExceptionMapper.class);
        classes.add(
            com.smartcampus.mapper.SensorUnavailableExceptionMapper.class);
        classes.add(
            com.smartcampus.mapper.GenericExceptionMapper.class);

        classes.add(
            com.smartcampus.filter.LoggingFilter.class);

        return classes;
    }
}

