package io.plansource.views.fragments.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Token;
import com.stripe.exception.AuthenticationException;
import io.plansource.R;
import java.util.ArrayList;
import java.util.Calendar;

import com.stripe.android.model.Card;
import io.plansource.utils.SendStripeTokenToServerTask;
import io.plansource.utils.StateUtils;

/**
 * Created by Shane on 8/24/13.
 */
public class CardDialogFragment extends DialogFragment{

    private static final String TITLE = "Place Your Order";
    private static final String STRIPE_API_KEY = "pk_test_aRHL7CefYn1FjAjpDgIGVtuE";

    Spinner month, year, state;
    EditText cardNumber, cardCVC, address, city, zipcode, name;
    Button okay, cancel;
    Handler handler;
    int[] planIDs;
    ProgressDialog dialog;
    Context context;
    String price;

    public CardDialogFragment(int[] planIDs, String price, Context context){
        this.planIDs = planIDs;
        this.context = context;
        this.price = price;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        handler = new Handler();
        String title = TITLE;
        View v = View.inflate(getActivity(), R.layout.card_dialog, null);
        initSpinners(v);
        if(price != null)
            ((TextView) v.findViewById(R.id.pricing)).setText(price);
        name = (EditText) v.findViewById(R.id.name);
        address = (EditText) v.findViewById(R.id.address);
        zipcode = (EditText) v.findViewById(R.id.zipcode);
        city = (EditText) v.findViewById(R.id.city);
        cardNumber = (EditText) v.findViewById(R.id.card_number);
        cardNumber.addTextChangedListener(new CardInputListener());
        cardCVC = (EditText) v.findViewById(R.id.card_cvc);
        okay = (Button) v.findViewById(R.id.ok);
        cancel = (Button) v.findViewById(R.id.cancel);
        Dialog dialog = new Dialog(getActivity());
        dialog.setTitle(title);
        dialog.setContentView(v);
        dialog.setCancelable(false);
        okay.setOnClickListener(new OnOkayClickListener(dialog));
        cancel.setOnClickListener(new OnCancelClickListener(dialog));
        return dialog;
    }

    private void initSpinners(View v){
        month = (Spinner) v.findViewById(R.id.experation_date_month);
        ArrayList<String> months = new ArrayList<String>();
        months.add("Month");
        String[] m = getResources().getStringArray(R.array.exp_months);
        for(String s : m) months.add(s);
        ArrayAdapter<String> monthApapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, months);
        monthApapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        month.setAdapter(monthApapter);

        year = (Spinner) v.findViewById(R.id.experation_date_year);
        Calendar now = Calendar.getInstance();
        int y = now.get(Calendar.YEAR);
        String[] years = new String[10];
        years[0] = "Year";
        for(int i = 0 ; i < years.length - 1 ; i ++)
            years[i + 1] = (y + i) + "";
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        year.setAdapter(yearAdapter);

        state = (Spinner) v.findViewById(R.id.state);
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, StateUtils.getStatesWithHeader());
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        state.setAdapter(stateAdapter);
    }

    private class OnOkayClickListener implements View.OnClickListener{
        Dialog dial;
        public OnOkayClickListener(Dialog dialog){
            this.dial = dialog;
        }
        @Override
        public void onClick(View v) {
            String cardNum = cardNumber.getText().toString().replace(" ", "-");
            String cvc = cardCVC.getText().toString();
            String expMonth = month.getSelectedItem().toString();
            String expYear = year.getSelectedItem().toString();
            //Optional params
            String cardHolder = name.getText().toString();
            String billingAddress = address.getText().toString();
            String billingCity = city.getText().toString();
            String billingState = (String) state.getSelectedItem();
            String billingZipcode = zipcode.getText().toString();
            if(isInteger(expMonth) && isInteger(expYear)){
                int m = Integer.parseInt(expMonth);
                int y = Integer.parseInt(expYear);
                if(isEmpty(cardHolder)){
                    Toast.makeText(getActivity(), "You need to enter a name.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(isEmpty(billingAddress) || isEmpty(billingCity) || isEmpty(billingState) || isEmpty(billingZipcode)){
                    Toast.makeText(getActivity(), "You need to fill out your billing address", Toast.LENGTH_SHORT).show();
                    return;
                }
                Card card = new Card(cardNum, m, y, cvc, cardHolder, billingAddress, null,
                        billingCity, billingState, billingZipcode, null);
                if(!card.validateCard()){
                    Toast.makeText(getActivity(), "There is a problem with your card.", Toast.LENGTH_SHORT).show();
                    return;
                }
                dial.dismiss();
                try {
                    Stripe stripe = new Stripe(STRIPE_API_KEY);
                    dialog = new ProgressDialog(context);
                    dialog.setMessage("Making the charge...");
                    dialog.show();
                    stripe.createToken(card, new StripeTokenCallback());
                } catch (AuthenticationException e){
                    Toast.makeText(getActivity(), "Could not connect to payment server.", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getActivity(), "There is an error with your expiration date.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    private boolean isEmpty(String var){
        return var == null || var.equals("");
    }

    private class OnCancelClickListener implements View.OnClickListener{
        Dialog dial;
        public OnCancelClickListener(Dialog dialog){
            this.dial = dialog;
        }
        @Override
        public void onClick(View view) {
            dial.dismiss();
        }
    }

    private class StripeTokenCallback extends TokenCallback {
        @Override
        public void onError(Exception error) {
            Toast.makeText(getActivity(), error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onSuccess(Token token) {
            new SendStripeTokenToServerTask(planIDs, context, dialog).execute(token);
        }
    }

    private class CardInputListener implements TextWatcher {
        private boolean spaceDeleted;

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // check if a space was deleted
            CharSequence charDeleted = s.subSequence(start, start + count);
            spaceDeleted = " ".equals(charDeleted.toString());
        }

        public void afterTextChanged(Editable editable) {
            cardNumber.removeTextChangedListener(this);

            int cursorPosition = cardNumber.getSelectionStart();
            String withSpaces = formatText(editable);
            cardNumber.setText(withSpaces);
            cardNumber.setSelection(cursorPosition + (withSpaces.length() - editable.length()));

            if (spaceDeleted) {
                cardNumber.setSelection(cardNumber.getSelectionStart() - 1);
                spaceDeleted = false;
            }

            cardNumber.addTextChangedListener(this);
        }

        private String formatText(CharSequence text){
            StringBuilder formatted = new StringBuilder();
            int count = 0;
            for (int i = 0; i < text.length(); ++i){
                if (Character.isDigit(text.charAt(i))){
                    if (count % 4 == 0 && count > 0)
                        formatted.append(" ");
                    formatted.append(text.charAt(i));
                    ++count;
                }
            }
            return formatted.toString();
        }
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
    }
}
