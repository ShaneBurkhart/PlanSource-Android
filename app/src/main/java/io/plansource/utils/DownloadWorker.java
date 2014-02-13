package io.plansource.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;

import io.plansource.controllers.MainActivity;
import io.plansource.helpers.D;
import io.plansource.helpers.DeltaHelper;
import io.plansource.helpers.StorageHelper;
import io.plansource.models.Job;
import io.plansource.models.Plan;
import io.plansource.models.User;
import io.plansource.views.ShaneProgressDialog;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Shane on 5/20/13.
 */
public class DownloadWorker {
    public static final String OVERALL_KEY = "overall";
    public static final String TOTAL_KEY = "total";
    public static final String PAGE_KEY = "page";
    public static final String MESSAGE_KEY = "message";

    private Context context;
    private User user;
    private Worker worker;
    private ShaneProgressDialog dialog;
    private ProgressCallbackHandler progHandler;
    private InvalidLoginHandler loginHandler;
    private boolean loginSuccess = false;

    public DownloadWorker(Context context, User user){
        this.context = context;
        this.user = user;
        this.progHandler = new ProgressCallbackHandler();
        this.loginHandler = new InvalidLoginHandler();
        this.dialog = new ShaneProgressDialog();
        this.worker = new Worker();
        this.addWork();
    }

    private void addWork(){
        worker.addWork(new Runnable() {
            @Override
            public void run() {
                loginSuccess = false;
                DownloadWorker.sendMessage(MESSAGE_KEY, "Checking Credentials", DownloadWorker.this.progHandler);
                D.out(DownloadWorker.class, user.token);
                if(user.token == null && !user.login(DownloadWorker.this.context)){
                    User.delete(DownloadWorker.this.context);
                    loginHandler.sendEmptyMessage(0);
                    return;
                }
                ArrayList<Job> jobs = Job.findAll(user);
                if(jobs == null){
                    D.out(DownloadWorker.class, "An error occured when getting jobs");
                    return;
                }
                D.out(DownloadWorker.class, "Retrieved " + jobs.size() + " jobs");

                ArrayList<Job> prevJobs = Job.getPrevious(DownloadWorker.this.context);

                loginSuccess = true;

                if(!StorageHelper.storageAvailable(DownloadWorker.this.context))
                    return;
                ArrayList[] delta = DeltaHelper.determineDelta(jobs, prevJobs);
                this.download(delta);
                this.delete(delta);
                this.deleteEmptyDirectories();
                Job.save(DownloadWorker.this.context, jobs);
            }

            private void download(ArrayList[] delta){
                if(delta[0] == null)
                    return;
                ArrayList<Plan> toAdd = delta[0];
                DownloadWorker.sendMessage(TOTAL_KEY, toAdd.size() + "", DownloadWorker.this.progHandler);
                DownloadWorker.sendMessage(OVERALL_KEY, "0", DownloadWorker.this.progHandler);
                for(int i = 0 ; i < toAdd.size() ; i ++){
                    DownloadWorker.sendMessage(MESSAGE_KEY, "Downloading: " + toAdd.get(i).name, DownloadWorker.this.progHandler);
                    toAdd.get(i).save(DownloadWorker.this.context, progHandler);
                    DownloadWorker.sendMessage(OVERALL_KEY, (i + 1) + "", DownloadWorker.this.progHandler);
                }
            }

            private void delete(ArrayList[] delta){
                if(delta[1] == null)
                    return;
                DownloadWorker.sendMessage(MESSAGE_KEY, "Deleting DeltaHelper", DownloadWorker.this.progHandler);
                Plan plan;
                for(int i = 0 ; i < delta[1].size() ; i ++){
                    plan = (Plan) delta[1].get(i);
                    if(plan.delete(DownloadWorker.this.context))
                        D.out(DownloadWorker.class, plan.name + " Delete");
                    else
                        D.err(DownloadWorker.class, plan.name + " Not delete");
                }
            }

            private void deleteEmptyDirectories(){
                if(!StorageHelper.storageAvailable(DownloadWorker.this.context))
                    return;
                File[] files = StorageHelper.getRoot(DownloadWorker.this.context).listFiles();
                for(File f : files){
                    if(!f.isDirectory())
                        continue;
                    if(f.listFiles().length == 0)
                        f.delete();
                }
            }
        });

        worker.addWork(new Runnable() {
            @Override
            public void run() {
                DownloadWorker.sendMessage(MESSAGE_KEY, "hide", DownloadWorker.this.progHandler);
            }
        });

        worker.addWork(new Runnable() {
            @Override
            public void run() {
                if(!loginSuccess)
                    return;
                Intent i = new Intent(DownloadWorker.this.context, MainActivity.class);
                DownloadWorker.this.context.startActivity(i);
                ((Activity) DownloadWorker.this.context).finish();
            }
        });
    }

    public void start(){
        this.dialog.show(((FragmentActivity) DownloadWorker.this.context).getSupportFragmentManager(), "loading");
        this.worker.start();
    }

    private class InvalidLoginHandler extends Handler{
        AlertDialog.Builder builder;

        public InvalidLoginHandler(){
            this.builder = new AlertDialog.Builder(DownloadWorker.this.context);
            builder.setTitle("Invalid Login").
                    setMessage("Invalid email or password").
                    setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            builder.show();
        }
    }

    private class ProgressCallbackHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String message = msg.getData().getString(MESSAGE_KEY);
            String overall = msg.getData().getString(OVERALL_KEY);
            String page = msg.getData().getString(PAGE_KEY);
            String total= msg.getData().getString(TOTAL_KEY);
            if(message != null){
                if(message == "hide")
                    DownloadWorker.this.dialog.dismiss();
                else
                    DownloadWorker.this.dialog.setMessage("Downloading: " + message);
            }
            if(overall != null && DownloadWorker.this.isInteger(overall))
                DownloadWorker.this.dialog.setOverallProgress(Integer.parseInt(overall));
            if(page != null && DownloadWorker.this.isInteger(page))
                DownloadWorker.this.dialog.setPageProgress(Integer.parseInt(page));
            if(total != null && DownloadWorker.this.isInteger(total))
                DownloadWorker.this.dialog.setOverallTotal(Integer.parseInt(total));
        }
    }

    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static void sendMessage(String key, String value, Handler handler){
        Bundle bund = new Bundle();
        bund.putString(key, value);
        Message msg = new Message();
        msg.setData(bund);
        handler.sendMessage(msg);
    }
}
