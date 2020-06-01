package com.example.tp2;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class ServiceHTTP_Post extends IntentService {

    public ServiceHTTP_Post() {
        super("ServiceHTTP_Post");
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.i("Service","Creado");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("Service","Iniciado");
        try{
            String uri = intent.getExtras().getString("uri");
            JSONObject datosJson = new JSONObject(intent.getExtras().getString("datosJson"));
            ejecutarPost(uri,datosJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    protected void ejecutarPost(String uri,JSONObject datosJson){

        String resultado = Post(uri,datosJson);

        if (resultado == null){
            Log.e("Service","Error");
            return;
        }
        if (resultado == "NO_OK"){
            Log.e("Service", "Recibi NO_OK");
            return;
        }

        Intent intent = new Intent("com.example.tp2.intent.action.RESPUESTA_OPERACION_REGISTRO");;
        if(uri.contains("register"))
            intent = new Intent("com.example.tp2.intent.action.RESPUESTA_OPERACION_REGISTRO");
        if(uri.contains("login"))
            intent = new Intent("com.example.tp2.intent.action.RESPUESTA_OPERACION_LOGIN");
        if(uri.contains("event"))
            intent = new Intent("com.example.tp2.intent.action.RESPUESTA_OPERACION_EVENTO");
        intent.putExtra("datosJson",resultado);
        sendBroadcast(intent);
    }

    private String Post(String uri,JSONObject datosJson){

        HttpURLConnection urlConnection = null;
        String resultado = "";
        try{
           URL mUrl = new URL(uri);
           urlConnection = (HttpURLConnection)mUrl.openConnection();
           if (uri.contains("event"))
               urlConnection.setRequestProperty("token", Global.getToken());
           urlConnection.setRequestProperty("Content-Type","application/json; charset=UTF-8");
           urlConnection.setDoOutput(true);
           urlConnection.setDoInput(true);
           urlConnection.setConnectTimeout(5000);
           urlConnection.setRequestMethod("POST");

            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            Log.i("Service",datosJson.toString());
            wr.write(datosJson.toString().getBytes("UTF-8"));
            wr.flush();
            wr.close();

            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();
            Log.i("Service", String.valueOf(responseCode));
            if(responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED){
                resultado = convertInputStreamToString(urlConnection.getInputStream());
                Log.i("Service",resultado);
            }
            else
                resultado = "NO_OK";
            urlConnection.disconnect();
            return resultado;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String convertInputStreamToString(InputStream inputStream){
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();

    }

}
