package com.seiko.player.widgets;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.seiko.player.utils.ConvertUtils;
import com.seiko.player.core.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xyoye on 2019/4/30.
 *
 * @author xyoye
 */

public class DialogScreenShot extends Dialog {
    private static final String TAG = "DialogScreenShot";

    private Bitmap bitmap;

    public DialogScreenShot(@NonNull Context context, Bitmap bitmap) {
        super(context, R.style.AnimateDialog);
        this.bitmap = bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_screen_shot);

        ImageView shotIv = findViewById(R.id.shot_iv);
        Button shotCancelBt = findViewById(R.id.shot_cancel_bt);
        Button shotSaveBt = findViewById(R.id.shot_save_bt);

        shotIv.setImageBitmap(bitmap);

        shotCancelBt.setOnClickListener(v -> DialogScreenShot.this.dismiss());

        shotSaveBt.setOnClickListener(v -> {
            String path = saveImage(bitmap);
            if (path == null) {
                Toast.makeText(getContext(), "保存截图失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "保存成功：" + path, Toast.LENGTH_SHORT).show();
            }
            DialogScreenShot.this.dismiss();
        });
    }

    @Override
    public void show() {
        super.show();

        if (getWindow() == null) {
            return;
        }

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = ConvertUtils.dp2px(getContext(), 450);
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(layoutParams);
    }

    @SuppressLint("SimpleDateFormat")
    private String saveImage(Bitmap bitmap) {
        FileOutputStream fos = null;
        String imagePath;
        try {
            //make folder
            String folderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DanDanPlay/_image";
            File folder = new File(folderPath);
            if (!folder.exists() && !folder.mkdirs()) {
                return null;
            }

            //make file name
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
            //获取当前时间
            Date curDate = new Date(System.currentTimeMillis());
            String curTime = formatter.format(curDate);
            String fileName = "/SHOT_" + curTime + ".jpg";

            //make file
            File file = new File(folder, fileName);
            if (file.exists() && !file.delete()) {
                Log.w(TAG, "File can't delete: " + file.getAbsolutePath());
                return null;
            }

            if (!file.createNewFile()) {
                Log.w(TAG, "File can't create new file: " + file.getAbsolutePath());
                return null;
            }

            //save bitmap
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            imagePath = file.getAbsolutePath();

            //通知系统相册刷新
            getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(new File(file.getPath()))));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return imagePath;
    }
}
