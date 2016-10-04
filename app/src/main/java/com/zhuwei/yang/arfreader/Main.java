package com.zhuwei.yang.arfreader;

import android.content.Context;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import static com.zhuwei.yang.arfreader.RFReaderConstant.error_info;

public class Main extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    //private static PendingIntent mPermissionIntent;
    private static String TAG = "aRfreader.example";
    private UsbDevice m_device;


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button scanButton = (Button) findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onClick(View v) {
                TextView tv_cardid = (TextView) findViewById(R.id.TV_cardid);
                UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

                RFReader.RFReaderResp resp = RFReader.read_card_snr(manager);

                switch (resp.errno) {
                    case 0:
                        tv_cardid.setText(resp.card_sernum());
                        break;
                    case 1:
                        tv_cardid.setText("card reader returns: " + error_info(resp.error_code()));
                        break;
                    default:
                        tv_cardid.setText("Error:" + error_info(resp.errno));
                }

            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(RFReader.UsbPermitReceiver, filter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(RFReader.UsbPermitReceiver);
    }


}

