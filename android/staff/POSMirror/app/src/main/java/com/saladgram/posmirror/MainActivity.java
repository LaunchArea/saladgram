package com.saladgram.posmirror;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    private List<SaleMirrorItem> mSaleList = new LinkedList<SaleMirrorItem>();
    private RecyclerView mSaleRecyclerView;
    private RecyclerViewClickListener mSaleClickListener;
    private SaleMirrorItemAdapter mSaleAdapter;
    private int mCashReceived = 0;
    private double mDiscount = 5;
    private int mPoint = 0;
    private int mSubTotal;
    private int mTotal;
    private Server mServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mServer = new Server();

        initializeUI();
        initListeners();

        refreshUI();

        mServer.onCreate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mServer.onStop();
    }

    private void initListeners() {

        mSaleClickListener = new RecyclerViewClickListener() {
            @Override
            public void onItemClick(View view, int position) {
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        };
    }

    private void refreshUI() {
        int change = mCashReceived > 0 ? -1 * (mTotal - mCashReceived) : 0;

        ((TextView) findViewById(R.id.subtotal)).setText(String.valueOf(mSubTotal) + "원");
        ((TextView) findViewById(R.id.discount)).setText(String.valueOf(mDiscount) + "%");
        ((TextView) findViewById(R.id.point)).setText(String.valueOf(mPoint));
        ((TextView) findViewById(R.id.total)).setText(String.valueOf(mTotal) + "원");
        ((TextView) findViewById(R.id.cash_received)).setText(String.valueOf(mCashReceived) + "원");
        ((TextView) findViewById(R.id.change)).setText(String.valueOf(change) + "원");

        mSaleAdapter.setList(mSaleList);
        mSaleAdapter.notifyDataSetChanged();
    }

    public Activity getActivity() {
        return this;
    }

    private void initializeUI() {

        mSaleRecyclerView = (RecyclerView) getActivity().findViewById(R.id.sale_list);
        mSaleRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mSaleAdapter = new SaleMirrorItemAdapter(getActivity(), mSaleClickListener);
        mSaleAdapter.setList(mSaleList);

        mSaleRecyclerView.setAdapter(mSaleAdapter);
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
                        getActivity().finish();
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

                        String read = input.readLine();
                        if (read == null) {
                            break;
                        }

                        updateConversationHandler.post(new updateUIThread(read));

                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }

        }

        class updateUIThread implements Runnable {
            private String msg;

            public updateUIThread(String str) {
                this.msg = str;
            }

            @Override
            public void run() {
                applyData(msg);
            }
        }
    }

    private void applyData(String data) {
        try {
            mSaleList.clear();
            JSONObject json = new JSONObject(data);
            mCashReceived = json.getInt("mCashReceived");
            mDiscount = json.getInt("mDiscount");
            mPoint = json.getInt("mPoint");
            mSubTotal = json.getInt("mSubTotal");
            mTotal = json.getInt("mTotal");

            JSONArray arr = json.getJSONArray("saleMirrorItemList");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject each = arr.getJSONObject(i);
                SaleMirrorItem item = new SaleMirrorItem();
                item.amount = each.getInt("amount");
                item.name = each.getString("name");
                item.price = each.getInt("price");
                item.quantity = each.getInt("quantity");
                item.takeout = each.getBoolean("takeout");
                mSaleList.add(item);
            }
            refreshUI();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
