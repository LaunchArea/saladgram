package com.saladgram.assembleprinter;

import android.app.Application;
import android.os.Handler;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by yns on 7/10/16.
 */
public class AssemblePrinterApplication extends Application {

    @Override
        public void onCreate() {
            super.onCreate();
            PrinterService.start(getApplicationContext());
        Service.start(getApplicationContext());
        }

}
