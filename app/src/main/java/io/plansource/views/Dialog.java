package io.plansource.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.plansource.utils.DownloadWorker;
import io.plansource.Version;
import io.plansource.models.User;

/**
 * Created by Shane on 5/22/13.
 */
public class Dialog {

    private static final String ADOBE_PDF_VIEWER_PACKAGE = "com.adobe.reader";

    private static final String ABOUT_TEXT = "This app allows users to download and views building plans.\n\n" +
            "Note: It is recommended to use Adobe Reader for viewing PDFs." +
            "\n\nFor more information contact us.\nCreator: Shane Burkhart\n" +
            "Email:shaneburkhart@gmail.com\n\nVersion: " + Version.VERSION;

    public static AlertDialog.Builder getAboutDialog(Context context){
        return new AlertDialog.Builder(context)
                .setTitle("About")
                .setMessage(ABOUT_TEXT)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.dismiss();
                    }
                })
                .setCancelable(false);
    }

    public static AlertDialog.Builder getNoPDFViewerDialog(final Context context){
        return new AlertDialog.Builder(context).
                setTitle("No PDF Viewer").
                setMessage("You need to install a PDF viewer to views PDF's.").
                setCancelable(false).
                setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + ADOBE_PDF_VIEWER_PACKAGE)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + ADOBE_PDF_VIEWER_PACKAGE)));
                        }
                        dialogInterface.dismiss();
                    }
                });
    }

    public static AlertDialog.Builder getNoFileFoundDialog(final Context context){
        return new AlertDialog.Builder(context).
                setTitle("No File Found").
                setMessage("No file found.  Ask for us to update the file that you are having trouble with.").
                setCancelable(false).
                setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
    }

    public static AlertDialog.Builder getStorageNotAvailableDialog(final Context context){
        return new AlertDialog.Builder(context).
                setTitle("StorageHelper Not Available").
                setMessage("The file system is not available.  Usually this happens when your device is plugged in.  Please unplug your device to use this app.").
                setCancelable(false).
                setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((Activity) context).finish();
                    }
                });
    }

    public static AlertDialog.Builder getNotLoggedInDialog(final Context context){
        return new AlertDialog.Builder(context).
                setTitle("You Aren't Logged In!").
                setMessage("Sync your files to login").
                setCancelable(false).
                setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((Activity) context).finish();
                    }
                });
    }

    public static AlertDialog.Builder getDebugDialog(String msg, final Context context){
        return new AlertDialog.Builder(context).
                setTitle("Debugger").
                setMessage("You shouldn't be seeing this.\n\n" + msg).
                setCancelable(false).
                setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
    }

    public static AlertDialog.Builder getLoginDialog(final Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);
        final EditText email = new EditText(context);
        final EditText password = new EditText(context);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        TextView tvu = new TextView(context);
        tvu.setText("Email");
        tvu.setTextSize(20);
        TextView tvp = new TextView(context);
        tvp.setText("Password");
        tvp.setTextSize(20);
        layout.addView(tvu);
        layout.addView(email);
        layout.addView(tvp);
        layout.addView(password);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        email.setLayoutParams(params);
        password.setLayoutParams(params);
        builder.setView(layout)
                .setCancelable(false)
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String u = email.getText().toString().toLowerCase();
                        String p = password.getText().toString();
                        DownloadWorker worker = new DownloadWorker(context, new User(u, p));
                        worker.start();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        return builder;
    }
}
