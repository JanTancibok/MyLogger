package eu.mcomputing.syslogger.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

import eu.mcomputing.syslogger.MyMainActivity;
import eu.mcomputing.syslogger.screen.LinuxLogFragment;
import eu.mcomputing.syslogger.utils.CommandRunner;
import eu.mcomputing.syslogger.utils.FileWriteUtil;
import eu.mcomputing.syslogger.utils.IpAdressHex2Dec;
import eu.mcomputing.syslogger.utils.MyDBAdapter;

import static eu.mcomputing.syslogger.utils.FileWriteUtil.*;

/**
 * Created by Janko on 4/9/2015.
 */
public class NetDevService extends IntentService {
    private static final String TAG = "NetDevService";
    private static final String MEMINFO_PATH = "/proc/meminfo";
    private static final String NMAP_COMMAND = "./nmap ";
    private static final String LOG_DIR = "LOGS";
    private static final String TCP_HEADER = "time;uid;state;loc_address;loc_port;rem_address;rem_port;inode;tx:rx;protocol;device_id\n";//"uid;loc_address;rem_address;state;inode;tx:rx\n";
    private static final String[] TCP_HEAD_AR = {"time", "uid", "state", "loc_address", "loc_port", "rem_address", "rem_port", "inode", "tx:rx"};
    private static final String TCP_PATH = "tcp_log_all.csv";
    private static final String NMAP_PATH = "ip_info.csv";
    private static final String UDP_PATH = "udp_log.csv";
    private static final String TCP6_PATH = "tcp6_log.csv";
    private static final String UDP6_PATH = "udp6_log.csv";
    private String TYPE = "";
    private String dev_id = "noname_con";

    public NetDevService() {
        super(NetDevService.class.getName());
        setIntentRedelivery(true);
    }

    public void getNetDev(String devfile) {
        StringTokenizer linest;
        String header, devName, recvBytes, recvPackets,
                rcvErr, rcvDrop, zero, sendBytes;
        StringBuffer lineBuff = new StringBuffer();
        Time now = new Time("UTC");
        now.setToNow();

        try {
            BufferedReader reader = new BufferedReader(new
                    InputStreamReader(new FileInputStream(devfile)), 2048);

            char[] buffer = new char[2024];
            reader.read(buffer, 0, 2000);

            StringTokenizer st = new StringTokenizer(
                    new String(buffer), "\n", false);

            //The first two lines of the file are headers
            zero = st.nextToken();
            header = st.nextToken();

            while ((linest = new StringTokenizer(st.nextToken())) != null) {
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

                if (!(recvBytes.equals("0") && sendBytes.equals("0"))) {

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
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        } finally {
            if (isExternalStorageWritable()) {
                // Get the directory for the app's private pictures directory.
                File pathToSd = Environment.getExternalStorageDirectory();
                File file = new File(pathToSd, "LOGS");
                file.mkdirs();
                if (!file.exists()) {
                    Log.e(TAG, "Directory not created");
                } else {
                    File logfile = new File(file.getAbsolutePath() + "/net_stat.csv");
                    if (!logfile.exists()) {
                        //write headers one
                        String headers = "Time;Interface;Rxbytes;Rxpackets;Rerrs;Rdrop;Txbytes;Txpackets;Txerrs;Txdrop\n";
                        appendToFile(headers, file.getAbsolutePath() + "/net_stat.csv");
                    }
                    appendToFile(lineBuff.toString(), file.getAbsolutePath() + "/net_stat.csv");
                }
            } else {
                Log.e(TAG, "Cant write to SDcard");
            }
        }
    }

    public void parseUids() {
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
                    String headers = "TYPE;Time;UID;Rxbytes;Txbytes\n";
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

        //4G ConnectivityManager.TYPE_WIMAX

        if (wifi.isConnected()) {
            TYPE = "wifi";
        } else {
            if (mobile.isConnected()) {
                TYPE = "mobil";
            }
        }

        dev_id = intent.getStringExtra(LinuxLogFragment.DEVICE_ID);

        if (!TYPE.equals("")) {
            getNetTCP("/proc/net/tcp", "TCP");//TCP_PATH);
            getNetTCP("/proc/net/udp", "UDP");//UDP_PATH);
            parseUids();
            getNetTCP("/proc/net/udp6", "UDP6");//UDP6_PATH);
            getNetDev("/proc/net/dev");
            getNetTCP("/proc/net/tcp6", "TCP6");//TCP6_PATH);
        }
    }

    private void getNetTCP(String path, String mypath) {
        StringBuilder stb = new StringBuilder();
        // JSONObject job = new JSONObject();
        String header = "";
        Time now = new Time("UTC");
        now.setToNow();

        try {
            File tcpfile = new File(path);
            Scanner scanner = new Scanner(tcpfile);

            //The first two lines of the file are headers
            header = scanner.nextLine();
            scanner.nextLine();
            String line;
            while ((line = scanner.nextLine()) != null) {
                while (line.startsWith(" ")) {
                    line = line.substring(1, line.length());
                }
                String[] fields = line.split("\\s+");
                //for(int i=0;i<fields.length;i++) {}
                //"time","uid","loc_address","loc_port","rem_address","rem_port","state","inode","tx:rx"

                String loca[] = fields[1].split(":");
                String rema[] = fields[2].split(":");

                //if(mypath.equals("UDP") || mypath.equals("TCP")){
                loca[0] = IpAdressHex2Dec.hexa2decIP(loca[0]);     //check length inside ipv6 vs ipv4
                String hexaIP = rema[0];
                rema[0] = IpAdressHex2Dec.hexa2decIP(rema[0]);
                //}

                loca[1] = IpAdressHex2Dec.hexa2decPort(loca[1]);
                rema[1] = IpAdressHex2Dec.hexa2decPort(rema[1]);

                /*job.put(TCP_HEAD_AR[0], now.format2445());
                job.put(TCP_HEAD_AR[1], fields[7]);
                if(FileWriteUtil.states.containsKey(fields[3])) {
                    job.put(TCP_HEAD_AR[2], FileWriteUtil.states.get(fields[3]));
                }else{
                    job.put(TCP_HEAD_AR[2], fields[3]);
                }
                job.put(TCP_HEAD_AR[3], loca[0]);
                job.put(TCP_HEAD_AR[4], loca[1]);
                job.put(TCP_HEAD_AR[5], rema[0]);
                job.put(TCP_HEAD_AR[6], rema[1]);
                job.put(TCP_HEAD_AR[7], fields[9]);
                job.put(TCP_HEAD_AR[8], fields[4]);*/


                stb.append(now.format2445());stb.append(";");
                stb.append(fields[7]);stb.append(";");
                if (FileWriteUtil.states.containsKey(fields[3])) {
                    stb.append(FileWriteUtil.states.get(fields[3]));
                } else {
                    stb.append(fields[3]);
                }
                stb.append(";");
                stb.append(loca[0]);stb.append(";");
                stb.append(loca[1]);stb.append(";");
                stb.append(rema[0]);stb.append(";");
                stb.append(rema[1]);stb.append(";");
                stb.append(fields[9]);stb.append(";");
                stb.append(fields[4]);stb.append(";");
                stb.append(mypath);stb.append(";");//protocol
                stb.append(dev_id);//device_id
                /*stb.append(fields[7]+";");
                stb.append(fields[1]+";");//loc
                stb.append(fields[2]+";");//rem
                stb.append(fields[3]+";");
                stb.append(fields[9]+";");
                stb.append(fields[4]);*/
                stb.append("\n");
                //Log.e(TAG, "new linezzzzzzzzzzzzzzzzzzzzzzzz");
                new AsyncCommandExecutor().execute(mypath, stb.toString(), rema[0], hexaIP);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        } finally {
            //Write with Nmap async

            /*
            if(isExternalStorageWritable()){
                // Get the directory for the app's private pictures directory.
                File pathToSd = Environment.getExternalStorageDirectory();
                File file = new File(pathToSd, LOG_DIR);
                if (!file.exists()) {
                    Log.e(TAG, "Directory not created");
                }else{
                    File logfile = new File(file.getAbsolutePath() +"/"+ mypath);
                    if(!logfile.exists()) {
                        //write headers one

                        //";socket_ref_count;adress_tosocket;retransmit_timeout;predicted_tick_ACK_controll;ack_pingpong;send_con_window;threshold\n";
                        appendToFile(headers,logfile.getAbsolutePath());
                    }

                    appendToFile(stb.toString(),logfile.getAbsolutePath());
                }
            }else{
                Log.e(TAG, "Cant write to SDcard");
            }*/
        }
    }

    /*
    <string-array name="scan_values_array">
        <item></item><item>Regular</item>
        <item>-T4 -A -v</item><item>Intense</item>
        <item>-T4 -F</item><item>Quick</item>
        <item>-sn</item><item>Ping</item>
        <item>-sV -T4 -O -F --version-light</item><item>Quick+</item>
        <item>-sn --traceroute</item>  <item>Quick+Trace</item>
        <item>-sS -sU -T4 -A -v</item>   <item>Intense+UDP</item>
        <item>-p 1-65535 -T4 -A -v</item>     <item>Intense/All</item>
        <item>-T4 -A -v -Pn</item><item>Intense-Ping</item>
        <item>-sS -sU -T4 -A -v -PE -PS80,443 -PA3389 -PP -PU40125 -PY --source-port 53 --script "default or (discovery and safe)"</item>  <item>Slow</item>
    </string-array>*/

    public class AsyncCommandExecutor extends AsyncTask<String, Void, Void> {
        //private JSONObject job;
        public String mybuffer;
        private String mypath;
        private String output = "";
        String resp = "no";
        //db or file

        @Override
        protected Void doInBackground(String... strings) {

            if (isExternalStorageWritable()) {
                mypath = strings[0];
                mybuffer = strings[1];
                MyDBAdapter mdb = new MyDBAdapter(getApplicationContext());
                List<String> l;
                String ip = strings[2];
                String hexaIP = strings[3];
                String i6dns = "2001:4860:4860::8888";

                // Get the directory for the app's private pictures directory.
                File pathToSd = Environment.getExternalStorageDirectory();
                File file = new File(pathToSd, LOG_DIR);
                if (!file.exists()) {
                    Log.e(TAG, "Directory not created");
                } else {
                    File logfile = new File(file.getAbsolutePath() + "/" + TCP_PATH);
                    if (!logfile.exists()) {
                        //write headers one
                        //";socket_ref_count;adress_tosocket;retransmit_timeout;predicted_tick_ACK_controll;ack_pingpong;send_con_window;threshold\n";
                        appendToFile(TCP_HEADER, logfile.getAbsolutePath());
                    }

                    appendToFile(mybuffer, logfile.getAbsolutePath());
////////////////////////////////////////////////////////////////////////////////
                    String ipv6 = ip;
                    String dns = "8.8.8.8";
                    if (mypath.equals("UDP6") || mypath.equals("TCP6")) {
                      //hexaIP = IpAdressHex2Dec.toRegularHexa(hexaIP);
                        ipv6 = "-6 "+IpAdressHex2Dec.toRegularHexa(hexaIP);//ip.substring(0, nthOccurrence(ip, '.', 4) - 2);
                        dns = i6dns;
                    }

                    //-p"+port+" "
                    l = mdb.getDNas(ip);
                    if (l == null || l.size() < 3) {
                       try {
                            output = CommandRunner.execCommand(NMAP_COMMAND + "-T5 --top-ports 300 --version-light -R --dns-servers "+dns+" "+ ipv6, MyMainActivity.getbinPath().getAbsoluteFile());

                            if (output != null) {
                                l = new ArrayList<String>();
                                l.add(ip);
                                if(output.contains("Host seems down")) {
                                    l.add("no_name");
                                    l.add("Host seems down");
                                }else{
                                    int a = output.indexOf("for ") + 4;
                                    int b = nthOccurrence(output, '(', 1) - 1;
                                    if(a<output.length() && a<b) {
                                        String pom = (output.substring(a, b));
                                        if(pom.contains("Host is up")){
                                            l.add("no_name");
                                        }else{
                                            l.add(pom);
                                        }
                                    }else{
                                        l.add("noname");
                                    }
                                    a = output.indexOf("SERVICE") + 8;
                                    b = output.indexOf("\n\n");
                                    if(a<output.length() && a<b && a>8) {
                                        l.add(output.substring(a, b));
                                    }else{
                                        l.add("no open ports");
                                    }
                                }
                                mdb.insertNmaIP(l.get(0), l.get(1), l.get(2));
                            }

                        } catch (IOException e) {
                            output = "IOException while trying to scan!";
                            Log.d(TAG, e.getMessage());
                        } catch (InterruptedException e) {
                            output = "Nmap Scan Interrupted!";
                            Log.d(TAG, e.getMessage());
                        }
                    }else{
                        //existujem v databaze
                        return null;
                    }
                    //ak je co
                    if (l != null && l.size() > 2) {
                        File lognmap_file = new File(file.getAbsolutePath() + "/" + NMAP_PATH);
                        StringBuilder st = new StringBuilder();

                        if (!lognmap_file.exists()) {
                            st.append("IP;NAME;PORT;STATUS;SERVICE\n");
                        }
                        try {
                            String[] lines = l.get(2).split("\n");
                            for (int i = 0; i < lines.length; i++) {
                                st.append(l.get(0));
                                st.append(";");
                                st.append(l.get(1));
                                st.append(";");
                                String[] vals = lines[i].split("\\s+");
                                st.append(vals[0]);
                                st.append(";");
                                st.append(vals[1]);
                                st.append(";");
                                st.append(vals[2]);
                                st.append("\n");
                            }
                            appendToFile(st.toString(), lognmap_file.getAbsolutePath());
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                            // Log.e(TAG,);
                        }
                    }
                }
            } else {
                Log.e(TAG, "Cant write to SDcard");
            }
             /*try {
                job = new JSONObject(strings[1]);

                String ipv6 = "";
                String ip = job.get(TCP_HEAD_AR[5]).toString();
                String port = job.get(TCP_HEAD_AR[6]).toString();
                if(mypath.equals("UDP6") || mypath.equals("TCP6")){
                    //ipv6 = "-6 ";
                    ip = ip.substring(0,nthOccurrence(ip,'.',4)-2);
                }

                //-p"+port+" "
                l = mdb.getDNas(ip);
                if(l!=null && l.size()>0){
                }else {
                    output = CommandRunner.execCommand(NMAP_COMMAND + ipv6 + "-T5 --top-ports 300 --version-light -R --dns-servers 8.8.8.8 " + ip, MyMainActivity.getbinPath().getAbsoluteFile());
                    if(output!=null) {
                        l = new ArrayList<String>();
                        l.add(ip);
                        int a = output.indexOf("for ")+4;
                        int b = nthOccurrence(output,'(',1)-2;
                        l.add(output.substring( a, b));
                        l.add(output.substring(output.indexOf("SERVICE") + 8,output.indexOf("\n\n")));
                        mdb.insertNmaIP(l.get(0), l.get(1), l.get(2));
                    }
                }
                job.put("nmap_name",l.get(1));
                job.put("nmap_ports",l.get(2));

            } catch (IOException e) {
                output = "IOException while trying to scan!";
                Log.d(TAG, e.getMessage());
            } catch (InterruptedException e) {
                output = "Nmap Scan Interrupted!";
                Log.d(TAG, e.getMessage());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(job!=null && job.length()>0){
                JSONObject request = new JSONObject();
                try {
                    job.put("device_id",dev_id);
                    job.put("protocol",mypath);
                    request.put("riadok", job);
                    request.put("method","insertNet");
                    request.put("table","NET_STAT_ALL");

                    //resp = FileWriteUtil.httpObject(request);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }*/
            return null;
        }
    }
}
