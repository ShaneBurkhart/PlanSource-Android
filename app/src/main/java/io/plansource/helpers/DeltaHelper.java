package io.plansource.helpers;

import io.plansource.models.Job;
import io.plansource.models.Plan;

import java.util.ArrayList;

/**
 * Created by Shane on 5/20/13.
 */
public class DeltaHelper {

    public static ArrayList[] determineDelta(ArrayList<Job> newJobs, ArrayList<Job> oldJobs){
        ArrayList<Plan> newPlans = Job.allPlans(newJobs);
        ArrayList<Plan> oldPlans = Job.allPlans(oldJobs);
        ArrayList<Plan> toDelete = null;
        ArrayList<Plan> toAdd = null;

        if(oldJobs == null && newJobs == null)
            D.out(DeltaHelper.class, "All Data is null");
        if(newJobs == null)
            D.out(DeltaHelper.class, "New Data is null");
        if(oldJobs == null){
            D.out(DeltaHelper.class, "Old Data is null");
            toAdd = newPlans;
        }
        if(oldJobs != null && newJobs != null){
            toDelete = determineRemoved(newPlans, oldPlans);
            toAdd = determineAdded(newPlans, oldPlans);
        }
        if(toAdd != null)
            D.out(DeltaHelper.class, "Plans to add: " + toAdd.size());
        if(toDelete != null)
            D.out(DeltaHelper.class, "Plans to remove: " + toDelete.size());

        ArrayList[] delta = {toAdd, toDelete};
        return delta;
    }

    private static ArrayList<Plan> determineRemoved(ArrayList<Plan> newData, ArrayList<Plan> oldData) {
        ArrayList<Plan> toDelete = new ArrayList<Plan>();
        boolean delete = true;
        for(int i = 0 ; i < oldData.size() ; i ++){
            delete = true;
            for(int j = 0 ; j < newData.size() ; j ++){
                if(oldData.get(i).id == newData.get(j).id){
                    delete = false;
                    break;
                }
            }
            if(delete)
                toDelete.add(oldData.get(i));
        }
        return toDelete;
    }

    private static ArrayList<Plan> determineAdded(ArrayList<Plan> newData, ArrayList<Plan> oldData){
        ArrayList<Plan> toAdd = new ArrayList<Plan>();
        boolean add = true;
        for(int i = 0 ; i < newData.size() ; i ++){
            add = true;
            for(int j = 0 ; j < oldData.size() ; j ++){
                if(newData.get(i).id == oldData.get(j).id){
                    if(newData.get(i).updated_at.equals(oldData.get(j).updated_at)){
                        add = false;
                        break;
                    }
                }
            }
            if(add == true)
                toAdd.add(newData.get(i));
        }
        return toAdd;
    }
}
