package aaq.ovcharovam.parkovkav2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImage;
import com.itextpdf.text.pdf.PdfIndirectObject;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

import static com.itextpdf.text.PageSize.A4;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class MainActivity extends Activity {

    //EditText txt;
    Button btn;
    Button SendEmail;
    TextView tvEnabledNet;
    TextView tvStatusNet;
    TextView tvLocationNet;

    private LocationManager locationManager;
    StringBuilder sbGPS = new StringBuilder();
    StringBuilder sbNet = new StringBuilder();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //txt = (EditText) findViewById(R.id.text);
        btn = (Button) findViewById(R.id.button);
        SendEmail = (Button) findViewById(R.id.Send);

        tvEnabledNet = (TextView) findViewById(R.id.tvEnabledNet);
        tvLocationNet = (TextView) findViewById(R.id.tvLocationNet);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Intent intent = getIntent();
        final String time = intent.getStringExtra("time");

        View.OnClickListener oclbtn = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {
                    createPdf(time);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        View.OnClickListener sendbtn = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"test@test.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Email subject");
                emailIntent.putExtra(Intent.EXTRA_TEXT,
                        Html.fromHtml("<b>Bold message body</b>"));
                emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:/sdcard/Download/Аэропорт_Анапа_" + time + ".pdf"));
                emailIntent.setType("application/octet-stream");
                startActivity(Intent.createChooser(emailIntent, "Send Email"));
            }
        };
        btn.setOnClickListener(oclbtn);
        SendEmail.setOnClickListener(sendbtn);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1000 * 10, 10,
                locationListener);
        checkEnabled();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            checkEnabled();
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onProviderEnabled(String provider) {
            checkEnabled();
            showLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private void showLocation(Location location) {
        if (location == null)
            return;
        if (location.getProvider().equals(
                LocationManager.GPS_PROVIDER)) {
            tvLocationNet.setText(formatLocation(location));
        }
    }

    private String formatLocation(Location location) {
        if (location == null)
            return "";
        return String.format(
                "Широта %1$.4f, Долгота %2$.4f",
                location.getLatitude(), location.getLongitude());
    }

    private void checkEnabled() {
        tvEnabledNet.setText("Enabled: "
                + locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER));
    }


    private void createPdf(String time) throws IOException, DocumentException {

        File pdfFolder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "Аэропорт_Анапа_");
        if (!pdfFolder.exists()) {
            pdfFolder.mkdir();
        }



        Bitmap src = BitmapFactory.decodeFile("/sdcard/Parkovka_photo/"+ time +".jpg"); // the original file is cuty.jpg i added in resources
        Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTime = sdf.format(Calendar.getInstance().getTime()); // reading local time in the system

        Canvas cs = new Canvas(dest);
        float ahah = cs.getHeight();
        Paint tPaint = new Paint();
        tPaint.setTextSize(80);
        tPaint.setColor(Color.YELLOW);
        tPaint.setStyle(Paint.Style.FILL);
        cs.drawBitmap(src, 0f, 0f, null);
        float height = tPaint.measureText("yY");
        cs.drawText(tvLocationNet.getText().toString(), 20f, ahah - 200f, tPaint);
        cs.drawText(dateTime, 20f, ahah - 100f, tPaint);
        try {
            dest.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(new File("/sdcard/Parkovka_photostamp/"+ time +".jpg")));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //Create time stamp
        Date date = new Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);
        String timeStampApp = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(date);

        try {

            //String SRC = "/sdcard/CameraExample/source.pdf";

            File myFile = new File(pdfFolder + time + ".pdf");

            PdfReader reader = new PdfReader(getAssets().open("source.pdf"));
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(pdfFolder + time + ".pdf"));

            Image image = Image.getInstance("/sdcard/Parkovka_photostamp/" + time + ".jpg");
            //image.scalePercent(50f);
            image.setAlignment(Element.ALIGN_CENTER);
            float reqheight = 590;
            float reqwidth = 590;
            float height1 = image.getHeight();
            float width1 = image.getWidth();
            float hmin = height1 / reqheight;
            float wmin = width1 / reqwidth;
            float ratio = hmin > wmin ? hmin : wmin;
            image.scaleAbsolute(width1 / ratio, height1 / ratio);

            //document.add(image);

            PdfImage stream = new PdfImage(image, "", null);
            stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
            PdfIndirectObject ref = stamper.getWriter().addToBody(stream);
            image.setDirectReference(ref.getIndirectReference());
            image.setAbsolutePosition(3, 330);
            PdfContentByte over = stamper.getOverContent(2);
            over.addImage(image);
            stamper.close();
            reader.close();


        } catch (IOException ex) {
            return;
        } catch (BadElementException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }


    }
}

