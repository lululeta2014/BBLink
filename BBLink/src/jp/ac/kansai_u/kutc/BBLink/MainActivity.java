package jp.ac.kansai_u.kutc.BBLink;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.io.*;

/**
 * アプリケーションのスタートとなるアクティビティ
 * @author akasaka
 */
public class MainActivity extends Activity implements View.OnClickListener{
    private final String TAG = MainActivity.class.getSimpleName();  // クラス名
    final int REQUEST_GALALLY_IMAGE = 0x12FCEA7;  // ギャラリーインテント時の返却値（任意の数値）
    Intent wallPaperService = null;  // WallPaperServiceクラス起動用のインテント
    ImageView img;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        img = (ImageView)findViewById(R.id.image);
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
                Bitmap bitmap = createBmpImagefromGallery(data.getData());
                img.setImageBitmap(bitmap);
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
        // ポップウィンドウの中身
        TextView popupBody = (TextView)popupWindow.getContentView().findViewById(R.id.popup_body);
        switch(item.getItemId()){
            case R.id.menu_info:
                // Selected "Whant's app"
                popupBody.setText(extractStringFromTextFile(R.raw.whatsapp));
                break;
            case R.id.menu_help:
                // Selected "Help"
                popupBody.setText(extractStringFromTextFile(R.raw.help));
                break;
            default:
                break;
        }
        // ポップウィンドウの表示
        popupWindow.showAtLocation(this.findViewById(R.id.menu_info), Gravity.CENTER, 0, 0);
        return super.onOptionsItemSelected(item);
    }

    /**
     * ポップウィンドウを作成する
     * @return 作成したポップウィンドウ
     */
    private PopupWindow createPopupWindow(){
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // /res/layout/popup.xmlからレイアウト情報を引っ張ってくる
        View popupView = inflater.inflate(R.layout.popup, null);
        popupView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        final PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // 背景の設定，これを設定しないと外側タッチで非表示という処理が正常に動作しなくなる
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.pw_bg));
        // ポップウィンドウの外側をタッチすると非表示にする
        popupWindow.setOutsideTouchable(true);
        // ポップウィンドウ表示後フォーカスを強制するか否か（他のコンポーネントをタッチできるか否か）
//        popupWindow.setFocusable(true);
        Button dismissBtn = (Button)popupView.findViewById(R.id.dismiss);
        dismissBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                popupWindow.dismiss();
            }
        });

        return popupWindow;
    }

    /**
     * テキストファイルから文字列を抽出する
     * @param id テキストファイルのID
     * @return テキストファイルの内容
     */
    private String extractStringFromTextFile(int id){
        InputStream is = getResources().openRawResource(id);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String str, text;
        try{
            try{
                // テキストファイルから全ての文章を抽出する
                while((str = br.readLine()) != null)
                    sb.append(str).append('\n');
            }finally{
                br.close();
                // 抽出した全ての文章を格納する
                text = sb.toString();
            }
        }catch(IOException e){
            // 読み込み失敗
            text = "Read ERROR";
        }
        return text;
    }

    /**
     * ギャラリーで選択した画像からBitmap画像を作成する
     * @param uri ギャラリーから選択した画像のURI
     * @return bmp Bitmap画像
     */
    private Bitmap createBmpImagefromGallery(Uri uri){
        InputStream inputStream;
        Bitmap bmp;  // 返り値
        ExifInterface exifInterface;
        try{
            // ギャラリーから選択した画像を開く
            inputStream = getContentResolver().openInputStream(uri);
            // ストリームからBitmapに変換
            bmp = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToPosition(0);
            String path = cursor.getString(1);  // 画像のパスを取得
            cursor.close();

            // 画像のパスからEXIF情報を弄るためのオブジェクトを生成
            exifInterface = new ExifInterface(path);
        }catch(IOException e){
            Log.e(TAG, "CANNOT CREATE BITMAP IMAGE");
            return null;
        }

        // 画像の表示角度の取得
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        // 画像の表示角度を修正するための変数（angle）の宣言
        int angle = 0;
        // 表示角度が90度の場合
        if(orientation == ExifInterface.ORIENTATION_ROTATE_90){
            angle = 90;
            // 表示角度が180度の場合
        }else if(orientation == ExifInterface.ORIENTATION_ROTATE_180){
            angle = 180;
            // 表示角度が270度の場合
        }else if(orientation == ExifInterface.ORIENTATION_ROTATE_270){
            angle = 270;
        }
        Matrix matrix = new Matrix();
        // 画像の表示角度を変更
        matrix.preRotate(angle);
        // 画像の作成
        try{
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        }catch(OutOfMemoryError error){
            // メモリエラーが出た場合，ガベージコレクションを走らせてトライ
            java.lang.System.gc();
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        }
        return bmp;
    }
}
