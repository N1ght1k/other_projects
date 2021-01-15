package com.example.brsv2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class FlightActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private ListView lv;

    // URL to get contacts JSON
    private static String url_get_flights = "http://***/BRS/FlightDay";
    private static String url_send_flight = "http://***/BRS/DestinationFlight?FlightId=";
    //private static String url_get_flights = "http://***/BRS/FlightDay";
    //private static String url_send_flight = "http://***/BRS/DestinationFlight?FlightId=";
    private static String currentFlight = "";
    private static String FlightForScreenshot;

    ArrayList<HashMap<String, String>> flightList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flight_list);

        flightList = new ArrayList<>();

        lv = (ListView) findViewById(R.id.list);

        new GetFlights().execute();

        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.e(TAG, "itemClick: position = " + position + ", id = "
                        + id);
                Log.e(TAG, flightList.get(position).get("flightId"));
                currentFlight = flightList.get(position).get("flightId");
                FlightForScreenshot = flightList.get(position).get("flightNumber");
                //url_send_flight = url_send_flight + currentFlight;
                new SendFlight().execute();
            }
        });
    }
    @Override
    public void onRestart() {
        super.onRestart();
        setContentView(R.layout.flight_list);

        flightList = new ArrayList<>();

        lv = (ListView) findViewById(R.id.list);

        new GetFlights().execute();

        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.e(TAG, "itemClick: position = " + position + ", id = "
                        + id);
                Log.e(TAG, flightList.get(position).get("flightId"));
                currentFlight = flightList.get(position).get("flightId");
                FlightForScreenshot = flightList.get(position).get("flightNumber");
                //url_send_flight = url_send_flight + currentFlight;
                new SendFlight().execute();
            }
        });
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetFlights extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(FlightActivity.this);
            pDialog.setMessage("Подождите...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            HttpHandler sh = new HttpHandler();
            for(int k = 0; k < 3; k++) {
                // Making a request to url and getting response
                String jsonStr = sh.makeServiceCall(url_get_flights);

                Log.e(TAG, "Response from url: " + jsonStr);

                if (jsonStr != null && jsonStr != "") {
                    try {

                        // Getting JSON Array node
                        JSONArray flights = new JSONArray(jsonStr);

                        // looping through All Contacts
                        for (int i = 0; i < flights.length(); i++) {
                            JSONObject f = flights.getJSONObject(i);

                            String flightId = f.getString("flightId");
                            String flightNumber = f.getString("flightNumber");
                            String airlineName = f.getString("airlineName");

                            // tmp hash map for single contact
                            HashMap<String, String> flight = new HashMap<>();

                            // adding each child node to HashMap key => value
                            flight.put("flightId", flightId);
                            flight.put("flightNumber", flightNumber);
                            flight.put("airlineName", airlineName);

                            // adding contact to contact list
                            flightList.add(flight);
                            Log.e(TAG, flightList.get(i).get("flightId"));
                        }

                    } catch (final JSONException e) {
                        Log.e(TAG, "Json parsing error: " + e.getMessage());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Json parsing error: " + e.getMessage(),
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        });

                    }
                    break;
                } else {
/*
                    Log.e(TAG, "Couldn't get json from server.");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Couldn't get json from server. Check LogCat for possible errors!",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
*/
                    if (k == 2)
                    {
                        Log.e(TAG, "Couldn't get json from server.");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Couldn't get json from server.",
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                    }
                    try {
                        Thread.sleep(1000);
                    }
                    catch (Throwable t)
                    {

                    }

                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             **/
            ListAdapter adapter = new SimpleAdapter(
                    FlightActivity.this, flightList,
                    R.layout.list_item, new String[]{"flightId", "flightNumber",
                    "airlineName"}, new int[]{R.id.flightId,
                    R.id.flightNumber, R.id.airlineName});

            lv.setAdapter(adapter);
        }

    }

    private class SendFlight extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(FlightActivity.this);
            pDialog.setMessage("Подождите...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            for(int k = 0; k < 3; k++) {
                // Making a request to url and getting response
                String jsonStr = sh.makeServiceCall(url_send_flight + currentFlight);
                Log.e(TAG, "url: " + url_send_flight + currentFlight);

                Log.e(TAG, "Response from url: " + jsonStr);

                if (jsonStr != null && jsonStr != "") {
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);
                        String result = jsonObj.getString("result");
                        Log.e(TAG, "Result: " + result);
                        if (result.equals("true")) {
                            Intent intent = new Intent(FlightActivity.this, MainActivity.class); //это код из фрагмента
                            intent.putExtra("FlightForScreenshot", FlightForScreenshot);
                            //intent.putExtra("pos2", position);
                            startActivity(intent);
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),
                                            "Ошибка выбора рейса",
                                            Toast.LENGTH_LONG)
                                            .show();
                                }
                            });
                        }

                    } catch (final JSONException e) {
                        Log.e(TAG, "Json parsing error: " + e.getMessage());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Json parsing error: " + e.getMessage(),
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        });

                    }
                    break;
                } else {
                    /*
                    Log.e(TAG, "Couldn't get json from server.");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Couldn't get json from server. Check LogCat for possible errors!",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                     */
                    if (k == 2)
                    {
                        Log.e(TAG, "Couldn't get json from server.");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Couldn't get json from server",
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                    }
                    try {
                        Thread.sleep(1000);
                    }
                    catch (Throwable t)
                    {

                    }

                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
        }

    }

    @Override
    public void onBackPressed() {
        recreate();
    }
}
