/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.mapper;

/**
 *
 * @author ishamhasmin
 */
import com.smartcampus.model.ErrorResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GenericExceptionMapper
        implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER =
            Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {

        // Log the real error
        LOGGER.log(Level.SEVERE,
                "Unhandled exception caught", exception);

        // Return a generic message to the client
        ErrorResponse error = new ErrorResponse(
                500,
                "Internal Server Error",
                "An unexpected error occurred. "
                + "Please contact the administrator."
        );

        return Response.status(
                Response.Status.INTERNAL_SERVER_ERROR)
                      .entity(error)
                      .type(MediaType.APPLICATION_JSON)
                      .build();
    }
}

