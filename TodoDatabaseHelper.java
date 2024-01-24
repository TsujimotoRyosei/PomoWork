package es.exsample;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class TodoDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "todo_database";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_TODO = "todo";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_DEADLINE = "deadline";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_TODO + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CONTENT + " TEXT, " +
                    COLUMN_DEADLINE + " TEXT);";

    public TodoDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO);
        onCreate(db);
    }

    public void addTodoData(Tododata todoData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONTENT, todoData.getContent());
        values.put(COLUMN_DEADLINE, todoData.getDeadline());

        try {
            db.insert(TABLE_TODO, null, values);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public void updateTodoData(Tododata todoData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONTENT, todoData.getContent());
        values.put(COLUMN_DEADLINE, todoData.getDeadline());

        try {
            db.update(TABLE_TODO, values, COLUMN_ID + " = ?",
                    new String[]{String.valueOf(todoData.getId())});
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }


    public List<Tododata> getAllTodoData() {
        List<Tododata> todoList = new ArrayList<>();
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(TABLE_TODO, null, null, null, null, null, COLUMN_DEADLINE)) {

            if (cursor != null && cursor.moveToFirst()) {
                int idColumnIndex = cursor.getColumnIndex(COLUMN_ID);
                int contentColumnIndex = cursor.getColumnIndex(COLUMN_CONTENT);
                int deadlineColumnIndex = cursor.getColumnIndex(COLUMN_DEADLINE);

                do {
                    Tododata tododata = new Tododata();

                    // データにアクセスする前に列インデックスが有効かどうかを確認する
                    if (idColumnIndex >= 0) {
                        tododata.setId(cursor.getInt(idColumnIndex));
                    }

                    if (contentColumnIndex >= 0) {
                        tododata.setContent(cursor.getString(contentColumnIndex));
                    }

                    if (deadlineColumnIndex >= 0) {
                        tododata.setDeadline(cursor.getString(deadlineColumnIndex));
                    }

                    todoList.add(tododata);
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // 例外を丁寧に処理する。例えば、エラーをログに記録するか、ユーザーにメッセージを表示するなど。
        }

        return todoList;
    }

    public void deleteTodoData(Tododata todoData) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_TODO, COLUMN_ID + " = ?", new String[]{String.valueOf(todoData.getId())});
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

}
