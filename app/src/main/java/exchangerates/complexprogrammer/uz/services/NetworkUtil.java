package exchangerates.complexprogrammer.uz.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {
    public static boolean getConnectivityStatusString(Context context) {
        String status = "No internet is available";
        boolean internet_yes=false;
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                status = "Wifi enabled";
                internet_yes=true;
                return internet_yes;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                status = "Mobile ResponseData enabled";
                internet_yes=true;
                return internet_yes;
            }
        } else {
            internet_yes=false;
            status = "No internet is available";
            return internet_yes;
        }
        return internet_yes;
    }
}
