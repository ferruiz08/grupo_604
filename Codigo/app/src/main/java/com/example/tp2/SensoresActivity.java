package com.example.tp2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.EditText;

import java.text.DecimalFormat;

public class SensoresActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private EditText textXRot;
    private EditText textYRot;
    private EditText textZRot;
    private EditText textOrientacion;
    private EditText textPos;
    private EditText textPasos;

    private int contadorPasos;
    DecimalFormat dosdecimales = new DecimalFormat("###.###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensores);

        textXRot = (EditText) findViewById(R.id.editTextXRot);
        textYRot = (EditText) findViewById(R.id.editTextYRot);
        textZRot = (EditText) findViewById(R.id.editTextZRot);
        textOrientacion = (EditText) findViewById(R.id.editTextOrientacion);
        textPos = (EditText) findViewById(R.id.editTextPos);
        textPasos = (EditText) findViewById(R.id.editTextPasos);

        //Inicializo el contador de pasos
        contadorPasos = 0;

        // Accedemos al servicio de sensores
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

    }

    // Metodo para iniciar el acceso a los sensores
    protected void Ini_Sensores()
    {
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),   SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),       SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),       SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),       SensorManager.SENSOR_DELAY_NORMAL);
    }

    // Metodo para parar la escucha de los sensores
    private void Parar_Sensores()
    {
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.d("sensor", event.sensor.getName());
        synchronized (this){
            switch(event.sensor.getType()){

                case Sensor.TYPE_ACCELEROMETER :

                    textXRot.setText(dosdecimales.format(event.values[0]) + "m/seg2");
                    textYRot.setText(dosdecimales.format(event.values[1]) + "m/seg2");
                    textZRot.setText(dosdecimales.format(event.values[2]) + "m/seg2");
                    break;

                case Sensor.TYPE_ROTATION_VECTOR :
                    //Log.i("rotacion", event.sensor.getName());
                    // Creo objeto para saber como esta la pantalla
                    Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                    int rotation = display.getRotation();
                    if( rotation == 0 )
                    {
                        textOrientacion.setText("Vertical");
                    }
                    else if( rotation == 1 )
                    {
                        textOrientacion.setText("Horizontal");
                    }
                    else if (rotation == 3)
                    {
                        textOrientacion.setText("Horizontal");
                    }
                    break;

                case Sensor.TYPE_PROXIMITY :

                    textPos.setText(dosdecimales.format(event.values[0]) + "cm");
                    // Si detecta 0 lo represento
                    if( event.values[0] == 0 )
                    {
                       // detecta.setText("Proximidad Detectada");
                    }
                    break;

                case Sensor.TYPE_STEP_DETECTOR :
                    contadorPasos++;
                    textPasos.setText(Integer.toString(contadorPasos));
                    break;
            }
        }
    }

    @Override
    protected void onStop()
    {

        Parar_Sensores();

        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        Parar_Sensores();

        super.onDestroy();
    }

    @Override
    protected void onPause()
    {
        Parar_Sensores();

        super.onPause();
    }

    @Override
    protected void onRestart()
    {
        Ini_Sensores();

        super.onRestart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Ini_Sensores();
    }



}
