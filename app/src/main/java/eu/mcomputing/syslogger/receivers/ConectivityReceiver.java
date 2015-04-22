package eu.mcomputing.syslogger.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import static eu.mcomputing.syslogger.utils.FileWriteUtil.*;

public class ConectivityReceiver extends BroadcastReceiver {
    private final String PATH_TODIR = "LOGS";
    private final String FILE_CON_CHANGE = "connectivity_changes.csv";
    private final String TAG = "Connnect_change_receiver";

    public ConectivityReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final android.net.NetworkInfo mobile = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        boolean disconectedW  = false;
        boolean disconectedM  = false;

        /*ArrayList<String> test = new ArrayList<String>();
        test.add(intent.getExtras().getString(ConnectivityManager.EXTRA_EXTRA_INFO));
        test.add(intent.getStringExtra(ConnectivityManager.EXTRA_IS_FAILOVER));
        test.add(intent.getStringExtra(ConnectivityManager.EXTRA_NETWORK_TYPE));
        test.add(intent.getExtras().getString(ConnectivityManager.EXTRA_NO_CONNECTIVITY));
        test.add(intent.getStringExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO));
        test.add(intent.getStringExtra(ConnectivityManager.EXTRA_REASON));*/

        Bundle extra = intent.getExtras();
        final android.net.NetworkInfo nInfo = (android.net.NetworkInfo) extra.get("networkInfo");

        if(isExternalStorageWritable()) {
            File pathToSd = Environment.getExternalStorageDirectory();
            File file = new File(pathToSd, PATH_TODIR);
            if (!file.exists()) {
                file.mkdirs();
            }
            if (!file.exists()) {
                Log.e(TAG, "Directory not created");
            }else {
                StringBuilder conbuild = new StringBuilder();
                File logfile = new File(file.getAbsolutePath() +"/"+ FILE_CON_CHANGE);
                if (!logfile.exists()) {
                    conbuild.append("Time;TypeName;SubTypeName;DetailedState;ExtraInfo;Reason");
                }else{
                    conbuild.append(getNowUTC()+";");

                    if(nInfo!=null) {
                        //conbuild.append(nInfo.getType()); wiif = 1
                        conbuild.append(nInfo.getTypeName() + ";");
                        conbuild.append(nInfo.getSubtypeName() + ";");
                        conbuild.append(nInfo.getDetailedState() + ";");
                        conbuild.append(nInfo.getExtraInfo() + ";");
                        conbuild.append(nInfo.getReason() + ";");
                        //conbuild.append(nInfo.getState().toString() + ";"); == DetailedState
                    }else{
                        if (wifi.isAvailable() && wifi.isConnected()) {
                            conbuild.append("WIFI;");
                            conbuild.append(wifi.getSubtypeName() + ";");
                            conbuild.append(wifi.getDetailedState() + ";");
                            conbuild.append(wifi.getExtraInfo() + ";");
                            Log.d(TAG, "Wifi Available ");
                        } else {
                            disconectedW = true;
                        }
                        if (mobile.isAvailable() && mobile.isConnected()) {
                            conbuild.append("mobile;");
                            conbuild.append(mobile.getSubtypeName() + ";");
                            conbuild.append(mobile.getDetailedState() + ";");
                            conbuild.append(mobile.getExtraInfo() + ";");
                            Log.d(TAG, "Mobile network Available ");
                        } else {
                            disconectedM = true;
                        }

                        if(disconectedM && disconectedW){conbuild.append("Disconnected");}
                    }
                }

                conbuild.append("\n");
                appendToFile(conbuild.toString(),logfile.getPath());
            }
        }
    }
}
