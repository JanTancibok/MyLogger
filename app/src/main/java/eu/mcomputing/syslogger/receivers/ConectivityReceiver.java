package eu.mcomputing.syslogger.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import eu.mcomputing.syslogger.MyMainActivity;
import eu.mcomputing.syslogger.utils.FileWriteUtil;

import static eu.mcomputing.syslogger.utils.FileWriteUtil.*;

public class ConectivityReceiver extends BroadcastReceiver {
    private final String PATH_TODIR = "LOGS";
    private final String ZIP_NAME = "all.zip";
    private final String FILE_CON_CHANGE = "connectivity_changes.csv";
    private final String TAG = "Connnect_change_rec";

    android.net.NetworkInfo nInfo;
    SharedPreferences preferences;
    ConnectivityManager connMgr;

    public ConectivityReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extra = intent.getExtras();
        nInfo = (android.net.NetworkInfo) extra.get("networkInfo");

        connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        preferences = context.getSharedPreferences("myPrefs",Context.MODE_PRIVATE);

        AsyncCommandExecutor ase = new AsyncCommandExecutor();
        ase.execute();
    }

    public class AsyncCommandExecutor extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            final android.net.NetworkInfo wifi = connMgr
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            final android.net.NetworkInfo mobile = connMgr
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            boolean disconectedW = false;
            boolean disconectedM = false;

        /*ArrayList<String> test = new ArrayList<String>();
        test.add(intent.getExtras().getString(ConnectivityManager.EXTRA_EXTRA_INFO));
        test.add(intent.getStringExtra(ConnectivityManager.EXTRA_IS_FAILOVER));
        test.add(intent.getStringExtra(ConnectivityManager.EXTRA_NETWORK_TYPE));
        test.add(intent.getExtras().getString(ConnectivityManager.EXTRA_NO_CONNECTIVITY));
        test.add(intent.getStringExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO));
        test.add(intent.getStringExtra(ConnectivityManager.EXTRA_REASON));*/

            if (isExternalStorageWritable()) {
                File pathToSd = Environment.getExternalStorageDirectory();
                File file = new File(pathToSd, PATH_TODIR);
                if (!file.exists()) {
                    file.mkdirs();
                }
                if (!file.exists()) {
                    Log.e(TAG, "Directory not created");
                } else {
                    StringBuilder conbuild = new StringBuilder();
                    File logfile = new File(file.getAbsolutePath() + "/" + FILE_CON_CHANGE);
                    if (!logfile.exists()) {
                        conbuild.append("Time;TypeName;SubTypeName;DetailedState;ExtraInfo;Reason\n");
                    }
                    conbuild.append(getNowUTC() + ";");

                    if (nInfo != null) {
                        //conbuild.append(nInfo.getType()); wiif = 1
                        conbuild.append(nInfo.getTypeName() + ";");
                        conbuild.append(nInfo.getSubtypeName() + ";");
                        conbuild.append(nInfo.getDetailedState() + ";");
                        conbuild.append(nInfo.getExtraInfo() + ";");
                        conbuild.append(nInfo.getReason());
                        //conbuild.append(nInfo.getState().toString() + ";"); == DetailedState
                    } else {
                        if (wifi.isAvailable() && wifi.isConnected()) {
                            conbuild.append("WIFI;");
                            conbuild.append(wifi.getSubtypeName() + ";");
                            conbuild.append(wifi.getDetailedState() + ";");
                            conbuild.append(wifi.getExtraInfo() + ";");
                            conbuild.append(wifi.getReason() + ";");
                            Log.d(TAG, "Wifi Available ");
                        } else {
                            disconectedW = true;
                        }
                        if (mobile.isAvailable() && mobile.isConnected()) {
                            conbuild.append("mobile;");
                            conbuild.append(mobile.getSubtypeName() + ";");
                            conbuild.append(mobile.getDetailedState() + ";");
                            conbuild.append(mobile.getExtraInfo() + ";");
                            conbuild.append(mobile.getReason() + ";");
                            Log.d(TAG, "Mobile network Available ");
                        } else {
                            disconectedM = true;
                        }

                        if (disconectedM && disconectedW) {
                            //conbuild.append("Disconnected");
                        }
                    }

                    conbuild.append("\n");
                    appendToFile(conbuild.toString(), logfile.getPath());

                    //ak som sa prave odpojil sprav zalohu => a zmaz data
                    //ak som sa prave pripojil na wifi posli zalohu na server => zmaz zalohu
                    if (mobile.isAvailable() && mobile.isConnected()) {
                        //prave som sa pripojil na mobil
                    } else if (wifi.isAvailable() && wifi.isConnected()) {
                        //prave som sa pripojil na wifi
                        File zip = new File(pathToSd, PATH_TODIR +"/"+ ZIP_NAME);

                        if (zip.exists()) {
                            String result = FileWriteUtil.httpZIP(zip,preferences.getString("deviceID", "not_set"));
                            if(result!=null){zip.delete();}
                        }
                    } else {
                        File zipOne = new File(pathToSd, PATH_TODIR +"/"+ ZIP_NAME);
                        //ak zaloha existuje nerob novu
                        if (!zipOne.exists()) {
                            //som odpojeny
                            File[] listOfFiles = file.listFiles();
                            StringBuilder zipfiles = new StringBuilder();
                            for (int i = 0; i < listOfFiles.length; i++) {
                                if (listOfFiles[i].isFile()) {
                                    zipfiles.append(listOfFiles[i].getAbsolutePath());
                                    zipfiles.append(";");
                                    //System.out.println("File " + listOfFiles[i].getName());
                                } else if (listOfFiles[i].isDirectory()) {
                                    File[] rekurzia = listOfFiles[i].listFiles();
                                    for (int j = 0; j < rekurzia.length; j++) {
                                        if (rekurzia[j].isFile()) {
                                            zipfiles.append(rekurzia[j].getAbsolutePath());
                                            zipfiles.append(";");
                                        }
                                    }
                                    //System.out.println("Directory " + listOfFiles[i].getName());
                                }
                            }

                            FileWriteUtil.ziper(zipfiles.toString().split(";"), file.getAbsolutePath() + "/" + ZIP_NAME);

                            File zip = new File(pathToSd, PATH_TODIR + "/" + ZIP_NAME);
                            //ak je zaloha hotova zmaz data LOGS
                            if (zip.exists()) {
                                Process process = null;
                               // try {
                                    File[] logs = file.listFiles();
                                    for (int i = 0; i < logs.length; i++) {
                                        if (!logs[i].getName().equals(ZIP_NAME))
                                            if(logs[i].isDirectory()){
                                                File[] reku = logs[i].listFiles();
                                                for (int j = 0; j < reku.length; j++) {
                                                    reku[j].delete();
                                                }
                                                logs[i].delete();
                                            }else{
                                                logs[i].delete();
                                            }
                                           // process = Runtime.getRuntime().exec("rm -f -r", null, logs[i]);
                                    }
                                /*} catch (IOException e) {
                                    e.printStackTrace();
                                }*/
                            }
                        }
                    }
                }
            }else{
                Log.e(TAG,"nonwritable storage");
            }
            return null;
        }
    }
}
