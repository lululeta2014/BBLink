package jp.ac.kansai_u.kutc.BBLink;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * カバーフローで実際に表示するライナーレイアウト TODO: FrameLayout に変更する
 * Created by yukihiro on 2014/07/05.
 */
public class CustomViewGroup extends LinearLayout {

    private Bitmap bmp; // 表示する Bitmap 画像 TODO: 恐らくいらない（※今後次第）
    private ImageView imageView;


    public CustomViewGroup(Context context) {
        // TODO: レイアウトは大幅変更する
        super(context);

        this.setOrientation(VERTICAL);
        this.imageView = new ImageView(context);

        // 画像の表示領域
        LinearLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.imageView.setLayoutParams(layoutParams);

        this.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        this.imageView.setAdjustViewBounds(true);

        this.addView(this.imageView);
    }

    /**
     * ImageView を取得する
     * @return 作成した ImageView
     */
    public ImageView getImageView() {
        return imageView;
    }

    /**
     * Bitmap 画像を設定する
     * @param b 設定する Bitmap 画像　
     */
    public void setBitmap(Bitmap b) {
        this.bmp = b;
    }

    /**
     * Bitmap 画像を取得する
     * @return 設定した Bitmap 画像
     */
    public Bitmap getBitmap() { return this.bmp; }
}
