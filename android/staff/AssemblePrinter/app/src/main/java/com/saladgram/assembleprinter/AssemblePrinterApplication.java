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
    private Server mServer;

    @Override
        public void onCreate() {
            super.onCreate();
            PrinterService.start(getApplicationContext());
            if (mServer == null) {
                mServer = new Server();
                mServer.onCreate();
            }
        }
    public class Server {

        private ServerSocket serverSocket;

        Handler updateConversationHandler;

        Thread serverThread = null;

        public static final int SERVERPORT = 6000;

        public void onCreate() {

            updateConversationHandler = new Handler();

            this.serverThread = new Thread(new ServerThread());
            this.serverThread.start();

        }

        protected void onStop() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        class ServerThread implements Runnable {

            public void run() {
                Socket socket = null;
                try {
                    serverSocket = new ServerSocket(SERVERPORT);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (!Thread.currentThread().isInterrupted()) {

                    try {

                        socket = serverSocket.accept();

                        CommunicationThread commThread = new CommunicationThread(socket);
                        new Thread(commThread).start();

                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }

        class CommunicationThread implements Runnable {

            private Socket clientSocket;

            private BufferedReader input;

            public CommunicationThread(Socket clientSocket) {

                this.clientSocket = clientSocket;

                try {

                    this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void run() {

                while (!Thread.currentThread().isInterrupted()) {

                    try {

                        final String read = input.readLine();
                        if (read == null) {
                            break;
                        }

                        try {
                            JSONObject json = new JSONObject(read);
                            String data = json.getString("data");
                            PrinterService.doPrint(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }

        }
    }
}
