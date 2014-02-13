package io.plansource.helpers;

import android.content.Context;
import android.os.Environment;
import io.plansource.views.Dialog;

import java.io.File;

/**
 * Created by Shane on 5/20/13.
 */
public class StorageHelper {

    public static boolean storageAvailable(Context context){
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        if(!(mExternalStorageAvailable && mExternalStorageWriteable))
            showNoStorageDialog(context);
        return mExternalStorageAvailable && mExternalStorageWriteable;
    }

    private static void showNoStorageDialog(Context context){
        Dialog.getStorageNotAvailableDialog(context).show();
    }

    public static boolean jobExists(Context context){
        if(!storageAvailable(context))
           return false;
        return false;
    }

    private static final String ROOT = "Jobs";

    public static File getRoot(Context context){
        if(!storageAvailable(context))
            return null;
        File dir = new File(context.getExternalFilesDir(null), ROOT);
        if(!dir.exists())
            dir.mkdirs();
        return dir;
    }
}
