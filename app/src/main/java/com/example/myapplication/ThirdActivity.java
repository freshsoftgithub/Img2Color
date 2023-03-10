package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;

public class ThirdActivity extends AppCompatActivity {

    static String pencil;
    ImageView image_pencil;
    Button Paint;
    Button back;
    Button send;
    Button mail;

    public static String GetURLPencil(String image_pencil){
        pencil = image_pencil;
        return pencil;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);


        image_pencil = (ImageView)findViewById(R.id.ViewImage_pencil);

        Bitmap bit_pencil = getBitmapFromURL(pencil);
        image_pencil.setImageBitmap(bit_pencil);

        back = findViewById(R.id.btnBack);
        Paint = findViewById(R.id.btnPaintShow);
        send = findViewById(R.id.btnGetPictures);
        mail = findViewById(R.id.btnEmail);

        Paint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pencil = new Intent(ThirdActivity.this,SecondActivity.class);
                startActivity(pencil);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent back_to_main = new Intent(ThirdActivity.this,MainActivity.class);
                startActivity(back_to_main);
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dowbloadImageOnDevice(bit_pencil);
                Toast message = Toast.makeText(ThirdActivity.this,"?????????????????????? ??????????????.",Toast.LENGTH_LONG);
                message.show();
            }
        });

        mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent send = new Intent(ThirdActivity.this,FourthActivity.class);
                startActivity(send);
            }
        });
    }

    private static Bitmap getBitmapFromURL(String src) {
        try {
            Log.e("src", src);
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            Log.e("Bitmap", "returned");
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Exception", e.getMessage());
            return null;
        }
    }

    private void dowbloadImageOnDevice(Bitmap picture){
        OutputStream fos;
        try{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,"Image" + ".jpg");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
                picture.compress(Bitmap.CompressFormat.JPEG,100,fos);
                Objects.requireNonNull(fos);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }



}