package com.seiko.singlevideoplayer.ui;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.seiko.singlevideoplayer.R;
import com.seiko.singlevideoplayer.data.PlayParam;
import com.seiko.singlevideoplayer.utils.FileUtils;
import com.seiko.singlevideoplayer.utils.ToastUtils;

import java.io.File;

/**
 * @author xyoye
 */
public class PlayerManagerActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 视频来源-外部
     */
    public static final int SOURCE_ORIGIN_OUTSIDE = 1000;

    /**
     * 视频来源-本地文件
     */
    public static final int SOURCE_ORIGIN_LOCAL = 1001;

    /**
     * 视频来源-串流
     */
    public static final int SOURCE_ORIGIN_STREAM = 1002;

    /**
     * 视频来源-局域网
     */
    public static final int SOURCE_ORIGIN_SMB = 1003;

    /**
     * 视频来源-远程连接
     */
    public static final int SOURCE_ORIGIN_REMOTE = 1004;

    /**
     * 视频来源-在线播放
     */
    public static final int SOURCE_ONLINE_PREVIEW = 1005;


    private String videoPath;
    private String videoTitle;
//    private String danmuPath;
    private long currentPosition;
    private int episodeId;
//    private String searchWord;

    private TextView mErrorTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_manager);
        initViews();
    }

    private void initViews() {
        mErrorTv = findViewById(R.id.error_tv);

        View decorView = this.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mErrorTv.setVisibility(View.GONE);

        findViewById(R.id.back_iv).setOnClickListener(this);

        initIntent();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back_iv) {
            PlayerManagerActivity.this.finish();
        }
    }

    private void initIntent() {
        Intent openIntent = getIntent();
        videoTitle = openIntent.getStringExtra("video_title");
        videoPath = openIntent.getStringExtra("video_path");
//        danmuPath = openIntent.getStringExtra("danmu_path");
        currentPosition = openIntent.getLongExtra("current_position", 0);
        episodeId = openIntent.getIntExtra("episode_id", 0);
//        thunderTaskId = openIntent.getLongExtra("thunder_task_id", -1);
//        searchWord = openIntent.getStringExtra("search_word");

//        sourceOrigin = openIntent.getIntExtra("source_origin", SOURCE_ORIGIN_LOCAL);

        //本地文件播放可预先绑定弹幕，其它播放来源都应弹窗询问是否选择弹幕
//        boolean showSelectDanmuDialog = AppConfig.getInstance().isShowOuterChainDanmuDialog();
//        boolean autoLaunchDanmuPage = AppConfig.getInstance().isOuterChainDanmuSelect();


        //外部打开
        if (Intent.ACTION_VIEW.equals(openIntent.getAction())) {
//            sourceOrigin = SOURCE_ORIGIN_OUTSIDE;
            //获取视频地址
            Uri data = getIntent().getData();
            if (data != null) {
                videoPath = getRealFilePath(PlayerManagerActivity.this, data);
            }
        }

        //检查视频地址
        if (TextUtils.isEmpty(videoPath)) {
            ToastUtils.showShort("解析视频地址失败");
            mErrorTv.setVisibility(View.VISIBLE);
            return;
        }

//        //检查弹幕地址
//        if (!TextUtils.isEmpty(danmuPath) && danmuPath.toLowerCase().endsWith(".xml")) {
//            File danmuFile = new File(danmuPath);
//            if (!danmuFile.exists() || !danmuFile.isFile()) {
//                danmuPath = "";
//            }
//        }

        //检查视频标题
        videoTitle = TextUtils.isEmpty(videoTitle) ? FileUtils.getFileName(videoPath) : videoTitle;
//        searchWord = TextUtils.isEmpty(searchWord) ? FileUtils.getFileNameNoExtension(videoPath) : searchWord;

//        //选择弹幕弹窗及跳转
//        if (sourceOrigin != SOURCE_ORIGIN_LOCAL) {
//            if (showSelectDanmuDialog) {
//                new DanmuSelectDialog(this, isSelectDanmu -> {
//                    if (isSelectDanmu) {
//                        launchDanmuSelect(searchWord);
//                    } else {
//                        launchPlayerActivity();
//                    }
//                }).show();
//                return;
//            }
//            if (autoLaunchDanmuPage) {
//                launchDanmuSelect(searchWord);
//                return;
//            }
//        }
        launchPlayerActivity();
    }

    /**
     * 启动播放器
     */
    private void launchPlayerActivity() {

        PlayParam playParam = new PlayParam();
        playParam.setVideoTitle(videoTitle);
        playParam.setVideoPath(videoPath);
//        playParam.setDanmuPath(danmuPath);
        playParam.setEpisodeId(episodeId);
        playParam.setCurrentPosition(currentPosition);
//        playParam.setSourceOrigin(sourceOrigin);
//        if (sourceOrigin == SOURCE_ONLINE_PREVIEW) {
//            playParam.setThunderTaskId(thunderTaskId);
//        }

        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("video_data", playParam);
        this.startActivity(intent);
        PlayerManagerActivity.this.finish();
    }

    private static final String EXTERNAL_STORAGE = "com.android.externalstorage.documents";
    private static final String DOWNLOAD_DOCUMENT = "com.android.providers.downloads.documents";
    private static final String MEDIA_DOCUMENT = "com.android.providers.media.documents";
    private static final String DOWNLOAD_URI = "content://downloads/public_downloads";

    private static final String SCHEME_CONTENT = "content";
    private static final String SCHEME_FILE = "file";
    private static final String SCHEME_HTTP = "http";
    private static final String SCHEME_HTTPS = "https";

    /**
     * 获取Uri真实地址
     */
    @Nullable
    private static String getRealFilePath(@NonNull Context context, @NonNull Uri uri) {
        boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            String authority = uri.getAuthority();
            if (authority == null) {
                return "";
            }
            switch (authority) {
                case EXTERNAL_STORAGE:
                    String docId = DocumentsContract.getDocumentId(uri);
                    String[] exSplit = docId.split(":");
                    String type = exSplit[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + exSplit[1];
                    }
                    break;
                case DOWNLOAD_DOCUMENT:
                    String id = DocumentsContract.getDocumentId(uri);
                    Uri documentUri = ContentUris.withAppendedId(Uri.parse(DOWNLOAD_URI), Long.valueOf(id));
                    return getDataColumn(context, documentUri, null, null);
                case MEDIA_DOCUMENT:
                    String[] split = DocumentsContract.getDocumentId(uri).split(":");
                    Uri contentUri = null;
                    switch (split[0]) {
                        case "image":
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                            break;
                        case "video":
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                            break;
                        case "audio":
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                            break;
                        default:
                    }
                    return getDataColumn(context, contentUri, "_id=?", new String[]{split[1]});
                default:
            }
        } else if (SCHEME_CONTENT.equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        } else if (SCHEME_FILE.equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        } else if (SCHEME_HTTP.equalsIgnoreCase(uri.getScheme())) {
            return uri.toString();
        } else if (SCHEME_HTTPS.equalsIgnoreCase(uri.getScheme())) {
            return uri.toString();
        }
        return null;
    }

    @Nullable
    private static String getDataColumn(@NonNull Context context, Uri uri, String selection, String[] selectionArgs) {
        context.grantUriPermission(context.getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try (Cursor cursor = context.getContentResolver().query(uri, new String[]{"_data"}, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getString(0);
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 播放本地文件
     */
    public static void launchPlayerLocal(Context context, String title, String path, long position, int episodeId) {
        Intent intent = new Intent(context, PlayerManagerActivity.class);
        intent.putExtra("video_title", title);
        intent.putExtra("video_path", path);
//        intent.putExtra("danmu_path", danmu);
        intent.putExtra("current_position", position);
        intent.putExtra("episode_id", episodeId);
        intent.putExtra("source_origin", PlayerManagerActivity.SOURCE_ORIGIN_LOCAL);
        context.startActivity(intent);
    }

    /**
     * 播放局域网文件
     */
    public static void launchPlayerSmb(Context context, String title, String path) {
        Intent intent = new Intent(context, PlayerManagerActivity.class);
        intent.putExtra("video_title", title);
        intent.putExtra("video_path", path);
//        intent.putExtra("danmu_path", "");
        intent.putExtra("current_position", 0);
        intent.putExtra("episode_id", 0);
        intent.putExtra("source_origin", PlayerManagerActivity.SOURCE_ORIGIN_SMB);
        context.startActivity(intent);
    }

    /**
     * 播放串流链接
     */
    public static void launchPlayerStream(Context context, String title, String path, long position, int episodeId) {
        Intent intent = new Intent(context, PlayerManagerActivity.class);
        intent.putExtra("video_title", title);
        intent.putExtra("video_path", path);
//        intent.putExtra("danmu_path", danmu);
        intent.putExtra("current_position", position);
        intent.putExtra("episode_id", episodeId);
        intent.putExtra("source_origin", PlayerManagerActivity.SOURCE_ORIGIN_STREAM);
        context.startActivity(intent);
    }

    /**
     * 播放远程文件
     */
    public static void launchPlayerRemote(Context context, String title, String path, long position, int episodeId) {
        Intent intent = new Intent(context, PlayerManagerActivity.class);
        intent.putExtra("video_title", title);
        intent.putExtra("video_path", path);
//        intent.putExtra("danmu_path", danmu);
        intent.putExtra("current_position", position);
        intent.putExtra("episode_id", episodeId);
        intent.putExtra("source_origin", PlayerManagerActivity.SOURCE_ORIGIN_REMOTE);
        context.startActivity(intent);
    }

//    /**
//     * 在线播放文件
//     */
//    public static void launchPlayerOnline(Context context, String title, String path, long position, int episodeId, long thunderTaskId, String searchWord) {
//        Intent intent = new Intent(context, PlayerManagerActivity.class);
//        intent.putExtra("video_title", title);
//        intent.putExtra("video_path", path);
////        intent.putExtra("danmu_path", danmu);
//        intent.putExtra("current_position", position);
//        intent.putExtra("episode_id", episodeId);
//        intent.putExtra("thunder_task_id", thunderTaskId);
//        intent.putExtra("search_word", searchWord);
//        intent.putExtra("source_origin", PlayerManagerActivity.SOURCE_ONLINE_PREVIEW);
//        context.startActivity(intent);
//    }

//    /**
//     * 播放历史记录
//     */
//    public static void launchPlayerHistory(Context context, String title, String path, long position, int episodeId, int sourceOrigin) {
//        Intent intent = new Intent(context, PlayerManagerActivity.class);
//        intent.putExtra("video_title", title);
//        intent.putExtra("video_path", path);
////        intent.putExtra("danmu_path", danmu);
//        intent.putExtra("current_position", position);
//        intent.putExtra("episode_id", episodeId);
//        intent.putExtra("source_origin", sourceOrigin);
//        context.startActivity(intent);
//    }
}
