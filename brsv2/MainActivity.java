package com.example.brsv2;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;

public class MainActivity extends AppCompatActivity {

    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action
    private ImageView imageView;
    private TextView barcodeText, flightView, all_countView, count_of_acceptedView;
    private ScanManager mScanManager;
    int[] idactionbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
    String[] action_value_buf = new String[]{ScanManager.ACTION_DECODE, ScanManager.BARCODE_STRING_TAG};
    int flag2 = 0;
    int[] idbuf2 = new int[]{PropertyID.I25_LENGTH1, PropertyID.I25_LENGTH2};
    int[] value_buff2 = new int[]{2, 50};
    //private Timer mTimer;
    //private MyTimerTask mMyTimerTask;
    private static String resultBarcode="";
    private ProgressDialog pDialog;
    String TAG="MainActivity";
    private static String url_send_barcode = "http://***/BRS/CheckBaggageNew?RfidLabel=";
    //private static String url_send_barcode = "http://***/BRS/CheckBaggageNew?RfidLabel=";

    private SoundPool mSoundPool;
    private int mSoundId = 1;
    private int mStreamId;

    private SoundPool soundpool = null;
    private int soundid;
    private int soundError;
    private int soundSuccess;
    private Vibrator mVibrator;

    private static String FlightForScreenshot;

    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            byte[] barcode = intent.getByteArrayExtra(ScanManager.DECODE_DATA_TAG);
            int barcodelen = intent.getIntExtra(ScanManager.BARCODE_LENGTH_TAG, 0);
            byte temp = intent.getByteExtra(ScanManager.BARCODE_TYPE_TAG, (byte) 0);
            resultBarcode = intent.getStringExtra(action_value_buf[1]);
            /*if(barcodelen != 0)
                barcodeStr = new String(barcode, 0, barcodelen);
            else
                barcodeStr = intent.getStringExtra("barcode_string");*/
            if(resultBarcode != null) {
                soundpool.play(soundid, 1, 1, 0, 0, 1);
                barcodeText.setText("" + resultBarcode);
                mVibrator.vibrate(100);
                new SendBarcode().execute();
            }
        }

    };
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScanManager = new ScanManager();
        mScanManager.openScanner();
        mScanManager.setParameterInts(idbuf2, value_buff2);
        soundpool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100); // MODE_RINGTONE
        soundid = soundpool.load(this, R.raw.scan, 1);
        soundError = soundpool.load(this, R.raw.error, 1);
        soundSuccess = soundpool.load(this, R.raw.success, 1);

        //mTimer = new Timer();
        //mMyTimerTask = new MyTimerTask();
        //mTimer.schedule(mMyTimerTask, 0, 5000);

        action_value_buf = mScanManager.getParameterString(idactionbuf);
        barcodeText = (TextView) findViewById(R.id.barcodeText);
        flightView = (TextView) findViewById(R.id.flight);
        all_countView = (TextView) findViewById(R.id.all_count);
        count_of_acceptedView = (TextView) findViewById(R.id.count_of_accepted);
        imageView = (ImageView) findViewById(R.id.image1);

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        Bundle arguments = getIntent().getExtras();
        FlightForScreenshot = arguments.get("FlightForScreenshot").toString();
    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        unregisterReceiver(mScanReceiver);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        IntentFilter filter = new IntentFilter();
        action_value_buf = mScanManager.getParameterString(idactionbuf);
        filter.addAction(action_value_buf[0]);
        registerReceiver(mScanReceiver, filter);
    }

    /*
    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            if (flag2 == 0) {
                value_buff2[0] = 3;
                value_buff2[1] = 49;
                flag2 = 1;
                mScanManager.setParameterInts(idbuf2, value_buff2);
            }
            else {
                value_buff2[0] = 4;
                value_buff2[1] = 48;
                flag2 = 0;
                mScanManager.setParameterInts(idbuf2, value_buff2);
            }

        }
    }
*/
    private class SendBarcode extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Подождите...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            for(int k = 0; k < 3; k++) {
                // Making a request to url and getting response
                String jsonStr = sh.makeServiceCall(url_send_barcode + resultBarcode);
                Log.e(TAG, "url: " + url_send_barcode + resultBarcode);

                Log.e(TAG, "Response from url: " + jsonStr);

                if (jsonStr != null && jsonStr != "") {
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);
                        String result = jsonObj.getString("result");
                        final String flight = jsonObj.getString("flight");
                        final String all_count = jsonObj.getString("all_count");
                        final String count_of_accepted = jsonObj.getString("count_of_accepted");
                        Log.e(TAG, "Result: " + result);
                        if (result.equals("true")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    imageView.setImageResource(R.drawable.true_image);
                                    flightView.setText(flight);
                                    //all_countView.setText(all_count);
                                    count_of_acceptedView.setText(count_of_accepted);
                                    soundpool.play(soundSuccess, 1, 1, 0, 0, 1);
                                }
                            });

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    imageView.setImageResource(R.drawable.false_image);
                                    flightView.setText(flight);
                                    //all_countView.setText(all_count);
                                    count_of_acceptedView.setText(count_of_accepted);
                                    soundpool.play(soundError, 1, 1, 0, 0, 1);
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
                                imageView.setImageResource(R.drawable.false_image);
                                soundpool.play(soundError, 1, 1, 0, 0, 1);
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
                                imageView.setImageResource(R.drawable.false_image);
                                soundpool.play(soundError, 1, 1, 0, 0, 1);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Вы действительно хотите выйти?");
        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user pressed "yes", then he is allowed to exit from application
                Intent intent = new Intent(MainActivity.this, FlightActivity.class); //это код из фрагмента
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("Нет",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user select "No", just cancel this dialog and continue with app
                dialog.cancel();
            }
        });
        AlertDialog alert=builder.create();
        alert.show();
    }

    private void takeScreenshot() {
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_hh:mm:ss", Locale.getDefault());
        String dateText = dateFormat.format(now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/Download/" + FlightForScreenshot + " " + dateText + ".jpg";

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Скриншот сохранен",
                            Toast.LENGTH_LONG)
                            .show();
                }
            });

        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }

    @Override
    // catches the onKeyDown button event
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == 514) {
            takeScreenshot();
        }

        return super.onKeyDown(keyCode, event);
    }
}
