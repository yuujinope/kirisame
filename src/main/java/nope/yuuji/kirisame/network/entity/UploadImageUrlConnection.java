package nope.yuuji.kirisame.network.entity;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import nope.yuuji.kirisame.network.mime.MultipartEntity;
import nope.yuuji.kirisame.network.util.UrlConnectionWraper;

/**
 * Created by Tkpd_Eka on 8/6/2015.
 */
public class UploadImageUrlConnection {

    public interface OnUploadListener {
        void onSuccess(String result);

        void onFailure();
    }

    public class UploadingThread extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String url = strings[0];
            return startConnection(url);
        }

        @Override
        protected void onPostExecute(String s) {
            if (listener != null)
                listener.onSuccess(s);
            super.onPostExecute(s);
        }
    }

    private OnUploadListener listener;
    private MultipartEntity requestParameters = new MultipartEntity();
    private String uri;
    private Proxy proxy;

    public UploadImageUrlConnection(String uri) {
        this.uri = uri;
    }

    public static byte[] convertBitmapToByte(Bitmap bitmap, int compressQuality) {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bao);
        return bao.toByteArray();
    }

    public void setListener(OnUploadListener listener) {
        this.listener = listener;
    }

    public void addPart(String param, String value) {
        requestParameters.addPart(param, value);
    }

    public void addPart(String param, String fileName, byte[] file) {
        requestParameters.addFile(param, fileName, file);
    }

    public void openConnection() {
        UploadingThread thread = new UploadingThread();
        thread.execute(uri);
    }

    public void setProxy(String address, int port){
        proxy = new Proxy(Proxy.Type.HTTP,
                InetSocketAddress.createUnresolved("192.168.60.153", 8888));
    }

    private String startConnection(String url) {
        try {
            return createUrlConnection(url);
        } catch (IOException e) {
            e.printStackTrace();
            return "FAIL";
        }
    }

    private String createUrlConnection(String link) throws IOException {
        URL url = new URL(link);
        UrlConnectionWraper con = new UrlConnectionWraper();
        con.openConnection(url, proxy);

        con.setRequestMethod("POST");
        con.setDoInput(true);
        con.setDoOutput(false);
        con.setChunkedStreamingMode(1024);
        con.setRequestProperty("charset", "utf-8");
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("Cache-Control", "no-cache");
        con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + MultipartEntity.BOUNDARY);

        requestParameters.setOutputStream(con.getOutputStream());
        System.out.println("Response Code : " + con.getResponseCode());
        System.out.println("Cipher Suite : " + con.getCipherSuite());
        return changeInputStreamToString(con.getInputStream());
    }

    private String changeInputStreamToString(InputStream inputStream) throws IOException {
        String temp;
        String result;
        StringBuilder sb = new StringBuilder();
        BufferedReader is = new BufferedReader(new InputStreamReader(inputStream));
        while ((temp = is.readLine()) != null) {
            sb.append(temp);
        }
        result = sb.toString();
        is.close();
        return result;
    }

}
