
package vn.edu.ueh.ngocha.squiditytempprj.Monitor;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

public class NetworkMonitor {

    public interface NetworkStatusListener {
        void onNetworkStatusChanged(boolean isConnected);
    }

    private final ConnectivityManager connectivityManager;
    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    private final NetworkStatusListener listener;

    private ConnectivityManager.NetworkCallback callback;
    private boolean isStarted = false;
    private Boolean lastStatus = null;

    public NetworkMonitor(
            Context context,
            NetworkStatusListener listener
    ) {
        connectivityManager =
                (ConnectivityManager) context.getApplicationContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

        this.listener = listener;
    }

    private boolean isInternetAvailable() {
        if (connectivityManager == null) {
            return false;
        }

        Network network = connectivityManager.getActiveNetwork();

        if (network == null) {
            return false;
        }

        NetworkCapabilities capabilities =
                connectivityManager.getNetworkCapabilities(network);

        return capabilities != null
                && capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    private void updateStatus(boolean connected) {
        if (lastStatus == null || lastStatus != connected) {
            lastStatus = connected;

            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onNetworkStatusChanged(connected);
                    }
                }
            });
        }
    }

    public void start() {
        if (isStarted || connectivityManager == null) {
            return;
        }

        isStarted = true;

        // Kiểm tra trạng thái mạng hiện tại
        updateStatus(isInternetAvailable());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            callback = new ConnectivityManager.NetworkCallback() {

                @Override
                public void onAvailable(Network network) {
                    updateStatus(isInternetAvailable());
                }

                @Override
                public void onCapabilitiesChanged(
                        Network network,
                        NetworkCapabilities capabilities
                ) {
                    boolean connected =
                            capabilities.hasCapability(
                                    NetworkCapabilities.NET_CAPABILITY_INTERNET)
                                    && capabilities.hasCapability(
                                    NetworkCapabilities.NET_CAPABILITY_VALIDATED);

                    updateStatus(connected);
                }

                @Override
                public void onLost(Network network) {
                    updateStatus(isInternetAvailable());
                }
            };

            try {
                connectivityManager.registerDefaultNetworkCallback(callback);
            } catch (RuntimeException e) {
                callback = null;
                isStarted = false;
            }
        }
    }

    public void stop() {
        if (callback != null && connectivityManager != null) {
            try {
                connectivityManager.unregisterNetworkCallback(callback);
            } catch (IllegalArgumentException ignored) {
                // Callback đã được hủy đăng ký
            }

            callback = null;
        }

        isStarted = false;
    }
}
