package sk.bratia4.mylogger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class LinuxService extends Service {
    private static final String TAG = "LinuxService";
    private NotificationManager mNM;
    private int NOTIFICATION = R.string.virdir_service_started;
    private String SDCardPath = "";

    private static final long DELAY = 1000 * 5;

    //used for getting the handler from other class for sending messages
    public static Handler mMyServiceHandler = null;
    //used for keep track on Android running status
    public static Boolean mIsServiceRunning = false;

    StringBuilder virtual_log;

    public LinuxService() {
        SDCardPath = getExternalDir();
        if (SDCardPath == null) {
            Log.e(TAG, "SdCard dir can not be resolved!");
        }
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }

    public class MyBinder extends Binder {
        LinuxService getService() {
            return LinuxService.this;
        }
    }

    private final IBinder mBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        CharSequence text = getText(R.string.virdir_service_started);
        Notification notification = new Notification(R.drawable.abc_spinner_mtrl_am_alpha, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MyMainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.virdir_service_name),
                text, contentIntent);

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        MyThread myThread = new MyThread();
        myThread.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mIsServiceRunning = true; // set service running status = true

        LoggingThread lthr = new LoggingThread();
        lthr.run();

        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        return Service.START_STICKY;
    }

    //Your inner thread class is here to getting response from Activity and processing them
    class MyThread extends Thread {
        private static final String INNER_TAG = "MyVFThread";

        public void run() {
            this.setName(INNER_TAG);
            // Prepare the looper before creating the handler.
            Looper.prepare();
            Log.i("looper", "handleMessage");

            mMyServiceHandler = new Handler() {
                //here we will receive messages from activity(using sendMessage() from activity)
                public void handleMessage(Message msg) {
                    Log.i("BackgroundThread", "handleMessage(Message msg)");
                    switch (msg.what) {
                        case 0: // we sent message with what value =0 from the activity. here it is
                            //Reply to the activity from here using same process handle.sendMessage()
                            //So first get the Activity handler then send the message
                            if (null !=  MyMainActivity.mUiHandler) {
                                //first build the message and send.
                                //put a integer value here and get it from the Activity handler
                                //For Example: lets use 0 (msg.what = 0;)
                                //for receiving service running status in the activity
                                Message msgToActivity = new Message();
                                msgToActivity.what = 0;
                                if (true == mIsServiceRunning)
                                    msgToActivity.obj = "Request Received. Service is Running"; // you can put extra message here
                                else
                                    msgToActivity.obj = "Request Received. Service is not Running"; // you can put extra message here

                                MyMainActivity.mUiHandler.sendMessage(msgToActivity);
                            }

                            break;

                        default:
                            break;
                    }
                }
            };
            Looper.loop();
        }
    }

    //Your inner thread class is here to getting response from Activity and processing them
    class LoggingThread extends Thread {
        private static final String INNER_TAG = "VirtualDirectoryThread";

        public void run() {

            //startVirtualLogging();


            this.setName(INNER_TAG);
            Log.i("VirtualDirectoryThread", "listening started !!");

            /*Message msgToActivity = new Message();
            msgToActivity.what = 0;

            msgToActivity.obj = virtual_log.toString(); // you can put extra message here

            MyMainActivity.mUiHandler.sendMessage(msgToActivity);*/

        }
    }

    public static boolean canRunRootCommands() {
        boolean retval = false;
        Process suProcess;

        try {
            suProcess = Runtime.getRuntime().exec("su");

            DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
            DataInputStream osRes = new DataInputStream(suProcess.getInputStream());

            if (null != os && null != osRes) {
                // Getting the id of the current user to check if this is root
                os.writeBytes("id\n");
                os.flush();

                String currUid = osRes.readLine();
                boolean exitSu = false;
                if (null == currUid) {
                    retval = false;
                    exitSu = false;
                    Log.d("ROOT", "Can't get root access or denied by user");
                } else if (true == currUid.contains("uid=0")) {
                    retval = true;
                    exitSu = true;
                    Log.d("ROOT", "Root access granted");
                } else {
                    retval = false;
                    exitSu = true;
                    Log.d("ROOT", "Root access rejected: " + currUid);
                }

                if (exitSu) {
                    os.writeBytes("exit\n");
                    os.flush();
                }
            }
        } catch (Exception e) {
            // Can't get root !
            // Probably broken pipe exception on trying to write to output stream (os) after su failed, meaning that the device is not rooted
            retval = false;
            Log.d("ROOT", "Root access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
        }

        return retval;
    }

    public final boolean executeRoot() {
        boolean retval = false;

        try {
            ArrayList<String> commands = getCommandsToExecute();
            if (null != commands && commands.size() > 0) {
                Process suProcess = Runtime.getRuntime().exec("su"); //su

                DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());

                // Execute commands that require root access
                for (String currCommand : commands) {
                    os.writeBytes(currCommand + "\n");
                    os.flush();
                }

                os.writeBytes("exit\n");
                os.flush();

                try {
                    int suProcessRetval = suProcess.waitFor();
                    if (255 != suProcessRetval) {
                        // Root access granted
                        retval = true;
                    } else {
                        // Root access denied
                        retval = false;
                    }
                } catch (Exception ex) {
                    Log.e("ROOT", "Error executing root action", ex);
                }
            }
        } catch (IOException ex) {
            Log.w("ROOT", "Can't get root access", ex);
        } catch (SecurityException ex) {
            Log.w("ROOT", "Can't get root access", ex);
        } catch (Exception ex) {
            Log.w("ROOT", "Error executing internal operation", ex);
        }

        return retval;
    }


    private ArrayList<String> getCommandsToExecute() {
        ArrayList<String> as = new ArrayList<String>();
        //as.add("cat /sdcard/test_log.txt > /sdcard/test_log_zaloha.txt");
        as.add("cat pwd >> " + SDCardPath + "/test_pwd.txt");
        as.add("cat echo '" + SDCardPath + "\n' >> " + SDCardPath + "/test_pwd.txt");
        as.add("cat /proc/net/wireless >> " + SDCardPath + "/test_log_wireless.txt");
        as.add("cat /proc/net/dev >> " + SDCardPath + "/test_log_dev.txt");
        as.add("cat /proc/net/raw >> " + SDCardPath + "/test_log_raw.txt");
        as.add("cat /proc/net/tcp >> " + SDCardPath + "/test_log_tcp.txt");
        as.add("cat /proc/net/udp >> " + SDCardPath + "/test_log_udp.txt");
        as.add("cat /proc/net/arp >> " + SDCardPath + "/test_log_arp.txt");
        return as;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private String getExternalDir() {
        final String state = Environment.getExternalStorageState();
        String resultPath = null;

        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {  // we can read the External Storage...
            //Retrieve the primary External Storage:
            final File primaryExternalStorage = Environment.getExternalStorageDirectory();

            //Retrieve the External Storages root directory:
            final String externalStorageRootDir;
            if ((externalStorageRootDir = primaryExternalStorage.getParent()) == null) {  // no parent...
                Log.i(TAG, "External Storage: " + primaryExternalStorage + "\n");
                resultPath = primaryExternalStorage.getAbsolutePath();
            } else {
                final File externalStorageRoot = new File(externalStorageRootDir);
                final File[] files = externalStorageRoot.listFiles();

                for (final File file : files) {
                    if (file.isDirectory() && file.canRead() && (file.listFiles().length > 0)) {  // it is a real directory (not a USB drive)...
                        Log.i(TAG, "External Storage: " + file.getAbsolutePath() + "\n");
                        resultPath = file.getAbsolutePath();
                    }
                }
            }
        }

        return resultPath;
    }

    private void execute(ArrayList<String> com) {
        if (null != com && com.size() > 0) {
            try {
                for (int i = 0; com.size() > i; i++) {
                    Process process = Runtime.getRuntime().exec(com.get(i).toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startVirtualLogging() {
       boolean roted = canRunRootCommands();

       if (roted) {
           Log.i("LinuxService","Phone is rooted!");
       }
       //Loop all running processes BETTER all files

       /*for (ActivityManager.RunningAppProcessInfo pid : am.getRunningAppProcesses()) {
          //pid.processName
          getFd(Integer.toString(pid.pid));
       }*/

       //getNetDev("/proc/net/dev");

       //make some timer
       /*while(mIsServiceRunning){

           try {
               Thread.sleep(delay);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       }*/
    }

    HashMap<String,Integer> fdSocket = new HashMap<String,Integer>();
    public void getFd(String pid){
        StringTokenizer linest;
        String zero, fdesc;
        StringBuffer lineBuff = new StringBuffer();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader( new
                    InputStreamReader( new FileInputStream("/proc/"+pid+"/fd")), 2048 );


            char[] buffer = new char[2024];
            reader.read(buffer, 0, 2000);

            StringTokenizer st = new StringTokenizer(
                    new String(buffer), "\n", false);

            //The first two lines of the file are headers
            zero = st.nextToken();

            while((linest = new StringTokenizer(st.nextToken()))!=null){

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);

        mIsServiceRunning = false;

        // Tell the user we stopped.
        Toast.makeText(this, R.string.virdir_service_stopped, Toast.LENGTH_SHORT).show();
    }
}
