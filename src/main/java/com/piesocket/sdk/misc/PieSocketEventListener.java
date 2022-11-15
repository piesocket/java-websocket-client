package com.piesocket.sdk.misc;

import org.json.JSONObject;

import java.util.HashMap;

public abstract class PieSocketEventListener {
    public PieSocketEventListener(){}

    public abstract void handleEvent(PieSocketEvent event);
}