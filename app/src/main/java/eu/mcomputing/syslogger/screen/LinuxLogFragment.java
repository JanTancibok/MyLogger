package eu.mcomputing.syslogger.screen;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import eu.mcomputing.syslogger.MyMainActivity;
import eu.mcomputing.syslogger.R;
import eu.mcomputing.syslogger.services.AppInfoService;
import eu.mcomputing.syslogger.services.NetDevService;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LinuxLogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LinuxLogFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class LinuxLogFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MIN_DELAY = 2000;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ToggleButton toggleButtonLin;
    AlarmManager alarm;
    Intent intent;
    PendingIntent pintent;
    Intent apintent;
    PendingIntent appintent;
    private static final String TAG_NETDEVSERVICE = "NetDev_Service";
    private int NOTIFICATION = R.string.virdir_service_started;
    private int netDevRunning = 0;                                              //NEED PERSIST
    public static final int REQUEST_CODE = 120591;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LinuxLogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LinuxLogFragment newInstance() {
        LinuxLogFragment fragment = new LinuxLogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public LinuxLogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            netDevRunning = getArguments().getInt("run",0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_linux_log, container, false);
        toggleButtonLin = (ToggleButton) view.findViewById(R.id.toggleButton_LinuxLog);
        toggleButtonLin.setOnClickListener(this);
        if (getArguments() != null) {
            netDevRunning = getArguments().getInt("run",0);
        }
        toggleButtonLin.setChecked(1==netDevRunning);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onClick(View v) {
        //do what you want to do when button is clicked

        if(((ToggleButton) v).isChecked()) {
            int delay = Integer.parseInt(((TextView) getView().findViewById(R.id.editText)).getText().toString());

            if(delay<MIN_DELAY){
                delay=MIN_DELAY;
            }

            // handle toggle on
            //startService(new Intent(getBaseContext(), LinuxService.class));
            alarm = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

            intent = new Intent(getActivity(), NetDevService.class);
            pintent = PendingIntent.getService(getActivity(), REQUEST_CODE, intent, 0);

            alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), delay, pintent);

            apintent = new Intent(getActivity(), AppInfoService.class);
            apintent.setAction(AppInfoService.ACTION_GETAPP);
            appintent = PendingIntent.getService(getActivity(), 1234, apintent, 0);

            alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), delay, appintent);

            //mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // Display a notification about us starting.  We put an icon in the status bar.
            ((MyMainActivity) getActivity()).showNotification();
            Log.d(TAG_NETDEVSERVICE, "AlarmManager started with interval: "+delay);

            netDevRunning = 1;
        } else {
            //stopService(new Intent(getBaseContext(), LinuxService.class));
            alarm = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

            if (pintent==null) {
                intent = new Intent(getActivity(), NetDevService.class);
                pintent = PendingIntent.getService(getActivity(), REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            }

            alarm.cancel(pintent);

            apintent = new Intent(getActivity(), AppInfoService.class);
            apintent.setAction(AppInfoService.ACTION_GETAPP);
            appintent = PendingIntent.getService(getActivity(), 1234, apintent, PendingIntent.FLAG_CANCEL_CURRENT);

            alarm.cancel(appintent);
            ((MyMainActivity) getActivity()).cancelNotification();
            netDevRunning = 0;
        }
    }
}
