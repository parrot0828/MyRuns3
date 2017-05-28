package kangwon.cse.jck.myruns3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by jeon on 2017-05-28.
 */

public class ExerciseDbHelper extends SQLiteOpenHelper {
    public static final String CREATE_TABLE_ENTRIES = "CREATE TABLE IF NOT EXISTS entry (_id INTEGER PRIMARY KEY AUTOINCREMENT, input_type INTEGER NOT NULL, activity_type INTEGER NOT NULL, date_time DATETIME NOT NULL, duration FLOAT, distance FLOAT, avg_speed FLOAT,calories INTEGER, climb FLOAT, heartrate INTEGER, comment TEXT, privacy INTEGER);";
    private static final String DATABASE_NAME = "entry.db";
    private static final int DATABASE_VERSION = 1;
    public static final String KEY_ACTIVITY_TYPE = "activity_type";
    public static final String KEY_AVG_SPEED = "avg_speed";
    public static final String KEY_CALORIES = "calories";
    public static final String KEY_CLIMB = "climb";
    public static final String KEY_COMMENT = "comment";
    public static final String KEY_DATE_TIME = "date_time";
    public static final String KEY_DISTANCE = "distance";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_HEARTRATE = "heartrate";
    public static final String KEY_INPUT_TYPE = "input_type";
    public static final String KEY_PRIVACY = "privacy";
    public static final String KEY_ROWID = "_id";
    public static final String TABLE_NAME_ENTRIES = "entry";
    private static final String TAG = "Storing in Database";
    private static ExerciseDbHelper sInstance;

    public static ExerciseDbHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ExerciseDbHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private ExerciseDbHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(ExerciseDbHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS entry");
        onCreate(db);
    }

    public long insertEntry(ExerciseEntry entry) {
        ContentValues values = new ContentValues();
        values.put("input_type", Integer.valueOf(entry.getmInputType()));
        values.put("activity_type", Integer.valueOf(entry.getmActivityType()));
        values.put(KEY_DATE_TIME, Long.valueOf(entry.getmDateTime()));
        values.put(KEY_DURATION, Double.valueOf(entry.getmDuration()));
        values.put(KEY_DISTANCE, Double.valueOf(entry.getmDistance()));
        values.put(KEY_AVG_SPEED, Double.valueOf(entry.getmAvgSpeed()));
        values.put(KEY_CALORIES, Integer.valueOf(entry.getmCalorie()));
        values.put(KEY_CLIMB, Double.valueOf(entry.getmClimb()));
        values.put(KEY_HEARTRATE, Integer.valueOf(entry.getmHeartRate()));
        values.put(KEY_COMMENT, entry.getmComment());
        SQLiteDatabase database = getWritableDatabase();
        long insertId = database.insert(TABLE_NAME_ENTRIES, null, values);
        database.close();
        return insertId;
    }

    public void removeEntry(long rowIndex) {
        SQLiteDatabase database = getWritableDatabase();
        database.delete(TABLE_NAME_ENTRIES, "_id = " + rowIndex, null);
        database.close();
    }

    public ExerciseEntry fetchEntryByIndex(long rowId) {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(TABLE_NAME_ENTRIES, null, "_id = " + rowId, null, null, null, null);
        cursor.moveToFirst();
        ExerciseEntry entry = cursorToExerciseEntry(cursor);
        Log.d(TAG, cursorToExerciseEntry(cursor).toString());
        cursor.close();
        database.close();
        return entry;
    }

    public ArrayList<ExerciseEntry> fetchEntries() {
        ArrayList<ExerciseEntry> entries = new ArrayList();
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(TABLE_NAME_ENTRIES, null, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ExerciseEntry entry = cursorToExerciseEntry(cursor);
            Log.d(TAG, cursorToExerciseEntry(cursor).toString());
            entries.add(entry);
            cursor.moveToNext();
        }
        cursor.close();
        database.close();
        return entries;
    }
    private ExerciseEntry cursorToExerciseEntry(Cursor cursor) {
        ExerciseEntry entry = new ExerciseEntry();
        entry.setmId(cursor.getLong(0));
        entry.setmInputType(cursor.getInt(1));
        entry.setmActivityType(cursor.getInt(2));
        entry.setmDateTime(cursor.getLong(3));
        entry.setmDuration((double) cursor.getInt(4));
        entry.setmDistance(cursor.getDouble(5));
        entry.setmAvgSpeed(cursor.getDouble(7));
        entry.setmCalorie(cursor.getInt(8));
        entry.setmClimb(cursor.getDouble(9));
        entry.setmHeartRate(cursor.getInt(10));
        entry.setmComment(cursor.getString(11));
        return entry;
    }
}
