package com.piesocket.channels.misc;

public abstract class PieSocketEventListener {
    public PieSocketEventListener(){}

    public abstract void handleEvent(PieSocketEvent event);
}