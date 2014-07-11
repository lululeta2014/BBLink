package jp.ac.kansai_u.kutc.BBLink;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import at.technikum.mti.fancycoverflow.FancyCoverFlow;

import java.io.*;

/**
 * アプリケーションのスタートとなるアクティビティ
 * @author akasaka
 */
public class MainActivity extends Activity implements View.OnClickListener{
    private FancyCoverFlow fancyCoverFlow;
    private CoverFlowAdapter coverFlowAdapter = new CoverFlowAdapter();

    // フレーム画像格納用 TODO: 後々変更の可能性あり
    private final int[] frameImages = {R.drawable.image1, R.drawable.image2, R.drawable.image3,
                                 R.drawable.image4, R.drawable.image5, R.drawable.image6, };

    private final String TAG = MainActivity.class.getSimpleName();  // クラス名
    final int GALALLY_INTENT = 0x12FCEA7;  // ギャラリーインテント時の返却値（任意の数値）
    Intent wallPaperService = null;  // WallPaperServiceクラス起動用のインテント

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

        // 初期フレームの画像を設定する TODO: 後々変更の可能性あり
        for (int i = 0; i < frameImages.length; i++) {
            Resources r = getResources();
            Bitmap bmp = BitmapFactory.decodeResource(r, frameImages[i]);
            coverFlowAdapter.setBitmap(i, bmp);
        }

        // カバーフローを作成する
        this.fancyCoverFlow = (FancyCoverFlow) this.findViewById(R.id.fancyCoverFlow);
        this.fancyCoverFlow.setAdapter(coverFlowAdapter); //アダプター
        this.fancyCoverFlow.setUnselectedAlpha(1.0f); //透明度
        this.fancyCoverFlow.setUnselectedSaturation(1.0f); //彩度
        this.fancyCoverFlow.setUnselectedScale(0.5f); //縮小
        this.fancyCoverFlow.setSpacing(50); //距離
        this.fancyCoverFlow.setMaxRotation(0); //回転角度
        this.fancyCoverFlow.setScaleDownGravity(0.5f); //数値を上げるほど，未選択の画像が下に下がってくる
        this.fancyCoverFlow.setActionDistance(FancyCoverFlow.ACTION_DISTANCE_AUTO); // わからない
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
                startActivityForResult(intent, GALALLY_INTENT);
            }else{
                // Version 4.4 <=
                // TODO: 恐らく4.4未満と同じインテント処理でもいける，後々考える
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");  // 画像タイプに限定
                startActivityForResult(intent, GALALLY_INTENT);
            }
        }else if(v.getId() == R.id.preferencesButton){

        }else if(v.getId() == R.id.setServiceButton){
//            startService(wallPaperService);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK) return;
        if(data == null) return;
        switch(requestCode){
            case GALALLY_INTENT:
                Uri uri = data.getData();
                // Bitmap画像を作成する
                Bitmap bitmap = createBmpImagefromGallery(uri);

                // カバーフローの該当箇所を取得してBitmap画像に変更する
                int position = this.fancyCoverFlow.getSelectedItemPosition();
                coverFlowAdapter.setBitmap(position, bitmap);
                this.fancyCoverFlow.setAdapter(coverFlowAdapter); //アダプター
                this.fancyCoverFlow.setSelection(position);

                break;
            default:
                break;
        }
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
     * ギャラリーで選択した画像から縮小したBitmap画像を作成する
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
        float scale = Math.min((float)SCALE_WIDTH/bmpWidth, (float)SCALE_HEIGHT/bmpHeight);

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
