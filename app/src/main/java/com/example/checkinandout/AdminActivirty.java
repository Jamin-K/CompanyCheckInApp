/*
 * MainActivity로부터 call
 * 관리자 탭, 근태 데이터 확인 가능
 *
 * 2021/11/07 : 신규생성
 * */
package com.example.checkinandout;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class AdminActivirty extends AppCompatActivity {
    Button btn_query;
    EditText et_DateTo;
    EditText et_DateFrom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_activirty);

        btn_query = findViewById(R.id.btn_moveToAdmin);
        et_DateTo = findViewById(R.id.et_DateTo);
        et_DateFrom = findViewById(R.id.et_DateFrom);

    }
}