package jp.ac.kansai_u.kutc.BBLink;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;

import at.technikum.mti.fancycoverflow.FancyCoverFlow;
import at.technikum.mti.fancycoverflow.FancyCoverFlowAdapter;

/**
 * カバーフローに適用するアダプター
 * @author yukihiro akasaka
 */
public class CoverFlowAdapter extends FancyCoverFlowAdapter {
    private int[] images;
    private CustomViewGroup imageViews[];
    private Bitmap bmpArray[];

    /**
     * @param size カバーフローのアイテム数
     */
    CoverFlowAdapter(int size){
        super();
        images = new int[size];  // カバーフローのアイテム数領域を確保（※ライブラリの都合上必ず必要）
        imageViews = new CustomViewGroup[size];  // オリジナルレイアウト
        bmpArray = new Bitmap[size];  // アイテム数の画像領域
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public Integer getItem(int i) {
        return images[i];
    }

    /**
     * Bitmap 画像を保存する
     * @param i Bitmap 画像を保存するインデックス番号
     * @param bmp 保存する Bitmap 画像
     */
    public void setBitmap(int i, Bitmap bmp) {
        bmpArray[i] = bmp;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getCoverFlowItem(int i, View reuseableView, ViewGroup viewGroup) {
        imageViews[i] = null;

        if (reuseableView != null) { // 一度作成した View が存在するかどうか，という意味かな？
            imageViews[i] = (CustomViewGroup) reuseableView;
        } else {
            imageViews[i] = new CustomViewGroup(viewGroup.getContext());
            imageViews[i].setLayoutParams(new FancyCoverFlow.LayoutParams(300, 700));
        }

        imageViews[i].setBitmap(bmpArray[i]);
        imageViews[i].getImageView().setImageBitmap(imageViews[i].getBitmap());
        return imageViews[i];
    }
}

