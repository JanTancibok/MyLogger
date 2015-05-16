package eu.mcomputing.syslogger.utils;

import android.os.Environment;

/**
 * Created by Janko on 5/7/2015.
 * http://serverfault.com/questions/592574/why-does-proc-net-tcp6-represents-1-as-1000
 */
public final class IpAdressHex2Dec {

    /**0100A8C0 -> 192.168.0.1*/
    public static String hexa2decIPv4 (String hexa) {
        StringBuilder result = new StringBuilder();
        //reverse Little to Big
        for (int i = hexa.length() - 1; i >= 0; i = i - 2) {
            String wtf = hexa.substring(i - 1, i + 1);
            result.append(Integer.parseInt(wtf, 16));
            result.append(".");
        }
        //remove last ".";
        return result.substring(0,result.length()-1).toString();
    }

    /**0000000000000000FFFF00008370E736 -> 0.0.0.0.0.0.0.0.0.0.255.255.54.231.112.131
      0100A8C0 -> 192.168.0.1
    */
    public static String hexa2decIP (String hexa) {
        StringBuilder result = new StringBuilder();
        if(hexa.length()==32){
            for(int i=0;i<hexa.length();i=i+8){
                result.append(hexa2decIPv4(hexa.substring(i, i + 8)));
                result.append(".");
            }
        }else {
            if(hexa.length()!=8){return "0.0.0.0";}
            return hexa2decIPv4(hexa);
        }
        //remove last ".";
        return result.substring(0,result.length()-1).toString();
    }

    /**Simple hexa to dec, for ports
     * 01BB -> 403
     * */
    public static String hexa2decPort(String hexa) {
        StringBuilder result = new StringBuilder();
        result.append(Integer.parseInt(hexa, 16));
        return result.toString();
    }

    /**B80D0120 00000000 67452301 EFCDAB89 -> 2001:0db8:0000:0000:0123:4567:89ab:cdef
     * */
    public static String toRegularHexa(String hexaIP){
        StringBuilder result = new StringBuilder();
        for(int i=0;i<hexaIP.length();i=i+8){
            String word = hexaIP.substring(i,i+8);
            for (int j = word.length() - 1; j >= 0; j = j - 2) {
                result.append(word.substring(j - 1, j + 1));
                result.append((j==5)?":":"");//in the middle
            }
            result.append(":");
        }
        return result.substring(0,result.length()-1).toString();
    }
}
