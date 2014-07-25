package jp.ac.kansai_u.kutc.BBLink;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 画像関連の処理を纏めたもの
 *
 * @author akasaka
 */
public class ImageUtils{
    static private final String TAG = ImageUtils.class.getSimpleName();

    static public Bitmap loadBitmapFromFileName(Context context, String fileName){
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try{
            // 画像ファイル名からストリームを作成する
            fis = context.openFileInput(fileName + ".png");
            bitmap = BitmapFactory.decodeStream(fis);
        }catch(FileNotFoundException e){
            // ファイルが存在しない場合，初期画像を表示する
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.init_img);
        }finally{
            if(fis != null)
                try{
                    fis.close();
                }catch(IOException e){
                    Log.d(TAG, "ストリームの解放に失敗しました");
                }
        }
        return bitmap;
    }

    /**
     * ギャラリーで選択した画像から縮小したBitmap画像を作成する
     *
     * @param uri ギャラリーインテントから取得したURI
     * @return bmp Bitmap画像
     */
    static public Bitmap createBmpImageFromUri(Context context, Uri uri){

        // 最終的にリサイズしたい幅と高さを指定
//        final int SCALE_WIDTH = img.getWidth();
//        final int SCALE_HEIGHT = img.getHeight();
        // TODO: 変更の可能性大
        final int SCALE_WIDTH = 300;
        final int SCALE_HEIGHT = 700;

        InputStream is = null;
        // 画像オプションを設定するインスタンスを生成
        BitmapFactory.Options opt = new BitmapFactory.Options();
        // 画像のサイズ情報を読み込み，縮小率（inSampleSize）を決定する
        {
            // true: メモリ上に画像サイズの情報だけ読み込む
            opt.inJustDecodeBounds = true;
            try{
                is = context.getContentResolver().openInputStream(uri);
                BitmapFactory.decodeStream(is, null, opt);
            }catch(FileNotFoundException e){
                Toast.makeText(context, "ファイルが見つかりません", Toast.LENGTH_SHORT).show();
            }finally{
                if(is != null)
                    try{
                        is.close();
                    }catch(IOException e){
                        Log.e(TAG, "ストリームのクローズに失敗しました");
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
            is = context.getContentResolver().openInputStream(uri);
            bmp = BitmapFactory.decodeStream(is, null, opt);
        }catch(FileNotFoundException e){
            Toast.makeText(context, "ファイルが見つかりません", Toast.LENGTH_SHORT).show();
        }finally{
            if(is != null)
                try{
                    is.close();
                }catch(IOException e){
                    Log.e(TAG, "ストリームのクローズに失敗しました");
                }
        }

        if(bmp == null){
            // 画像の取得に失敗した場合
            Toast toast = Toast.makeText(context, "画像の読み込みに失敗しました", Toast.LENGTH_SHORT);
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
        matrix.preRotate(getAngleFromExif(PathUtils.getPath(context, uri)));
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
    static public int getAngleFromExif(String path){
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
