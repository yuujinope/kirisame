package nope.yuuji.kirisame.network.entity;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import nope.yuuji.kirisame.network.util.VolleyNetworkRequestQueue;

/**
 * Created by Tkpd_Eka on 7/23/2015.
 * Ver 1.2.0
 */
public abstract class VolleyNetwork {

    public class PostStringRequest extends StringRequest {

        Map<String, String> param = new HashMap<>();
        Map<String, String> header = new HashMap<>();

        public PostStringRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
            super(method, url, listener, errorListener);
        }

        public void setParam(Map<String, String> param) {
            this.param = param;
        }

        public void setHeader(Map<String, String> header) {
            this.header = header;
        }

        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            return param;
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            return header;
        }

        @Override
        public String getBodyContentType() {
            return null;
        }
    }

    public static final int ERROR_TIMEOUT = 1;
    public static final int ERROR_NO_CONNECTION = 2;
    public static final int ERROR_SERVER_ERROR = 3;
    public static final int ERROR_AUTH_FAILURE = 4;
    public static final int ERROR_PARSE = 5;

    public static final int DEFAULT_TIMEOUT = 10000;
    public static final int DEFAULT_RETRY_COUNT = 2;

    public static final int METHOD_GET = Request.Method.GET;
    public static final int METHOD_POST = Request.Method.POST;
    public static final int METHOD_PUT = Request.Method.PUT;

    protected Context context;
    protected String url;
    protected Map<String, String> param = new HashMap<>();
    protected Map<String, String> header = new HashMap<>();
    protected Map<String, String> urlQuery = new HashMap<>();
    private PostStringRequest request;

    private int retryTimeout = DEFAULT_TIMEOUT;
    private int retryMaxCount = DEFAULT_RETRY_COUNT;
    private int method = METHOD_GET;

    public VolleyNetwork(Context context, String url) {
        this.url = url;
        this.context = context;
    }

    public abstract void onRequestResponse(String response);

    public abstract void onRequestError(NetError e, int responseCode);

    /**
     * Override this class to add event on network retry
     */
    @SuppressWarnings("unused")
    protected void onNetworkRetrying(VolleyError volleyError) throws VolleyError {
    }

    /**
     * Override this class to add event on network stop retrying
     */

    @SuppressWarnings("unused")
    protected void onNetworkRetryingStop(VolleyError volleyNetwork) throws VolleyError {
    }

    public void commit() {
        request = new PostStringRequest(method, getUrlWithQuery(), onRequestListener(), onRequestErrorListener());
        request.setHeader(header);
        request.setParam(param);
        request.setRetryPolicy(getRetryPolicy());
        VolleyNetworkRequestQueue.getInstance(context).addToRequestQueue(request);
    }

    public final void killConnection() {
        request.cancel();
    }

    public final void retryRequest() {
        VolleyNetworkRequestQueue.getInstance(context).addToRequestQueue(request);
    }

    public final void addParam(String key, String value) {
        param.put(key, value);
    }

    public final void addHeader(String key, String value) {
        header.put(key, value);
    }

    /**
     * Used in adding URL Query for GET Methods
     */
    public final void addQuery(String key, String name) {
        urlQuery.put(key, name);
    }

    public final void setRetryPolicy(int timeout, int maxCount) {
        retryTimeout = timeout;
        retryMaxCount = maxCount;
    }

    public final void setMethod(int method) {
        this.method = method;
    }

    private Response.Listener<String> onRequestListener() {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                onRequestResponse(s);
            }
        };
    }

    protected RetryPolicy getRetryPolicy() {
        return new RetryPolicy() {

            int retry;

            @Override
            public int getCurrentTimeout() {
                return retryTimeout;
            }

            @Override
            public int getCurrentRetryCount() {
                return retry;
            }

            @Override
            public void retry(VolleyError volleyError) throws VolleyError {
                onNetworkRetrying(volleyError);
                retry++;
                if (retry >= retryMaxCount) {
                    onNetworkRetryingStop(volleyError);
                    throw volleyError;
                }
            }
        };
    }

    private Response.ErrorListener onRequestErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                onRequestError(onResponseRequestError(volleyError)
                        , volleyError.networkResponse.statusCode);
            }
        };
    }

    protected NetError onResponseRequestError(VolleyError volleyError) {
        switch (getErrorInstance(volleyError)) {
            case ERROR_NO_CONNECTION:
                return NetError.NO_CONNECTION;
            case ERROR_TIMEOUT:
                return NetError.TIMEOUT;
            case ERROR_SERVER_ERROR:
                return NetError.SERVER_ERROR;
            case ERROR_AUTH_FAILURE:
                return NetError.AUTH_FAILURE;
            case ERROR_PARSE:
                return NetError.PARSE_ERROR;
            default:
                return NetError.UNKNOWN;
        }
    }

    protected String getUrlWithQuery() {
        if (urlQuery.size() == 0)
            return url;

        StringBuilder queries = new StringBuilder();
        for (Map.Entry<String, String> entry : urlQuery.entrySet()) {
            queries.append(String.format("%s=%s&", urlEncoder(entry.getKey()), urlEncoder(entry.getValue())));
        }
        return url + queries.substring(0, queries.length() - 1);
    }

    private String urlEncoder(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    protected final int getErrorInstance(VolleyError error) {
        if (error instanceof NoConnectionError) {
            return ERROR_NO_CONNECTION;
        } else if (error instanceof TimeoutError) {
            return ERROR_TIMEOUT;
        } else if (error instanceof ServerError) {
            return ERROR_SERVER_ERROR;
        } else if (error instanceof AuthFailureError) {
            return ERROR_AUTH_FAILURE;
        } else if (error instanceof ParseError) {
            return ERROR_PARSE;
        }
        return 0;
    }

}
