package com.ds.app.pricereading.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ds.app.pricereading.R;

import java.util.GregorianCalendar;

public class CalendarActivity extends AppCompatActivity {

    public static final int RESULT_CODE_MAIN_OK = 1;
    public static final int RESULT_CODE_MAIN_CANCELLED = 2;

    public static final String EXTRA_KEY_MAIN_OUTPUT_DATE = "date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_layout);

        calendarView = findViewById(R.id.calendar_calendar_view);
        clearButton = findViewById(R.id.calendar_clear_button);
        pickButton = findViewById(R.id.calendar_pick_button);

        calendarView
                .setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(
                            @NonNull CalendarView view,
                            int year,
                            int month,
                            int dayOfMonth
                    ) {
                        calendar.set(GregorianCalendar.DATE, dayOfMonth);
                        calendar.set(GregorianCalendar.MONTH, month);
                        calendar.set(GregorianCalendar.YEAR, year);
                    }
                });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CODE_MAIN_CANCELLED);
                finish();
            }
        });

        pickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_DATE, calendar.getTime().getTime());
                setResult(RESULT_CODE_MAIN_OK, intent);
                finish();
            }
        });

        calendar = new GregorianCalendar();

    }

    private CalendarView calendarView;
    private ImageButton clearButton;
    private ImageButton pickButton;

    private GregorianCalendar calendar;

}