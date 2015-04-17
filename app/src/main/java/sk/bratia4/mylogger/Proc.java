package sk.bratia4.mylogger;
/**
 * SystemSens
 *
 * Copyright (C) 2009 Hossein Falaki
 */

        import java.io.FileInputStream;
        import java.io.IOException;
        import java.util.StringTokenizer;
        import java.io.BufferedReader;
        import java.io.InputStreamReader;

        import android.util.Log;

        import org.json.JSONObject;
        import org.json.JSONException;

        import java.util.List;
        import java.util.ArrayList;

/**
 * Reads varios information from the /proc file system.
 *
 * After an object of this class is constructed, each call to
 * get*() methods returns a HashMap containing some information from
 * the /proc of the Linux kernel.
 *
 * @author Hossein Falaki
 */
public class Proc {
    /**
     * TAG of this class for logging
     */
    private static final String TAG = "SystemSens:Proc";

    /**
     * Address of memory information file
     */
    private static final String MEMINFO_PATH = "/proc/meminfo";

    private static long sTotal = 0;
    private long idle = 0;
    private long user = 0;
    private long system = 0;
    private long nice = 0;


    /**
     * Constructs a Proc object.
     */
    public Proc() {
        // No initialization needed yet.
        getCpuLoad();
    }

    public JSONObject getMemInfo() {

        JSONObject result = new JSONObject();

        StringTokenizer linest;
        String key, value;

        try {

            BufferedReader reader = new BufferedReader(new
                    InputStreamReader(new FileInputStream(MEMINFO_PATH)), 2048);


            char[] buffer = new char[2024];
            reader.read(buffer, 0, 2000);


            StringTokenizer st = new StringTokenizer(
                    new String(buffer), "\n", false);

            for (int i = 0; i < st.countTokens(); i++) {

                linest = new StringTokenizer(st.nextToken());
                key = linest.nextToken();
                value = linest.nextToken();

                try {
                    result.put(key, value);
                } catch (JSONException je) {
                    Log.e(TAG, "Exception", je);
                }
            }

            reader.close();

        } catch (Exception e) {

            Log.e(TAG, "Exception parsing the file", e);
        }


        return result;
    }

    public static long getCpuTotalTime() {
        return sTotal;
    }


    public static List<Long> readProcessCpuTime(long processId) {

        List res = null;
        long utime = 0;
        long stime = 0;

        String line;
        String[] toks;

        try {
            BufferedReader reader = new BufferedReader(new
                    InputStreamReader(new
                    FileInputStream(
                    "/proc/" + processId + "/stat")), 512);

            while ((line = reader.readLine()) != null) {
                toks = line.split(" ");

                utime = Long.parseLong(toks[13]);
                stime = Long.parseLong(toks[14]);
            }

            reader.close();

            res = new ArrayList<Long>();
            res.add(utime);
            res.add(stime);


        } catch (IOException ex) {
            Log.e(TAG, "Could not read /proc file", ex);
        }

        return res;

    }

    public JSONObject getCpuLoad() {
        JSONObject result = new JSONObject();

        float totalUsage, userUsage, niceUsage, systemUsage;
        Double cpuFreq = 0.0;

        String line;
        String[] toks;
        String[] words;

        try {
            BufferedReader reader = new BufferedReader(new
                    InputStreamReader(new
                    FileInputStream("/proc/cpuinfo")), 2048);

            while ((line = reader.readLine()) != null) {
                toks = line.split(" ");
                words = toks[0].split("\t");

                if (words[0].equals("BogoMIPS")) {
                    cpuFreq = Double.parseDouble(toks[1]);
                }
            }

            reader.close();

        } catch (IOException ioe) {
            Log.e(TAG, "Exception parsing /proc/cpuinfo", ioe);
        }

        try {
            BufferedReader reader = new BufferedReader(new
                    InputStreamReader(new
                    FileInputStream("/proc/stat")), 2048);

            while ((line = reader.readLine()) != null) {
                toks = line.split(" ");

                if (toks[0].equals("cpu")) {
                    long currUser, currNice, currSystem, currTotal,
                            currIdle;

                    JSONObject cpuObject = new JSONObject();

                    currUser = Long.parseLong(toks[2]);
                    currNice = Long.parseLong(toks[3]);
                    currSystem = Long.parseLong(toks[4]);
                    currTotal = currUser + currNice + currSystem;
                    currIdle = Long.parseLong(toks[5]);

                    totalUsage = (currTotal - sTotal) * 100.0f /
                            (currTotal - sTotal + currIdle - idle);
                    userUsage = (currUser - user) * 100.0f /
                            (currTotal - sTotal + currIdle - idle);
                    niceUsage = (currNice - nice) * 100.0f /
                            (currTotal - sTotal + currIdle - idle);
                    systemUsage = (currSystem - system) * 100.0f /
                            (currTotal - sTotal + currIdle - idle);


                    sTotal = currTotal;
                    idle = currIdle;
                    user = currUser;
                    nice = currNice;
                    system = currSystem;

                    // Update the Status Object
                    //Status.setCPU(totalUsage);

                    try {
                        cpuObject.put("total", totalUsage);
                        cpuObject.put("user", userUsage);
                        cpuObject.put("nice", niceUsage);
                        cpuObject.put("system", systemUsage);
                        cpuObject.put("freq", cpuFreq);

                        result.put("cpu", cpuObject);

                    } catch (JSONException je) {
                        Log.e(TAG, "Exception", je);
                    }
                } else if (toks[0].equals("ctxt")) {
                    String ctxt = toks[1];

                    try {
                        result.put("ContextSwitch", ctxt);
                    } catch (JSONException je) {
                        Log.e(TAG, "Exception", je);
                    }
                } else if (toks[0].equals("btime")) {
                    String btime = toks[1];

                    try {
                        result.put("BootTime", btime);
                    } catch (JSONException je) {
                        Log.e(TAG, "Exception", je);
                    }
                } else if (toks[0].equals("processes")) {
                    String procs = toks[1];

                    try {
                        result.put("Processes", procs);
                    } catch (JSONException je) {
                        Log.e(TAG, "Exception", je);
                    }
                }

            }

            reader.close();

        } catch (IOException ex) {
            Log.e(TAG, "Could not read /proc file", ex);
        }

        return result;
    }
}