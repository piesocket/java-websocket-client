package com.piesocket.channels.misc;

import org.json.JSONObject;

public class PieSocketEvent {

    private String event;
    private String data;
    private String meta;

    public PieSocketEvent(){}

    public PieSocketEvent(String eventName){
        this.setEvent(eventName);
    }

    public String getEvent() {
        return event;
    }

    public PieSocketEvent setEvent(String event) {
        this.event = event;
        return this;
    }


    public String getData() {
        return data;
    }

    public PieSocketEvent setData(String data) {
        this.data = data;
        return this;
    }

    public String getMeta() {
        return meta;
    }


    public PieSocketEvent setMeta(String meta) {
        this.meta = meta;
        return  this;
    }

    public String toString() {
        JSONObject eventString = new JSONObject();

        try{
            eventString.put("event", this.event);

            if(this.data != null){
                eventString.put("data", this.data);
            }
            if(this.meta != null){
                eventString.put("meta", this.meta);
            }

            return eventString.toString();
        }catch (Exception e){
            return e.getMessage();
        }
    }

}
