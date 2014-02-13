package io.plansource.helpers;

/**
 * Created by Shane on 7/25/13.
 */
public class D {

    public static void out(Class<?> c, String message){
        System.out.println(c.getName() + " - " + message);
    }

    public static void err(Class<?> c, String message){
        System.err.println(c.getName() + " - " + message);
    }
}
