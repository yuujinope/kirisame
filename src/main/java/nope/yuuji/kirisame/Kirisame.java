package nope.yuuji.kirisame;

import android.util.Log;

/**
 * Created by Tkpd_Eka on 7/23/2015.
 */
public class Kirisame {

    public static void print(String string) {
        log("Kirisame", string);
    }

    private static void log (String TAG, String msg) {
        if (msg.length() > 4000 ) {
            int end = 4001;
            Log.d(TAG, msg.substring(0, 4000));
            log(TAG, msg.substring(end));
        } else {
            Log.d(TAG, msg);
        }
    }

}
