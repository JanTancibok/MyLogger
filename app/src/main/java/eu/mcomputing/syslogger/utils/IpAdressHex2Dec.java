package eu.mcomputing.syslogger.utils;

import android.os.Environment;

/**
 * Created by Janko on 5/7/2015.
 */
public final class IpAdressHex2Dec {
    public static String hexa2decIP (String hexa) {
        StringBuilder result = new StringBuilder();
        //reverse
        for(int i=hexa.length()-1;i>=0;i=i-2){
            String wtf = hexa.substring(i-1,i+1);
            result.append(Integer.parseInt(wtf, 16));
            result.append(".");
        }
        return result.substring(0,result.length()-1).toString();
    }
    public static String hexa2decPort (String hexa) {
        StringBuilder result = new StringBuilder();

        result.append(Integer.parseInt(hexa, 16));

        return result.toString();
    }
}
