package io.plansource.utils;

import java.util.ArrayList;

/**
 * Created by Shane on 5/20/13.
 */
public class Worker {

    private ArrayList<Runnable> works;

    public Worker(){
        works = new ArrayList<Runnable>();
    }

    public void addWork(Runnable work){
        works.add(work);
    }

    public void start(){
        System.out.println("Worker Started");
        this.start(null);
    }

    public void start(final Runnable callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0 ; i < works.size() ; i ++)
                    works.get(i).run();
                if(callback != null)
                    callback.run();
            }
        }).start();
    }
}
