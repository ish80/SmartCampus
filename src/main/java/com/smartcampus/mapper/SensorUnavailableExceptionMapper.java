/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.mapper;

/**
 *
 * @author ishamhasmin
 */
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.ErrorResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(
            SensorUnavailableException e) {
        ErrorResponse error = new ErrorResponse(
                403,
                "Forbidden",
                e.getMessage()
        );

        return Response.status(Response.Status.FORBIDDEN)
                      .entity(error)
                      .type(MediaType.APPLICATION_JSON)
                      .build();
    }
}
