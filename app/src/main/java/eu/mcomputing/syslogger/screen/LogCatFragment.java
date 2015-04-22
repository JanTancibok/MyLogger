package eu.mcomputing.syslogger.screen;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.mcomputing.syslogger.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LogCatFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LogCatFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class LogCatFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public static Handler mUiHandler = null;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LogCatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LogCatFragment newInstance(String param1, String param2) {
        LogCatFragment fragment = new LogCatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public LogCatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mUiHandler = new Handler() // Receive messages from service class
        {
            public void handleMessage(Message msg)
            {
                switch(msg.what)
                {
                    case 0:
                        // add the status which came from service and show on GUI
                        //Toast.makeText(MyMainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
                        TextView tv = (TextView) getActivity().findViewById(R.id.textView);
                        tv.setMovementMethod(new ScrollingMovementMethod());

                        EditText edita = (EditText) getActivity().findViewById(R.id.editText);

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_log_cat, container, false);
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
}
