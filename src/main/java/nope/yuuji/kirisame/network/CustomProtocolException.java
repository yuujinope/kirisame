package nope.yuuji.kirisame.network;

import java.io.IOException;

/**
 * Created by Tkpd_Eka on 8/11/2015.
 */
public class CustomProtocolException extends IOException{

    public static final int REQUEST_DENIED = 403;
    public static final int NOT_FOUND = 404;
    public static final int INTERNAL_ERROR = 500;
    public static final int NOT_IMPLEMENTED = 501;
    public static final int OVERLOAD = 502;
    public static final int GATEWAY_TIMEOUT = 503;

    private int code;

    public CustomProtocolException(String detailMessage, int code) {
        super(detailMessage);
        this.code = code;
    }

    public CustomProtocolException(int code) {
        this.code = code;
    }

    public int getCode(){
        return code;
    }
}
