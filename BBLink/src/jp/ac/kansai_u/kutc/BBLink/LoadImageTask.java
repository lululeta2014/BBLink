package jp.ac.kansai_u.kutc.BBLink;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 画像読み込みを非同期で行うクラス
 *
 * @author akasaka
 */
public class LoadImageTask extends AsyncTask<String, Integer, Bitmap[]>{
    private final String TAG = LoadImageTask.class.getSimpleName();
    private Activity activity;
    private CoverFlowAdapter coverFlowAdapter;
    private ProgressDialog progressDialog;

    LoadImageTask(Activity a, CoverFlowAdapter adapter){
        super();
        this.activity = a;
        this.coverFlowAdapter = adapter;
    }

    @Override
    protected void onPreExecute(){
        // プログレスダイアログの生成
        progressDialog = new ProgressDialog(activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Loading Images...");
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.show();
    }

    @Override
    protected Bitmap[] doInBackground(String... imgNames){
        progressDialog.setMax(imgNames.length);
        Bitmap[] bitmaps = new Bitmap[imgNames.length];
        for(int i = 0; i < imgNames.length; i++){
            FileInputStream fis = null;
            try{
                // 画像ファイル名からストリームを作成する
                fis = activity.openFileInput(imgNames[i] + ".png");
                bitmaps[i] = BitmapFactory.decodeStream(fis);
            }catch(FileNotFoundException e){
                // ファイルが存在しない場合，初期画像を表示する
                bitmaps[i] = BitmapFactory.decodeResource(activity.getResources(), R.drawable.init_img);
            }finally{
                if(fis != null)
                    try{
                        fis.close();
                    }catch(IOException e){
                        Log.d(TAG, "ストリームの解放に失敗しました");
                    }
            }
            progressDialog.incrementProgressBy(1);  // 進捗プラス１
        }
        return bitmaps;
    }

    @Override
    protected void onPostExecute(Bitmap[] bitmaps){
        progressDialog.dismiss();  // プログレスダイアログの非表示
        coverFlowAdapter.swapImages(bitmaps);
    }
}
