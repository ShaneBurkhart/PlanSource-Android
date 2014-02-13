package io.plansource.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.stripe.android.model.Token;
import io.plansource.Version;
import io.plansource.models.User;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Shane on 8/25/13.
 */
public class SendStripeTokenToServerTask extends AsyncTask<Token, Void, Boolean>{
    private static final String CHARGE_ROUTE_SUFFIX = "/api/charge.json";
    private static final String CHARGE_URL = Version.BASE_URL + CHARGE_ROUTE_SUFFIX;
    private static final String STRIPE_TOKEN_KEY = "stripeToken";
    private static final String USER_TOKEN_KEY = "token";
    private static final String PLANS_TOKEN = "plan_ids";

    private int[] planIDs;
    private Context context;
    private ProgressDialog dialog;

    public SendStripeTokenToServerTask(int[] planIDs, Context context, ProgressDialog dialog){
        this.planIDs = planIDs;
        this.context = context;
        this.dialog = dialog;
    }
    @Override
    protected Boolean doInBackground(Token... tokens) {
        Token token = null;
        if(tokens.length > 0)
            token = tokens[0];
        if(token == null)
            return false;
        JSONObject object = new JSONObject();
        try {
            User user = User.getStoredUser(context);
            if(user.token == null)
                return false;
            JSONArray array = new JSONArray();
            for(int i = 0 ; i < planIDs.length ; i ++)
                array.put(planIDs[i]);
            object.put(STRIPE_TOKEN_KEY, token.getId());
            object.put(PLANS_TOKEN, array);
            object.put(USER_TOKEN_KEY, user.token);
            HttpResponse response = getResponse(object, CHARGE_URL);
            String r = getResponseBodyString(response);
            Log.d("Stripe JSON", object.toString());
            Log.d("Stripe Token", token.getId());
            Log.d("Stripe Server URL", CHARGE_URL);
            Log.d("Stripe Response", r);
            Log.d("Stripe Response Code", response.getStatusLine().getStatusCode() + "");
            if(response.getStatusLine().getStatusCode() == 200)
                return true;
            else
                return false;
        } catch (JSONException e){
            return false;
        }
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onPostExecute(Boolean succeeded) {
        this.dialog.dismiss();
        if(succeeded)
            Toast.makeText(context, "Your plans have been ordered.", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context, "Failed to order your plans.  Try again later.", Toast.LENGTH_LONG).show();
    }

    private HttpResponse getResponse(JSONObject params, String url){
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        HttpResponse response = null;
        try {
            httppost.setHeader("Content-Type", "application/json");
            httppost.setEntity(new StringEntity(params.toString()));
            // Execute HTTP Post Request
            response = httpclient.execute(httppost);
        }catch (IOException e){
            Log.d("Register Response", e.getMessage());
        }
        return response;
    }

    public static String getResponseBodyString(HttpResponse response){
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = null;
            while ((line = reader.readLine()) != null)
                sb.append(line);
            return sb.toString();
        }catch (IOException e) {
            Log.d("Register Response String", e.getMessage());
        }catch (Exception e) {
            Log.d("Register Response String", e.getMessage());
        }
        return null;
    }


}
