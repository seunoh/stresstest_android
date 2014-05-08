package com.urqa.alpha;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.urqa.alpha.app.App;
import com.urqa.alpha.app.AppPreferences;
import com.urqa.alpha.common.FileHelper;
import com.urqa.alpha.service.ExecuteService;
import com.urqa.alpha.service.Time;
import com.urqa.clientinterface.URQAController;

import java.util.List;

/**
 * @author seunoh on 2014. 05. 06..
 */
public class MainFragment extends Fragment {


    private EditText mApiKeyText;

    private Spinner mTimeSpinner;

    private RadioGroup mRadioGroup;
    private Button mStartView;
    private Button mStopView;

    private TextView mCheckView;


    private List<Time> mTimes = Lists.newArrayList(Time.values());

    private OnActivityCommandListener mListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View VIEW = inflater.inflate(R.layout.fragment_main, container, false);

        TextView filePath = (TextView) VIEW.findViewById(R.id.file_path);
        filePath.setText(FileHelper.absolutePath());

        mApiKeyText = (EditText) VIEW.findViewById(R.id.key_text);
        mTimeSpinner = (Spinner) VIEW.findViewById(R.id.spinner);
        mRadioGroup = (RadioGroup) VIEW.findViewById(R.id.radio_group);
        mStartView = (Button) VIEW.findViewById(R.id.start_button);
        mStopView = (Button) VIEW.findViewById(R.id.stop_button);
        mCheckView = (TextView) VIEW.findViewById(R.id.check_text);


        ArrayAdapter<Time> adapter = new ArrayAdapter<Time>(getActivity(), R.layout.spinner_item, mTimes);
        mTimeSpinner.setAdapter(adapter);

        mApiKeyText.setText(App.getPreferences().getString(AppPreferences.KEY_API, ""));


        mStartView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String KEY = mApiKeyText.getText() != null ? mApiKeyText.getText().toString() : "";

                if (!Strings.isNullOrEmpty(KEY)) {
                    URQAController.InitializeAndStartSession(getActivity(), KEY);
                    selectedRadioButton(mRadioGroup.getCheckedRadioButtonId());
                    selectedSpinner(mTimeSpinner);
                    App.getPreferences().putString(AppPreferences.KEY_API, KEY);
                    start();
                } else {
                    mApiKeyText.setError(getString(R.string.error_empty_text));
                    mApiKeyText.requestFocus();
                }
            }
        });

        mStopView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().stopService(App.getServiceIntent());
                stateService();
            }
        });

        VIEW.findViewById(R.id.result_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.execute(App.RESULT_URI);
            }
        });


        stateService();

        return VIEW;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnActivityCommandListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnActivityCommandListener");
        }
    }

    private void stateService() {
        if (isServiceRunning()) {
            mStartView.setVisibility(View.GONE);
            mStopView.setVisibility(View.VISIBLE);
            mCheckView.setText(R.string.running);
        } else {
            mStartView.setVisibility(View.VISIBLE);
            mStopView.setVisibility(View.GONE);
            mCheckView.setText(R.string.stopping);
        }
    }


    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ExecuteService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void start() {
        getActivity().startService(App.getServiceIntent());
        stateService();
    }

    private void selectedRadioButton(int id) {

        switch (id) {
            case R.id.on:
                App.getPreferences().putBoolean(AppPreferences.KEY_SERVICE, true);
                break;
            case R.id.off:
            default:
                App.getPreferences().putBoolean(AppPreferences.KEY_SERVICE, false);
                break;
        }

    }

    private void selectedSpinner(Spinner spinner) {
        Time time = (Time) spinner.getSelectedItem();
        Log.i("MAIN", time.toString());
        Log.i("MAIN", time.getMilliseconds() + "");
        App.getPreferences().putLong(AppPreferences.KEY_TIME, time.getMilliseconds());
    }
}
