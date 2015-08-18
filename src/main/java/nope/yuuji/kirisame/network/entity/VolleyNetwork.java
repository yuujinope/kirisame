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

import java.util.HashMap;
import java.util.Map;

import nope.yuuji.kirisame.Kirisame;
import nope.yuuji.kirisame.network.util.VolleyNetworkRequestQueue;

/**
 * Created by Tkpd_Eka on 7/23/2015.
 */
public abstract class VolleyNetwork {
    public interface OnRequestErrorListener {
        void onErrorTimeout(VolleyError volleyError);

        void onErrorNoConnection(VolleyError volleyError);

        void onErrorParse(VolleyError volleyError);

        void onErrorAuthFailure(VolleyError volleyError);

        void onErrorServerError(VolleyError volleyError);

        void onRequestErrorDefault(VolleyError volleyError);
    }

    public class PostStringRequest extends StringRequest {

        Map<String, String> param = new HashMap<>();

        public PostStringRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
            super(method, url, listener, errorListener);
        }

        public void setParam(Map<String, String> param) {
            this.param = param;
        }

        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            return param;
        }
    }

    public static final int ERROR_TIMEOUT = 1;
    public static final int ERROR_NO_CONNECTION = 2;
    public static final int ERROR_SERVER_ERROR = 3;
    public static final int ERROR_AUTH_FAILURE = 4;
    public static final int ERROR_PARSE = 5;

    public static final int DEFAULT_TIMEOUT = 10000;
    public static final int DEFAULT_RETRY_COUNT = 2;

    protected Context context;
    protected String url;
    protected Map<String, String> param = new HashMap<>();
    private OnRequestErrorListener onRequestErrorListener;
    private PostStringRequest request;

    private int retryTimeout = DEFAULT_TIMEOUT;
    private int retryMaxCount = DEFAULT_RETRY_COUNT;

    public VolleyNetwork(Context context, String url) {
        this.url = url;
        this.context = context;
        onRequestErrorListener = getDefaultOnRequestErrorListener();
    }

    public abstract void onRequestResponse(String response);

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

    /**
     * @return Override this class to set default RequestError listener on child
     */
    protected OnRequestErrorListener getDefaultOnRequestErrorListener() {
        return null;
    }

    public void commit() {
        request = new PostStringRequest(Request.Method.POST, url, onRequestListener(), onRequestErrorListener());
        request.setParam(param);
        request.setRetryPolicy(getRetryPolicy());
        VolleyNetworkRequestQueue.getInstance(context).addToRequestQueue(request);
    }

    public final void restartRequest() {
        VolleyNetworkRequestQueue.getInstance(context).addToRequestQueue(request);
    }

    public final void addParam(String key, String value) {
        param.put(key, value);
    }

    public final void setOnRequestErrorListener(OnRequestErrorListener listener) {
        onRequestErrorListener = listener;
    }

    public final void setRetryPolicy(int timeout, int maxCount) {
        retryTimeout = timeout;
        retryMaxCount = maxCount;
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
                onRequestError(volleyError);
            }
        };
    }

    private void onRequestError(VolleyError volleyError) {
        if (onRequestErrorListener == null) {
            return;
        }

        switch (getErrorInstance(volleyError)) {
            case ERROR_NO_CONNECTION:
                onRequestErrorListener.onErrorNoConnection(volleyError);
                break;
            case ERROR_TIMEOUT:
                onRequestErrorListener.onErrorTimeout(volleyError);
                break;
            case ERROR_SERVER_ERROR:
                onRequestErrorListener.onErrorServerError(volleyError);
                break;
            case ERROR_AUTH_FAILURE:
                onRequestErrorListener.onErrorAuthFailure(volleyError);
                break;
            case ERROR_PARSE:
                onRequestErrorListener.onErrorParse(volleyError);
                break;
            default:
                onRequestErrorListener.onRequestErrorDefault(volleyError);
                break;
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
