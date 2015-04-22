package eu.mcomputing.syslogger.screen;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import eu.mcomputing.syslogger.MyMainActivity;
import eu.mcomputing.syslogger.R;

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

    private OnFragmentInteractionListener mListener;

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
        Button button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener(this);

        Button button2 = (Button) view.findViewById(R.id.button2);
        button2.setOnClickListener(this);

        Button button3 = (Button) view.findViewById(R.id.button3);
        button3.setOnClickListener(this);

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
        Fragment fr = null;
        switch (view.getId()) {
            case R.id.button:
                fr = new LinuxLogFragment();

                if (args == null) {
                    // Restore last state for checked position.
                    Bundle b = new Bundle();
                    b.putInt("run", ((MyMainActivity) getActivity()).getNetDevRunning());
                    fr.setArguments(b);
                }else fr.setArguments(args);
                ((MyMainActivity) getActivity()).changeFragment(fr);
                break;
            case R.id.button2:
                fr = new LogCatFragment();
                ((MyMainActivity) getActivity()).changeFragment(fr);
                break;
            case R.id.button3:


                /*AlarmManager alarm = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                if (pintent==null) {
                    intent = new Intent(getActivity(), NetDevService.class);
                    pintent = PendingIntent.getService(getActivity(), REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                }
                alarm.cancel(pintent);
                ((MyMainActivity) getActivity()).cancelNotification();*/

                break;
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
