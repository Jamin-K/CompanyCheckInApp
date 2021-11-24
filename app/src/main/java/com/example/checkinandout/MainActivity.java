/*
 * MainActiviry진입
 * 근태여부 확인가능
 * 로그인이 풀려있을 시 자동으로 체크하여 LoginActivity로 이동
 * 2021/11/07 : 신규생성
 * */

/*
* 데이터베이스 이름 : guentae
*
* 테이블 이름 : Employee
* empNum VARCHAR(PK), name VARCHAR, phone VARCHAR
*
* 테이블 이름 : EmployeeInOut
* empNum VARCHAR(PK), seq int(FK, 자동 증가), Intime varchar(20), Outtime varchar(20)
* * */

package com.example.checkinandout;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    Button btn_moveToAdmin;
    int int_adminCode = 0; //0 = false, 1 = true
    int int_loginSuccess = 0; // 0 = false, 1 = true, 로그인 여부 검사
    public String str_ip = "192.168.0.59";//server IP입력
    public String str_TAG = "phpquery"; //php 태그
    private char state;
    public String str_empName;
    public String str_empNum;
    public String str_phone;
    public String str_inEmpNum;
    public String str_inName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(int_loginSuccess == 0){
            showLoginDialog();
        }

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

    public void showLoginDialog(){
        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout loginLayout = (LinearLayout) vi.inflate(R.layout.logindialog, null);

        final EditText empNum = (EditText)loginLayout.findViewById(R.id.empNum);
        final EditText empName = (EditText)loginLayout.findViewById(R.id.empName);
        new AlertDialog.Builder(this).setTitle("Login").setView(loginLayout).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //테스트코드
                Toast.makeText(MainActivity.this, "사번 : "+empNum.getText().toString() + "이름 : "+empName.getText().toString(),Toast.LENGTH_SHORT).show();
                str_inEmpNum = empNum.getText().toString();
                str_inName = empName.getText().toString();
                GetData task = new GetData();
                // 로그인
                task.execute(str_inEmpNum, str_inName);
            }
        }).show();
    }




    public class GetData extends AsyncTask<String, Void, String>{

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() { //작업 시작 전 초기 호출
            super.onPreExecute();

            progressDialog =ProgressDialog.show(MainActivity.this, "please wait", null, true, true);

        }

        @Override
        protected void onPostExecute(String result) { //AsyncTask의 모든 작업이 완료된 후 최종 1회 호출, 자동로그인을 위함
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(str_TAG, "respose : " + result);
            if(result == null){
            }
            else{
                try{
                    JSONObject jsonObject = new JSONObject(result);

                    JSONArray jsonArray = jsonObject.getJSONArray("jaemin");


                    for(int i = 0 ; i<jsonArray.length() ; i++){
                        JSONObject subObject = jsonArray.getJSONObject(i);
                        str_empNum = subObject.getString("empNum");
                        str_empName = subObject.getString("name");
                        str_phone = subObject.getString("phone");

                    }
                    /*
                    * 자동로그인 구현 후 int_loginSuccess = 1 초기화
                    * */
                    System.out.println("----------------------------------------------------");
                    System.out.println(str_empNum);
                    System.out.println(str_empName);
                    System.out.println("----------------------------------------------------");

                    if(str_empNum == str_inEmpNum && str_empName == str_inName){

                        Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();
                        int_loginSuccess = 1;
                    }

                }
                catch (JSONException e){
                    Toast.makeText(getApplicationContext(), "정보 확인 요망", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

        }

        @Override
        protected String doInBackground(String... params) { //스레드에서 처리 될 내용 기술
            String str_search1 = params[0];
            String str_search2 = params[1];
            String str_serverURL = "http://"+str_ip+"/loginCheck.php";
            String str_postParameters = "empNum=" + str_search1 + "&name=" +str_search2;


            try {

                URL url = new URL(str_serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(str_postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(str_TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();


            } catch (Exception e) {

                Log.d(str_TAG, "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){ // 권한을 허용했을 경우
            int length  = permissions.length;
            for(int i = 0 ; i<length ; i++){
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    //권한 허용
                    Log.d("MainActivity", "권한허용 : " + permissions[i]);
                }
            }
        }
    }

    private void checkSelfPermission() {
        String temp = "";
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            temp = temp + Manifest.permission.ACCESS_FINE_LOCATION + " ";
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            temp = temp + Manifest.permission.ACCESS_COARSE_LOCATION + " ";
        }
        if(TextUtils.isEmpty(temp) == false){ // 권한 없을 시 요청
            ActivityCompat.requestPermissions(this, temp.trim().split(" "), 1);
        }
        else
            Toast.makeText(this, "권한 허용 완료", Toast.LENGTH_SHORT).show();
    }

}