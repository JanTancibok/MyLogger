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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private static final String PARAM1 = "sk.bratia4.mylogger.services.extra.PARAM1";
    private static final String TAG = "AppInfo_Log";

    private final String applog_dir = "app";
    private final String applog_file = "installed_apps.csv";
    private final String perinfo_file = "per_info.csv";
    private final String activityinfo_file = "activity_info.csv";
    private final String serviceinfo_file = "service_info.csv";
    private final String rcvinfo_file = "receiver_info.csv";
    private final String RUNAPP_FILE = "running_app#_list.csv";

    public AppInfoService() {
        super("AppInfoService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GETAPP.equals(action)) {
                final String param1 = intent.getStringExtra(PARAM1);
                startAppLog(param1);
            }
            if (ACTION_RUNAPP.equals(action)) {
                this.logactualRunningApps();
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
    private void startAppLog(String param1) {
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
                    logAppInfo(file2);
                }
                //else instaled apps are atualised by broadcast receiveer

                logactualRunningApps();

            }
        } else {
            Log.e(TAG, "Cant write to SDcard");
        }
    }

    private void logAppInfo(File file2) {
        StringBuilder lineBuff = new StringBuilder();
        StringBuilder perLB = new StringBuilder();
        StringBuilder recLB = new StringBuilder();
        StringBuilder appLB = new StringBuilder();
        StringBuilder serLB = new StringBuilder();

        lineBuff.append("TimeUTC;UID;Name;ClasName;Permission;lastUpdateTime");
        perLB.append("TimeUTC;UID;packadgeName;name;group");
        recLB.append("TimeUTC;UID;packadgeName;name;permission;TargetActivity;taskAffinity;processName");
        appLB.append("TimeUTC;UID;name");
        serLB.append("TimeUTC;UID;name");

        Time now = new Time("UTC");
        now.setToNow();

        PackageManager pm = getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        List<PackageInfo> packagesSR = pm.getInstalledPackages(PackageManager.GET_RECEIVERS + PackageManager.GET_SERVICES + PackageManager.GET_META_DATA + PackageManager.GET_PERMISSIONS);

        //GET_UNINSTALLED_PACKAGES
        for (PackageInfo packageInfo : packages) {
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

            if (packageInfo.activities != null) {
                for (ActivityInfo ai : packageInfo.activities) {
                    appLB.append(now.format2445() + ";");
                    appLB.append(ai.applicationInfo.uid);
                    appLB.append(";");
                    appLB.append(ai.name);
                    appLB.append("\n");
                }
            }
        }
        for (PackageInfo packageInfo : packagesSR) {
                        /*List<ActivityInfo> appInfo = packageInfo*/
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
            if (packageInfo.services != null) {
                for (ServiceInfo ai : packageInfo.services) {
                    serLB.append(now.format2445() + ";");
                    serLB.append(ai.applicationInfo.uid + ";");
                    serLB.append(ai.name + "\n");
                }
            }
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
        }

        appendToFile(perLB.toString(), file2.getAbsolutePath() + "/" + perinfo_file);
        appendToFile(lineBuff.toString(), file2.getAbsolutePath() + "/" + applog_file);
        appendToFile(recLB.toString(), file2.getAbsolutePath() + "/" + rcvinfo_file);
        appendToFile(appLB.toString(), file2.getAbsolutePath() + "/" + activityinfo_file);
        appendToFile(serLB.toString(), file2.getAbsolutePath() + "/" + serviceinfo_file);
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
                        uidLine.append("Time;Pid;sTime;uTime;MemoryInfo-dalvikPrivateDirty;dalvikPss;dalvikSharedDirty;" +
                                "nativePrivateDirty;nativePss;nativeSharedDirty;otherPrivateDirty;otherPss;otherSharedDirty" +
                                "TotalPrivateDirty;TotalSharedDirty\n");
                    }

                    RandomAccessFile rifle = null;
                    try {
                        rifle = new RandomAccessFile("/proc/" + poleintov[i] + "/stat", "r");
                        uidLine.append(rifle.readInt() + ";");
                        rifle.close();

                        rifle = new RandomAccessFile("/proc/" + poleintov[i] + "/stat", "r");
                        uidLine.append(rifle.readInt() + ";");
                        rifle.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    uidLine.append(mei[i].dalvikPrivateDirty);
                    uidLine.append(mei[i].dalvikPss);
                    uidLine.append(mei[i].dalvikSharedDirty);
                    uidLine.append(mei[i].nativePrivateDirty);
                    uidLine.append(mei[i].nativePss);
                    uidLine.append(mei[i].nativeSharedDirty);
                    uidLine.append(mei[i].otherPrivateDirty);
                    uidLine.append(mei[i].otherPss);
                    uidLine.append(mei[i].otherSharedDirty);
                    //kikat uidLine.append(mei[i].getTotalPrivateClean());
                    //kikat uidLine.append(mei[i].getTotalSharedClean());
                    uidLine.append(mei[i].getTotalPrivateDirty());
                    uidLine.append(mei[i].getTotalSharedDirty());

                    i++;
                }

            } else Log.d(TAG, "Non writable storage");
        } else {
            Log.d(TAG, "No app is running");
        }
    }
}
