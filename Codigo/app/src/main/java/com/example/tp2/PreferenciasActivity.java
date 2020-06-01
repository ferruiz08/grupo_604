package com.example.tp2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Map;

public class PreferenciasActivity extends AppCompatActivity {

    private ListView listViewPreferencias;
    private ArrayList<String> listaEventos;

    private Button cmdBorrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferencias);

        listViewPreferencias = (ListView) findViewById(R.id.listViewPreferencias);
        listaEventos = new ArrayList<String>();

        cmdBorrar = (Button) findViewById(R.id.buttonCmdBorrar);
        cmdBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                borrarPreferencias();
            }
        });

        leerPreferencias();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,listaEventos);
        listViewPreferencias.setAdapter(adapter);
    }

    private void leerPreferencias(){
        SharedPreferences preferencias = getSharedPreferences("eventos", Context.MODE_PRIVATE);
        Map<String,?> mapEventos = preferencias.getAll();
        for (Map.Entry<String, ?> entry : mapEventos.entrySet()) {
            if (!entry.getKey().contains("indice"))
                listaEventos.add(entry.getKey() + " " + entry.getValue().toString());
        }

    }

    private void borrarPreferencias(){
        SharedPreferences preferencias = getSharedPreferences("eventos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.clear();
        editor.commit();
        Global.setIndicePreferencias(0);

    }
}
