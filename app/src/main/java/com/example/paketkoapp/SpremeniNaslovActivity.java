package com.example.paketkoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.AclEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

interface VolleyCallBack {
    void onSuccess();
}

public class SpremeniNaslovActivity extends AppCompatActivity {

    //private String naslov;
    private EditText naslov;
    private TextView status;

    private ArrayList<String> podatkiPosiljke;

    private RequestQueue requestQueue;
    private String url = "https://paketko.azurewebsites.net/api/PosiljkeApi";

    double longitude;// = 5;
    double latitude;// = 5;
    double myDistance; //= 5;
    int myTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spremeni_naslov);

        naslov = (EditText) findViewById(R.id.naslovDostave);
        status = (TextView) findViewById(R.id.status);
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        naslov.setText((String) getIntent().getStringExtra("com.example.paketkoapp.MESSAGE"));
        podatkiPosiljke = getIntent().getStringArrayListExtra("com.example.paketkoapp.PODATKI");
    }


    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        return (dist);
    }

    public void posodobiNaslov2(final VolleyCallBack callBack) throws UnsupportedEncodingException {
        String address = naslov.getText().toString();

        ////////////////////////////////////////////////////////////////////////////////////////
        //RequestQueue queue = Volley.newRequestQueue(this);
        String url2 = "https://api.geoapify.com/v1/geocode/search?text=" + URLEncoder.encode(address, String.valueOf(StandardCharsets.UTF_8)) + "&apiKey=d00e457a82ee4025a18df721d57d115b";//"https://api.geoapify.com/v1/geocode/search?text=38%20Upper%20Montagu%20Street%2C%20Westminster%20W1H%201LJ%2C%20United%20Kingdom&apiKey=d00e457a82ee4025a18df721d57d115b";
        Log.i("url",url2);

        // Request a string response from the provided URL.
        StringRequest stringRequest2 = new StringRequest(Request.Method.GET, url2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        //Log.i("test", response.substring(0,500));
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.i("longitude", jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("geometry").getJSONArray("coordinates").getString(0).toString());
                            Log.i("latitude", jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("geometry").getJSONArray("coordinates").getString(1).toString());
                            longitude = Double.parseDouble(String.format("%.6f",jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("geometry").getJSONArray("coordinates").getDouble(0)));
                            latitude = Double.parseDouble(String.format("%.6f",jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("geometry").getJSONArray("coordinates").getDouble(1)));
                            myDistance = distance(latitude, longitude, Double.parseDouble(podatkiPosiljke.get(7)), Double.parseDouble(podatkiPosiljke.get(6)));
                            myTime = (int) (myDistance / 400);
                            Log.i("distance", Double.toString(myDistance));
                            Log.i("time", Integer.toString(myTime));

                            callBack.onSuccess();

                        }catch (JSONException err){
                            Log.d("Error", err.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("test", "Napaka");
            }
        });

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest2);
        ////////////////////////////////////////////////////////////////////////////////////////
    }

    public void posodobiNaslov(View view) throws UnsupportedEncodingException {
        posodobiNaslov2(new VolleyCallBack() {
            @Override
            public void onSuccess() {
            status.setText("Putting to " + url);
            try {

                String address = naslov.getText().toString();


                JSONObject jsonBody = new JSONObject();

                //Log.i("test", podatkiPosiljke.get(0));

                jsonBody.put("id", Integer.parseInt(podatkiPosiljke.get(0)));
                jsonBody.put("trackingNumber", podatkiPosiljke.get(1));
                jsonBody.put("weight", podatkiPosiljke.get(2));
                jsonBody.put("size", podatkiPosiljke.get(3));
                jsonBody.put("recipientShippingAddress", address);
                jsonBody.put("senderShippingAddress", podatkiPosiljke.get(5));
                jsonBody.put("senderLongitude", Double.parseDouble(podatkiPosiljke.get(6)));
                jsonBody.put("senderLatitude", Double.parseDouble(podatkiPosiljke.get(7)));
                jsonBody.put("recipientLongitude", longitude);
                jsonBody.put("recipientLatitude", latitude);
                jsonBody.put("shippingTime", myTime);
                jsonBody.put("sendDate", podatkiPosiljke.get(11));
                jsonBody.put("status", Integer.parseInt(podatkiPosiljke.get(12)));
                jsonBody.put("owner", null);//podatkiPosiljke.get(13));
                jsonBody.put("uporabnikID", Integer.parseInt(podatkiPosiljke.get(14)));
                jsonBody.put("dostavnaPotID", Integer.parseInt(podatkiPosiljke.get(15)));
                jsonBody.put("posiljateljID", Integer.parseInt(podatkiPosiljke.get(16)));
                jsonBody.put("dostavljalecID", Integer.parseInt(podatkiPosiljke.get(17)));
                jsonBody.put("dostavljalec", null);//podatkiPosiljke.get(18));
                jsonBody.put("dostavnaPot", null);//podatkiPosiljke.get(19));
                jsonBody.put("posiljatelj", null);//podatkiPosiljke.get(20));
                jsonBody.put("uporabnik", null);//podatkiPosiljke.get(21));

                final String mRequestBody = jsonBody.toString();
                Log.i("TEST->posiljam", mRequestBody);

                //Log.i("test", mRequestBody);

                status.setText(mRequestBody);

                StringRequest stringRequest = new StringRequest(Request.Method.PUT, url + "/" + podatkiPosiljke.get(0), new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("LOG_VOLLEY", response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("LOG_VOLLEY", error.toString());
                    }
                }
                ) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }
                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                            return null;
                        }
                    }
                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        String responseString = "";
                        if (response != null) {
                            responseString = String.valueOf(response.statusCode);
                            status.setText(responseString);
                        }
                        return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                    }

                    @Override
                    public Map<String,String> getHeaders() throws AuthFailureError
                    {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("ApiKey", "SecretKey");
                        params.put("Content-Type", "application/json");
                        return params;
                    }

                };

                Log.i("test", stringRequest.toString());
                requestQueue.add(stringRequest);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }});

        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    public void preklici(View view) {
        finish();
    }

}