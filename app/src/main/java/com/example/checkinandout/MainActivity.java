/*
* MainActiviry진입
* 근태여부 확인가능
* 로그인이 풀려있을 시 자동으로 체크하여 LoginActivity로 이동
* 2021/11/07 : 신규생성
* */

package com.example.checkinandout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btn_moveToAdmin;
    int int_adminCode = 0; //0 = false, 1 = true
    int int_loginSuccess = 0; // 0 = false, 1 = true, 로그인 여부 검사

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        * 자동로그인 구현, SharedPreferences Interface 사용
        * 로그인 실패 시 LoginActivity 이동
        * */


        /*
        * 관리자 버튼 클릭 시 권한 검사 후 권리자 페이지 이동
        * */
        findViewById(R.id.btn_moveToAdmin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(int_adminCode == 1){
                    //move to admin activity
                    Intent intent = new Intent(getApplicationContext(), AdminActivirty.class);
                    startActivity(intent);
                }
                else{
                    //show dialog alert
                    AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                    dlg.setTitle("경고");
                    dlg.setMessage("권한이 없습니다");
                }

            }
        });
    }
}