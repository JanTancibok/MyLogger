package eu.mcomputing.syslogger.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.StringTokenizer;

import static eu.mcomputing.syslogger.utils.FileWriteUtil.*;

/**
 * Created by Janko on 4/9/2015.
 */
public class NetDevService extends IntentService {
    private static final String TAG = "mylogger.NetDevService";
    private static final String MEMINFO_PATH = "/proc/meminfo";
    private String TYPE = "";

    public NetDevService() {
        super(NetDevService.class.getName());
        setIntentRedelivery(true);
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
            ArrayList<Integer> uids = new ArrayList<Integer>();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    int uid = Integer.parseInt(children[i]);
                    if ((uid >= 0 && uid < 2000) || (uid >= 10000)) {
                        uids.add(uid);
                    }
                }
            }

            RandomAccessFile rifle;
            StringBuilder sbuff = new StringBuilder();
            Time now = new Time("UTC");
            now.setToNow();

            //tcp_snd = new byte[4];

            for (int i : uids) {
                sbuff.append(TYPE + ";" + now.format2445() + ";" + i + ";");
                try {
                    rifle = new RandomAccessFile("/proc/uid_stat/" + i + "/tcp_snd", "r");
                    //rifle.readFully(tcp_snd);
                    sbuff.append(rifle.readInt() + ";");
                    rifle.close();

                    rifle = new RandomAccessFile("/proc/uid_stat/" + i + "/tcp_rcv", "r");
                    sbuff.append(rifle.readInt() + "\n");
                    rifle.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (isExternalStorageWritable()) {
                // Get the directory for the app's private pictures directory.
                File pathToSd = Environment.getExternalStorageDirectory();
                File file = new File(pathToSd, "LOGS");
                file.mkdirs();
                if (!file.exists()) {
                    Log.e(TAG, "Directory not created");
                } else {
                    File logfile = new File(file.getAbsolutePath() + "/net_stat_app.csv");
                    if (!logfile.exists()) {
                        //write headers one
                        String headers = "TYPE;Time;UID;Rxbytes;Txbytes;";
                        appendToFile(headers, file.getAbsolutePath() + "/net_stat_app.csv");
                    }
                    appendToFile(sbuff.toString(), file.getAbsolutePath() + "/net_stat_app.csv");
                }
            } else {
                Log.e(TAG, "Cant write to SDcard");
            }

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        final ConnectivityManager connMgr = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final android.net.NetworkInfo mobile = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if(wifi.isConnected()){
            TYPE="wifi";
        }else{
            if(mobile.isConnected()){TYPE="mobil";}
        }

        if(!TYPE.equals("")) {
            getNetDev("/proc/net/dev");
            parseUids();
        }


    }
}
