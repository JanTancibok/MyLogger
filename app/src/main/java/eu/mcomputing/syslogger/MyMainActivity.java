package eu.mcomputing.syslogger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.support.v4.app.Fragment;

import java.io.File;
import java.util.List;
import android.os.Handler;
import android.widget.ToggleButton;

import eu.mcomputing.syslogger.screen.LinuxLogFragment;
import eu.mcomputing.syslogger.screen.LogCatFragment;
import eu.mcomputing.syslogger.screen.MenuFragment;
import eu.mcomputing.syslogger.screen.VPN_fragment;
import eu.mcomputing.syslogger.services.LoggerService;
import eu.mcomputing.syslogger.services.VPN_logger_service;
import eu.mcomputing.syslogger.utils.NmapBinaryInstaller;


public class MyMainActivity extends FragmentActivity implements LogCatFragment.OnFragmentInteractionListener, LinuxLogFragment.OnFragmentInteractionListener, VPN_fragment.OnFragmentInteractionListener {
    private LoggerService s;
    private ArrayAdapter<String> adapter;
    private List<String> wordList;

    boolean mIsBound = false;
    private NotificationManager mNM;
    private String deviceID;

    public int getNetDevRunning() {
        return netDevRunning;
    }
    public static File getbinPath() {
        return binPath;
    }

    private static File binPath = null;
    private int netDevRunning = 0;
    private int NOTIFICATION = R.string.virdir_service_started;
    public static Handler mUiHandler = null;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void changeFragment(Fragment fr){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragmentPlace, fr);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    protected void  onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            //netDevRunning = savedInstanceState.getInt("run", 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            //netDevRunning = savedInstanceState.getInt("run", 0);
        }

        boolean first = true;
        //first Create Install
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        first = preferences.getBoolean("first",true);
        if(first){
            NmapBinaryInstaller installer = new NmapBinaryInstaller(getApplicationContext());
            installer.installResources();
            Log.i("aktivita", "installer started");
            preferences.edit().putBoolean("first", false).commit();

            //create device ID
            TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
            deviceID = this.getDeviceName()+" "+ tm.getDeviceId();
            preferences.edit().putString("deviceID", deviceID).commit();
        }else{
            Log.i("aktivita", "installed");
            deviceID = preferences.getString("deviceID","noname_pref");
        }
        binPath = getDir("bin", Context.MODE_MULTI_PROCESS);

        setContentView(R.layout.activity_my_main);
        MenuFragment menuf = MenuFragment.newInstance();

        menuf.setArguments(savedInstanceState);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragmentPlace, menuf);
        ft.commit();

        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        netDevRunning = preferences.getInt("run",0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("run", netDevRunning);                                     //??test
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showNotification() {
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

        netDevRunning = 1;
    }

    public void cancelNotification(){
        netDevRunning = 0;
        mNM.cancel(NOTIFICATION);
    }

    // Method to start the service
    public void startService(View view) {
        startService(new Intent(getBaseContext(), LoggerService.class));
    }

    // Method to stop the service
    public void stopService(View view) {
        stopService(new Intent(getBaseContext(), LoggerService.class));
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("run", netDevRunning);
    }

    @Override
    public void onFragmentInteraction(View view) {
        if(((ToggleButton) view).isChecked()) {
            startService(new Intent(getBaseContext(), VPN_logger_service.class));
        }else{
            stopService(new Intent(getBaseContext(), VPN_logger_service.class));
        }
    }

    public String getDeviceID() {
        return deviceID;
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer+" "+model;
        }
    }
}
