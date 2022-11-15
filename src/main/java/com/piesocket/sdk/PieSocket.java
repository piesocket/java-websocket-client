package com.piesocket.sdk;

import com.piesocket.sdk.misc.Logger;
import com.piesocket.sdk.misc.PieSocketException;
import com.piesocket.sdk.misc.PieSocketOptions;

import java.util.HashMap;

public class PieSocket {

    public HashMap<String, Room> rooms;
    public PieSocketOptions options;
    public Logger logger;

    public PieSocket(PieSocketOptions pieSocketOptions){
        this.rooms = new HashMap<String, Room>();
        this.options = pieSocketOptions;
        this.logger = new Logger(true);

        validateOptions();

    }


    private void validateOptions(){
        if(this.options.getClusterId() == null){
            throw new PieSocketException("Cluster ID is not provided");
        }

        if(this.options.getApiKey() == null){
            throw new PieSocketException("API Key is not provided");
        }
    }


    public Room join(String roomId){

        if(this.rooms.containsKey(roomId)){
            logger.log("Returning existing room instance: "+roomId);
            return this.rooms.get(roomId);
        }

        Room room = new Room(roomId, this.options, this.logger);
        this.rooms.put(roomId, room);

        return  room;
    }

    public void leave(String roomId) {
        if (this.rooms.containsKey(roomId)) {
            logger.log("DISCONNECT: Closing room connection: "+roomId);
            this.rooms.get(roomId).disconnect();
            this.rooms.remove(roomId);
        }else{
            logger.log("DISCONNECT: Room does not exist: "+roomId);
        }
    }

    public HashMap<String, Room> getAllRooms(){
        return this.rooms;
    }

}
