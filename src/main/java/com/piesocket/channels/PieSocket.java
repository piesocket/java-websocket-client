package com.piesocket.channels;

import com.piesocket.channels.misc.Logger;
import com.piesocket.channels.misc.PieSocketException;
import com.piesocket.channels.misc.PieSocketOptions;

import java.util.HashMap;

public class PieSocket {

    public HashMap<String, Channel> rooms;
    public PieSocketOptions options;
    public Logger logger;

    public PieSocket(PieSocketOptions pieSocketOptions){
        this.rooms = new HashMap<String, Channel>();
        this.options = pieSocketOptions;
        this.logger = new Logger(this.options.getEnableLogs());


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


    public Channel join(String roomId){

        if(this.rooms.containsKey(roomId)){
            logger.log("Returning existing room instance: "+roomId);
            return this.rooms.get(roomId);
        }

        Channel room = new Channel(roomId, this.options, this.logger);
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

    public HashMap<String, Channel> getAllRooms(){
        return this.rooms;
    }

}
