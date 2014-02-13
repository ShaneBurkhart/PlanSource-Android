package io.plansource.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Shane on 5/20/13.
 */
public class ConnectionHelper {

    public static boolean checkConnection(Context ctx){
        ConnectivityManager conMgr =  (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo i = conMgr.getActiveNetworkInfo();
        if (i == null){
            showNoConnectionDialog(ctx);
            return false;
        }
        if (!i.isConnected()){
            showNoConnectionDialog(ctx);
            return false;
        }
        if (!i.isAvailable()){
            showNoConnectionDialog(ctx);
            return false;
        }
        return true;
    }

    public static void showNoConnectionDialog(Context context){
        System.out.println("No ConnectionHelper");
    }
}
