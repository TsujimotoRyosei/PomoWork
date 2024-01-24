package es.exsample;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import java.util.List;
import android.content.DialogInterface;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;
import java.util.Calendar;


public class TodoAdapter extends ArrayAdapter<Tododata> {

    public TodoAdapter(Context context, List<Tododata> todoList) {
        super(context, 0, todoList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Tododata todoData = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        TextView textViewContent = convertView.findViewById(R.id.textViewContent);
        TextView textViewDeadline = convertView.findViewById(R.id.textViewDeadline);

        textViewContent.setText("ToDo: " + todoData.getContent());
        textViewDeadline.setText("締切日: " + todoData.getDeadline());

        // 締切日が過ぎている場合は赤文字に設定
        if (isPastDeadline(todoData.getDeadline()) && !isToday(todoData.getDeadline())) {
            textViewContent.setTextColor(Color.RED);
            textViewDeadline.setTextColor(Color.RED);
        } else {
            // 締切日が過ぎていない場合は通常の色に設定（例えば、黒色）
            textViewContent.setTextColor(Color.BLACK);
            textViewDeadline.setTextColor(Color.BLACK);
        }

        // アイテムをクリックした際の処理を追加
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsDialog(todoData);
            }
        });

        return convertView;
    }

    // 締切日が過ぎているかどうかを判定するメソッド
    private boolean isPastDeadline(String deadline) {
        // 締切日が過ぎているかどうかの判定ロジックを実装
        // ここでは簡易的に現在の日付と比較して過去の場合は true を返すと仮定
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        try {
            Date currentDate = new Date();
            Date deadlineDate = sdf.parse(deadline);
            return currentDate.after(deadlineDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false; // エラーが発生した場合は過去として扱わない
        }
    }

    // 今日の日付かどうかを判定するメソッド
    private boolean isToday(String deadline) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        try {
            Date currentDate = new Date();
            Date deadlineDate = sdf.parse(deadline);
            return isSameDay(currentDate, deadlineDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false; // エラーが発生した場合は当日として扱わない
        }
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    // アイテムをクリックした際の選択肢を表示するメソッド
    private void showOptionsDialog(Tododata todoData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("選択してください");

        // 編集ボタン
        builder.setPositiveButton("編集", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 編集の処理をここに追加
                ((Todo) getContext()).showEditDialog(todoData);
            }
        });

        // 削除ボタン
        builder.setNegativeButton("削除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 削除の処理をここに追加
                ((Todo) getContext()).deleteTodoData(todoData);
            }
        });

        // キャンセルボタン
        builder.setNeutralButton("キャンセル", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }
}
