package io.plansource.controllers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import io.plansource.utils.DownloadWorker;
import io.plansource.R;
import io.plansource.helpers.D;
import io.plansource.helpers.StorageHelper;
import io.plansource.models.Job;
import io.plansource.models.Plan;
import io.plansource.models.User;
import io.plansource.utils.FetchPageSizesTask;
import io.plansource.views.Dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private static final String JOB_KEY = "job";

    String dir = null;
    File file;
    ArrayList<String> fileNames;
    ArrayList<Job> jobs;

    public MainActivity(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setIcon(R.drawable.app_icon);
        if(!StorageHelper.storageAvailable(this))
            return;

        jobs = Job.getPrevious(this);

        LinearLayout container = (LinearLayout) findViewById(R.id.content);

        this.dir = getIntent().getStringExtra(JOB_KEY);

        if(dir == null || dir.equals("")){
            getSupportActionBar().setTitle("Your Jobs");
            file = StorageHelper.getRoot(this);
            fileNames = this.getDirs(file);
        }else{
            getSupportActionBar().setTitle(dir);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            file = new File(StorageHelper.getRoot(this), dir);
            fileNames = this.getPages(file);
        }

        ListView listView = new ListView(this);
        listView.setDivider(new ColorDrawable(Color.parseColor("#aaaaaa")));
        listView.setDividerHeight(1);
        listView.setPadding(10, 0, 10, 0);
        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.custom_item, R.id.item_text, fileNames));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView tv = (TextView) view.findViewById(R.id.item_text);
                String text = tv.getText().toString();
                if(dir == null || dir == "")
                    MainActivity.this.openJob(text);
                else
                    MainActivity.this.openPage(text);
            }
        });

        container.addView(listView);
    }

    private void openJob(String job){
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra(JOB_KEY, job);
        startActivity(i);
    }

    private void openPage(String planName){
        String filename = "";
        for(Plan plan : Job.allPlans(Job.getPrevious(this))){
            if(plan.job.equals(this.dir) && plan.name.equals(planName)){
                filename = plan.id + ".pdf";
                break;
            }
        }
        if(filename == ""){
            //Show No file found
            System.out.println("MainActivity: No file found to open");
            Dialog.getNoFileFoundDialog(this).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(file, filename)),"application/pdf");
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
        if (activities.size() > 0) {
            D.out(MainActivity.class, "Opening filename - " + filename);
            startActivity(intent);
        } else {
            D.err(MainActivity.class, "No PDF Viewer installed");
            Dialog.getNoPDFViewerDialog(this).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(dir == null || dir.equals(""))
            getMenuInflater().inflate(R.menu.main, menu);
        else
            ;//getMenuInflater().inflate(R.menu.plan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
            break;
            case R.id.sync:
                this.sync();
            break;
            case R.id.print:
                this.openPrintDialog();
            break;
            case R.id.about:
                Dialog.getAboutDialog(this).show();
            break;
        }

        return true;
    }

    private void openPrintDialog(){
        String[] items = new String[fileNames.size()];
        for(int i = 0 ; i < items.length ; i ++) items[i] = fileNames.get(i);
        final ArrayList<String> selectedItems = new ArrayList<String>();  // Where we track the selected items
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle("Select plans to print.")
        // Specify the list array, the items to be selected by default (null for none),
        // and the listener through which to receive callbacks when items are selected
        .setMultiChoiceItems(items, null,
            new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    if (isChecked) {
                       selectedItems.add(fileNames.get(which));
                    } else if (selectedItems.contains(fileNames.get(which))) {
                        selectedItems.remove(fileNames.get(which));
                    }
                }
        })
       .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int id) {
               if(selectedItems.size() < 1)
                   return;
               ArrayList<Plan> previous = Job.allPlans(Job.getPrevious(MainActivity.this));
               ArrayList<Plan> selected = new ArrayList<Plan>();
               for (String s : selectedItems) {
                   for (Plan plan : previous) {
                       if (plan.job.equals(dir) && plan.name.equals(s)) {
                           selected.add(plan);
                           break;
                       }
                   }
               }
               new FetchPageSizesTask(MainActivity.this, selected).execute();
           }
       })
       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int id) {
           }
       }).show();
    }

    private void openCardDialog(){

    }

    private void sync(){
        User user = User.getStoredUser(this);
        if(user == null){
            Dialog.getLoginDialog(this).show();
            return;
        }
        DownloadWorker worker = new DownloadWorker(this, user);
        worker.start();
    }

    private ArrayList<String> getDirs(File dir){
        if(dir == null)
            return new ArrayList<String>();
        if(!dir.exists() || !dir.isDirectory())
            return new ArrayList<String>();

        File[] files = dir.listFiles();
        ArrayList<String> dirs = new ArrayList<String>();
        for(File f : files){
            if(f.isDirectory())
                dirs.add(f.getName());
        }
        System.out.println("MainActivity: Fetched dirs");
        Collections.sort(dirs);
        return dirs;
    }

    private ArrayList<String> getPages(File dir){
        if(dir == null)
            return new ArrayList<String>();
        if(!dir.exists() || !dir.isDirectory())
            return new ArrayList<String>();

        String job = dir.getName();

        ArrayList<Plan> plans = Job.allPlans(Job.getPrevious(this));

        File[] files = dir.listFiles();
        String[] pgs = new String[plans.size()];
        for(File f : files){
            String[] p = f.getName().split("\\.");
            if(f.isFile() && p[p.length - 1].toLowerCase().equals("pdf")){
                for(Plan plan : plans){
                    if(plan.id == Integer.parseInt(p[p.length - 2]))
                        pgs[plan.num - 1] = plan.name;
                }
            }
        }

        ArrayList<String> t = new ArrayList<String>();
        for(String s : pgs){
            if(s != null)
                t.add(s);
        }
        D.out(MainActivity.class, "Fetched pages");
        return t;
    }
}
