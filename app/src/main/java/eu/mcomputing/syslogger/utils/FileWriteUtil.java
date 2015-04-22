package eu.mcomputing.syslogger.utils;

import android.os.Environment;
import android.text.format.Time;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Janko on 4/22/2015.
 */
public final class FileWriteUtil {
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static void appendToFile(String what, String path){
        try {
            File logfile = new File(path);
            FileOutputStream stream = new FileOutputStream(logfile, true);
            try {
                stream.write(what.getBytes());
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getNowUTC() {
        Time now = new Time("UTC");
        now.setToNow();
        return now.format2445();
    }
}
