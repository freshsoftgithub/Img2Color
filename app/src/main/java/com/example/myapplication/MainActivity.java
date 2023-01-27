package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.example.myapplication4.api.VolleyMultipartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

public class MainActivity extends AppCompatActivity {

    ProgressBar progressBar;
    ImageView imageView;
    final int Pick_image = 1;
    final int Catch_image = 0;
    Button send;
    Button btnCamera;
    Button btnPick_Img;
    Bitmap imageData;
    Timer timer;
    int count;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        progressBar = findViewById(R.id.progbar);
        progressBar.setVisibility(View.INVISIBLE);

        imageView = findViewById(R.id.ViewImage);

        btnCamera = findViewById(R.id.btnCamera);
        btnPick_Img = findViewById(R.id.btnPickImg);

        send = findViewById(R.id.btnSend);
        send.setVisibility(View.INVISIBLE);



        btnPick_Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send.setEnabled(true);
                Intent photo = new Intent(Intent.ACTION_PICK);
                photo.setType("image/*");
                startActivityForResult(photo,Pick_image);
            }
        });
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send.setEnabled(true);
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camera,Catch_image);
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage(imageData,null);

                send.setEnabled(false);
                btnCamera.setEnabled(false);
                btnPick_Img.setEnabled(false);

                Toast message = Toast.makeText(MainActivity.this,"Происходит чудо, пожалуйста подождите.",Toast.LENGTH_LONG);
                message.show();

                progressBar.setVisibility(View.VISIBLE);
                timer = new Timer(){
                    public void run(){
                        count++;
                        progressBar.setProgress(count);
                    }
                };

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case Pick_image:
                if(resultCode == RESULT_OK){
                    try {
                        final Uri imageUri = data.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedimage = BitmapFactory.decodeStream(imageStream);
                        imageView.setImageBitmap(selectedimage);
                        imageData = selectedimage;
                        send.setVisibility(View.VISIBLE);
                    }
                    catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
            case Catch_image:{
                if(resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                        imageData = bitmap;
                        imageView.setImageBitmap(bitmap);
                        send.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void uploadImage(final Bitmap image, String email) {
        String url = "http://10.0.2.2:8000/upload"; //ссылка на сервер

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));

                            //Ссылка paint
                            String paint =  obj.getJSONObject("image_paint").getString("first");
                            //Ссылка pencil
                            String pencil = obj.getJSONObject("image_pencil").getString("first");

                            //Отправка на второй и третий экраны
                            SecondActivity.GetURLPaint(paint);
                            ThirdActivity.GetURLPencil(pencil);

                            timer.cancel();

                            //Открыть второй экран
                            Intent second = new Intent(MainActivity.this,SecondActivity.class);
                            MainActivity.this.startActivity(second);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("GotError",""+error.getMessage());
                    }
                }) {


            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("file", new DataPart(imagename + ".png", getFileDataFromDrawable(image)));
                return params;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("email", email);

                return params;
            }
        };
        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(90000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //adding the request to volley
        Volley.newRequestQueue(this).add(volleyMultipartRequest);

    }
}



