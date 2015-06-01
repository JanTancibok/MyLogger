package eu.mcomputing.syslogger.screen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import eu.mcomputing.syslogger.AppDetailAct;
import eu.mcomputing.syslogger.MyMainActivity;
import eu.mcomputing.syslogger.R;
import eu.mcomputing.syslogger.utils.PackageInfoData;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MenuFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class MenuFragment extends Fragment implements View.OnClickListener{

    private Bundle args;
    private ListView listview = null;
    private OnFragmentInteractionListener mListener;
    private final static String TAG = "MenuFrag";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     * @return A new instance of fragment Menu.
     */
    // TODO: Rename and change types and number of parameters
    public static MenuFragment newInstance() {
        MenuFragment fragment = new MenuFragment();
        return fragment;
    }
    public MenuFragment() {
        // Required empty public constructor
        args = getArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu,
                container, false);
        /*Button button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener(this);

        Button button2 = (Button) view.findViewById(R.id.button2);
        button2.setOnClickListener(this);

        Button button4 = (Button) view.findViewById(R.id.button_menu_vpn);
        button4.setOnClickListener(this);

        Button button3 = (Button) view.findViewById(R.id.button3);
        button3.setOnClickListener(this);*/

        if (this.listview == null) {
            this.listview = (ListView) view.findViewById(R.id.listview);
        }
        showApplications("", 99, true);

        try {
            ((MyMainActivity) getActivity()).startLog();
        }catch (ClassCastException e){
        }
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onClick(View view) {
       /* Fragment fr = null;
        switch (view.getId()) {
            case R.id.button:
                fr = new LinuxLogFragment();

                if (args == null) {
                    // Restore last state for checked position.
                    Bundle b = new Bundle();
                    b.putInt("run", ((MyMainActivity) getActivity()).getNetDevRunning());
                    b.putString(LinuxLogFragment.DEVICE_ID, ((MyMainActivity) getActivity()).getDeviceID());
                    fr.setArguments(b);
                }else fr.setArguments(args);
                ((MyMainActivity) getActivity()).changeFragment(fr);
                break;
            case R.id.button2:
                fr = new LogCatFragment();
                ((MyMainActivity) getActivity()).changeFragment(fr);
                break;
            case R.id.button_menu_vpn:
                fr = new VPN_fragment();
                ((MyMainActivity) getActivity()).changeFragment(fr);
                break;
            case R.id.button3:


//                AlarmManager alarm = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//                if (pintent==null) {
//                    intent = new Intent(getActivity(), NetDevService.class);
//                    pintent = PendingIntent.getService(getActivity(), REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//                }
//                alarm.cancel(pintent);
//                ((MyMainActivity) getActivity()).cancelNotification();

                break;
        }*/
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public class AppAdapter extends ArrayAdapter<PackageInfoData> implements CompoundButton.OnCheckedChangeListener {
        private final ArrayList<PackageInfoData> pole;
        private final Context context;

        public AppAdapter(Context context, List<PackageInfoData> aps) {
            super(context, 0, aps);
            pole = (ArrayList) aps;
            this.context = context;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            //do check
            final PackageInfoData app = (PackageInfoData) compoundButton.getTag();

            MyMainActivity m = ((MyMainActivity) getActivity());

            if (m.mnozina == null) {
                SharedPreferences preferences = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                m.mnozina = preferences.getStringSet("applist",null);
            }
            if (m!=null) {
                //mnozina = m.mnozina;

                //SharedPreferences preferences = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                //Set mnozina;
                //mnozina = preferences.getStringSet("applist", null);
                /**/
                if (app.selected_wifi != isChecked) {
                    app.selected_wifi = isChecked;
                    //MainActivity.dirty = true;
                }
                if (m.mnozina.contains(app.uid) && !isChecked) {
                    m.mnozina.remove(app.uid);
                }
                if (isChecked) {
                    m.mnozina.add(app.uid);
                }
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_user, parent, false);

                holder = new ViewHolder();
                holder.box_wifi = (CheckBox) convertView.findViewById(R.id.iitemcheck_wifi);
                holder.box_wifi.setOnCheckedChangeListener(this);

                holder.text = (TextView) convertView.findViewById(R.id.iitemtext);
                holder.icon = (ImageView) convertView.findViewById(R.id.iitemicon);

			    convertView.setTag(holder);
            } else {
                // Convert an existing view
                holder = (ViewHolder) convertView.getTag();
                holder.box_wifi = (CheckBox) convertView.findViewById(R.id.iitemcheck_wifi);

                holder.text = (TextView) convertView.findViewById(R.id.iitemtext);
                holder.icon = (ImageView) convertView.findViewById(R.id.iitemicon);
            }

            holder.app = pole.get(position);
            holder.text.setText(holder.app.toString());

            final int id = holder.app.uid;
            if (id > 0) {
                holder.text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, AppDetailAct.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("appid", id);
                        context.startActivity(intent);
                    }
                });
            }

            ApplicationInfo info = holder.app.appinfo;
            holder.text.setTextColor(Color.BLACK);

            holder.icon.setImageDrawable(holder.app.cached_icon);
            if (!holder.app.icon_loaded && info != null) {
                try {
                    new LoadIconTask().execute(holder.app,
                            context.getPackageManager(), convertView);
                } catch (RejectedExecutionException r) {
                }
            }

            holder.box_wifi.setTag(holder.app);
            //holder.box_wifi.setChecked(holder.app.selected_wifi);
//            SharedPreferences preferences = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
//            Set mnozina;
//            mnozina = preferences.getStringSet("applist",null);
            MyMainActivity m = ((MyMainActivity) getActivity());
            if (m.mnozina == null) {
                m.mnozina = new HashSet<String>();
            }
            if(m.mnozina!=null){
                holder.box_wifi.setChecked(m.mnozina.contains(id));
            }
            return convertView;
        }
    }

    /**
     * Show the list of applications
     */
    private void showApplications(final String searchStr, int flag, boolean showAll) {
        List<PackageInfoData> searchApp = new ArrayList<PackageInfoData>();
        final List<PackageInfoData> apps = PackageInfoData.getApps(getActivity());
        boolean isResultsFound = false;
        if(searchStr !=null && searchStr.length() > 1) {
            for(PackageInfoData app:apps) {
                for(String str: app.names) {
                    if(str.contains(searchStr.toLowerCase()) || str.toLowerCase().contains(searchStr.toLowerCase())
                            && !searchApp.contains(app)) {
                        searchApp.add(app);
                        isResultsFound = true;
                    }
                }
            }
        } else if (flag > -1){
            switch(flag){
                case 0:
                    for(PackageInfoData app:apps) {
                        if(app.pkgName.startsWith("dev.afwall.special")) {
                            searchApp.add(app);
                        }
                    }
                    break;
                case 1:
                    for(PackageInfoData app: apps) {
                        if (app.appinfo != null && (app.appinfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                            searchApp.add(app);
                        }
                    }
                    break;
                case 2:
                    for(PackageInfoData app: apps) {
                        if (app.appinfo != null && (app.appinfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                            searchApp.add(app);
                        }
                    }
                    break;
            }

        }
        List<PackageInfoData> apps2;
        if(showAll || (searchStr != null && searchStr.equals(""))) {
            apps2 = apps;
        } else if(isResultsFound || searchApp.size() > 0) {
            apps2 = searchApp;
        } else {
            apps2 = new ArrayList<PackageInfoData>();
        }

        // Sort applications - selected first, then alphabetically
        Collections.sort(apps2, new PackageComparator());

        ArrayList<String> ahoj = new ArrayList<String>();
        ahoj.add("ahojaa");
        ahoj.add("test");
         this.listview.setAdapter(new AppAdapter(getActivity().getApplicationContext(), apps2));
        //this.listview.setSelectionFromTop(((MyMainActivity) getActivity()).index, ((MyMainActivity) getActivity()).top);
    }

    class PackageComparator implements Comparator<PackageInfoData> {

        @Override
        public int compare(PackageInfoData o1, PackageInfoData o2) {
            if (o1.firstseen != o2.firstseen) {
                return (o1.firstseen ? -1 : 1);
            }
            boolean o1_selected = o1.selected_wifi;
            boolean o2_selected = o2.selected_wifi;

            if (o1_selected == o2_selected) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.names.get(0).toString(),o2.names.get(0).toString());
            }
            if (o1_selected)
                return -1;
            return 1;
        }
    }

    static class ViewHolder {
        private CheckBox box_wifi;
        private TextView text;
        private ImageView icon;
        private PackageInfoData app;
    }

    /**
     * Asynchronous task used to load icons in a background thread.
     */
    private static class LoadIconTask extends AsyncTask<Object, Void, View> {
        @Override
        protected View doInBackground(Object... params) {
            try {
                final PackageInfoData app = (PackageInfoData) params[0];
                final PackageManager pkgMgr = (PackageManager) params[1];
                final View viewToUpdate = (View) params[2];
                if (!app.icon_loaded) {
                    app.cached_icon = pkgMgr.getApplicationIcon(app.appinfo);
                    app.icon_loaded = true;
                }
                // Return the view to update at "onPostExecute"
                // Note that we cannot be sure that this view still references
                // "app"
                return viewToUpdate;
            } catch (Exception e) {
                Log.e(TAG, "Error loading icon", e);
                return null;
            }
        }

        protected void onPostExecute(View viewToUpdate) {
            try {
                // This is executed in the UI thread, so it is safe to use
                // viewToUpdate.getTag()
                // and modify the UI
                final ViewHolder entryToUpdate = (ViewHolder) viewToUpdate.getTag();
                entryToUpdate.icon.setImageDrawable(entryToUpdate.app.cached_icon);
            } catch (Exception e) {
                Log.e(TAG, "Error showing icon", e);
            }
        };
    }
}
