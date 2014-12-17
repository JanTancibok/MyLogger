package sk.bratia4.mylogger;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import android.os.Handler;

import java.util.Scanner;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyMainActivity extends ListActivity {
    private LoggerService s;
    private ArrayAdapter<String> adapter;
    private List<String> wordList;

    public static Handler mUiHandler = null;

    boolean mIsBound = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_main);

        mUiHandler = new Handler() // Receive messages from service class
        {
            public void handleMessage(Message msg)
            {
                switch(msg.what)
                {
                    case 0:
                        // add the status which came from service and show on GUI
                        //Toast.makeText(MyMainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
                        TextView tv = (TextView)findViewById(R.id.textView);
                        tv.setMovementMethod(new ScrollingMovementMethod());

                        EditText edita = (EditText)findViewById(R.id.editText);

                        Pattern pattern = Pattern.compile(".*mylogger.*");

                        if(!edita.getText().equals("")){
                            pattern = Pattern.compile(".*"+edita.getText()+".*");
                        }

                        StringBuffer sb = new StringBuffer();

                        Scanner scanner = new Scanner(msg.obj.toString());
                        while(scanner.hasNextLine()) {
                            String line = scanner.nextLine();
                            Matcher matcher = pattern.matcher(line);
                            if(matcher.find()){
                                sb.append(line + '\n');
                            }
                        }
                        scanner.close();

                        tv.setText(sb.toString());
                        break;

                    default:
                        break;
                }
            }
        };
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

    // Method to start the service
    public void startService(View view) {
        startService(new Intent(getBaseContext(), LoggerService.class));
    }

    // Method to stop the service
    public void stopService(View view) {
        stopService(new Intent(getBaseContext(), LoggerService.class));
    }

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
}
