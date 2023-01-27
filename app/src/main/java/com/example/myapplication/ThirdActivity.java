package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

public class ThirdActivity extends AppCompatActivity {

    static String pencil;
    ImageView image_pencil;
    static Bitmap imageData;
    ProgressBar progressBar;
    int count;
    Timer timer;
    EditText emailT;
    Button Paint;
    Button back;
    Button send;

    public static String GetURLPencil(String image_pencil){
        pencil = image_pencil;
        return pencil;
    }

    public static Bitmap GetImageData(Bitmap img) {
        imageData = img;
        return imageData;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        emailT = (EditText)findViewById(R.id.Email);

        progressBar = (ProgressBar)findViewById(R.id.progbar);
        progressBar.setVisibility(View.INVISIBLE);

        image_pencil = (ImageView)findViewById(R.id.ViewImage_pencil);

        Bitmap bit_pencil = getBitmapFromURL(pencil);
        image_pencil.setImageBitmap(bit_pencil);

        back = (Button)findViewById(R.id.btnBack);
        Paint = (Button)findViewById(R.id.btnPaintShow);
        send = (Button)findViewById(R.id.btnGetPictures);

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
                uploadImage(imageData,emailT.getText().toString());

                back.setEnabled(false);
                Paint.setEnabled(false);
                send.setEnabled(false);
                emailT.setEnabled(false);

                Toast message = Toast.makeText(ThirdActivity.this,"Идет отправка изображений, пожалуйста подождите.",Toast.LENGTH_LONG);
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

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void uploadImage(final Bitmap image, String email) {
        String url = "http://10.0.2.2:8000/upload"; //урл, куда пост-запрос отправляется

        com.example.myapplication4.api.VolleyMultipartRequest volleyMultipartRequest = new com.example.myapplication4.api.VolleyMultipartRequest(Request.Method.POST, url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));

                            //Ссылка paint
                            String paint =  obj.getJSONObject("image_paint").getString("first");
                            //Ссылка pencil
                            String pencil = obj.getJSONObject("image_pencil").getString("first");

                            //Отправка на второй экран
                            //SecondActivity.GetURLPaint(paint);
                            //ThirdActivity.GetURLPencil(pencil);

                            timer.cancel();
                            progressBar.setVisibility(View.INVISIBLE);

                            Toast message = Toast.makeText(ThirdActivity.this,"Изображения отправлены на почту", Toast.LENGTH_SHORT);
                            message.show();

                            back.setEnabled(true);
                            Paint.setEnabled(true);
                            send.setEnabled(true);
                            emailT.setEnabled(true);
                            //Открыть второй экран


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