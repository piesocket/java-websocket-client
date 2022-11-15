package com.piesocket.sdk;

import android.util.Log;

import com.piesocket.sdk.misc.PieSocketEvent;
import com.piesocket.sdk.misc.PieSocketEventListener;
import com.piesocket.sdk.misc.Logger;
import com.piesocket.sdk.misc.PieSocketException;
import com.piesocket.sdk.misc.PieSocketOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class Room extends WebSocketListener implements Callback {

    public String id;
    public WebSocket ws;
    public String uuid;

    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private HashMap<String, PieSocketEventListener> listeners;
    private Logger logger;
    private PieSocketOptions options;
    private JSONArray members;
    private Boolean shouldReconnect;

    public Room(String roomId, PieSocketOptions pieSocketOptions, Logger logger){
        this.listeners = new HashMap<String, PieSocketEventListener>();
        this.id = roomId;
        this.logger = logger;
        this.options = pieSocketOptions;
        this.uuid = UUID.randomUUID().toString();
        this.shouldReconnect = false;

        this.connect(roomId);
    }

    public String buildEndpoint(){
        String endpoint = "wss://" + this.options.getClusterId() + ".piesocket.com/v" + this.options.getVersion() + "/" + this.id + "?api_key=" + this.options.getApiKey() + "&notify_self=" + this.options.getNotifySelf() + "&source=androidsdk&v=1&presence="+ this.options.getPresence();

        String jwt = this.getAuthToken();
        if(jwt != null){
            endpoint = endpoint + "&jwt=" + jwt;
        }

        if(this.options.getUserId() != null){
            endpoint = endpoint + "&user=" + this.options.getUserId();
        }

        //Add UUID
        endpoint = endpoint +"&uuid=" + this.uuid;

        logger.log("Room endpoint: "+endpoint);
        return endpoint;
    }

    public Boolean isGuarded(){
        if(this.options.getForceAuth()){
            return true;
        }

        return this.id.startsWith("private-");
    }

    public String getAuthToken(){
        if(this.options.getJwt() != null){
            return this.options.getJwt();
        }

        if(this.isGuarded()){
            if(this.options.getAuthEndpoint() != null){
                getAuthTokenFromServer();
                throw new PieSocketException("JWT not provided, will fetch from authEndpoint.");
            }else{
                throw new PieSocketException("Neither JWT, nor authEndpoint is provided for private channel authentication.");
            }
        }

        return null;
    }

    public void getAuthTokenFromServer(){
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder().url(this.options.getAuthEndpoint()).build();
        okHttpClient.newCall(request).enqueue(this);
    }

    public void connect(String roomId){
        logger.log("Connecting to: "+roomId);

        try{
            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(0,  TimeUnit.MILLISECONDS)
                    .build();

            Request request = new Request
                    .Builder()
                    .url(this.buildEndpoint())
                    .build();

            this.ws = client.newWebSocket(request, this);
        }catch(RuntimeException e){
            if(e.getMessage().contains("will fetch from authEndpoint")){
                logger.log("Defer connection: "+e.getMessage());
            }else{
                throw e;
            }
        }
    }

    public  void disconnect(){
        this.shouldReconnect = false;
        this.ws.close(NORMAL_CLOSURE_STATUS, null);
    }

    public  void reconnect(){
        if(this.shouldReconnect){
            this.connect(this.id);
        }
    }


    public void shareVideo(){

    }


    public void listen(String eventName, PieSocketEventListener callback){
        listeners.put(eventName, callback);
    }


    public void publish(PieSocketEvent event){
        this.ws.send(event.toString());
    }

    public  void fireEvent(PieSocketEvent event){
        logger.log("Fired Event: " +event.toString());

        if (this.listeners.containsKey(event.getEvent())) {
            this.listeners.get(event.getEvent()).handleEvent(event);
        }

        if (this.listeners.containsKey("*")) {
            this.listeners.get("*").handleEvent(event);
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        PieSocketEvent event = new PieSocketEvent();
        event.setEvent("system:connected");
        this.fireEvent(event);

        this.shouldReconnect = true;
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {

        PieSocketEvent payload = new PieSocketEvent();

        if (this.listeners.containsKey("system:message")) {
            payload.setEvent("system:message");
            payload.setData(text);
            this.listeners.get("system:message").handleEvent(payload);
        }

        try {
            JSONObject obj = new JSONObject(text);
            if(obj.has ("event")){
                String event = obj.getString("event");

                //Prepare payload
                payload.setEvent(event);

                if(obj.has("data")){
                    payload.setData(obj.getString("data"));
                }
                if(obj.has("meta")){
                    payload.setMeta(obj.getString("meta"));
                }


                //Trigger listeners
                this.handleSystemEvents(payload);
                this.fireEvent(payload);
            }
            if(obj.has("error")){
                this.shouldReconnect = false;

                payload.setEvent("system:error");
                payload.setData(obj.getString("error"));
                this.fireEvent(payload);
            }
        } catch (Throwable tx) {
            //Ignore error
        }

    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null);

        PieSocketEvent event = new PieSocketEvent();
        event.setEvent("system:closed");
        event.setData(reason);
        this.fireEvent(event);

        this.reconnect();
    }



    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        PieSocketEvent event = new PieSocketEvent();
        event.setEvent("system:error");
        event.setData(t.getMessage());
        this.fireEvent(event);
    }



    public JSONObject getMemberByUUID(String uuid){
        for (int i =0 ; i < this.members.length(); i++){
            try{
                JSONObject member = this.members.getJSONObject(i);
                if(member.getString("uuid").equals(uuid)){
                    return member;
                }

            }catch (Exception e){
                //Ignore errors, member can be a string and JSONException is possible
            }
        }

        return null;
    }

    public JSONObject getCurrentMember(){
        return this.getMemberByUUID(this.uuid);
    }

    public JSONArray getAllMembers(){
        return this.members;
    }

    public void handleSystemEvents(PieSocketEvent event){
        try{

            //Update members list
            if(
                    event.getEvent().equals("system:member_list") ||
                    event.getEvent().equals("system:member_joined") ||
                    event.getEvent().equals("system:member_left")
            ) {
                JSONObject data = new JSONObject(event.getData());
                this.members = data.getJSONArray("members");
            }


        }catch (Exception e){
            throw new PieSocketException(e.getMessage());
        }
    }

    @Override
    public void onFailure(Call call, IOException e) {
        throw  new PieSocketException("Auth Token Server Error"+ e.getMessage());
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        String responseText = response.body().string();

        this.logger.log("Auth Token Server Response: "+ responseText);
        try{

            JSONObject serverResponse = new JSONObject(responseText);
            String jwt = serverResponse.getString("auth");

            if(jwt != null){
                this.logger.log("Auth token fetched, resuming connection");
                this.options.setJwt(jwt);
                this.connect(this.id);
            }

        }catch (Exception e){
            throw  new PieSocketException("Auth Token Response Parsing Error: "+ e.getMessage());
        }
    }
}
