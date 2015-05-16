package eu.mcomputing.syslogger.utils;

/**
 * Created by Janko on 5/8/2015.
 */
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBAdapter {

    // Variable to hold the database instance
    private SQLiteDatabase db;
    // Context of the application using the database.
    private final Context context;

    private static final String DATABASE_NAME = "logs";
    private static int DATABASE_VERSION = 1;
    private myDbHelper dbHelper;

    public MyDBAdapter(Context _context) {
        context = _context;
        dbHelper = new myDbHelper(context, DATABASE_NAME, null,
                DATABASE_VERSION);
    }

    public MyDBAdapter open() throws SQLException {
        if (db == null || (db != null && !db.isOpen()))
            db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (db != null)
            db.close();
    }

    // Database open/upgrade helper
    private static class myDbHelper extends SQLiteOpenHelper {

        public myDbHelper(Context context, String name,
                          CursorFactory factory,
                          int version) {
            super(context, name, factory, version);
        }

        // Called when no database exists in disk
        // and the helper class needs to create a new one.
        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL("CREATE TABLE nmap("
                    + "ip TEXT PRIMARY KEY,"
                    + "name TEXT,"
                    + "ports TEXT" + ")");
        }

        @Override
        public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub

        }
    }

    public void insertNmaIP(String... str) {
        if (!isOpen()) {
            open();
        }
        ContentValues initialValues = new ContentValues();
        // as column'a' is INTEGER PRIMARY KEY, it get increament by SQLite
        initialValues.put("ip", str[0]);
        initialValues.put("name", str[1]);
        initialValues.put("ports", str[2]);
        db.insert("nmap", // table name
                null, initialValues // column name-value pairs
        );

        db.close();
    }

    public List<String> getDNas(String ip) throws SQLException {
        List<String> list = new ArrayList<String>();
        if (!isOpen()) {
            open();
        }
        Cursor mCursor = db.query(true, // isdistinct
                "nmap", // table name
                new String[] { "ip", "name", "ports" },// select clause
                "ip='"+ip+"'", // where cluase
                null, // where clause parameters
                null, // group by
                null, // having
                null, // orderby
                null);// limit

        if (mCursor != null) {
            // mCursor.moveToFirst();
            if (mCursor.moveToFirst()) {
                do {
                    list.add(mCursor.getString(0));
                    list.add(mCursor.getString(1));
                    list.add(mCursor.getString(2));
                } while (mCursor.moveToNext());
            }
        }
        if (!mCursor.isClosed())
            mCursor.close();
        close();
        return list;
    }

   /* public int updateRecord(int id, String str) {
        int result = -1;
        if (!isOpen()) {
            open();
        }
        ContentValues values = new ContentValues();
        values.put("b", str);
        result = db.update("t1", // table
                values, // values to be updated
                "a" + "=" + id,// where clause
                null);
        close();
        return result;
    }*/

    /*public int deleteRecord(int id) {
        System.out.println("Inside deleteAccount.");
        if (!isOpen()) {
            open();
        }
        int result = db.delete("t1",// table name
                "a" + " = " + id,// where clause
                null);
        close();
        return result;
    }*/

    public void dropTable(String tableName) {
        if (!isOpen()) {
            open();
        }
        System.out.println("Inside Drop Table.");

        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor!=null) {
           if(cursor.getCount()>0) {
              cursor.close();
              db.execSQL("drop table " + tableName );
           }
           cursor.close();
        }
        close();
    }

    private boolean isOpen() {
        if (db == null)
            return false;
        return db.isOpen();
    }
}
