package com.example.hpfromdoha.mmda_ticketing;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.StringTokenizer;

/**
 * Created by HP FROM DOHA on 12/2/2016.
 */

public class BackgroundWorker extends AsyncTask<String,Void,String> {

    Context context;
    AlertDialog alertDialog;
    boolean c = false;
    String user = "", result = "", type = "";

    BackgroundWorker(Context ctx) {

        context = ctx;

    }

    @Override
    protected String doInBackground(String... strings) {
        type = strings[0];
        String login = "http://mmdaenforcingsystem.hol.es/enforcer_app/app_login.php";
        String scan = "http://mmdaenforcingsystem.hol.es/enforcer_app/app_insert.php";

        if(type.equals("login")) {
            try {
                user = strings[1];
                String pass = strings[2];
                URL url = new URL(login);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data = URLEncoder.encode("idcode", "UTF-8") + "=" + URLEncoder.encode(user, "UTF-8")+"&"+URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(pass, "UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));

                String line = "";

                while((line=bufferedReader.readLine())!=null){
                    result+=line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }else if(type.equals("barcode")){
            try {

                String sendData = strings[3];
                StringTokenizer st = new StringTokenizer(sendData,";");
                String[] arr = new String[st.countTokens()];
                int ctr = 0;

                while (st.hasMoreTokens()) {
                    arr[ctr++] = st.nextToken();
                }

                String date = arr[0];
                String time = arr[1];
                String enforcer_id = arr[2];
                String enforcer_name = arr[3];
                String license_no = arr[4];
                String driver_name = arr[5];
                String car_plate = arr[6];
                String violation = arr[7];
                String price = arr[8];

                URL url = new URL(scan);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data = URLEncoder.encode("date", "UTF-8") + "=" + URLEncoder.encode(date, "UTF-8")+"&"+
                        URLEncoder.encode("time", "UTF-8") + "=" + URLEncoder.encode(time, "UTF-8")+"&"+
                        URLEncoder.encode("enforcer_id", "UTF-8") + "=" + URLEncoder.encode(enforcer_id, "UTF-8")+"&"+
                        URLEncoder.encode("enforcer_name", "UTF-8") + "=" + URLEncoder.encode(enforcer_name, "UTF-8")+"&"+
                        URLEncoder.encode("drivers_fullname", "UTF-8") + "=" + URLEncoder.encode(driver_name, "UTF-8")+"&"+
                        URLEncoder.encode("offense", "UTF-8") + "=" + URLEncoder.encode(violation, "UTF-8")+"&"+
                        URLEncoder.encode("license_no", "UTF-8") + "=" + URLEncoder.encode(license_no, "UTF-8")+"&"+
                        URLEncoder.encode("price", "UTF-8") + "=" + URLEncoder.encode(price, "UTF-8")+"&"+
                        URLEncoder.encode("car_plate", "UTF-8") + "=" + URLEncoder.encode(car_plate, "UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
                String echo = "";
                String line = "";

                while((line=bufferedReader.readLine())!=null){
                    echo+=line;
                }

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return echo;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }catch(NullPointerException e){
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        //super.onPreExecute();
        alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Login Status");
    }

    @Override
    protected void onPostExecute(String s) {
        //super.onPostExecute(s);

        if(type.equals("login")) {

            alertDialog.setMessage(s);
            alertDialog.show();

            if (alertDialog.isShowing() && !s.equals("Login Failed! Check your IDCODE and/or PASSWORD.")) {
                Thread thread = new Thread() {
                    public void run() {
                        try {
                            sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            alertDialog.dismiss();
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.putExtra("id", user);
                            String[] name = result.split("Welcome ", 2);
                            intent.putExtra("name", name[1]);
                            context.startActivity(intent);
                        }
                    }
                };
                thread.start();
            }
        }else if(type.equals("barcode")) {

            Toast.makeText(context,s, Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

}
