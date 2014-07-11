package jp.ac.kansai_u.kutc.BBLink;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 設定画面
 * @author akasaka
 * Created by akasaka on 2014/07/11.
 */
public class PrefsFragment extends PreferenceFragment{
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // preferences.xmlの読み込み
        addPreferencesFromResource(R.xml.preferences);

        // サービスの状態を読み込み，説明を設定する
        SwitchPreference runningService = (SwitchPreference)getPreferenceScreen().findPreference("running_service");
        if(runningService.isChecked())
            // サービスON
            runningService.setSummary(R.string.service_running_summary);
        else
            // サービスOFF
            runningService.setSummary(R.string.service_stop_summary);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = super.onCreateView(inflater, container, savedInstanceState);
        // 背景を変更
        if(view != null){
            view.setBackgroundColor(getResources().getColor(android.R.color.background_dark));
            return view;
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
