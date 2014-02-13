package io.plansource.utils;

import java.util.ArrayList;

/**
 * Created by Shane on 8/25/13.
 */
public class StateUtils {
    public static final String[] states = new String[] {
        "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA", "HI", "ID", "IL", "IN",
        "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV",
        "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN",
        "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"
    };

    public static ArrayList<String> getStatesWithHeader(){
        ArrayList<String> st= new ArrayList<String>();
        st.add("State");
        for(String s : states)
            st.add(s);
        return st;
    }
}
