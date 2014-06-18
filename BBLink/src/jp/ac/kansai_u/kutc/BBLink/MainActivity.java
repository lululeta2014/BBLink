package jp.ac.kansai_u.kutc.BBLink;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        PopupWindow popupWindow = createPopupWindow();
        TextView popupTitle = (TextView)popupWindow.getContentView().findViewById(R.id.popup_title);
        TextView popupBody = (TextView)popupWindow.getContentView().findViewById(R.id.popup_body);
        switch(item.getItemId()){
            case R.id.menu_info:
                // Whant's app
                popupTitle.setText("このアプリについて");
                popupBody.setText("What's app");
                popupWindow.showAtLocation(this.findViewById(R.id.menu_info), Gravity.CENTER, 0, 0);
                break;
            case R.id.menu_help:
                // Help
                popupTitle.setText("ヘルプ");
                popupBody.setText("HELP");
                popupWindow.showAtLocation(this.findViewById(R.id.menu_info), Gravity.CENTER, 0, 0);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * ポップウィンドウを作成する
     * @return 作成したポップウィンドウ
     */
    private PopupWindow createPopupWindow(){
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // /res/layout/popup.xmlからレイアウト情報を引っ張ってくる
        View pv = inflater.inflate(R.layout.popup, null);
        pv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        PopupWindow pw = new PopupWindow();
        // 背景の設定，これを設定しないと外側タッチで非表示という処理が正常に動作しなくなる
        pw.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_bg));
        pw.setContentView(pv);
        // ポップウィンドウの外側をタッチすると非表示にする
        pw.setOutsideTouchable(true);
        // ポップウィンドウ表示後フォーカスを強制するか否か（他のコンポーネントをタッチできるか否か）
//        pw.setFocusable(true);
        pw.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        pw.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        return pw;
    }
}
