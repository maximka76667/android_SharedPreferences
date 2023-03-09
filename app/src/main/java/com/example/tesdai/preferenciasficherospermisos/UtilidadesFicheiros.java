package com.example.tesdai.preferenciasficherospermisos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import java.io.File;


public class UtilidadesFicheiros {

    @SuppressLint("NewApi")
    public static String estadoSD(Context contexto, int num_sd) {
        File[] storages = ContextCompat.getExternalFilesDirs(contexto, null);
        if (num_sd==0) {
            return Environment.getExternalStorageState();
        }
        else if (storages.length > 1 && storages[1] != null) {
            return Environment.getExternalStorageState(storages[1]);
        }
        else {
            return null;
        }
    }
    public static boolean sdAccesoLectura(Context contexto, int num_sd) {
        String estado =estadoSD(contexto, num_sd);

        if (estado!=null && (estado.equals(Environment.MEDIA_MOUNTED) || estado.equals(Environment.MEDIA_MOUNTED_READ_ONLY)))
            return true;
        else
            return false;
    }


    public static boolean sdAccesoEscritura(Context contexto,int num_sd) {
        String estado =estadoSD(contexto, num_sd);

        if (estado!=null && estado.equals(Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }


    public static File establecerRutaFicheiroSD(Context contexto, int num_sd, String directorioStandard, boolean root, String directorio, String nomeFicheiro) {
            File directorioSD;
            File[] rutas = ContextCompat.getExternalFilesDirs(contexto, directorioStandard);

            if (num_sd==0) {
                if (root &&  Build.VERSION.SDK_INT<30) {
                    // Para escribir en raíz, fai falla o permiso WRITE_EXTERNAL_STORAGE (en Android 6 e superior hay que solicitalo en tempo de execución).
                    // Para que funcione en versións Android >=10, na etiqueta application de AndroidManifest.xml , temos que engadir: android:requestLegacyExternalStorage="true"
                    directorioSD = new File(rutas[0].toString().substring(0, rutas[0].toString().indexOf("Android")));
                }
                else {
                    directorioSD = rutas[0]; // Ruta da carpeta da aplicación. Non son necesarios os permisos.
                }
            }
            else if (rutas.length > 1 && rutas[1] != null) { 
                directorioSD = rutas[1]; // Ruta da carpeta da aplicación. Non son necesarios os permisos.
            }
            else{
                return null;
            }

            File rutaDirectorios;
            if (directorio!=null && !directorio.isEmpty())
                rutaDirectorios = new File(directorioSD.getAbsolutePath(), directorio);
            else
                rutaDirectorios = directorioSD;

            rutaDirectorios.mkdirs();

            File rutaCompleta = new File(rutaDirectorios,nomeFicheiro);

            return rutaCompleta;
        }


}




