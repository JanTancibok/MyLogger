package sk.bratia4.mylogger.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by Janko on 4/9/2015.
 */
public class NetDevService extends IntentService {
    private static final String TAG = "NetDevService";


    public NetDevService() {
        super(NetDevService.class.getName());
        setIntentRedelivery(true);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void appendToFile(String what, String path){
        try {
            File logfile = new File(path);
            FileOutputStream stream = new FileOutputStream(logfile, true);
            try {
                stream.write(what.getBytes());
                stream.write("\n".getBytes());
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getNetDev(String devfile)
    {
        StringTokenizer linest;
        String header, devName, recvBytes, recvPackets,
                rcvErr, rcvDrop, zero, sendBytes;
        StringBuffer lineBuff = new StringBuffer();
        Time now = new Time("UTC");
        now.setToNow();

        try
        {
            BufferedReader reader = new BufferedReader( new
                    InputStreamReader( new FileInputStream(devfile) ), 2048 );

            char[] buffer = new char[2024];
            reader.read(buffer, 0, 2000);

            StringTokenizer st = new StringTokenizer(
                    new String(buffer), "\n", false);

            //The first two lines of the file are headers
            zero = st.nextToken();
            header = st.nextToken();

            while((linest = new StringTokenizer(st.nextToken()))!=null)
            {
                //take just not zero lines
                devName = linest.nextToken();
                recvBytes = linest.nextToken();
                recvPackets = linest.nextToken();
                rcvErr = linest.nextToken();
                rcvDrop = linest.nextToken();

                // Skip 4 tokens
                for (int i = 0; i < 4; i++)
                    zero = linest.nextToken();

                sendBytes = linest.nextToken();

                if(!(recvBytes.equals("0") && sendBytes.equals("0"))){

                    lineBuff.append(now.format2445() + ";" + devName + ";"); //devName
                    lineBuff.append(recvBytes + ";");
                    lineBuff.append(recvPackets + ";");
                    lineBuff.append(rcvErr + ";");
                    lineBuff.append(rcvDrop + ";");
                    lineBuff.append(sendBytes + ";");
                    //Read sendBytes, sendPackets, errs, drop
                    for (int i = 0; i < 3; i++)
                        lineBuff.append(linest.nextToken() + ";");

                    lineBuff.append("\n");
                }
            }
            reader.close();
        }
        catch (Exception e)
        {
            Log.e(TAG, "Exception", e);
        }
        finally {
            if(isExternalStorageWritable()){
                // Get the directory for the app's private pictures directory.
                File pathToSd = Environment.getExternalStorageDirectory();
                File file = new File(pathToSd, "LOGS");
                file.mkdirs();
                if (!file.exists()) {
                    Log.e(TAG, "Directory not created");
                }else{
                    File logfile = new File(file.getAbsolutePath() + "/net_stat.csv");
                    if(!logfile.exists()) {
                        //write headers one
                        String headers = "Time;Interface;Rxbytes;Rxpackets;Rerrs;Rdrop;Txbytes;Txpackets;Txerrs;Txdrop";
                        appendToFile(headers,file.getAbsolutePath() + "/net_stat.csv");
                    }
                    appendToFile(lineBuff.toString(),file.getAbsolutePath() + "/net_stat.csv");
                }
            }else{
                Log.e(TAG, "Cant write to SDcard");
            }
        }
    }

    public void parseUids(){
        File dir = new File("/proc/uid_stat/");
        String[] children = dir.list();
        List<Integer> uids = new ArrayList<Integer>();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                int uid = Integer.parseInt(children[i]);
                if ((uid >= 0 && uid < 2000) || (uid >= 10000)) {
                    uids.add(uid);
                }
            }
        }


    }

    @Override
    protected void onHandleIntent(Intent intent) {
        getNetDev("/proc/net/dev");
    }
}
