package com.example.paketkoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.ClientError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RequestQueue requestQueue;
    private TextView posiljke;
    private EditText sledilnaSt;
    private Button spremeniNaslov;
    private Button slediSt;
    private String url = "https://paketko.azurewebsites.net/api/PosiljkeApi"; //"https://paketko-dev.azurewebsites.net/api/PosiljkeApi"
    private String naslovDostave = "";
    private ArrayList<String> podatkiPosiljke;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        posiljke = (TextView) findViewById(R.id.posiljke);
        sledilnaSt = (EditText)findViewById(R.id.sledilnaSt);
        spremeniNaslov = (Button) findViewById(R.id.button);
        spremeniNaslov.setVisibility(View.INVISIBLE);
        podatkiPosiljke = new ArrayList<>();
        slediSt = (Button) findViewById(R.id.button2);

        /*
        new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                posiljke.setText("Vpisana številka ne obstaja!-2");
                if (error instanceof TimeoutError) {
                    posiljke.setText("Vpisana številka ne obstaja!-1");
                } else if(error instanceof ClientError) {
                    posiljke.setText("Vpisana številka ne obstaja!0");
                    //404 error can be further traced under ClientError section
                    if(((NetworkResponse) ((ClientError)error).networkResponse).statusCode == 404){
                        posiljke.setText("Vpisana številka ne obstaja!");
                    }
                } else if (error instanceof ServerError) {
                    posiljke.setText("Vpisana številka ne obstaja!1");
                } else if (error instanceof AuthFailureError) {
                    posiljke.setText("Vpisana številka ne obstaja!2");
                } else if (error instanceof NetworkError) {
                    posiljke.setText("Vpisana številka ne obstaja!3");
                } else if (error instanceof ParseError) {
                    posiljke.setText("Vpisana številka ne obstaja!4");
                } else if (error instanceof NoConnectionError) {
                    posiljke.setText("Vpisana številka ne obstaja!5");
                }
            }
        };*/
    }

    public  void prikaziPosiljke(View view){
        if (view != null){
            JsonArrayRequest request = new JsonArrayRequest(url, jsonArrayListener, errorListener);
            requestQueue.add(request);
        }
    }

    public  void slediPosiljki(View view){
        Log.i("slediSt", "PONAVLAM");
        /*if (view != null){
            JsonArrayRequest request = new JsonArrayRequest(url, jsonArrayListener, errorListener);
            requestQueue.add(request);
        }*/
        //EditText sledilnaSt = (EditText)findViewById(R.id.sledilnaSt);
        String st = sledilnaSt.getText().toString();
        if (view != null){
            JsonObjectRequest request = new JsonObjectRequest(url + "/TrackingNumber/" + st, jsonObjectListener, errorListener){
                @Override
                public Map<String,String> getHeaders() throws AuthFailureError
                {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("ApiKey", "SecretKey");
                    return params;
                }
            };
            requestQueue.add(request);
        }
    }



    public static final String EXTRA_MESSAGE = "com.example.paketkoapp.MESSAGE";
    public static final String EXTRA_PODATKI = "com.example.paketkoapp.PODATKI";

    public void spremeniNaslov (View view) {
        Intent intent = new Intent(this,SpremeniNaslovActivity.class);
        String message = naslovDostave;//"Spremeni naslov dostave posiljke.";
        intent.putExtra(EXTRA_MESSAGE, message);
        intent.putExtra(EXTRA_PODATKI, podatkiPosiljke);

        startActivityForResult(intent,1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        slediPosiljki(slediSt);
                        Log.i("slediSt", "TUKI");
                    }
                }, 1000);

            }
        }
    }



    private Response.Listener<JSONArray> jsonArrayListener = new Response.Listener<JSONArray>() {
        @Override
        public void onResponse(JSONArray response){
            ArrayList<String> data = new ArrayList<>();

            for (int i = 0; i < response.length(); i++){
                try {
                    JSONObject object =response.getJSONObject(i);
                    String trackingNumber = object.getString("trackingNumber");
                    String sendDate = object.getString("sendDate");
                    String shippingTime = object.getString("shippingTime");
                    String status = object.getString("status");
                    data.add("Sledilna st: " + trackingNumber + ", Datum oddaje: " + sendDate + ", Trajanje dostave: " + shippingTime + "dni, Status: " + status);

                } catch (JSONException e){
                    e.printStackTrace();
                    return;

                }
            }

            posiljke.setText("");


            for (String row: data){
                String currentText = posiljke.getText().toString();
                posiljke.setText(currentText + "\n\n" + row);
            }

        }

    };

    private Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response){
            ArrayList<String> data = new ArrayList<>();

            try {
                JSONObject object = response;

                podatkiPosiljke = new ArrayList<>();
                podatkiPosiljke.add(object.getString("id"));
                podatkiPosiljke.add(object.getString("trackingNumber"));
                podatkiPosiljke.add(object.getString("weight"));
                podatkiPosiljke.add(object.getString("size"));
                podatkiPosiljke.add(object.getString("recipientShippingAddress"));
                podatkiPosiljke.add(object.getString("senderShippingAddress"));
                podatkiPosiljke.add(object.getString("senderLongitude"));
                podatkiPosiljke.add(object.getString("senderLatitude"));
                podatkiPosiljke.add(object.getString("recipientLongitude"));
                podatkiPosiljke.add(object.getString("recipientLatitude"));
                podatkiPosiljke.add(object.getString("shippingTime"));
                podatkiPosiljke.add(object.getString("sendDate"));
                podatkiPosiljke.add(object.getString("status"));
                podatkiPosiljke.add(object.getString("owner"));
                podatkiPosiljke.add(object.getString("uporabnikID"));
                podatkiPosiljke.add(object.getString("dostavnaPotID"));
                podatkiPosiljke.add(object.getString("posiljateljID"));
                podatkiPosiljke.add(object.getString("dostavljalecID"));
                podatkiPosiljke.add(object.getString("dostavljalec"));
                podatkiPosiljke.add(object.getString("dostavnaPot"));
                podatkiPosiljke.add(object.getString("posiljatelj"));
                podatkiPosiljke.add(object.getString("uporabnik"));

                if(!object.has("title")) { //&& !object.getString("title").equals("Not Found")) {
                    String trackingNumber = object.getString("trackingNumber");
                    String recipientShippingAddress = object.getString("recipientShippingAddress");
                    String sendDate = object.getString("sendDate");
                    int shippingTime = object.getInt("shippingTime");
                    int status = object.getInt("status");

                    naslovDostave = recipientShippingAddress;

                    sendDate = sendDate.substring(8, 10) +"."+ sendDate.substring(5, 7) +"."+ sendDate.substring(0, 4);

                    String klukica1 = "";
                    String klukica2 = "";
                    String klukica3 = "";
                    String klukica4 = "";
                    String klukica5 = "";

                    if(status == 0 || status == 1 || status == 2 || status == 3 || status == 4) {
                        klukica1 = "            <span id=\"checkmark1\">✓</span>\n";
                    }
                    if(status == 1 || status == 2 || status == 3 || status == 4) {
                        klukica2 = "            <span id=\"checkmark1\">✓</span>\n";
                    }
                    if(status == 2 || status == 3 || status == 4) {
                        klukica3 = "            <span id=\"checkmark1\">✓</span>\n";
                    }
                    if(status == 3 || status == 4) {
                        klukica4 = "            <span id=\"checkmark1\">✓</span>\n";
                    }
                    if(status == 4) {
                        klukica5 = "            <span id=\"checkmark1\">✓</span>\n";
                    }

                    if (status == 0) {
                        spremeniNaslov.setVisibility(View.VISIBLE);
                    }
                    else {
                        spremeniNaslov.setVisibility(View.INVISIBLE);
                    }


                    //data.add("Sledilna st: " + trackingNumber + ", Datum oddaje: " + sendDate + ", Trajanje dostave: " + shippingTime + "dni, Status: " + status);
                    //posiljke.setText("Sledilna st: " + trackingNumber + ", Datum oddaje: " + sendDate + ", Trajanje dostave: " + shippingTime + "dni, Status: " + status);
                    posiljke.setText(Html.fromHtml("<div class=\"container mt-5\">\n" +
                            "    <span class=\"h4\">Pošiljka: </span>\n" +
                            "    <span class=\"h4 fw-bold\">"+trackingNumber+"</span>\n" +
                            "</div>\n" +
                            "<div class=\"container mt-3\">\n" +
                            "    <div>\n" +
                            "        <h5>Naslov dostave: "+recipientShippingAddress+"\n" +
                            "        </h5>\n" +
                            "        <h5>Datum oddaje: "+sendDate+"</h5>\n" +
                            "        <h5>Čas dostave: "+ shippingTime +" dni</h5>\n" +
                            "    </div>\n" +
                            "    <ul class=\"fs-4\">\n" +
                            "        <li>Obdelava naročila \n" +
                            klukica1 +
                            "        </li>\n" +
                            "        <li>V tranzitu do paketnega centra \n" +
                            klukica2 +
                            "        </li>\n" +
                            "        <li>Dostava v paketni center \n" +
                            klukica3 +
                            "        </li>\n" +
                            "        <li>V tranzitu do končnega cilja  \n" +
                            klukica4 +
                            "        </li>\n" +
                            "        <li>Vročitev paketa \n" +
                            klukica5 +
                            "        </li>\n" +
                            "    </ul>\n" +
                            "</div>"));
                }
            } catch (JSONException e){
                e.printStackTrace();
                return;

            }

            /*posiljke.setText("");

            if(!data.isEmpty()) {
                for (String row : data) {
                    String currentText = posiljke.getText().toString();
                    posiljke.setText(currentText + "\n\n" + row);
                }
            }
            else
                posiljke.setText("Vpisana številka ne obstaja!");*/

        }

    };

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            if(error.getMessage() != null) {
                Log.d("REST error", error.getMessage());
            }
        }
    };

}