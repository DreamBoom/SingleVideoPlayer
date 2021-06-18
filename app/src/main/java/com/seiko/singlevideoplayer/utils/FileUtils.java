package com.seiko.singlevideoplayer.utils;

import java.io.File;

public class FileUtils {

    private static boolean isSpace(final String s) {
        if (s == null) {
            return true;
        }
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String getFileName(final String filePath) {
        if (isSpace(filePath)) {
            return "";
        }
        int lastSep = filePath.lastIndexOf(File.separator);
        return lastSep == -1 ? filePath : filePath.substring(lastSep + 1);
    }

    public static String getFileNameNoExtension(final String filePath) {
        if (isSpace(filePath)) {
            return "";
        }
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastSep == -1) {
            return (lastPoi == -1 ? filePath : filePath.substring(0, lastPoi));
        }
        if (lastPoi == -1 || lastSep > lastPoi) {
            return filePath.substring(lastSep + 1);
        }
        return filePath.substring(lastSep + 1, lastPoi);
    }

}
