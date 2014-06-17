package jp.ac.kansai_u.kutc.BBLink;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * アプリケーションのスタートとなるアクティビティ
 * @author akasaka
 */
public class MainActivity extends Activity implements View.OnClickListener{
    final int REQUEST_GALALLY_IMAGE = 0x12FCEA7;  // ギャラリーインテント時の返却値（任意の数値）
    Intent wallPaperService = null;  // WallPaperServiceクラス起動用のインテント
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button loadImageButton = (Button)findViewById(R.id.loadImageButton);
        Button setServiceButton = (Button)findViewById(R.id.setServiceButton);
        Button unsetServiceButton = (Button)findViewById(R.id.unsetServiceButton);
        loadImageButton.setOnClickListener(this);
        setServiceButton.setOnClickListener(this);
        unsetServiceButton.setOnClickListener(this);

        // サービス起動用のインテント
        wallPaperService = new Intent(this, WallPaperService.class);
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onRestart(){
        super.onRestart();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onClick(View v){
        if(v.getId() == R.id.loadImageButton){
            // ギャラリーから画像を取得する
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");  // 画像タイプに限定
            startActivityForResult(intent, REQUEST_GALALLY_IMAGE);
        }else if(v.getId() == R.id.setServiceButton){
            startService(wallPaperService);
        }else if(v.getId() == R.id.unsetServiceButton){
            stopService(wallPaperService);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case REQUEST_GALALLY_IMAGE:
                if(resultCode != RESULT_OK) return;
                // ここから下，取得したデータ処理ダバー
                break;
            default:
                break;
        }
    }
}
