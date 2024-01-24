package es.exsample;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Vibrator;
import android.content.Intent;

public class PomoTimer extends AppCompatActivity {
    Spinner[] spinners = new Spinner[3];
    TextView[] selectedValues = new TextView[3];
    Button actionButton;
    Button resetButton;
    Button todoButton;
    CountDownTimer countDownTimer;
    boolean isWorkPhase = true;
    int breakCount = 0;  //休憩回数
    int workCount = 0; //作業回数
    int selectedBreakCount = 0; //スピナーで選択した休憩回数
    Vibrator vibrator;
    Spinner workTimeSpinner; //作業時間のスピナー
    Spinner breakTimeSpinner; //休憩時間のスピナー
    Spinner breakCountSpinner; //休憩回数のスピナー
    TextView tvTime;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        String[] w_time = {"10秒","1分","10分", "20分", "30分", "40分", "50分", "60分"};
        String[] b_time = {"10秒","1分","5分", "10分", "15分", "20分", "25分", "30分"};
        String[] count = {"1回", "2回", "3回", "4回", "5回"};
        workTimeSpinner = findViewById(R.id.workTimeSpinner);
        breakTimeSpinner = findViewById(R.id.breakTimeSpinner);
        breakCountSpinner = findViewById(R.id.breakCountSpinner);
        selectedValues[0] = findViewById(R.id.worktime);
        selectedValues[1] = findViewById(R.id.breaktime);
        selectedValues[2] = findViewById(R.id.breakcount);
        spinners[0] = workTimeSpinner;
        spinners[1] = breakTimeSpinner;
        spinners[2] = breakCountSpinner;
        tvTime = findViewById(R.id.tv_time);
        // Spinnerの選択値が変更されたときのリスナーを設定
        for (int i = 0; i < spinners.length; i++) {
            spinners[i].setOnItemSelectedListener(new ExSampleItemSelectedListener(i));
        }
        ArrayAdapter<String> workTimeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, w_time);
        workTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workTimeSpinner.setAdapter(workTimeAdapter);
        ArrayAdapter<String> breakTimeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, b_time);
        breakTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        breakTimeSpinner.setAdapter(breakTimeAdapter);
        ArrayAdapter<String> breakCountAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, count);
        breakCountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        breakCountSpinner.setAdapter(breakCountAdapter);
        actionButton = findViewById(R.id.button16);
        resetButton = findViewById(R.id.button17);
        todoButton = findViewById(R.id.bt_todo);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isWorkPhase) {
                    startWork();
                } else {
                    startBreak();
                }
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetConfirmationDialog();
            }
        });
        todoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(),Todo.class);
                startActivity(intent);
            }
        });
    }
    private void resetApp(boolean showResetMessage) {
        // 休憩回数と作業回数を初期化
        breakCount = 0;
        workCount = 0;
        // 休憩回数を表示
        updateBreakCount();
        // カウントダウンタイマーが動作中であればキャンセル
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        // ボタンの状態や表示を初期化
        isWorkPhase = true;
        actionButton.setText("開始");
        actionButton.setEnabled(true);
        // スピナーの選択値や表示も初期化
        for (int i = 0; i < spinners.length; i++) {
            int selectedPosition = spinners[i].getSelectedItemPosition();
            spinners[i].setSelection(selectedPosition);
            selectedValues[i].setText(((ArrayAdapter<String>) spinners[i].getAdapter()).getItem(selectedPosition)); // 選択された値を表示
        }
        // 休憩回数のスピナーをデフォルトの値 (1回) にリセット
        spinners[2].setSelection(0);
        selectedValues[2].setText(((ArrayAdapter<String>) spinners[2].getAdapter()).getItem(0));
        // 休憩時間のスピナーもデフォルトの値にリセット
        breakTimeSpinner.setSelection(0);
        selectedValues[1].setText(((ArrayAdapter<String>) breakTimeSpinner.getAdapter()).getItem(0));
        // 作業時間のスピナーもデフォルトの値にリセット
        workTimeSpinner.setSelection(0);
        selectedValues[0].setText(((ArrayAdapter<String>) workTimeSpinner.getAdapter()).getItem(0));
        // タイマーで表示していた残り時間も初期化
        selectedValues[0].setText(spinners[0].getSelectedItem().toString());
        selectedValues[1].setText(spinners[1].getSelectedItem().toString());
        // tv_timeも初期化
        tvTime.setText("");
        if (showResetMessage) {
            new AlertDialog.Builder(PomoTimer.this).setTitle("リセットされました").setPositiveButton("OK",null).show();
        }
    }
    private void updateBreakCount() {
        selectedValues[2].setText("休憩回数: " + breakCount + "回");
    }
    class ExSampleItemSelectedListener implements AdapterView.OnItemSelectedListener {
        int index;
        public ExSampleItemSelectedListener(int index) {
            this.index = index;
        }
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String selectedValue = (String) parent.getSelectedItem();
            selectedValues[index].setText(selectedValue);
            // 3番目のスピナーが選択された場合にselectedBreakCountを更新
            if (index == 2) {
                selectedBreakCount = Integer.parseInt(selectedValue.replaceAll("[\\D]", ""));
            }
        }
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
    private void startWork() {
        int workTime= getSelectedTime(0);
        if (workTime > 0) {
            startCountdownTimer(workTime * 1000, "作業終了", "休憩");
        } else {
            Toast.makeText(this, "作業時間を選択してください", Toast.LENGTH_SHORT).show();
        }
    }
    private void startBreak() {
        int breakTime = getSelectedTime(1);
        if (breakTime > 0) {
            startCountdownTimer(breakTime * 1000, "休憩終了", "作業再開");
        } else {
            Toast.makeText(this, "休憩時間を選択してください", Toast.LENGTH_SHORT).show();
        }
    }
    private void startCountdownTimer(long millisInFuture, final String completionMessage, final String buttonText) {
        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                // 残り時間の表示
                if (isWorkPhase) {
                    selectedValues[0].setText(String.format("残り時間：%02d:%02d", seconds / 60, seconds % 60));
                } else {
                    selectedValues[1].setText(String.format("残り時間：%02d:%02d", seconds / 60, seconds % 60));
                }
                tvTime.setText(String.format("残り時間：%02d:%02d", seconds / 60, seconds % 60));
            }
            @Override
            public void onFinish() {
                if (isWorkPhase) {
                    selectedValues[0].setText(completionMessage);
                    vibrate();  // 作業終了後にバイブレーション
                    workCount++;
                    if (workCount > selectedBreakCount) {
                        showCycleCompletionMessage(); // サイクルが終了したことを表示
                        return;
                    }
                    new AlertDialog.Builder(PomoTimer.this).setTitle("作業終了です").setPositiveButton("OK",null).show();
                } else {
                    selectedValues[1].setText(completionMessage);
                    vibrate();  // 休憩終了後にバイブレーション
                    new AlertDialog.Builder(PomoTimer.this).setTitle("休憩終了です").setPositiveButton("OK",null).show();
                    breakCount++;  // 休憩回数を増やす
                    updateBreakCount();  // 休憩回数を表示更新
                }
                isWorkPhase = !isWorkPhase;
                actionButton.setText(buttonText);
                actionButton.setEnabled(true);
            }
        };
        countDownTimer.start();
        actionButton.setEnabled(false);
    }
    private void vibrate() {
        long[] pattern = {0, 1000, 1000}; // 1秒バイブレート、1秒待機、1秒バイブレートのパターン
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(pattern, -1); // -1 はパターンを繰り返さないことを意味します
        }
    }
    private void showCycleCompletionMessage() {
        // 合計時間、作業時間の合計、休憩時間の合計を計算
        int totalWorkTimeInSeconds = workCount * getSelectedTime(0);
        int totalBreakTimeInSeconds = breakCount * getSelectedTime(1);
        int totalTimeInSeconds = totalWorkTimeInSeconds + totalBreakTimeInSeconds;
        // 合計時間を時、分、秒に分解
        int[] totalComponents = convertToHoursMinutesSeconds(totalTimeInSeconds);
        int[] workComponents = convertToHoursMinutesSeconds(totalWorkTimeInSeconds);
        int[] breakComponents = convertToHoursMinutesSeconds(totalBreakTimeInSeconds);
        // 合計時間を整形した文字列に変換
        String totalTimeString = formatTime(totalComponents);
        String totalWorkTimeString = formatTime(workComponents);
        String totalBreakTimeString = formatTime(breakComponents);
        new AlertDialog.Builder(PomoTimer.this)
                .setTitle("サイクルが終了しました")
                .setMessage("お疲れ様でした\n\n" +
                        "合計時間: " + totalTimeString + "\n" +
                        "作業時間: " + totalWorkTimeString + "\n" +
                        "休憩時間: " + totalBreakTimeString)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // サイクルが終了したときだけ resetApp メソッドを呼ぶ
                        resetApp(false);
                    }
                })
                .show();
    }
    private int[] convertToHoursMinutesSeconds(int totalTimeInSeconds) {
        int hours = totalTimeInSeconds / 3600;
        int minutes = (totalTimeInSeconds % 3600) / 60;
        int seconds = totalTimeInSeconds % 60;
        return new int[]{hours, minutes, seconds};
    }
    private String formatTime(int[] timeComponents) {
        return String.format("%02d時間 %02d分 %02d秒", timeComponents[0], timeComponents[1], timeComponents[2]);
    }
    private int getSelectedTime(int spinnerIndex) {
        String selectedValue = (String) spinners[spinnerIndex].getSelectedItem();
        if (selectedValue.contains("秒")) {
            return Integer.parseInt(selectedValue.replace("秒", ""));
        } else {
            return Integer.parseInt(selectedValue.replace("分", "")) * 60;
        }
    }
    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(PomoTimer.this)
                .setTitle("リセットしますか？")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetApp(true);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}