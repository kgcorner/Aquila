package com.kgaurav.balancer;

/**
 * Created by admin on 4/11/2018.
 */
public class Test {
    public static void main(String[] args) {
        String path = Test.class.getResource("").getPath();
        path = path+"../../../../../libs/balancer-1.0-SNAPSHOT.jar";
        System.out.println(path);
    }
}
