package eu.mcomputing.syslogger;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.TrafficStats;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;

import eu.mcomputing.syslogger.R;

public class AppDetailAct extends ActionBarActivity {
    private static String packageName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);

        final Context ctx = getApplicationContext();

        ImageView image = (ImageView) findViewById(R.id.app_icon);
        TextView textView = (TextView) findViewById(R.id.app_title);
        TextView textView2 = (TextView) findViewById(R.id.app_package);
        TextView up = (TextView) findViewById(R.id.up);
        TextView down = (TextView) findViewById(R.id.down);

        textView.setTextColor(Color.BLACK);
        textView2.setTextColor(Color.BLACK);

        /**/

        int appid = getIntent().getIntExtra("appid", -1);
        if(appid > 0) {

            final PackageManager packageManager = getApplicationContext().getPackageManager();
            final String[] packageNameList = ctx.getPackageManager().getPackagesForUid(appid);

            if(packageNameList != null) {
                packageName = packageNameList.length > 0 ? packageNameList[0] : ctx.getPackageManager().getNameForUid(appid);
            } else {
                packageName = ctx.getPackageManager().getNameForUid(appid);
            }


            ApplicationInfo applicationInfo;

            try {

                applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                image.setImageDrawable(applicationInfo.loadIcon(packageManager));
                textView.setText(packageManager.getApplicationLabel(applicationInfo));
                if(packageNameList.length > 1) {
                    textView2.setText(Arrays.toString(packageNameList));
                } else {
                    textView2.setText(packageName);
                }

                long download_bytes = TrafficStats.getUidRxBytes(applicationInfo.uid);
                long uploaded_bytes = TrafficStats.getUidTxBytes(applicationInfo.uid);

                down.setText(" : " +humanReadableByteCount(download_bytes, false));
                up.setText(" : " +humanReadableByteCount(uploaded_bytes, false));

            } catch (final PackageManager.NameNotFoundException e) {
                long download_bytes = TrafficStats.getUidRxBytes(appid);
                long uploaded_bytes = TrafficStats.getUidTxBytes(appid);

                down.setText(" : " +humanReadableByteCount(download_bytes, false));
                up.setText(" : " + humanReadableByteCount(uploaded_bytes, false));
            }

        	/*long total = TrafficStats.getTotalRxBytes();
        	long mobileTotal = TrafficStats.getMobileRxBytes();
        	long wifiTotal = (total - mobileTotal);
        	Log.v(TAG, "total=" + total + " mob=" + mobileTotal + " wifi=" + wifiTotal);*/

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_app_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if(bytes < 0) return "0 B";
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
