package sk.bratia4.mylogger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.support.v4.app.Fragment;

import java.util.List;
import android.os.Handler;

import sk.bratia4.mylogger.screen.LinuxLogFragment;
import sk.bratia4.mylogger.screen.LogCatFragment;
import sk.bratia4.mylogger.screen.MenuFragment;
import sk.bratia4.mylogger.services.LoggerService;


public class MyMainActivity extends FragmentActivity implements LogCatFragment.OnFragmentInteractionListener, LinuxLogFragment.OnFragmentInteractionListener {
    private LoggerService s;
    private ArrayAdapter<String> adapter;
    private List<String> wordList;

    boolean mIsBound = false;
    private NotificationManager mNM;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_main);

        MenuFragment menuf = new MenuFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragmentPlace, menuf);
        ft.commit();

        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        Intent intent= new Intent(this, LoggerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }*/

    //send message to service
   /* public void onClick (View v)
    {
        //only we need a handler to send message to any component.
        //here we will get the handler from the service first, then
        //we will send a message to the service.

        if(null != LoggerService.mMyServiceHandler)
        {
            //first build the message and send.
            //put a integer value here and get it from the service handler
            //For Example: lets use 0 (msg.what = 0;) for getting service running status from the service
            Message msg = new Message();
            msg.what = 0;
            msg.obj  = "Add your Extra Meaage Here"; // you can put extra message here
            LoggerService.mMyServiceHandler.sendMessage(msg);
        }
    }*/

    /*public void onClick(View view) {
        if (s != null) {
            Toast.makeText(this, "Number of elements" + s.getWordList().size(),
                    Toast.LENGTH_SHORT).show();
            wordList.clear();
            wordList.addAll(s.getWordList());
            adapter.notifyDataSetChanged();
        }
    }*/

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
    }

    public void cancelNotification(){
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
}
