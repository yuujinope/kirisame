package nope.yuuji.kirisame.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;

/**
 * Created by Tkpd_Eka on 8/7/2015.
 */
public class SimpleCameraHandler {

    public static final String EXTRA_OUTPUT = MediaStore.EXTRA_OUTPUT;
    public static final String INTENT = MediaStore.ACTION_IMAGE_CAPTURE;

    private Context context;
    private File cameraFile;

    public SimpleCameraHandler(Context context) {
        this.context = context;
    }

    public void deleteTempFile() {
        if (cameraFile.exists())
            cameraFile.delete();
    }

    public Bitmap getBitmapFromFile(int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = sampleSize;
        return BitmapFactory.decodeFile(cameraFile.getPath(), options);
    }

    public Uri getOutputMediaFileUri(String imageName) {
        return Uri.fromFile(getOutputMediaFile(imageName));
    }

    private File getOutputMediaFile(String imageName) {
        File mediaStorageDir = new File(
                context.getExternalFilesDir(null) + File.separator
                        + "IMAGES" + File.separator);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("SimpleCameraHandler", "failed to create directory");
                return null;
            }
        }
        cameraFile = new File(mediaStorageDir + File.separator
                + "IMG_" + imageName + ".jpg");
        return cameraFile;
    }

}
