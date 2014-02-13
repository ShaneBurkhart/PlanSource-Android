package io.plansource.utils;

import android.support.v4.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import io.plansource.Version;
import io.plansource.models.Plan;
import io.plansource.models.User;
import io.plansource.views.Dialog;
import io.plansource.views.fragments.dialog.CardDialogFragment;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Shane on 8/20/13.
 */
public class FetchPageSizesTask extends AsyncTask<Void, Void, String> {

    public static final String BASE_URL = Version.BASE_URL + "/api/page_sizes/";
    private static final String USER_TOKEN_KEY = "token";
    private static final String PLAN_IDS_KEY = "plan_ids";

    ArrayList<Plan> selected;
    Context context;
    ProgressDialog dialog;
    int[] planIDs;

    public FetchPageSizesTask(Context context, ArrayList<Plan> selected){
        this.selected = selected;
        this.context = context;
        this.dialog = new ProgressDialog(context);
        planIDs = new int[selected.size()];
        for(int i = 0 ; i < selected.size() ; i ++)
            planIDs[i] = selected.get(i).id;
    }

    @Override
    protected void onPreExecute() {
        dialog.setCancelable(false);
        dialog.setMessage("Getting prices...");
        dialog.show();
    }

    @Override
    protected String doInBackground(Void... voids) {
        User user = User.getStoredUser(context);
        if(user == null || user.token == null){
            Dialog.getNotLoggedInDialog(context).show();
            return null;
        }
        return getPricingResponse(planIDs, user);
    }

    @Override
    protected void onPostExecute(String response) {
        if(dialog.isShowing()) dialog.dismiss();
        if(response == null){
            Toast.makeText(context, "There was an error getting prices.  Try syncing your files.", Toast.LENGTH_LONG).show();
            return;
        }
        String price = getPriceFromResponse(response);
        FragmentActivity activity = (FragmentActivity) context;
        new CardDialogFragment(planIDs, price, activity).show(activity.getSupportFragmentManager(), "card_input");
    }

    private String getPriceFromResponse(String response){
        try {
            JSONObject o = new JSONObject(response);
            if(o.has("price")){
                String cents = o.optString("price");
                if(cents.equals(""))
                    return null;
                try {
                    DecimalFormat currency = new DecimalFormat("$###,###,###.00");
                    int c = Integer.parseInt(cents);
                    return currency.format(c / 100f);
                } catch (Exception e){
                    return null;
                }
            } else
                return o.optString("error");
        } catch (Exception e){}
        return null;
    }

    public String getPricingResponse(int[] planIDs, User user){
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(BASE_URL);
        try {
            JSONObject params = new JSONObject();
            params.put(USER_TOKEN_KEY, user.token);
            JSONArray array = new JSONArray();
            for(int i = 0 ; i < planIDs.length ; i ++)
                array.put(planIDs[i]);
            params.put(PLAN_IDS_KEY, array);
            Log.d("Pricing Params", params.toString());
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(params.toString()));
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httpPost);
            Log.d("Pricing Status Code", response.getStatusLine().getStatusCode() + "");
            if(response.getStatusLine().getStatusCode() == 200){
                ResponseHandler<String> handler = new BasicResponseHandler();
                return handler.handleResponse(response);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
