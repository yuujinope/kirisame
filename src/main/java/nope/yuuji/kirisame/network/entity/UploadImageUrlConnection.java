package nope.yuuji.kirisame.network.entity;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;

import nope.yuuji.kirisame.network.CustomProtocolException;
import nope.yuuji.kirisame.network.mime.MultipartEntity;
import nope.yuuji.kirisame.network.util.UrlConnectionWrapper;

/**
 * Created by Tkpd_Eka on 8/6/2015.
 * Ver 1.2.1
 */
public abstract class UploadImageUrlConnection {

    public static final int CONNECTION_FAILED = 0;
    public static final int CONNECTION_SUCCESS = 1;
    public static final int CONNECTION_OK = 200;
    public static final String POST = "POST";

    private class UploadingThread extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            return startConnection();
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);
            switch (i) {
                case CONNECTION_SUCCESS:
                    onConnectionSuccess(result);
                    break;
                default:
                    onConnectionError(handleConnectionException(exception), errorCode);
                    break;
            }
        }
    }

    protected class Settings {
        public Proxy proxy;
        public int defaultChunkSize = 1024;
        public int readTimeOut = 20000; // Time limit for uploading file and server response
        public int connectTimeOut = 15000; // Time limit for establishing connection
    }

    private MultipartEntity requestParameters = new MultipartEntity();
    protected Settings setting = getSetting();
    private String result;
    private IOException exception;
    private int errorCode;

    public static byte[] convertBitmapToByte(Bitmap bitmap, int compressQuality) {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bao);
        return bao.toByteArray();
    }

    /**
     * Start the process of uploading image to server
     */
    public final void openConnection() {
        UploadingThread thread = new UploadingThread();
        thread.execute();
    }

    public abstract void onConnectionSuccess(String result);

    public abstract void onConnectionError(NetError e, int errorCode);

    public final void addPart(String param, String value) {
        requestParameters.addPart(param, value);
    }

    public final void addPart(String param, String fileName, byte[] file) {
        requestParameters.addFile(param, fileName, file);
    }

    public final void setProxy(String address, int port) {
        setting.proxy = new Proxy(Proxy.Type.HTTP,
                InetSocketAddress.createUnresolved(address, port));
    }

    protected Settings getSetting() {
        if (setting == null)
            return new Settings();
        else
            return setting;
    }

    public abstract String getUrl();

    private int startConnection() {
        try {
            handleConnection();
            return CONNECTION_SUCCESS;
        } catch (IOException e) {
            exception = e;
            return CONNECTION_FAILED;
        }
    }

    private void handleConnection() throws IOException {
        UrlConnectionWrapper con = createUrlConnection();
        requestParameters.setOutputStream(con.getOutputStream());
        getConnectionResult(con);
    }

    private UrlConnectionWrapper createUrlConnection() throws IOException {
        URL url = new URL(getUrl());
        UrlConnectionWrapper con = new UrlConnectionWrapper();
        con.openConnection(url, setting.proxy);

        con.setRequestMethod(POST);
        con.setDoInput(true);
        con.setDoOutput(false);
        con.setChunkedStreamingMode(setting.defaultChunkSize);
        con.setReadTimeOut(setting.readTimeOut);
        con.setConnectTimeOut(setting.connectTimeOut);
        setConnectionRequestProperties(con);
        return con;
    }

    protected void setConnectionRequestProperties(UrlConnectionWrapper con) {
        con.setRequestProperty("charset", "utf-8");
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("Cache-Control", "no-cache");
        con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + MultipartEntity.BOUNDARY);
    }

    private void getConnectionResult(UrlConnectionWrapper con) throws IOException {
        processResponseCode(con.getResponseCode());
        result = changeInputStreamToString(con.getInputStream());
    }

    private void processResponseCode(int responseCode) throws IOException {
        if (responseCode != CONNECTION_OK) {
            errorCode = responseCode;
            throw new CustomProtocolException(responseCode);
        }
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

    private NetError handleConnectionException(IOException e) {
        if (e instanceof SocketTimeoutException) {
            // Failed to establish connection or get response within timeout
            return NetError.TIMEOUT;
        } else if (e instanceof EOFException) {
            // Connection success but failed to get response
//            return ERROR_FAILED_TO_GET_INPUTSTREAM;
            return NetError.PARSE_ERROR;
        } else if (e instanceof SocketException) {
            // Failed to establish connection or existing connection distrupted
            // Connection is dropped or blocked
//            return ERROR_FAILED_TO_ESTABLISH;
            return NetError.NO_CONNECTION;
        } else if (e instanceof CustomProtocolException) {
            // Connection returned with recognizeable http code
            return NetError.SERVER_ERROR;
        } else {
            // Unhandled error instance
            e.printStackTrace();
            return NetError.UNKNOWN;
        }
    }

}
