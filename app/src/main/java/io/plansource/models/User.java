package io.plansource.models;

import android.content.Context;

import io.plansource.helpers.ConnectionHelper;
import io.plansource.helpers.D;
import io.plansource.helpers.StorageHelper;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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
import java.util.List;

/**
 * Created by Shane on 7/25/13.
 */
public class User {

    public static final String TOKEN_URL = "http://plansource.io/api/token";
    private static final String FILE_NAME = "token.txt";

    public String email;
    public String password;
    public String token;

    private String rawJSON;

    public User(String email, String password, String token){
        this.email = email;
        this.password = password;
        this.token = token;
    }

    public User(String email, String password){
        this(email, password, null);
    }

    public User(String token){
        this(null, null, token);
    }

    public boolean save(Context context){
        if(!StorageHelper.storageAvailable(context))
            return false;
        File file = new File(StorageHelper.getRoot(context), FILE_NAME);

        try{
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            writer.write(this.rawJSON);
            writer.close();
            D.out(this.getClass(), "Saved!");
            return true;
        } catch (IOException e){
            D.err(this.getClass(), e.getMessage());
            D.err(this.getClass(), "Failed to save...");
            return false;
        }
    }

    public boolean login(Context context){
        if(!ConnectionHelper.checkConnection(context))
            return false;

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(User.TOKEN_URL);

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("email", this.email));
            nameValuePairs.add(new BasicNameValuePair("password", this.password));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

            ResponseHandler<String> handler = new BasicResponseHandler();
            JSONObject body = new JSONObject(handler.handleResponse(response));
            if(body.has("token")){
                D.out(User.class, "Logged in successful!");
                this.rawJSON = body.toString();
                this.token = body.getString("token");
                this.save(context);
                return true;
            }else{
                D.out(User.class, "Login failed");
                if(body.has("message"))
                    D.out(this.getClass(), body.getString("message"));
                return false;
            }
        } catch (Exception e) {
            D.err(this.getClass(), e.getMessage());
            return false;
        }
    }

    public static User getStoredUser(Context context){
        if(!User.exists(context))
            return null;
        File file = new File(StorageHelper.getRoot(context), FILE_NAME);
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            JSONObject json = new JSONObject(reader.readLine());
            reader.close();
            return new User(json.getString("token"));
        } catch (Exception e){
            D.err(User.class, e.getMessage());
            return null;
        }
    }

    public static boolean delete(Context context){
        if(!User.exists(context))
            return true;
        File file = new File(StorageHelper.getRoot(context), FILE_NAME);
        if(file.delete()){
            D.out(User.class, "Deleted");
            return true;
        }else{
            D.err(User.class, "Not Deleted");
            return false;
        }
    }

    public static boolean exists(Context context){
        if(!StorageHelper.storageAvailable(context))
            return false;
        File file = new File(StorageHelper.getRoot(context), FILE_NAME);
        return file.exists() && file.isFile();
    }
}