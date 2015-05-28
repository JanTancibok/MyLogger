package eu.mcomputing.syslogger.utils;

import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Janko on 4/22/2015.
 */
public final class FileWriteUtil {
    public static final Map<Integer,String> states;
    static{
       HashMap cm = new HashMap<String,String>();
        cm.put("01", "TCP_ESTABLISHED");
        cm.put("02", "TCP_SYN_SENT");
        cm.put("03", "TCP_SYN_RECV");
        cm.put("04", "TCP_FIN_WAIT1");
        cm.put("05", "TCP_FIN_WAIT2");
        cm.put("06", "TCP_TIME_WAIT");
        cm.put("07", "TCP_CLOSE");
        cm.put("08", "TCP_CLOSE_WAIT");
        cm.put("09", "TCP_LAST_ACK");
        cm.put("0A", "TCP_LISTEN");
        cm.put("0B", "TCP_CLOSING");    // Now a valid state
        cm.put("0C", "TCP_MAX_STATES");  // Leave at the end!
       states = Collections.unmodifiableMap(cm);
    }

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

    public static String httpObject(JSONObject object) {
        HttpResponse response = null;
        String result = "";
        try {
            //Create a HTTP Client
            HttpClient httpclient = new DefaultHttpClient();
            //Create and object to Post values to the server
            //The url is specified in the Constants class to increase modifiability
            HttpPost httpPost = new HttpPost("http://jan.tancibok.sk/AndroidSysLogger/in.php");
            //Set the attributes to be posted as Parameters

            StringEntity entity = new StringEntity(object.toString(), HTTP.UTF_8);
            entity.setContentType("application/json");
            httpPost.setEntity(entity);

            //Execute the post and get the response
            response = httpclient.execute(httpPost);
            result = response.toString();
        } catch (IOException e) {
            e.printStackTrace();
            result = "IOexeption";
        }
        return result;
    }

    public static String httpZIP(File zipfile,String devid) {
        HttpResponse response = null;
        String result = "";
        try {
            //http://stackoverflow.com/questions/2017414/post-multipart-request-with-android-sdk
            HttpClient httpclient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost("http://jan.tancibok.sk/AndroidSysLogger/zip.php");

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            InputStream inputStream = new FileInputStream(zipfile);
            if(zipfile.exists())
            {
                entityBuilder.addBinaryBody("zipfile", inputStream, ContentType.create("application/zip"), "name");
            }
            StringBody stringBody2 = new StringBody(devid, ContentType.MULTIPART_FORM_DATA);
            entityBuilder.addPart("devid",stringBody2);

            HttpEntity entity = entityBuilder.build();
            httpPost.setEntity(entity);
            response = httpclient.execute(httpPost);
            HttpEntity httpEntity = response.getEntity();
            result = EntityUtils.toString(httpEntity);
            Log.v("result", result);
        } catch (IOException e) {
            e.printStackTrace();
            result = "IOexeption";
        }
        return result;
    }

    public static int nthOccurrence(String str, char c, int n) {
        int pos = str.indexOf(c, 0);
        while (n-- > 0 && pos != -1)
            pos = str.indexOf(c, pos+1);
        return pos;
    }
    public static boolean ziper(String[] _files, String zipFileName){
          try {
                BufferedInputStream origin = null;
                FileOutputStream dest = new FileOutputStream(zipFileName);
                ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                        dest));
                byte data[] = new byte[1024];

                for (int i = 0; i < _files.length; i++) {
                    Log.v("Compress", "Adding: " + _files[i]);
                    FileInputStream fi = new FileInputStream(_files[i]);
                    origin = new BufferedInputStream(fi, 1024);

                    ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;

                    while ((count = origin.read(data, 0, 1024)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }

                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        return true;
    }
}
