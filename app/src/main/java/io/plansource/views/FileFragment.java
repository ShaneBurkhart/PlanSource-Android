package io.plansource.views;

import android.support.v4.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by Shane on 5/21/13.
 */
public class FileFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {
        ListView listView = new ListView(this.getActivity());
        listView.setDivider(new ColorDrawable(Color.parseColor("#dbffe7")));
        return listView;
    }
}
