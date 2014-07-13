package jp.ac.kansai_u.kutc.BBLink;

import android.content.SharedPreferences;
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
public class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // preferences.xmlの読み込み
        addPreferencesFromResource(R.xml.preferences);

        // サービスの状態を読み込み，説明を設定する
        setSummaryToPref("running_service");
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
        // 設定が変更したときのコールバック関数
        // 動的にサマリを変更する
        setSummaryToPref("running_service");
    }

    @Override
    public void onResume(){
        super.onResume();
        // リスナの登録
        getPreferenceManager().
                getSharedPreferences().
                registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause(){
        // リスナの解除
        getPreferenceManager().
                getSharedPreferences().
                unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    /**
     * 各種設定にサマリを設定する
     * TODO: 後々ジェネリック型を使って，更に抽象化するかも
     */
    private void setSummaryToPref(CharSequence key){
        // 「サービスの状態」スイッチのインスタンス化
        SwitchPreference runningService = (SwitchPreference)getPreferenceScreen().findPreference(key);
        if(runningService.isChecked())
            // サービスON
            runningService.setSummary(R.string.service_running_summary);
        else
            // サービスOFF
            runningService.setSummary(R.string.service_stop_summary);
    }
}
