package com.example.tp2;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class EventosService extends Service implements SensorEventListener {

    Queue<Evento> colaEventos;
    final String URI_EVENT = "http://so-unlam.net.ar/api/api/event";
    private IntentFilter filtro;
    private ReceptorOperacion receiver = new ReceptorOperacion();
    private boolean hayInternet;
    private int contadorPasos = 0;
    private int rotacionAnterior = 1;

    private SensorManager mSensorManager;



    public void onCreate(){



        configurarBroadcastReceiver();
        Global.setIndicePreferencias(leerIndicePreferencias());

        // Accedemos al servicio de sensores
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Ini_Sensores();

        //Registro los eventos que quiero escuchar
        registerReceiver(miBroadcast,new IntentFilter(Intent.ACTION_BATTERY_LOW));
        registerReceiver(miBroadcast,new IntentFilter(Intent.ACTION_BATTERY_OKAY));
        registerReceiver(miBroadcast,new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        registerReceiver(miBroadcast,new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(new networkStateReceiver(), filter);
        //Crea el hilo secundario
        ServiceThread thread = new ServiceThread();
        thread.start();

    }


    BroadcastReceiver miBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(Intent.ACTION_BATTERY_LOW)){
                agregarEvento("Broadcast",true,"Bateria Baja");
            }

            if(intent.getAction().equals(Intent.ACTION_BATTERY_OKAY)){
                agregarEvento("Broadcast",true,"Bateria Ok");
            }

            if(intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)){
                agregarEvento("Broadcast",intent.getBooleanExtra("state",false),"Auricular Conectado");
            }

            if(intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)){
                agregarEvento("Broadcast",intent.getBooleanExtra("state",false),"Modo avión");
            }

        }
    };

    // Metodo para iniciar el acceso a los sensores
    protected void Ini_Sensores()
    {

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),       SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),       SensorManager.SENSOR_DELAY_NORMAL);
    }

    // Metodo para parar la escucha de los sensores
    private void Parar_Sensores()
    {

        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        synchronized (this){
            switch(event.sensor.getType()){



                case Sensor.TYPE_ROTATION_VECTOR :
                    //Log.i("rotacion", event.sensor.getName());
                    // Creo objeto para saber como esta la pantalla
                    Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                    int rotation = display.getRotation();
                    if( rotation == 0 && rotacionAnterior != 0)
                    {
                        rotacionAnterior = 0;
                        agregarEvento("Sensores",true,"Posición Vertical");
                    }
                    else if( rotation == 1 && rotacionAnterior != 1)
                    {
                        rotacionAnterior = 1;
                        agregarEvento("Sensores",true,"Posición Horizontal");
                    }
                    else if (rotation == 3 && rotacionAnterior != 3)
                    {
                        rotacionAnterior = 3;
                        agregarEvento("Sensores",true,"Posición Horizontal");
                    }
                    break;

                case Sensor.TYPE_PROXIMITY :


                    // Si detecta 0 lo represento
                    if( event.values[0] == 0 )
                    {
                        agregarEvento("Sensores",true,"Pantalla Tapada");
                    }
                    break;

                case Sensor.TYPE_STEP_DETECTOR :
                    contadorPasos++;
                    if(contadorPasos >=20){
                        contadorPasos = 0;
                        agregarEvento("Sensores",true,"Cantidad de Pasos Alcanzada");
                    }

                    break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class networkStateReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = manager.getActiveNetworkInfo();
            onNetworkChange(ni,context);

        }

        private void onNetworkChange(NetworkInfo networkInfo, Context context) {
            if (networkInfo != null && networkInfo.isConnected() ) {
                hayInternet = true;
            }else{
                hayInternet = false;
            }

        }
    };




    public class ServiceThread extends Thread {

        public void run(){
            String token = "";
            while(true){

                //Si hay un evento en la cola lo publico
                if (!colaEventos.isEmpty() && !Global.getToken().equals("") && hayInternet)
                    publicarEvento(colaEventos.remove());


                //Chequeo nuevos eventos para agregar a la cola

                //Chequeo se hubo un login porque el token tiene un valor
                token = eventoLogin(token);



            }

        }
    }

    private String eventoLogin(String t){
        String token = Global.getToken();
        if (token.compareTo(t) != 0){
            agregarEvento("Login",true,"Login usuario");
        }
        return token;
    }


    private void agregarEvento(String tipo, Boolean estado, String descripcion){
        colaEventos.add(new Evento(tipo,estado,descripcion));
    }

    private void publicarEvento(Evento evento){

       // Log.i("Evento",evento.descripcion);
        JSONObject obj = new JSONObject();
        try{
            obj.put("env","DEV");
            obj.put("type_events",evento.tipo);
            if (evento.estado)
                obj.put("state","Activo");
            else
                obj.put("state","Inactivo");
            obj.put("description",evento.descripcion);

            //Guardo el evento en la preferencia
            guardarPreferencias(evento);

            //Ejecuta el intent service para enviar los datos de login
            Intent i = new Intent( this,ServiceHTTP_Post.class);
            i.putExtra("uri",URI_EVENT);
            i.putExtra("datosJson",obj.toString());
            startService(i);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void guardarPreferencias(Evento evento){

        SharedPreferences preferencias = getSharedPreferences("eventos",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        String estado = "";
        if (evento.estado)
           estado = "Activo";
        else
            estado = "Inactivo";
       Global.incIndicePreferencias();
        editor.putInt("indice",Global.getIndicePreferencias());
        editor.putString("evento " + Global.getIndicePreferencias()," : " + evento.tipo + " : " + estado + " : " + evento.descripcion);
        editor.commit();
       // Log.i("eventos",Integer.toString(indicePreferencias));
    }

    private int leerIndicePreferencias(){
        int indice = 0;
        SharedPreferences preferencias = getSharedPreferences("eventos",Context.MODE_PRIVATE);
        return preferencias.getInt("indice",indice);

    }

    private void configurarBroadcastReceiver(){
        //Configuara el receiver para recibir el resultado del login
        filtro = new IntentFilter("com.example.tp2.intent.action.RESPUESTA_OPERACION_EVENTO");
        filtro.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver,filtro);
        colaEventos = new LinkedList<Evento>();

    }

    public class ReceptorOperacion extends BroadcastReceiver {

        //Recibe el resultado del login
        public void onReceive(Context context, Intent intent){
            try{
                String datosJsonString = intent.getStringExtra("datosJson");
                JSONObject jSon = new JSONObject(datosJsonString);
                String resultado = jSon.getJSONObject("event").getString("description");
                Log.i("evento",resultado);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }





    private class Evento {
        String tipo;
        Boolean estado;
        String descripcion;

        Evento(String tipo,Boolean estado,String descripcion){
            this.tipo = tipo;
            this.estado = estado;
            this.descripcion = descripcion;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
