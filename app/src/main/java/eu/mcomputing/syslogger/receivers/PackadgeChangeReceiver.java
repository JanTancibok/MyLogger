package eu.mcomputing.syslogger.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import eu.mcomputing.syslogger.services.AppInfoService;

public class PackadgeChangeReceiver extends BroadcastReceiver {
    public PackadgeChangeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        int u = intent.getIntExtra(Intent.EXTRA_UID,0);
        boolean replace = intent.getBooleanExtra(Intent.EXTRA_REPLACING,false);
        Log.d("RECEIVER", "Uidd : " + u + " replace " + String.valueOf(replace));

        if(replace){
            //APP REPLACED do nothing now
        }else {
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
                //APP UNINSTALED
                boolean dataRemove = intent.getBooleanExtra(Intent.EXTRA_DATA_REMOVED,true);
                Intent startUpdateApp = new Intent(context, AppInfoService.class);
                startUpdateApp.setAction(AppInfoService.ACTION_REMOVEAPP);
                startUpdateApp.putExtra(AppInfoService.EXTRA_UID, u);
                startUpdateApp.putExtra(AppInfoService.EXTRA_DATA_REMOVED, dataRemove);
                context.startService(startUpdateApp);
            } else {
                //NEW APP INSTALLED
                Intent startUpdateApp = new Intent(context, AppInfoService.class);
                startUpdateApp.setAction(AppInfoService.ACTION_UPDATEAPP);
                startUpdateApp.putExtra(AppInfoService.EXTRA_UID, u);
                startUpdateApp.putExtra(AppInfoService.EXTRA_REPLACE, replace);
                context.startService(startUpdateApp);
            }
        }
    }
}
