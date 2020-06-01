package com.example.tp2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText textEmail;
    private EditText textPassword;
    private EditText textResultado;
    private TextView textSinInternet;
    private ImageView imageSinInternet;

    private Button cmdLogin;

    private static final String URI_REGISTER = "http://so-unlam.net.ar/api/api/register";
    private static final String URI_LOGING = "http://so-unlam.net.ar/api/api/login";
    private static final String URI_EVENT = "http://so-unlam.net.ar/api/api/event";

    public IntentFilter filtro;
    private ReceptorOperacion receiver = new ReceptorOperacion();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textEmail = (EditText) findViewById(R.id.editTextEmailLogin);
        textPassword = (EditText) findViewById(R.id.editTextPasswordLogin);
        textResultado = (EditText) findViewById(R.id.editTextResultadoLogin);
        textSinInternet = (TextView) findViewById(R.id.textViewSinInternet);
        imageSinInternet = (ImageView) findViewById(R.id.imageViewSinInternet);

        cmdLogin = (Button) findViewById(R.id.buttonCmdLogin);
        cmdLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cmdLogin();
            }
        });
        configurarBroadcastReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(new networkStateReceiver(), filter);
    }

    private void cmdLogin(){
        textResultado.setText("");
        JSONObject obj = new JSONObject();
        try{
            obj.put("env","DEV");
            obj.put("name","");
            obj.put("lastname"," ");
            obj.put("dni"," ");
            obj.put("email",textEmail.getText().toString());
            obj.put("password",textPassword.getText().toString());
            obj.put("commission",0);
            obj.put("group",0);

            //Ejecuta el intent service para enviar los datos de login
            Intent i = new Intent( this,ServiceHTTP_Post.class);
            i.putExtra("uri",URI_LOGING);
            i.putExtra("datosJson",obj.toString());
            startService(i);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void configurarBroadcastReceiver(){

        //Configuara el receiver para recibir el resultado del login
        filtro = new IntentFilter("com.example.tp2.intent.action.RESPUESTA_OPERACION_LOGIN");
        filtro.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver,filtro);
    }

    public class ReceptorOperacion extends BroadcastReceiver {

        //Recibe el resultado del login
        public void onReceive(Context context, Intent intent){
            try{
                String datosJsonString = intent.getStringExtra("datosJson");
                JSONObject jSon = new JSONObject(datosJsonString);
                String resultado = jSon.getString("state");
                textResultado.setText(resultado);
                if (resultado.contains("success"))
                    textResultado.setTextColor(Color.GREEN);
                //Log.i("json",jSon.toString());
                Global.setToken(jSon.getString("token"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class networkStateReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = manager.getActiveNetworkInfo();
            onNetworkChange(ni,context);

        }

        private void onNetworkChange(NetworkInfo networkInfo, Context context) {
            if (networkInfo != null && networkInfo.isConnected() ) {
                textSinInternet.setVisibility(View.INVISIBLE);
                imageSinInternet.setVisibility(View.INVISIBLE);
            }else{
                textSinInternet.setVisibility(View.VISIBLE);
                imageSinInternet.setVisibility(View.VISIBLE);
            }

        }
    };

}
