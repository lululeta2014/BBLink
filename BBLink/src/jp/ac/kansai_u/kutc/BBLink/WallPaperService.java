package jp.ac.kansai_u.kutc.BBLink;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

import java.io.IOException;

/**
 * バッテリ容量をチェックし壁紙を変更するサービス
 *
 * @author akasaka
 */
public class WallPaperService extends Service{
    WallpaperManager wallpaperManager;
    int batteryLevel = 0;  // バッテリ残量を格納する

    /**
     * OSからのバッテリ残量変化のブロードキャストに応答するレシーバ
     */
    private BroadcastReceiver batteryReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){
                // 現在のバッテリ残量を取得する
                batteryLevel = intent.getIntExtra("level", 0);
                // ここから下，バッテリ残量ごとの条件分岐ダバー
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId){
        super.onStart(intent, startId);
        Toast.makeText(this, "Start Service", Toast.LENGTH_SHORT).show();
        wallpaperManager = WallpaperManager.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);  //バッテリ残量のチェンジに応答
        registerReceiver(batteryReceiver, filter);  // ブロードキャストレシーバに登録
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Toast.makeText(this, "Stop Service", Toast.LENGTH_SHORT).show();
        // ブロードキャストレシーバから解除する
        unregisterReceiver(batteryReceiver);
        try{
            //TODO: 削除予定
            // 壁紙の初期化
            wallpaperManager.clear();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
