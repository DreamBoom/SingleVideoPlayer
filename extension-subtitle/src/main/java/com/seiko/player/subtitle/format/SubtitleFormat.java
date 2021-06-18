package com.seiko.player.subtitle.format;

/**
 * Created by xyoye on 2018/9/20.
 */
public class SubtitleFormat {

    private static final String SEP = ".";

    public static TimedTextFileFormat format(String path){
        if(path.contains(SEP)){
            TimedTextFileFormat fileFormat = null;
            int end = path.lastIndexOf(".");
            String ext = path.substring(end+1);
            switch (ext.toUpperCase()){
                case "ASS":
                    fileFormat = new FormatASS();
                    break;
                case "SCC":
                    fileFormat = new FormatSCC();
                    break;
                case "SRT":
                    fileFormat = new FormatSRT();
                    break;
                case "STL":
                    fileFormat = new FormatSTL();
                    break;
                case "XML":
                    fileFormat = new FormatTTML();
                    break;
                default:
                    break;
            }
            return fileFormat;
        }
        return null;
    }
}
