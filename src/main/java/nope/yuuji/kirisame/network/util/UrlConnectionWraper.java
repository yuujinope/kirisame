package nope.yuuji.kirisame.network.util;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Tkpd_Eka on 8/10/2015.
 */
public class UrlConnectionWraper {

    private static final int HTTP = 1;
    private static final int HTTPS = 2;

    private HttpURLConnection http;
    private HttpsURLConnection https;
    private int protocol;

    public void openConnection(URL url, Proxy proxy) throws IOException {
        switch (url.getProtocol()) {
            case "http":
                initHttp(url, proxy);
                break;
            case "https":
                initHttps(url, proxy);
                break;
            default:
                protocolFailed();
                break;
        }
    }

    public void openConnection(URL url) throws IOException {
        openConnection(url, null);
    }

    public void setRequestMethod(String method) throws ProtocolException {
        switch (protocol) {
            case HTTP:
                http.setRequestMethod(method);
                break;
            case HTTPS:
                https.setRequestMethod(method);
                break;
            default:
                protocolFailed();
                break;
        }
    }

    public void setDoOutput(boolean value) {
        switch (protocol) {
            case HTTP:
                http.setDoOutput(value);
                break;
            case HTTPS:
                https.setDoOutput(value);
                break;
            default:
                protocolFailed();
                break;
        }
    }

    public void setDoInput(boolean value) {
        switch (protocol) {
            case HTTP:
                http.setDoInput(value);
                break;
            case HTTPS:
                https.setDoInput(value);
                break;
            default:
                protocolFailed();
                break;
        }
    }

    public void setChunkedStreamingMode(int size) {
        switch (protocol) {
            case HTTP:
                http.setChunkedStreamingMode(size);
                break;
            case HTTPS:
                https.setChunkedStreamingMode(size);
                break;
            default:
                protocolFailed();
                break;
        }
    }

    public void setRequestProperty(String field, String value) {
        switch (protocol) {
            case HTTP:
                http.setRequestProperty(field, value);
                break;
            case HTTPS:
                https.setRequestProperty(field, value);
                break;
            default:
                protocolFailed();
                break;
        }
    }

    public OutputStream getOutputStream() throws IOException {
        switch (protocol) {
            case HTTP:
                return http.getOutputStream();
            case HTTPS:
                return https.getOutputStream();
            default:
                protocolFailed();
                return null;
        }
    }

    public int getResponseCode() throws IOException {
        switch (protocol) {
            case HTTP:
                return http.getResponseCode();
            case HTTPS:
                return https.getResponseCode();
            default:
                protocolFailed();
                return 0;
        }
    }

    public InputStream getInputStream() throws IOException {
        switch (protocol) {
            case HTTP:
                return http.getInputStream();
            case HTTPS:
                return https.getInputStream();
            default:
                protocolFailed();
                return null;
        }
    }

    public String getCipherSuite() {
        switch (protocol) {
            case HTTP:
                return "is Http";
            case HTTPS:
                return https.getCipherSuite();
            default:
                protocolFailed();
                return "";
        }
    }

    public void setReadTimeOut(int timeoutMillis){
        switch(protocol){
            case HTTP:
                http.setReadTimeout(timeoutMillis);
                break;
            case HTTPS:
                https.setReadTimeout(timeoutMillis);
                break;
            default:
                protocolFailed();
                break;
        }
    }

    public void setConnectTimeOut(int timeoutMillis){
        switch(protocol){
            case HTTP:
                http.setConnectTimeout(timeoutMillis);
                break;
            case HTTPS:
                https.setConnectTimeout(timeoutMillis);
                break;
            default:
                protocolFailed();
                break;
        }
    }

    private void initHttp(URL url, Proxy proxy) throws IOException {
        protocol = HTTP;
        if (proxy == null)
            http = (HttpURLConnection) url.openConnection();
        else
            http = (HttpURLConnection) url.openConnection(proxy);


 }

    private void initHttps(URL url, Proxy proxy) throws IOException {
        protocol = HTTPS;
        if (proxy == null)
            https = (HttpsURLConnection) url.openConnection();
        else
            https = (HttpsURLConnection) url.openConnection(proxy);
    }

    private void protocolFailed() {
        Log.d("UrlConnectionWraper", "Failed to determine protocol");
    }

}
