/*
* MainActiviry진입
* 근태여부 확인가능
* 로그인이 풀려있을 시 자동으로 체크하여 LoginActivity로 이동
* 2021/11/07 : 신규생성
* */

package com.example.checkinandout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
    public String str_ip = "";//server IP입력
    public String str_TAG = "phpquery"; //php 태그

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

    public class getData extends AsyncTask<String, Void, String>{

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
                    String jaemin =jsonObject.getString("jaemin");
                    JSONArray jsonArray = new JSONArray(jaemin);
                    for(int i = 0 ; i<jsonArray.length() ; i++){
                        JSONObject subObject = jsonArray.getJSONObject(i);
                        /*
                         * DB table의 열 이름과 속성값을 값,쌍 형태로 매칭
                         * 속성 값이 담길 변수 = subObject.getString("열 이름");
                         * */

                        /*
                        * Intent를 통해 값을 activity간 공유
                        * */
                    }

                }
                catch (JSONException e){
                    Toast.makeText(getApplicationContext(), "DB확인 메세지", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

        }

        @Override
        protected String doInBackground(String... params) { //스레드에서 처리 될 내용 기술
            String str_search1 = params[0];
            String str_search2 = params[1];
            String str_serverURL = "http://"+str_ip+"/query.php";
            String str_postParameters = "userId=" + str_search1 + "&userPw" +str_search2;


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
}