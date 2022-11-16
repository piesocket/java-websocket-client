package com.piesocket.channels.misc;

import android.util.Log;

public class Logger {

    private Boolean enabled;

    public static String LOG_TAG;
    public static String ERROR_TAG;

    public Logger(Boolean enabled){
        this.enabled = enabled;
        this.LOG_TAG = "PIESOCKET-SDK-LOGS";
        this.ERROR_TAG = "PIESOCKET-SDK-ERRORS";
    }

    public void log(String text){
        if(this.enabled){
            Log.d( this.LOG_TAG , text);
        }
    }

    public void disableLogs(){
        this.enabled = false;
    }

    public void enableLogs(){
        this.enabled = true;
    }

}
