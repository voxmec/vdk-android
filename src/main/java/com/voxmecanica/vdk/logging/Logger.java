package com.voxmecanica.vdk.logging;

import android.util.Log;

public class Logger {
    private boolean LOGGER_ON = true;
    private String prefix;

    public Logger(){
        prefix = "VoxMecanica";
        assertAvail();
    }

    public Logger (String prefix) {
        this.prefix = prefix;
        assertAvail();
    }

    // hack - isLoggable is a native method
    // it will fail at runtime during stand-alone tests
    // This forces an UnsatisfiedLinkError (if not running on android)
    // to turn off logging.
    private void assertAvail() {
        try{
            Log.isLoggable(prefix, Log.DEBUG);
        }catch(UnsatisfiedLinkError err){
            LOGGER_ON = false;
        }
    }

    public void enabled(boolean f){
        LOGGER_ON = f;
    }

    // ************ INFO ************** //
    public void i(String message){

        if(LOGGER_ON && Log.isLoggable(prefix, Log.INFO))
            Log.i(prefix, message);
    }
    public void i(String prefix, String message){
        this.prefix = prefix;
        if(LOGGER_ON && Log.isLoggable(prefix, Log.INFO))
            Log.i(prefix, message);
    }
    public void i(String message, Throwable e){
        if(LOGGER_ON && Log.isLoggable(prefix, Log.INFO))
            Log.i(prefix, message, e);
    }

    // ************ DEBUG **************** //
    public void d(String message){
        if(LOGGER_ON  && Log.isLoggable(prefix, Log.INFO))
            Log.d(prefix, message);
    }
    public void d(Throwable ex){
        if(LOGGER_ON  && Log.isLoggable(prefix, Log.INFO))
            Log.d(prefix, ex.getMessage(), ex);
    }

    public void d(String prefix, String message){
        this.prefix = prefix;
        if(LOGGER_ON && Log.isLoggable(prefix, Log.INFO))
            Log.d(prefix, message);
    }
    public void d(String message, Throwable e){
        if(LOGGER_ON && Log.isLoggable(prefix, Log.INFO))
            Log.d(prefix, message, e);
    }
    public void d(String prefix, String message, Throwable e){
        this.prefix = prefix;
        if(LOGGER_ON && Log.isLoggable(prefix, Log.INFO))
            Log.d(prefix, message, e);
    }
}
