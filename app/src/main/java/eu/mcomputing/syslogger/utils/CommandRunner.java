package eu.mcomputing.syslogger.utils;

import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/*
* autor: https://github.com/amoghbl1/nmap-android
* */
public class CommandRunner {

    private static String TAG = "cmdrunner";

    public static String execCommand(String command, File currentDirectory) throws IOException, InterruptedException{
        Log.d(TAG,command);
        Log.d(TAG,currentDirectory.getAbsolutePath());
        Process process = Runtime.getRuntime().exec(command, null, currentDirectory);
        process.waitFor();
        BufferedReader readerInputStream = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        BufferedReader readerErrorStream = new BufferedReader(
                new InputStreamReader(process.getErrorStream())
        );
        int read;
        char[] buf = new char[4096];
        StringBuffer output = new StringBuffer();
        StringBuffer error = new StringBuffer();
        while ((read = readerInputStream.read(buf)) > 0) {
            output.append(buf, 0, read);
        }
        while((read = readerErrorStream.read(buf)) > 0) {
            error.append(buf, 0, read);
        }
        readerInputStream.close();
        readerErrorStream.close();
        Log.d(TAG, error.toString());
        return output.toString() + error.toString();
    }
}