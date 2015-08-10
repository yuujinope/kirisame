package nope.yuuji.kirisame.network.util;

import android.content.Context;
import android.support.annotation.Nullable;

import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

/**
 * Created by Tkpd_Eka on 7/24/2015.
 */
public class VolleyNetworkRequestQueue {

    private static final int THREAD_POOL_SIZE = 8;

    private static VolleyNetworkRequestQueue instance;
    private static Context context;
    private RequestQueue requestQueue;

    private VolleyNetworkRequestQueue(Context applicationContext) {
        context = applicationContext;
        requestQueue = getRequestQueue();
    }

    public static synchronized VolleyNetworkRequestQueue getInstance(Context context) {
        if (instance == null) {
            instance = new VolleyNetworkRequestQueue(context);
        }
        return instance;
    }

    public <T> void addToRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }

    public void setProxy(String proxyAdd, int port) {
        requestQueue.stop();
        requestQueue = newRequestQueue(proxyAdd, port);
    }

    private RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = newRequestQueue(null, 0);
        }
        return requestQueue;
    }

    private RequestQueue newRequestQueue(@Nullable String proxy, int port) {
        // getApplicationContext() is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        File cacheDir = new File(context.getApplicationContext().getCacheDir(), "volley");

        HurlStack stack = getHurlStack(proxy, port);

        Network network = new BasicNetwork(stack);

        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network, THREAD_POOL_SIZE);
        queue.start();

        return queue;
    }

    private HurlStack getHurlStack(final String proxyAddress, final int port) {

        if (proxyAddress == null)
            return new HurlStack();

        return new HurlStack() {
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                Proxy proxy = new Proxy(Proxy.Type.HTTP,
                        InetSocketAddress.createUnresolved(proxyAddress, port));
                return (HttpURLConnection) url.openConnection(proxy);
            }
        };
    }
}
