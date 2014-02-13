package io.plansource.models;

import android.content.Context;
import android.os.Handler;

import io.plansource.utils.DownloadWorker;
import io.plansource.Version;
import io.plansource.helpers.D;
import io.plansource.helpers.StorageHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by Shane on 7/25/13.
 */
public class Plan {

    //Attrs
    public int id;
    public String updated_at;
    public String name;
    public int num;
    public String filename;
    public String file_url;
    public String job;

    //Raw JSON
    private String raw_data;

    public Plan(JSONObject object){
        try{
            this.name = object.getString("plan_name");
            this.num = object.getInt("plan_num");
            this.filename = object.getString("plan_file_name");
            this.file_url = object.getString("plan");
            this.job = object.getString("job_name");
            this.id = object.getInt("id");
            this.updated_at = object.getString("updated_at");
        } catch (JSONException e){
            D.out(this.getClass(), "Plan Init - " + e.getMessage());
        }
    }

    public boolean delete(Context context){
        File job = new File(StorageHelper.getRoot(context), this.job);
        File plan = new File(job, this.id + ".pdf");
        if(!plan.exists())
            return true;
        return plan.delete();
    }

    public boolean save(Context context, Handler progHandler){
        try {
            URL url = new URL(this.file_url);
            URLConnection connection = url.openConnection();
            connection.connect();
            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            File dir = new File(StorageHelper.getRoot(context), this.job);
            if(!dir.exists())
                dir.mkdirs();
            OutputStream output = new FileOutputStream(new File(dir, this.id + ".pdf"));
            D.out(Plan.class, "Downloading - " + this.name + " - " + this.job);
            D.out(Plan.class, "Relative Path - " + dir.getPath() + "/" + this.id + ".pdf");
            D.out(Plan.class, "File Size - " + fileLength);

            byte data[] = new byte[1024];
            int total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                output.write(data, 0, count);
                DownloadWorker.sendMessage(DownloadWorker.PAGE_KEY, (int) (((float) total) / fileLength * 100f) + "", progHandler);
            }
            output.flush();
            output.close();
            input.close();
            return true;
        } catch (IOException e) {
            D.err(Plan.class, e.getMessage());
            D.err(Plan.class, "Problem saving page");
            return false;
        }
    }

    public static ArrayList<Plan> decodeJSON(String json){
        try{
            D.out(Plan.class, "Decoding - " + json);
            JSONArray plansArray = new JSONArray(json);
            ArrayList<Plan> plans = new ArrayList<Plan>();
            for(int i = 0 ; i < plansArray.length() ; i ++)
                plans.add(new Plan(plansArray.getJSONObject(i)));
            return plans;
        } catch (JSONException e){
            D.out(Plan.class, "Decode JSON - " + e.getMessage());
            return null;
        }
    }
}
