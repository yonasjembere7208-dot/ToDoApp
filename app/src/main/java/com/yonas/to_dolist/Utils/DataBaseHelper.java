package com.yonas.to_dolist.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yonas.to_dolist.Model.ToDoModel;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TODO_DATABASE";
    private static final String TABLE_NAME = "TODO_TABLE";
    private static final String COL_1 = "ID";
    private static final String COL_2 = "TASK";
    private static final String COL_3 = "STATUS";
    private static final String COL_4 = "DUE_DATE";
    private static final String COL_5 = "PRIORITY";
    private static final String COL_6 = "CATEGORY";
    private static final String COL_7 = "TASK_TIME";

    private DatabaseReference dbReference;

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 3); // Version increased to 3 for TIME column
        dbReference = FirebaseDatabase.getInstance("https://my-to-do-list-8d39a-default-rtdb.firebaseio.com/").getReference("tasks");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_2 + " TEXT, " +
                COL_3 + " INTEGER, " +
                COL_4 + " TEXT, " +
                COL_5 + " INTEGER, " +
                COL_6 + " TEXT, " +
                COL_7 + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            sqLiteDatabase.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_4 + " TEXT");
            sqLiteDatabase.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_5 + " INTEGER DEFAULT 1");
            sqLiteDatabase.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_6 + " TEXT");
        }
        if (oldVersion < 3) {
            sqLiteDatabase.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_7 + " TEXT");
        }
    }

    public void insertTask(ToDoModel model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, model.getTask());
        contentValues.put(COL_3, 0);
        contentValues.put(COL_4, model.getDueDate());
        contentValues.put(COL_5, model.getPriority());
        contentValues.put(COL_6, model.getCategory());
        contentValues.put(COL_7, model.getTaskTime());

        long id = db.insert(TABLE_NAME, null, contentValues);
        
        model.setId((int) id);
        dbReference.child(String.valueOf(id)).setValue(model);
    }

    public void updateTask(int id, String task, String dueDate, String time, int priority, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, task);
        contentValues.put(COL_4, dueDate);
        contentValues.put(COL_7, time);
        contentValues.put(COL_5, priority);
        contentValues.put(COL_6, category);

        db.update(TABLE_NAME, contentValues, "ID=?", new String[]{String.valueOf(id)});
        
        dbReference.child(String.valueOf(id)).child("task").setValue(task);
        dbReference.child(String.valueOf(id)).child("dueDate").setValue(dueDate);
        dbReference.child(String.valueOf(id)).child("taskTime").setValue(time);
        dbReference.child(String.valueOf(id)).child("priority").setValue(priority);
        dbReference.child(String.valueOf(id)).child("category").setValue(category);
    }

    public void updateStatus(int id, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_3, status);
        db.update(TABLE_NAME, contentValues, "ID=?", new String[]{String.valueOf(id)});
        dbReference.child(String.valueOf(id)).child("status").setValue(status);
    }

    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "ID=?", new String[]{String.valueOf(id)});
        dbReference.child(String.valueOf(id)).removeValue();
    }

    public void deleteAllTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        dbReference.removeValue();
    }

    public void deleteCompletedTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Get IDs of completed tasks first to update Firebase
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_1}, COL_3 + "=?", new String[]{"1"}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                dbReference.child(String.valueOf(id)).removeValue();
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.delete(TABLE_NAME, COL_3 + "=?", new String[]{"1"});
    }

    public int getTasksCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int getCompletedTasksCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + COL_3 + "=1", null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public List<ToDoModel> getAllTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        List<ToDoModel> modelList = new ArrayList<>();

        db.beginTransaction();
        try {
            cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ToDoModel toDoModel = new ToDoModel();
                    toDoModel.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_1)));
                    toDoModel.setTask(cursor.getString(cursor.getColumnIndexOrThrow(COL_2)));
                    toDoModel.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(COL_3)));
                    toDoModel.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_4)));
                    toDoModel.setPriority(cursor.getInt(cursor.getColumnIndexOrThrow(COL_5)));
                    toDoModel.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_6)));
                    toDoModel.setTaskTime(cursor.getString(cursor.getColumnIndexOrThrow(COL_7)));
                    modelList.add(toDoModel);
                } while (cursor.moveToNext());
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            if (cursor != null) cursor.close();
        }
        return modelList;
    }
}
