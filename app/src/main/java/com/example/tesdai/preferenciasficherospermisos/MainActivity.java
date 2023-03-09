package com.example.tesdai.preferenciasficherospermisos;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity {
    Spinner spinnerColor;
    Button bExportar;
    EditText etNota;
    SharedPreferences prefs;
    int colorIndex;

    String colores[]={"Magenta","Amarillo","Gris"};

    final int CODIGO_PETICION_WRITE_EXTERNAL_STORAGE_PARA_EXPORTAR_NOTA = 1; // Para la gestión de solicitud de permiso

    @Override
    protected void onPause() {  // Este método siempre se ejecuta antes de cerrarse la aplicación.
        super.onPause();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("color", colorIndex);

        editor.commit();
    }

    String recuperaTextoNota(){ // Si existe lee y devuelve el texto de la nota previamente guardado en el almacenamiento interno.

        return "";
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bExportar = findViewById(R.id.bExportar);
        etNota = findViewById(R.id.edNota);

        spinnerColor = findViewById(R.id.spinnerColor);
        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,colores);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColor.setAdapter(adaptador);

        prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);

        spinnerColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> spinner, View v,
                                       int posicion, long id) {
                colorIndex = posicion;
                if (colores[posicion].equals("Magenta")){
                    etNota.setBackgroundColor(Color.MAGENTA);
                }
                else if (colores[posicion].equals("Amarillo")){
                    etNota.setBackgroundColor(Color.YELLOW);
                }
                else if (colores[posicion].equals("Gris")){
                    etNota.setBackgroundColor(Color.GRAY);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        etNota.setText(prefs.getInt("color", 0) + "");

//        etNota.setText(recuperaTextoNota());  // Recupera del almacenamiento interno el texto de la nota guardada y la visualiza


        bExportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( PermisoConcedido(MainActivity.this,
                         Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        "Para poder exportar la nota, es necesario conceder permisos para crear el fichero en el almacenamiento externo de su dispositivo",
                        CODIGO_PETICION_WRITE_EXTERNAL_STORAGE_PARA_EXPORTAR_NOTA)
                    )
                {
                    try {
                        exportaNota(); // Guarda la nota en el almacenamiento externo.
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    /* Prerequisito: Añadir permiso a AndroidManifest.xml:
         <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
         y gestionar adecuadamente la solicitud del permiso para versiones de Android >=6 (M)
    */
    void exportaNota() throws IOException { // Guarda el texto del EditText en el almacenamiento externo


        File f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        f.mkdirs(); // Crea la ruta de carpetas
        String carpeta = f.getAbsolutePath();

        FileOutputStream output = new FileOutputStream(new File(carpeta + File.separator + "nota.txt"));
        output.write(etNota.getText().toString().getBytes());
        output.close();

        Toast.makeText(MainActivity.this, "Exportado con exito", Toast.LENGTH_LONG);
    }


    private boolean PermisoConcedido(final Activity a, final String permiso, final String textoAclaratorio, final int codigoSolicitudPermiso) {
                                                            // Build.VERSION.SDK_INT  => Versión Android del dispositivo en el que se está ejecutando la app.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { // Build.VERSION_CODES.M => Versión 6 (M - Marshmallow) de Android. https://developer.android.com/reference/android/os/Build.VERSION_CODES.html
            return true;
        }
        if (ActivityCompat.checkSelfPermission(a,permiso) == PackageManager.PERMISSION_GRANTED) { // Permiso ya previamente concedido.
            return true;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(a,permiso)) { // Es necesario dar una explicación (textoAclaratorio) acerca de por qué nuestra aplicacion necesita el permiso.
            AlertDialog.Builder datosDialog = new AlertDialog.Builder(a);
            datosDialog.setTitle("Solicitud de permiso");
            datosDialog.setMessage(textoAclaratorio);
            datosDialog.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                        String[] arrayPermisos = {permiso}; // Se usa un array porque es posible solicitar varios permisos al mismo tiempo
                        ActivityCompat.requestPermissions(a,arrayPermisos, codigoSolicitudPermiso); // muestra una ventana al usuario
                }
            });
            datosDialog.show();

        } else {
             // new String[]{permiso} ==> Atajo para crear e inicilizar el array en una sola instrucción
            ActivityCompat.requestPermissions(a, new String[]{permiso}, codigoSolicitudPermiso);  // muestra una ventana al usuario
        }
        return false;
    }

    /**
     * Callback Despues de ActivityCompat.requestPermissions, se ejectua cuando el usuario concede o rechaza permisos
     */
    @Override                                                // Se reciben arrays porque es posible solicitar varios permisos al mismo tiempo
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // requestCode = Código de Peticiñon, permissions=Permisos solicitados, grantResults=array paralelo de concesiones o rechazos de permisos.
        if (requestCode == CODIGO_PETICION_WRITE_EXTERNAL_STORAGE_PARA_EXPORTAR_NOTA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido.
                // La próxima llamada a checkSelfPermission(WRITE_EXTERNAL_STORAGE) devolverá:  PackageManager.PERMISSION_GRANTED
                bExportar.callOnClick();
            }
            else { // El usuario ha rechazado el permiso.
                Toast.makeText(getApplicationContext(),"Permiso para escritura en almaceniento externo rechazado",Toast.LENGTH_LONG).show();
            }
        }
    }
}
