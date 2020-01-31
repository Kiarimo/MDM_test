package com.abupdate.mdm.utils;

import java.io.File;

public class FileUtil {

    public static long getFileSize(String path) {

        File file = new File(path);
        return (file.exists() && file.isFile()) ? file.length() : 0;

    }


}
