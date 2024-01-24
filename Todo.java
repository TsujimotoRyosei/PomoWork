package es.exsample;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import java.util.Calendar;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class Todo extends AppCompatActivity {
    Button todoButton;
    private TodoDatabaseHelper databaseHelper;
    private EditText editTextContent;
    private EditText editTextDeadline;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);
        databaseHelper = new TodoDatabaseHelper(this);
        todoButton = findViewById(R.id.bt_timer);
        editTextContent = findViewById(R.id.editTextContent);
        editTextDeadline = findViewById(R.id.editTextDeadline);
        // データベースからデータを取得
        List<Tododata> todoList = databaseHelper.getAllTodoData();
        // カスタムアダプターを作成
        TodoAdapter todoAdapter = new TodoAdapter(this, todoList);
        // レイアウト内のListViewを見つける
        ListView listViewTodo = findViewById(R.id.listViewTodo);
        // ListViewにアダプターを設定
        listViewTodo.setAdapter(todoAdapter);
        todoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tododata tododata = new Tododata();
                tododata.setContent("ToDo");
                tododata.setDeadline("締切日");
                finish();
            }
        });
        editTextDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
        Button addButton = findViewById(R.id.bt_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTodoData();
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
    public void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                Todo.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // 選択された日付を処理
                        String selectedDate = year + "年" + (monthOfYear + 1) + "月" + dayOfMonth + "日";
                        editTextDeadline.setText(selectedDate);
                    }
                },
                year, month, day
        );
        datePickerDialog.show();
    }
    public void saveTodoData() {
        String content = editTextContent.getText().toString();
        String deadline = editTextDeadline.getText().toString();
        // content と deadline が空でないことを確認
        if (!content.isEmpty() && !deadline.isEmpty()) {
            Tododata tododata = new Tododata();
            tododata.setContent(content);
            tododata.setDeadline(deadline);
            // 新しいTodoデータをデータベースに追加
            databaseHelper.addTodoData(tododata);
            // ListViewを新しいデータで更新
            updateListView();
            // 入力フィールドをクリア
            editTextContent.getText().clear();
            editTextDeadline.getText().clear();
            // アラートダイアログを作成して表示
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("保存完了");
            builder.setMessage("ToDo: " + content + "\n締切日: " + deadline);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // ボタンがクリックされたときの処理（今回は特に何もしない）
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            // 空の場合はエラーメッセージを表示
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("エラー");
            builder.setMessage("入力が完了していません");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // ボタンがクリックされたときの処理（今回は特に何もしない）
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }
    public void showEditDialog(Tododata todoData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("編集");
        View dialogView = getLayoutInflater().inflate(R.layout.edit_dialog, null);
        EditText editContent = dialogView.findViewById(R.id.editContent);
        EditText editDeadline = dialogView.findViewById(R.id.editDeadline);
        // 現在のデータをダイアログに表示
        editContent.setText(todoData.getContent());
        editDeadline.setText(todoData.getDeadline());
        // 締切日を選択するボタンにクリックリスナーを追加
        editDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(editDeadline);
            }
        });
        // 締切日のEditTextにフォーカスを変更できないようにする
        editDeadline.setFocusable(false);
        editDeadline.setFocusableInTouchMode(false);
        builder.setView(dialogView);
        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newContent = editContent.getText().toString();
                String newDeadline = editDeadline.getText().toString();
                // 入力が完了しているかを確認
                if (!newContent.isEmpty() && !newDeadline.isEmpty()) {
                    // データベースを更新
                    todoData.setContent(newContent);
                    todoData.setDeadline(newDeadline);
                    databaseHelper.updateTodoData(todoData);
                    // ListViewをリフレッシュ
                    updateListView();
                    // アラートダイアログで内容と締切日を表示
                    showAlertDialog("編集完了", "ToDo: " + newContent + "\n締切日: " + newDeadline);
                } else {
                    // 入力が完了していない場合はエラーメッセージを表示
                    showErrorMessage("入力が完了していません");
                }
            }
        });
        builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // キャンセルされた場合の処理
            }
        });
        builder.create().show();
    }
    // 締切日を選択するメソッド
    private void showDatePickerDialog(EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // 選択された日付を処理
                        String selectedDate = year + "年" + (monthOfYear + 1) + "月" + dayOfMonth + "日";
                        editText.setText(selectedDate);
                    }
                },
                year, month, day
        );
        datePickerDialog.show();
    }
    public void deleteTodoData(Tododata todoData) {
        // データベースからアイテムを削除する処理を追加
        databaseHelper.deleteTodoData(todoData);
        // ListViewをリフレッシュ
        updateListView();
    }
    private void updateListView() {
        // データベースから更新されたデータでListViewをリフレッシュ
        List<Tododata> updatedTodoList = databaseHelper.getAllTodoData();
        // リストを締切日でソート
        Collections.sort(updatedTodoList, new Comparator<Tododata>() {
            @Override
            public int compare(Tododata o1, Tododata o2) {
                // 締切日が文字列として保存されている場合の比較
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
                try {
                    Date date1 = dateFormat.parse(o1.getDeadline());
                    Date date2 = dateFormat.parse(o2.getDeadline());
                    return date1.compareTo(date2);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });
        TodoAdapter updatedAdapter = new TodoAdapter(this, updatedTodoList);
        ListView listViewTodo = findViewById(R.id.listViewTodo);
        listViewTodo.setAdapter(updatedAdapter);
    }
    private void showErrorMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("エラー");
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ボタンがクリックされたときの処理
            }
        });
        builder.create().show();
    }
    private void showAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ボタンがクリックされたときの処理（今回は特に何もしない）
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
