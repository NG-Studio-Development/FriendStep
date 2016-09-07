package com.ngstudio.friendstep.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ngstudio.friendstep.CodesMap;
import com.ngstudio.friendstep.FragmentPool;
import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.components.CustomLocationManager;
import com.ngstudio.friendstep.components.GeoService;
import com.ngstudio.friendstep.model.connectivity.HttpServer;
import com.ngstudio.friendstep.model.connectivity.requests.stepserver.InsertGeoCordsRequestStepServer;
import com.ngstudio.friendstep.ui.activities.MainActivity;
import com.ngstudio.friendstep.ui.activities.ProfileActivity;
import com.ngstudio.friendstep.utils.SettingsHelper;


import org.apache.http.HttpException;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SettingsFragment extends BaseFragment<MainActivity> {

    @Override
    public int getLayoutResID() {
        return R.layout.fragment_settings;
    }

    private LinearLayout profileSettings;
    private FragmentPool fragmentPool = FragmentPool.getInstance();
    private TextView tvCounter;
    private SeekBar sbAlertDistance;
    //private ToggleButton tbSwitchAlertFriend;
    //private ToggleButton tbChatTransaction;
    private ToggleButton tbSendLocation;
    //private Spinner spinnerChatTransaction;
    private RelativeLayout rlAlertDistancePanel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentPool.popFragment(MapFragment.class);
        return super.onCreateView(inflater,container,savedInstanceState);
    }

    @Override
    public void findChildViews(@NotNull View view) {
        super.findChildViews(view);
        final SettingsHelper settings = SettingsHelper.getInstance();
        Map<String,String> map = CodesMap.getLanguageMap();
        final String[] languageMap = (String[])(map.keySet().toArray(new String[map.keySet().size()]));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,languageMap);
        rlAlertDistancePanel = (RelativeLayout) view.findViewById(R.id.rlAlertDistancePanel);
        //rlAlertDistancePanel.setVisibility(View.GONE);
        /*spinnerChatTransaction = (Spinner) view.findViewById(R.id.spinnerChatTransaction);
        spinnerChatTransaction.setAdapter(adapter);
        spinnerChatTransaction.setSelection(Arrays.asList(languageMap).indexOf(settings.getLanguage()));
        spinnerChatTransaction.setVisibility(View.GONE);
        spinnerChatTransaction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                settings.setLanguage(languageMap[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {  }
        }); */

        tbSendLocation = (ToggleButton) view.findViewById(R.id.tbSwitchSendLocation);
        tbSendLocation.setChecked(settings.getStateSendLocation());
        tbSendLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.putStateSendLocation(isChecked);
                sendStateShowLocationToServer(isChecked);
                if(isChecked) {
                    getActivity().startService(new Intent(getActivity(), GeoService.class));
                } else {
                    CustomLocationManager.getInstance().stopTrackLocation();
                }
            }
        });

        /*tbChatTransaction = (ToggleButton) view.findViewById(R.id.tbChatTransaction);
        tbChatTransaction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    spinnerChatTransaction.setVisibility(View.VISIBLE);
                else
                    spinnerChatTransaction.setVisibility(View.GONE);
            }
        });
        tbSwitchAlertFriend = (ToggleButton) view.findViewById(R.id.tbSwitchAlertFriend); */
        /*tbSwitchAlertFriend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    rlAlertDistancePanel.setVisibility(View.VISIBLE);
                } else {
                    rlAlertDistancePanel.setVisibility(View.GONE);
                }
            }
        }); */

        tvCounter = (TextView) view.findViewById(R.id.tvCounter);
        sbAlertDistance = (SeekBar) view.findViewById(R.id.sbAlertDistance);

        sbAlertDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int distance;
                switch(progress) {
                    default:
                    case SettingsHelper.DISTANCE_50:
                        distance = 50;
                        break;
                    case SettingsHelper.DISTANCE_100:
                        distance = 100;
                        break;
                    case SettingsHelper.DISTANCE_500:
                        distance = 500;
                        break;
                    case SettingsHelper.DISTANCE_1000:
                        distance = 1000;
                        break;
                }
                tvCounter.setText(distance+getString(R.string.inscription_m));
                settings.putDistanceKey(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        sbAlertDistance.setProgress(settings.getDistanceKey());
        tvCounter.setText(SettingsHelper.getInstance().getDistance()+getString(R.string.inscription_m));
        profileSettings = (LinearLayout) view.findViewById(R.id.lnProfileSettings);
        profileSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity.startProfileActivity(getActivity(),null);
            }
        });

        getHostActivity().getSupportActionBar().setTitle(getString(R.string.title_screen_settings));

    }

    private void sendStateShowLocationToServer(boolean state) {
        final InsertGeoCordsRequestStepServer request  = InsertGeoCordsRequestStepServer
                .requestChangeAccessibleShowState( WhereAreYouApplication.getInstance().getUserId(),
                        state);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpServer.sendRequestToServer(request);
                } catch (HttpException ex) {ex.printStackTrace();}
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        fragmentPool.free(SettingsFragment.class,this);
        super.onDestroyView();
    }
}
