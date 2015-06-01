package eu.mcomputing.syslogger.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import eu.mcomputing.syslogger.MyMainActivity;

public class AlarmStarterService extends IntentService {

    public AlarmStarterService() {
        super("AlarmStarterService");
    }

    @Override
    public void onCreate()
    {
        // TODO Auto-generated method stub
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @SuppressWarnings("static-access")
    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent!=null){
            if(intent.getAction()=="0"){stopLog();}
            if(intent.getAction()=="1"){startLog();}
        }
    }

    @Override
    public void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    public void startLog(){
        //run just once
        SharedPreferences preferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        boolean first = preferences.getBoolean("run_app_info", true);
        String deviceID = preferences.getString("deviceID", "noname_pref");

        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, NetDevService.class);
        intent.putExtra(MyMainActivity.DEVICE_ID,deviceID);
        PendingIntent pintent = PendingIntent.getService(this, 2580, intent, 0);

        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), (20 * 60000), pintent);

        ///////////////////
        if(first) {
            Intent apintent = new Intent(this, AppInfoService.class);
            apintent.setAction(AppInfoService.ACTION_GETAPP);
            PendingIntent appintent = PendingIntent.getService(this, 1234, apintent, 0);
            try {
                appintent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
        //this.startService(appintent);

        //alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), (30*60*1000), appintent);
        //run every time
        Intent runintent = new Intent(this, AppInfoService.class);
        runintent.setAction(AppInfoService.ACTION_RUNAPP);
        PendingIntent runpintent = PendingIntent.getService(this, 1818, runintent, 0);

        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), (45 * 60000), runpintent);
        preferences.edit().putBoolean("run_app_info", false).apply();
    }

    public void stopLog() {
        SharedPreferences preferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String deviceID = preferences.getString("deviceID", "noname_pref");

        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, NetDevService.class);
        intent.putExtra(MyMainActivity.DEVICE_ID,deviceID);
        PendingIntent    pintent = PendingIntent.getService(this, 2580, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarm.cancel(pintent);

        Intent apintent = new Intent(this, AppInfoService.class);
        apintent.setAction(AppInfoService.ACTION_GETAPP);
        PendingIntent appintent = PendingIntent.getService(this, 1234, apintent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarm.cancel(appintent);

        //if (runpintent==null) {
        Intent runintent = new Intent(this, AppInfoService.class);
        runintent.setAction(AppInfoService.ACTION_RUNAPP);
        PendingIntent   runpintent = PendingIntent.getService(this, 1818, runintent, PendingIntent.FLAG_CANCEL_CURRENT);
        //}
        alarm.cancel(runpintent);
    }
}
