package sk.bratia4.mylogger.services;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ServiceInfo;
import android.os.Debug;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class AppInfoService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_GETAPP = "sk.bratia4.mylogger.services.action.AppLog";
    public static final String ACTION_UPDATEAPP = "sk.bratia4.mylogger.services.action.AppLogUpdate";
    public static final String ACTION_RUNAPP = "sk.bratia4.mylogger.services.action.AppLogRun";
    public static final String ACTION_REMOVEAPP = "sk.bratia4.mylogger.services.action.AppLogRemoved";

    public static final String EXTRA_DATA_REMOVED = "sk.bratia4.mylogger.services.extra.DATA_REMOVED";
    public static final String EXTRA_UID = "sk.bratia4.mylogger.services.extra.UID";
    public static final String EXTRA_REPLACE = "sk.bratia4.mylogger.services.extra.REPLACE";
    private static final String TAG = "AppInfo_Log";

    private final String applog_dir = "app";
    private final String applog_file = "installed_apps.csv";
    private final String perinfo_file = "per_info.csv";
    private final String activityinfo_file = "activity_info.csv";
    private final String serviceinfo_file = "service_info.csv";
    private final String rcvinfo_file = "receiver_info.csv";
    private final String RUNAPP_FILE = "running_app#_list.csv";
    private final String CPU_INFO = "cpu_info.csv";
    private final String REMOVED_APPS = "removed_apps.csv";

    public AppInfoService() {
        super("AppInfoService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GETAPP.equals(action)) {
                startAppLog();
            }
            if (ACTION_RUNAPP.equals(action)) {
                this.logactualRunningApps();
            }
            if (ACTION_UPDATEAPP.equals(action)) {
                final int param1 = intent.getIntExtra(EXTRA_UID, -1);
                final boolean param2 = intent.getBooleanExtra(EXTRA_REPLACE, false);
                File pathToSd = Environment.getExternalStorageDirectory();
                if (isExternalStorageWritable()) {
                    logAppInfo(new File(pathToSd,"LOGS/" + applog_dir), param1); //applog dir should be created already
                }
            }
            if (ACTION_REMOVEAPP.equals(action)) {
                final int param1 = intent.getIntExtra(EXTRA_UID, -1);
                final boolean param2 = intent.getBooleanExtra(EXTRA_DATA_REMOVED, false);
                this.logRemovedApp(param1,param2);
            }
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void appendToFile(String what, String path) {
        try {
            File logfile = new File(path);
            FileOutputStream stream = new FileOutputStream(logfile, true);
            try {
                stream.write(what.getBytes());
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle action in the provided background thread with the provided
     * parameters.
     */
    private void startAppLog() {
        Log.d(TAG, "app log running");

        if (isExternalStorageWritable()) {
            // Get the directory for the app's private pictures directory.
            File pathToSd = Environment.getExternalStorageDirectory();
            File file = new File(pathToSd, "LOGS");
            file.mkdirs();
            File file2 = new File(file.getAbsolutePath(), applog_dir);
            file2.mkdirs();
            if (!file2.exists()) {
                Log.e(TAG, "Directory not created");
            } else {
                File logfile = new File(file2.getAbsolutePath() + "/" + applog_file);
                if (!logfile.exists()) {
                    logAppInfo(file2,-1);
                    logCPU(file);
                }
                //else instaled apps are atualised by broadcast receiveer

                logactualRunningApps();
            }
        } else {
            Log.e(TAG, "Cant write to SDcard");
        }
    }



    private String parse_packadgeInfo(PackageInfo packageInfo){
       Time now = new Time("UTC");
       now.setToNow();
       StringBuilder lineBuff = new StringBuilder();

       lineBuff.append(now.format2445() + ";");
       lineBuff.append(packageInfo.applicationInfo.uid);
       lineBuff.append(";");
       lineBuff.append(packageInfo.packageName);
       lineBuff.append(";");
       lineBuff.append(packageInfo.applicationInfo.className);
       lineBuff.append(";");
       lineBuff.append(packageInfo.applicationInfo.permission);
       lineBuff.append(";");
       lineBuff.append(packageInfo.lastUpdateTime);
       lineBuff.append("\n");

       return lineBuff.toString();
    }

    private String parse_activity(PackageInfo packageInfo){
        StringBuilder appLB = new StringBuilder();
        Time now = new Time("UTC");
        now.setToNow();
        if (packageInfo.activities != null) {
            for (ActivityInfo ai : packageInfo.activities) {
                appLB.append(now.format2445() + ";");
                appLB.append(ai.applicationInfo.uid);
                appLB.append(";");
                appLB.append(ai.name);
                appLB.append("\n");
            }
        }
        return appLB.toString();
    }

    private String parse_service(PackageInfo packageInfo){
        StringBuilder serLB = new StringBuilder();
        Time now = new Time("UTC");
        now.setToNow();
        if (packageInfo.services != null) {
            for (ServiceInfo ai : packageInfo.services) {
                serLB.append(now.format2445() + ";");
                serLB.append(ai.applicationInfo.uid + ";");
                serLB.append(ai.name + "\n");
            }
        }
        return serLB.toString();
    }

    private String parse_permission(PackageInfo packageInfo){
        StringBuilder perLB = new StringBuilder();
        Time now = new Time("UTC");
        now.setToNow();
        if (packageInfo.permissions != null) {
            for (PermissionInfo ai : packageInfo.permissions) {
                perLB.append(now.format2445() + ";");
                perLB.append(packageInfo.applicationInfo.uid);
                perLB.append(";");
                perLB.append(ai.packageName);
                perLB.append(";");
                perLB.append(ai.name);
                perLB.append(";");
                perLB.append(ai.group);
                perLB.append(";");
                perLB.append(ai.nonLocalizedDescription);
                perLB.append("\n");
            }
        }
        return perLB.toString();
    }
    private String parse_receivers(PackageInfo packageInfo){
        StringBuilder recLB = new StringBuilder();
        Time now = new Time("UTC");
        now.setToNow();
        if (packageInfo.receivers != null) {
            for (ActivityInfo ai : packageInfo.receivers) {
                recLB.append(now.format2445() + ";");
                recLB.append(packageInfo.applicationInfo.uid + ";");
                recLB.append(ai.packageName + ";");
                recLB.append(ai.name + ";");
                recLB.append(ai.permission + ";");
                recLB.append(ai.targetActivity + ";");
                recLB.append(ai.taskAffinity + ";");
                recLB.append(ai.processName + ";");
                recLB.append("\n");
            }
        }
        return recLB.toString();
    }

    private void logAppInfo(File directory, int uid) {
        StringBuilder lineBuff = new StringBuilder();
        StringBuilder perLB = new StringBuilder();
        StringBuilder recLB = new StringBuilder();
        StringBuilder appLB = new StringBuilder();
        StringBuilder serLB = new StringBuilder();

        PackageManager pm = getPackageManager();

        if (uid==-1) {
            lineBuff.append("TimeUTC;UID;Name;ClasName;Permission;lastUpdateTime\n");
            perLB.append("TimeUTC;UID;packadgeName;name;group\n");
            recLB.append("TimeUTC;UID;packadgeName;name;permission;TargetActivity;taskAffinity;processName\n");
            appLB.append("TimeUTC;UID;name\n");
            serLB.append("TimeUTC;UID;name\n");

            List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
            List<PackageInfo> packagesSR = pm.getInstalledPackages(PackageManager.GET_RECEIVERS + PackageManager.GET_SERVICES + PackageManager.GET_META_DATA + PackageManager.GET_PERMISSIONS);

            //GET_UNINSTALLED_PACKAGES
            for (PackageInfo packageInfo : packages) {
                lineBuff.append(parse_packadgeInfo(packageInfo));
                appLB.append(parse_activity(packageInfo));
            }

            for (PackageInfo packageInfo : packagesSR) {
                serLB.append(parse_service(packageInfo));
                perLB.append(parse_permission(packageInfo));
                recLB.append(parse_receivers(packageInfo));
            }
        }else{
            //uid!=-1 we are doing update
            PackageInfo packageInfo = null;
            String packadgeName = "";

            List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_META_DATA);
            for (PackageInfo pi : packages) {
                if(pi.applicationInfo.uid==uid){
                    packadgeName = pi.packageName;
                    break;
                }
            }

            try {
                packageInfo = pm.getPackageInfo(packadgeName, PackageManager.GET_ACTIVITIES+PackageManager.GET_RECEIVERS + PackageManager.GET_SERVICES + PackageManager.GET_META_DATA + PackageManager.GET_PERMISSIONS);
                lineBuff.append(parse_packadgeInfo(packageInfo));
                appLB.append(parse_activity(packageInfo));
                serLB.append(parse_service(packageInfo));
                perLB.append(parse_permission(packageInfo));
                recLB.append(parse_receivers(packageInfo));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        appendToFile(perLB.toString(), directory.getAbsolutePath() + "/" + perinfo_file);
        appendToFile(lineBuff.toString(), directory.getAbsolutePath() + "/" + applog_file);
        appendToFile(recLB.toString(), directory.getAbsolutePath() + "/" + rcvinfo_file);
        appendToFile(appLB.toString(), directory.getAbsolutePath() + "/" + activityinfo_file);
        appendToFile(serLB.toString(), directory.getAbsolutePath() + "/" + serviceinfo_file);
    }

    private void logCPU(File fio){
        try {
            BufferedReader reader = new BufferedReader(new
                    InputStreamReader(new
                    FileInputStream("/proc/cpuinfo")), 2048);
            String line;
            String[] toks;
            String[] words;
            StringBuilder cpuBuff = new StringBuilder("cpuid;BogoMIPS\n");
            int nocores =0;
            while ((line = reader.readLine()) != null) {
                toks = line.split(" ");
                words = toks[0].split("\t");

                if (words[0].equals("BogoMIPS")) {
                    cpuBuff.append("cpu"+nocores);
                    cpuBuff.append(";");
                    cpuBuff.append(Double.parseDouble(toks[1]));
                    cpuBuff.append("\n");
                    nocores++;
                }
            }
            appendToFile(cpuBuff.toString(), fio.getAbsolutePath() + "/" + CPU_INFO);
            reader.close();
        } catch (IOException ioe) {
            Log.e(TAG, "Parse /proc/cpuinfo", ioe);
        }
    }

    private void logactualRunningApps() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = manager.getRunningAppProcesses();

        if (runningProcesses != null && runningProcesses.size() > 0) {
            Time now = new Time("UTC");
            now.setToNow();
            if (isExternalStorageWritable()) {

                ArrayList<Integer> pids = new ArrayList<Integer>();

                File pathToSd = Environment.getExternalStorageDirectory();
                File file = new File(pathToSd, "LOGS");
                file.mkdirs();
                File file2 = new File(file.getAbsolutePath(), applog_dir);
                file2.mkdirs();
                if (!file2.exists()) {
                    Log.e(TAG, "Directory not created");
                } else {
                    File logfile = new File(file2.getAbsolutePath() + "/" + RUNAPP_FILE);
                    StringBuilder sb = new StringBuilder();
                    if (!logfile.exists()) {
                        sb.append("Time;CountRunning;[UID1#UID2#...#UIDn\n");
                    }
                    StringBuilder implode = new StringBuilder();

                    for (ActivityManager.RunningAppProcessInfo pi : runningProcesses) {
                        pids.add(pi.pid);
                        implode.append(pi.uid);
                        /*pi.pid
                        pi.importance*/
                        implode.append("#");
                    }

                    sb.append(now.format2445());
                    sb.append(";");
                    sb.append(runningProcesses.size());
                    sb.append(";");
                    sb.append(implode.toString().substring(0, implode.length() - 1));
                    sb.append("\n");

                    appendToFile(sb.toString(), file2.getAbsolutePath() + "/" + RUNAPP_FILE);
                }

                int[] poleintov = new int[pids.size()];
                int i = 0;
                for (int n : pids) {
                    poleintov[i++] = n;
                }
                Debug.MemoryInfo mei[] = manager.getProcessMemoryInfo(poleintov);

                i = 0;
                for (ActivityManager.RunningAppProcessInfo pi : runningProcesses) {

                    File procesfile = new File(file2.getAbsolutePath() + "/" + pi.uid + ".csv");
                    StringBuilder uidLine = new StringBuilder();

                    if (!procesfile.exists()) {
                        uidLine.append("Time;Name;Pid;State;ppid;uTime;sTime;cutime;cstime;starttime;virtualmem;rss;" +
                                "MemoryInfo-dalvikPrivateDirty;dalvikPss;dalvikSharedDirty;" +
                                "nativePrivateDirty;nativePss;nativeSharedDirty;otherPrivateDirty;otherPss;otherSharedDirty" +
                                "TotalPrivateDirty;TotalSharedDirty\n");
                    }

                    uidLine.append(now.format2445());//time of call this method

                    RandomAccessFile rifle = null;
                    try {
                        rifle = new RandomAccessFile("/proc/" + poleintov[i] + "/stat", "r");
                        String line = rifle.readLine();
                        rifle.close();
                        String[] stat = line.split("\\s");

                        if(Integer.decode(stat[0])!=poleintov[i]){
                            Log.e(TAG,"Something is wrong: "+stat[0]+" != "+poleintov[i]);
                        }

                        uidLine.append(stat[0] + ";");//pid
                        uidLine.append(stat[1] + ";");//name
                        uidLine.append(stat[2] + ";");//state R  Running, S  Sleeping, D  Waiting disk sleep, Z  Zombie, T  Stopped
                        uidLine.append(stat[3] + ";");//parent pid
                        uidLine.append(stat[14] + ";");//utime
                        uidLine.append(stat[15] + ";");//stime
                        uidLine.append(stat[16] + ";");//cutime
                        uidLine.append(stat[17] + ";");//cstime
                        uidLine.append(stat[22] + ";");//starttime
                        uidLine.append(stat[23] + ";");//virtual mem
                        uidLine.append(stat[24] + ";");//rss
                        //(36) nswap
                        //(37) cnswap
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    uidLine.append(mei[i].dalvikPrivateDirty);uidLine.append(";");
                    uidLine.append(mei[i].dalvikPss);uidLine.append(";");
                    uidLine.append(mei[i].dalvikSharedDirty);uidLine.append(";");
                    uidLine.append(mei[i].nativePrivateDirty);uidLine.append(";");
                    uidLine.append(mei[i].nativePss);uidLine.append(";");
                    uidLine.append(mei[i].nativeSharedDirty);uidLine.append(";");
                    uidLine.append(mei[i].otherPrivateDirty);uidLine.append(";");
                    uidLine.append(mei[i].otherPss);uidLine.append(";");
                    uidLine.append(mei[i].otherSharedDirty);uidLine.append(";");
                    //kikat uidLine.append(mei[i].getTotalPrivateClean());
                    //kikat uidLine.append(mei[i].getTotalSharedClean());
                    uidLine.append(mei[i].getTotalPrivateDirty());uidLine.append(";");
                    uidLine.append(mei[i].getTotalSharedDirty());
                    uidLine.append("\n");

                    appendToFile(uidLine.toString(),procesfile.getPath());

                    i++;
                }

            } else Log.d(TAG, "Non writable storage");
        } else {
            Log.d(TAG, "No app is running");
        }
    }

    private void logRemovedApp(int uid, boolean dataremoved){
        File pathToSd = Environment.getExternalStorageDirectory();
        if (isExternalStorageWritable()) {
            File logfile = new File(pathToSd+"/LOGS/"+applog_dir+"/"+REMOVED_APPS);
            if (!logfile.exists()) {
                appendToFile("Time;UID;DataRemoved\n",logfile.getPath());
            }
            Time now = new Time("UTC");
            now.setToNow();
            appendToFile(now.format2445()+";"+String.valueOf(uid)+";"+String.valueOf(dataremoved)+"\n",logfile.getPath());
        }
    }
}
