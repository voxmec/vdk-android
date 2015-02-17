package com.voxmecanica.vdk;

public class VoxException extends RuntimeException{
    public enum ErrorType {
        InternalError,
        RuntimeError,
        NetworkError,
        SpeechError,
        DeviceFeatureError,
        HttpError,
        DialogLaunchError,
        DialogExecutionError,
        ;
    }

    private ErrorType type;

    public VoxException(){super();}

    public VoxException (ErrorType type){
        super();
        this.type = type;
    }
    public VoxException(ErrorType type, Throwable e){
    	super(e);
    	this.type = type;
    }
    
    public VoxException(ErrorType type, String m){
    	super(m);
    	this.type = type;
    }
    
    public VoxException(String msg) {
        super(msg);
    }

    public VoxException(String msg, Throwable ex) {
        super(msg, ex);
    }

    public VoxException(Throwable ex){
        super(ex);
    }
    
    public ErrorType getErrorType(){
    	return type;
    }
}
