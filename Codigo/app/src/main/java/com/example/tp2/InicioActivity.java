package com.example.tp2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class InicioActivity extends AppCompatActivity {

    private Button registro;
    private Button login;
    private Button sensores;
    private Button eventos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        //Define las acciones para los botones
        registro = (Button) findViewById(R.id.buttonRegistro);
        registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirRegistroActivity();
            }
        });

        login = (Button) findViewById(R.id.buttonLogin);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirLoginActivity();
            }
        });

        sensores = (Button) findViewById(R.id.buttonSensores);
        sensores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirSensoresActivity();
            }
        });

        eventos = (Button) findViewById(R.id.buttonEventos);
        eventos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirEventosActivity();
            }
        });

        //Se inicia el servicio que va a publicar eventos
        Intent intent = new Intent(this,EventosService.class);
        startService(intent);
    }

    //Abre el activity para registrar usuario
    private void abrirRegistroActivity(){
        Intent intent = new Intent(this,RegistroActivity.class);
        startActivity(intent);
    }

    //Abre el activity para logear usuarios
    private void abrirLoginActivity(){
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }

    //Abre el activity para ver el estado de los sensores
    private void abrirSensoresActivity(){
        Intent intent = new Intent(this,SensoresActivity.class);
        startActivity(intent);

    }

    //Abre el activity para ver el listado de eventos
    private void abrirEventosActivity(){
        Intent intent = new Intent(this,PreferenciasActivity.class);
        startActivity(intent);

    }

}
