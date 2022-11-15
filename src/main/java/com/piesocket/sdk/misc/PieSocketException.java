package com.piesocket.sdk.misc;

public class PieSocketException extends  RuntimeException{

    public PieSocketException(String message) {
        super(Logger.ERROR_TAG + ": " + message);
    }

}
