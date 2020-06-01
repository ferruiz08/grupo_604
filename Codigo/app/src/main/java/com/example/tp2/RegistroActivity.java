package com.example.tp2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class RegistroActivity extends AppCompatActivity {

    private EditText textNombre;
    private EditText textApellido;
    private EditText textDNI;
    private EditText textEmail;
    private EditText textPassword;
    private EditText textComision;
    private EditText textGrupo;
    private EditText textResultado;
    private TextView textSinInternet;
    private ImageView imageSinInternet;

    private ImageButton cmdRegistrar;

    private static final String URI_REGISTER = "http://so-unlam.net.ar/api/api/register";
    private static final String URI_LOGING = "http://so-unlam.net.ar/api/api/login";
    private static final String URI_EVENT = "http://so-unlam.net.ar/api/api/event";

    public IntentFilter filtro;
    private ReceptorOperacion receiver = new ReceptorOperacion();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        textNombre = (EditText) findViewById(R.id.editTextNombre);
        textApellido = (EditText) findViewById(R.id.editTextApellido);
        textDNI = (EditText) findViewById(R.id.editTextDNI);
        textEmail = (EditText) findViewById(R.id.editTextEmail);
        textPassword = (EditText) findViewById(R.id.editTextPassword);
        textComision = (EditText) findViewById(R.id.editTextComision);
        textGrupo = (EditText) findViewById(R.id.editTextGrupo);
        textResultado = (EditText) findViewById(R.id.editTextResultado);
        textSinInternet = (TextView) findViewById(R.id.textViewSinInternet);
        imageSinInternet = (ImageView) findViewById(R.id.imageViewSinInternet);

        cmdRegistrar = (ImageButton) findViewById(R.id.imageButtonCmdRegistrar);
        cmdRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cmdRegistrar();
            }
        });
        configurarBroadcastReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(new networkStateReceiver(), filter);

    }

    private void cmdRegistrar(){
        textResultado.setText("");
        JSONObject obj = new JSONObject();
        try{
            obj.put("env","DEV");
            obj.put("name",textNombre.getText().toString());
            obj.put("lastname",textApellido.getText().toString());
            obj.put("dni",Integer.parseInt(textDNI.getText().toString()));
            obj.put("email",textEmail.getText().toString());
            obj.put("password",textPassword.getText().toString());
            obj.put("commission",Integer.parseInt(textComision.getText().toString()));
            obj.put("group",Integer.parseInt(textGrupo.getText().toString()));

            Intent i = new Intent( RegistroActivity.this,ServiceHTTP_Post.class);
            i.putExtra("uri",URI_REGISTER);
            i.putExtra("datosJson",obj.toString());
            startService(i);
        } catch (JSONException e) {
            e.printStackTrace();
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

    private void configurarBroadcastReceiver(){
        filtro = new IntentFilter("com.example.tp2.intent.action.RESPUESTA_OPERACION_REGISTRO");
        filtro.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver,filtro);
    }

    public class ReceptorOperacion extends BroadcastReceiver{

        public void onReceive(Context context, Intent intent){
            try{
                String datosJsonString = intent.getStringExtra("datosJson");
                JSONObject jSon = new JSONObject(datosJsonString);
                String resultado = jSon.getString("state");
                textResultado.setText(resultado);
                if (resultado.contains("success"))
                    textResultado.setTextColor(Color.GREEN);
                else
                    textResultado.setTextColor(Color.RED);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
