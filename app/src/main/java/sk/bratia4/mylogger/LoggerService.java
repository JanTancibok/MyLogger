package sk.bratia4.mylogger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.os.Handler;
import java.util.logging.LogRecord;

public class LoggerService extends Service {
    private NotificationManager mNM;

    //used for getting the handler from other class for sending messages
    public static Handler mMyServiceHandler = null;
    //used for keep track on Android running status
    public static Boolean mIsServiceRunning = false;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.service_started;

    StringBuilder log;
    private ArrayList<String> list = new ArrayList<String>();

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class MyBinder extends Binder {
        LoggerService getService() {
            return LoggerService.this;
        }
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);

        mIsServiceRunning = false;

        // Tell the user we stopped.
        Toast.makeText(this, R.string.service_stopped, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new MyBinder();

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.service_started);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.abc_ic_go_search_api_mtrl_alpha, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MyMainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.logger_service_name),
                text, contentIntent);

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        MyThread myThread = new MyThread();
        myThread.start();

        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        mIsServiceRunning = true; // set service running status = true

        LoggingThread lthr = new LoggingThread();
        lthr.run();

        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        return Service.START_STICKY;
    }

    private void startLogging(){
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            log = new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
                log.append('\n');
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Your inner thread class is here to getting response from Activity and processing them
    class MyThread extends Thread
    {
        private static final String INNER_TAG = "MyThread";

        public void run()
        {

            this.setName(INNER_TAG);

            // Prepare the looper before creating the handler.
            Looper.prepare();

            Log.i("looper","handleMessage");

            mMyServiceHandler = new Handler()
            {
                //here we will receive messages from activity(using sendMessage() from activity)
                public void handleMessage(Message msg)
                {
                    Log.i("BackgroundThread","handleMessage(Message msg)" );
                    switch(msg.what)
                    {
                        case 0: // we sent message with what value =0 from the activity. here it is
                            //Reply to the activity from here using same process handle.sendMessage()
                            //So first get the Activity handler then send the message
                            if(null != MyMainActivity.mUiHandler)
                            {
                                //first build the message and send.
                                //put a integer value here and get it from the Activity handler
                                //For Example: lets use 0 (msg.what = 0;)
                                //for receiving service running status in the activity
                                Message msgToActivity = new Message();
                                msgToActivity.what = 0;
                                if(true ==mIsServiceRunning)
                                    msgToActivity.obj  = "Request Received. Service is Running"; // you can put extra message here
                                else
                                    msgToActivity.obj  = "Request Received. Service is not Running"; // you can put extra message here

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
    class LoggingThread extends Thread
    {
        private static final String INNER_TAG = "LoggingThread";

        public void run()
        {

            startLogging();

            this.setName(INNER_TAG);

            Log.i("LoggingThread", "do log");

            Message msgToActivity = new Message();
            msgToActivity.what = 0;

            msgToActivity.obj = log.toString(); // you can put extra message here

            MyMainActivity.mUiHandler.sendMessage(msgToActivity);

        }
    }
}
