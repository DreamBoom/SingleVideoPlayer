package com.seiko.player.subtitle;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.seiko.player.subtitle.format.FatalParsingException;
import com.seiko.player.subtitle.format.SubtitleFormat;
import com.seiko.player.subtitle.format.TimedTextFileFormat;
import com.seiko.player.subtitle.format.TimedTextObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Created by xyoye on 2019/5/6.
 * @author xyoye
 */
public class SubtitleParser {

    private final String subtitlePath;

    public SubtitleParser(@NonNull String path){
        this.subtitlePath = path;
    }

    public TimedTextObject parser() {
        try {
            if (!TextUtils.isEmpty(subtitlePath)) {
                //解析字幕文件
                File subtitleFile = new File(subtitlePath);
                if (subtitleFile.exists()) {
                    //获取解析工具
                    TimedTextFileFormat format = SubtitleFormat.format(subtitlePath);
                    if (format != null) {
                        //获取字幕对象
                        TimedTextObject subtitleObj = format.parseFile(subtitleFile);
                        if (subtitleObj != null && subtitleObj.captions.size() > 0) {
                            return subtitleObj;
                        }else {
                            throw new RuntimeException("解析字幕文件失败");
                        }
                    } else {
                        throw new RuntimeException("字幕文件错误");
                    }
                } else {
                    throw new RuntimeException("字幕文件不存在");
                }
            } else {
                throw new RuntimeException("字幕文件地址错误：" + subtitlePath);
            }
        } catch (FatalParsingException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("解析字幕文件失败");
        } catch (UnsupportedCharsetException ex) {
            ex.printStackTrace();
            throw new RuntimeException("解析编码格式失败");
        }
    }

//    使用兼容更多编码解析的库
//    /**
//     * 根据文件前两个字节获取编码格式
//     *
//     * 注：此处inputStream不能传入使用
//     */
//    private Charset getCharset(String filePath){
//        String encoding = "UTF-8";
//        InputStream inputStream = null;
//        BufferedInputStream bis = null;
//        try {
//            File file = new File(filePath);
//            inputStream = new FileInputStream(file);
//            bis = new BufferedInputStream(inputStream);
//            int code = (bis.read() << 8) + bis.read();
//            switch (code) {
//                case 0xefbb:
//                    encoding = "UTF-8";
//                    break;
//                case 0xfffe:
//                    encoding = "Unicode";
//                    break;
//                case 0xfeff:
//                    encoding = "UTF-16BE";
//                    break;
//                case 0x5c75:
//                    encoding = "ASCII";
//                    break;
//            }
//            LogUtils.e("字幕文件编码格式为："+encoding);
//        }catch (Exception e){
//            e.printStackTrace();
//            LogUtils.e("读取字幕编码格式失败："+e);
//        }finally {
//            try {
//                if (bis != null)
//                    bis.close();
//                if (inputStream != null)
//                    inputStream.close();
//            }catch (IOException e){
//                e.printStackTrace();
//            }
//
//        }
//        return Charset.forName(encoding);
//    }
}
