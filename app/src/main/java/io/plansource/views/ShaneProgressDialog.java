package io.plansource.views;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import io.plansource.R;

/**
 * Created by Shane on 5/23/13.
 */
public class ShaneProgressDialog extends DialogFragment{

    ProgressBar pageBar, overallBar;
    TextView pageRatio, overallRatio, message;
    int overallTotal = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.prog_dialog, container);
        message = (TextView) view.findViewById(R.id.progText);
        pageBar = (ProgressBar) view.findViewById(R.id.pageProg);
        overallBar = (ProgressBar) view.findViewById(R.id.overallProg);
        pageRatio = (TextView) view.findViewById(R.id.pageRatio);
        overallRatio = (TextView) view.findViewById(R.id.overallRatio);
        return view;
    }

    public void setMessage(CharSequence msg) {
        message.setText(msg);
    }

    public void setOverallTotal(int total){
        this.overallTotal = total;
    }

    public int getOverallTotal(){
        return this.overallTotal;
    }

    public void setOverallProgress(int prog){
        prog = Math.min(prog, this.getOverallTotal());
        prog = Math.max(prog, 0);
        overallBar.setProgress((int) (((float) prog) * 100f / this.getOverallTotal()));
        overallRatio.setText(prog + "/" + this.getOverallTotal());
    }

    public void setPageProgress(int prog){
        prog = Math.min(prog, 100);
        prog = Math.max(prog, 0);
        pageBar.setProgress(prog);
        pageRatio.setText(prog + "/100");
    }
}
