package eu.mcomputing.syslogger.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import eu.mcomputing.syslogger.MyMainActivity;
import eu.mcomputing.syslogger.services.AlarmStarterService;

public class OnOffReceiver extends BroadcastReceiver {
    public OnOffReceiver() {
    }

    //MyMainActivity myMain = null;

    /*public void setMainActivityHandler(MyMainActivity main){
        myMain = main;
    }*/

    @Override
    public void onReceive(Context context, Intent intent) {
        //myMain.startLog();
        Intent service1 = new Intent(context, AlarmStarterService.class);
        service1.setAction("1");
        context.startService(service1);
    }
}
