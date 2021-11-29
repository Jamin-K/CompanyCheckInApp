package com.example.checkinandout;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class InsertData extends AsyncTask<String, Void, String> {
    ProgressDialog progressDialog;
    String errorString = null;
    private char state;
    public String str_ip = "192.168.0.59";//server IP입력
    public String str_TAG = "phpquery"; //php 태그

    @Override
    protected void onPreExecute() {
        super.onPreExecute();


        //progressDialog =ProgressDialog.show(MainActivity.this, "please wait", null, true, true);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        state = result.charAt(0);
        if(result.charAt(0) == '1') {
            System.out.println("데이터 입력 성공");
            //finish();

        }
        else
            System.out.println("데이터 입력 실패");
        Log.d(str_TAG, "POST response  - " + result);
    }

    @Override
    protected String doInBackground(String... params) {
        String str_search1 = params[0];
        String str_search2 = params[1];
        String str_search3 = params[2];
        String str_serverURL = "http://"+str_ip+"/workStateInsert.php";
        String str_postParameters = "empNum=" + str_search1 + "&intime=" +str_search2+ "&outtime=" +str_search2;


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

