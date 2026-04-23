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
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.smartcampus.exception.RoomNotEmptyException;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    // Returns a list of all rooms
    @GET
    public List<Room> getAllRooms() {
        return new ArrayList<>(DataStore.getRooms().values());
    }

    
    // Returns a single room by its ID
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {

        Room room = DataStore.getRooms().get(roomId);

        if (room == null) {
            // Room not found, return 404
            Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "Room not found");
            error.put("roomId", roomId);
            return Response.status(Response.Status.NOT_FOUND)
                          .entity(error)
                          .build();
        }

        return Response.ok(room).build();
    }

    // Creates a new room
    @POST
    public Response createRoom(Room room) {

        // Basic validation
        if (room.getId() == null || room.getId().isEmpty()) {
            Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "Room ID is required");
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity(error)
                          .build();
        }

        // Check if a room with this ID already exists
        if (DataStore.getRooms().containsKey(room.getId())) {
            Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "Room already exists");
            error.put("roomId", room.getId());
            return Response.status(Response.Status.CONFLICT)
                          .entity(error)
                          .build();
        }

        // Store the room
        DataStore.getRooms().put(room.getId(), room);

        // Return 201
        return Response.status(Response.Status.CREATED)
                      .entity(room)
                      .build();
    }
    
    
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {

        Room room = DataStore.getRooms().get(roomId);

        // If the room does not exist, return 404
        if (room == null) {
            Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "Room not found");
            error.put("roomId", roomId);
            return Response.status(Response.Status.NOT_FOUND)
                          .entity(error)
                          .build();
        }

        // Check if the room still has sensors
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId);
        }


        // Safe to delete
        DataStore.getRooms().remove(roomId);

        // Return 204
        return Response.noContent().build();
    }

}
