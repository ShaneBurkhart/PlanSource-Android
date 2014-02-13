package io.plansource.models;

import android.content.Context;

import io.plansource.helpers.D;
import io.plansource.helpers.StorageHelper;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by Shane on 7/25/13.
 */
public class Job {

    public static final String JOB_URL = "http://plansource.io/api/jobs";
    public static final String FILE_NAME = "prev.txt";

    //Attributes
    public int id;
    public String name;
    public ArrayList<Plan> plans;

    //Data
    private String raw_data;

    public String getRawJSON(){
        return raw_data;
    }

    public Job(JSONObject object){
        try{
            this.raw_data = object.toString();
            this.plans = Plan.decodeJSON(object.getString("plans"));
            this.name = object.getString("name");
            this.id = object.getInt("id");
        } catch (JSONException e){
            D.err(this.getClass(), "Job Init - " + e.getMessage());
        }
    }

    public JSONObject toJSONObject() throws JSONException{
        return new JSONObject(raw_data);
    }

    public static ArrayList<Job> decodeJSON(String json){
        try{
            D.out(Job.class, "Decoding - " + json);
            JSONArray jobsArray = new JSONObject(json).getJSONArray("jobs");
            ArrayList<Job> jobs = new ArrayList<Job>();
            for(int i = 0 ; i < jobsArray.length() ; i ++)
                jobs.add(new Job(jobsArray.getJSONObject(i)));
            return jobs;
        } catch (JSONException e){
            D.err(Job.class, "Decode JSON - " + e.getMessage());
            return null;
        }
    }

    public static ArrayList<Job> findAll(User user){
        if(user.token == null)
            return null;

        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(Job.JOB_URL + "?token=" + user.token);

        try {
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httpGet);

            ResponseHandler<String> handler = new BasicResponseHandler();
            JSONObject body = new JSONObject(handler.handleResponse(response));

            if(body.has("jobs")){
                D.out(Job.class, "Successfully retrieved data");
                return Job.decodeJSON(body.toString());
            }else{
                D.out(Job.class, "Failed to retrieve data");
                return null;
            }
        } catch (Exception e) {
            D.err(Job.class, e.getMessage());
            return null;
        }
    }

    public static ArrayList<Job> getPrevious(Context context){
        if(!StorageHelper.storageAvailable(context))
            return null;

        File f = new File(StorageHelper.getRoot(context), FILE_NAME);
        if(!f.exists()){
            D.out(Job.class, "No previous data");
            return null;
        }
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            String line = reader.readLine();
            D.out(Job.class, "Previous Data: " + line);
            return Job.decodeJSON(line);
        }catch (IOException e){
            D.err(Job.class, "Error retrieving data");
            return null;
        }
    }

    public static boolean save(Context context, ArrayList<Job> jobs){
        if(!StorageHelper.storageAvailable(context))
            return false;

        try{
            JSONObject root = new JSONObject();
            JSONArray jobsArray = new JSONArray();
            for(Job job : jobs)
                jobsArray.put(job.toJSONObject());
            root.put("jobs", jobsArray);

            File file = new File(StorageHelper.getRoot(context), FILE_NAME);

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            writer.write(root.toString());
            writer.close();
            D.out(Job.class, "Saved jobs");
            return true;
        } catch(JSONException e){
            D.err(Job.class, e.getMessage());
            D.err(Job.class, "JSON - Could not save jobs");
            return false;
        } catch (IOException e){
            D.err(Job.class, e.getMessage());
            D.err(Job.class, "IO - Could not save jobs");
            return false;
        }
    }

    public static ArrayList<Plan> allPlans(ArrayList<Job> jobs){
        if(jobs == null)
            return null;
        ArrayList<Plan> plans = new ArrayList<Plan>();
        for(Job job : jobs){
            for(Plan plan : job.plans)
                plans.add(plan);
        }
        return plans;
    }
}
