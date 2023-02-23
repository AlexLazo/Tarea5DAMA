package com.lazo.fotos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button botonTomarFoto;
    Button btnVerGaleria;
    Button btnWhats;
    Button btnEmail;
    ImageView imgFoto;
    String rutaImagenes;

    private static final int REQUEST_CODIGO_CAMARA = 200;
    private static final int REQUEST_CODIGO_CAPTURAR_IMAGEN = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgFoto = findViewById(R.id.imgFoto);
        btnEmail = findViewById(R.id.btnMail);
        btnVerGaleria = findViewById(R.id.btnGallery);
        btnWhats = findViewById(R.id.btnWA);
        botonTomarFoto = findViewById(R.id.btnTomarFoto);

        botonTomarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                realizarProcesoFotografia();
            }
        });

        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail();
            }
        });
        btnVerGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentGaleria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentGaleria, 1);
            }
        });

        btnWhats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentShare = new Intent(Intent.ACTION_SEND);
                intentShare.setType("image/*");
                intentShare.setPackage("com.whatsapp");

                if(rutaImagenes != null){
                    intentShare.putExtra(Intent.EXTRA_STREAM, Uri.parse(rutaImagenes));
                    try{
                        startActivity(intentShare);
                    }catch (Exception e){
                        Toast.makeText(MainActivity.this,"Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this,"No hay imagen, tome una foto primero",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void sendEmail() {
        Intent it = new Intent(Intent.ACTION_SEND);
        it.putExtra(Intent.EXTRA_EMAIL, new String[]{"alex.lazo1403@gmail.com"});
        it.putExtra(Intent.EXTRA_SUBJECT, "Imagen");
        it.putExtra(Intent.EXTRA_TEXT, "Hola, adjunto tu imagen");
        //it.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imgFoto));
        it.setType("message/rfc822");
        startActivity(Intent.createChooser(it,"Elige una aplicación de Correo"));
    }


    public void realizarProcesoFotografia() {
        if (ActivityCompat.checkSelfPermission(
                MainActivity.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            tomarFoto();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CODIGO_CAMARA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODIGO_CAMARA) {
            if (permissions.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tomarFoto();
            } else {
                Toast.makeText(MainActivity.this,
                        "Se requiere permisos para la camara", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private void tomarFoto() {
        Intent intentCamara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intentCamara.resolveActivity(getPackageManager())!=null){
            File archivoFoto = null;
            archivoFoto = crearArchivo();
            if(archivoFoto!=null){
                Uri rutaFoto = FileProvider.getUriForFile(
                        MainActivity.this,
                        "com.lazo.fotos", archivoFoto);
                intentCamara.putExtra(MediaStore.EXTRA_OUTPUT, rutaFoto);
                startActivityForResult(intentCamara, REQUEST_CODIGO_CAPTURAR_IMAGEN);
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        if(requestCode == REQUEST_CODIGO_CAPTURAR_IMAGEN){
            if(resultCode == Activity.RESULT_OK){
                imgFoto.setImageURI(Uri.parse(rutaImagenes));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private File crearArchivo(){
        String nomenclatura = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String prefijoArchivo = "APPCAM_"+nomenclatura+"_";
        File directorioImagen = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File miImagen = null;
        try{
            miImagen = File.createTempFile(prefijoArchivo, ".jpg", directorioImagen);
            rutaImagenes = miImagen.getAbsolutePath();
        }catch(IOException error){
            Log.e("Error fichero",error.getMessage());
        }
        return miImagen;
    }
}
