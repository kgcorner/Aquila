package com.kgaurav.balancer;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 4/11/2018.
 */
public class Test {
    public static void main(String[] args) {
        String path = Test.class.getResource("").getPath();
        String pathTillBuild = path.split("build")[0];
        path = pathTillBuild+"build/libs";
        File folder  = new File(path);
        File[] files = folder.listFiles();
        for(File file : files) {
            String regEx = "^balancer.*jar$";
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(file.getName());
            if(matcher.find()) {
                path = file.getAbsolutePath();
                break;
            }
        }
        //path = path+"../../../../../libs/balancer-1.0-SNAPSHOT.jar";
        System.out.println(path);
    }
}
