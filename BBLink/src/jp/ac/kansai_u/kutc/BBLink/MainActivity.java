package jp.ac.kansai_u.kutc.BBLink;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.*;
import at.technikum.mti.fancycoverflow.FancyCoverFlow;

import java.io.*;

/**
 * アプリケーションのスタートとなるアクティビティ
 *
 * @author akasaka
 */
public class MainActivity extends Activity implements View.OnClickListener{
    private String[] imgNames;  // 各画像ファイル名
    private FancyCoverFlow fancyCoverFlow;  // カバーフロービュー
    private CoverFlowAdapter coverFlowAdapter;  // カバーフローにセットするアダプタ

    private final String TAG = MainActivity.class.getSimpleName();  // クラス名
    final int GALLERY_INTENT = 0x12FCEA7;  // ギャラリーインテント時の返却値（任意の数値）
    Intent wallPaperService = null;  // WallPaperServiceクラス起動用のインテント
    SharedPreferences preferences;  // アプリの設定情報

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button preferencesButton = (Button)findViewById(R.id.preferencesButton);
        Button loadImageButton = (Button)findViewById(R.id.loadImageButton);
        Button setServiceButton = (Button)findViewById(R.id.setServiceButton);
        preferencesButton.setOnClickListener(this);
        loadImageButton.setOnClickListener(this);
        setServiceButton.setOnClickListener(this);

        // サービス起動用のインテント
        wallPaperService = new Intent(this, WallPaperService.class);

        // 各画像のファイル名を取得する
        imgNames = getResources().getStringArray(R.array.images_name);
        // 画像ファイル数分のサイズを持つアダプタを生成
        coverFlowAdapter = new CoverFlowAdapter(imgNames.length);
        // 非同期処理で初期フレームの画像を表示する
        new LoadImageTask(MainActivity.this, coverFlowAdapter).execute(imgNames);

        // カバーフローを作成する
        this.fancyCoverFlow = (FancyCoverFlow)this.findViewById(R.id.fancyCoverFlow);
        this.fancyCoverFlow.setAdapter(coverFlowAdapter); //アダプター
        this.fancyCoverFlow.setUnselectedAlpha(1.0f); //透明度
        this.fancyCoverFlow.setUnselectedSaturation(1.0f); //彩度
        this.fancyCoverFlow.setUnselectedScale(0.5f); //縮小
        this.fancyCoverFlow.setSpacing(50); //距離
        this.fancyCoverFlow.setMaxRotation(0); //回転角度
        this.fancyCoverFlow.setScaleDownGravity(FancyCoverFlow.SCALEDOWN_GRAVITY_CENTER);  //未選択の画像の位置
        this.fancyCoverFlow.setActionDistance(FancyCoverFlow.ACTION_DISTANCE_AUTO); // わからない

        // 壁紙の最適なサイズを取得
        WallpaperManager wm = WallpaperManager.getInstance(this);
        Log.d(TAG, String.valueOf(wm.getDesiredMinimumWidth()));
        Log.d(TAG, String.valueOf(wm.getDesiredMinimumHeight()));

        // アプリの設定情報を取得
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onClick(View v){
        if(v.getId() == R.id.loadImageButton){
            // ギャラリーから画像を取得する
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
                // Version 4.4 >
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");  // 画像タイプに限定
                startActivityForResult(intent, GALLERY_INTENT);
            }else{
                // Version 4.4 <=
                // TODO: 恐らく4.4未満と同じインテント処理でもいける，後々考える
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");  // 画像タイプに限定
                startActivityForResult(intent, GALLERY_INTENT);
            }
        }else if(v.getId() == R.id.preferencesButton){
            // 設定画面の呼び出し
            getFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, new PrefsFragment())
                    .addToBackStack(null)
                    .commit();
        }else if(v.getId() == R.id.setServiceButton){
//            startService(wallPaperService);

            // サービスの状態をON（running）に設定する
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(getString(R.string.service_status_key), true);
            editor.commit();

            this.finish();  // アプリケーションを終了する
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK) return;
        if(data == null) return;
        switch(requestCode){
            case GALLERY_INTENT:
                Uri uri = data.getData();
                // Bitmap画像を作成する
                Bitmap bitmap = createBmpImagefromGallery(uri);

                // カバーフローの該当箇所を取得してBitmap画像に変更する
                int position = this.fancyCoverFlow.getSelectedItemPosition();
                coverFlowAdapter.setBitmap(position, bitmap);
                this.fancyCoverFlow.setAdapter(coverFlowAdapter); //アダプター
                this.fancyCoverFlow.setSelection(position);

                // 画像を保存
                FileOutputStream fos = null;
                try{
                    fos = openFileOutput(imgNames[position] + ".png", MODE_PRIVATE);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                }catch(FileNotFoundException e){
                    Toast.makeText(this, "画像の保存に失敗しました", Toast.LENGTH_SHORT).show();
                }finally{
                    try{
                        if(fos != null)
                            fos.close();
                    }catch(IOException e){
                        Toast.makeText(this, "ストリームの解放に失敗しました", Toast.LENGTH_SHORT).show();
                    }
                }

                break;
            default:
                break;
        }
    }

    /**
     * テキストファイルから文字列を抽出する
     *
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
     * ギャラリーで選択した画像から縮小したBitmap画像を作成する
     *
     * @param uri ギャラリーインテントから取得したURI
     * @return bmp Bitmap画像
     */
    private Bitmap createBmpImagefromGallery(Uri uri){

        InputStream is = null;
        // 最終的にリサイズしたい幅と高さを指定
//        final int SCALE_WIDTH = img.getWidth();
//        final int SCALE_HEIGHT = img.getHeight();
        // TODO: 変更の可能性大
        final int SCALE_WIDTH = 300;
        final int SCALE_HEIGHT = 700;
        // 画像オプションを設定するインスタンスを生成
        BitmapFactory.Options opt = new BitmapFactory.Options();
        // 画像のサイズ情報を読み込み，縮小率（inSampleSize）を決定する
        {
            // true: メモリ上に画像サイズの情報だけ読み込む
            opt.inJustDecodeBounds = true;
            try{
                is = getContentResolver().openInputStream(uri);
                BitmapFactory.decodeStream(is, null, opt);
            }catch(FileNotFoundException e){
                Toast.makeText(this, "ファイルが見つかりません", Toast.LENGTH_SHORT).show();
            }finally{
                if(is != null) try{
                    is.close();
                }catch(IOException e){
                    Toast.makeText(this, "ストリームのクローズに失敗しました", Toast.LENGTH_SHORT).show();
                }
            }
            // スケールする値を決める
            int sw = opt.outWidth / SCALE_WIDTH;  // opt.outWidth: 画像の幅
            int sh = opt.outHeight / SCALE_HEIGHT;  // opt.outHeight: 画像の高さ
            // 縮小するサイズを指定
            // 2のべき乗を指定する（べき乗でない場合は丸められる）
            // 2: 1/2， 4: 1/4, ...
            opt.inSampleSize = Math.max(sw, sh);
            // false: メモリ上に画像を読み込む
            opt.inJustDecodeBounds = false;
        }

        // 画像をメモリ上に展開する
        Bitmap bmp = null;
        try{
            is = getContentResolver().openInputStream(uri);
            bmp = BitmapFactory.decodeStream(is, null, opt);
        }catch(FileNotFoundException e){
            Toast.makeText(this, "ファイルが見つかりません", Toast.LENGTH_SHORT).show();
        }finally{
            if(is != null) try{
                is.close();
            }catch(IOException e){
                Toast.makeText(this, "ストリームのクローズに失敗しました", Toast.LENGTH_SHORT).show();
            }
        }

        if(bmp == null){
            // 画像の取得に失敗した場合
            Toast toast = Toast.makeText(this, "画像の読み込みに失敗しました", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return null;
        }

        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();
        // 縮小したいサイズ/画像サイズ = 縮小率
        float scale = Math.min((float)SCALE_WIDTH / bmpWidth, (float)SCALE_HEIGHT / bmpHeight);

        Matrix matrix = new Matrix();
        // 画像の表示角度を変更
        matrix.preRotate(getAngleFromExif(PathUtils.getPath(getApplicationContext(), uri)));
        // 画像のサイズを指定
        matrix.postScale(scale, scale);

        // 画像の作成
        try{
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeight, matrix, true);
        }catch(OutOfMemoryError error){
            // メモリエラーが出た場合，ガベージコレクションを走らせてトライ
            java.lang.System.gc();
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeight, matrix, true);
        }
        return bmp;
    }

    /**
     * 画像ファイルのEXIF情報から角度を取得する
     *
     * @param path 画像ファイルのパス
     * @return angle 角度（0, 90, 180, 270）
     */
    private int getAngleFromExif(String path){
        if(path == null) return 0;
        ExifInterface exifInterface;
        try{
            // 画像のパスからEXIF情報を弄るためのオブジェクトを生成
            exifInterface = new ExifInterface(path);
        }catch(IOException e){
            Log.e(TAG, "CANNOT INSTANCE EXIFINTERFACE");
            return 0;
        }

        // 画像の表示角度の取得
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        // 画像の表示角度を修正するための変数（angle）の宣言
        int angle = 0;
        if(orientation == ExifInterface.ORIENTATION_ROTATE_90)
            // 表示角度が90度の場合
            angle = 90;
        else if(orientation == ExifInterface.ORIENTATION_ROTATE_180)
            // 表示角度が180度の場合
            angle = 180;
        else if(orientation == ExifInterface.ORIENTATION_ROTATE_270)
            // 表示角度が270度の場合
            angle = 270;

        return angle;
    }
}
