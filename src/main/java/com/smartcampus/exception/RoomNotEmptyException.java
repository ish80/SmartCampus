/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.exception;

/**
 *
 * @author ishamhasmin
 */
public class RoomNotEmptyException extends RuntimeException {

    private String roomId;

    public RoomNotEmptyException(String roomId) {
        super("Room " + roomId
              + " cannot be deleted: it still has sensors");
        this.roomId = roomId;
    }

    public String getRoomId() { return roomId; }
}
