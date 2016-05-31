package com.saladgram.ready;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == Service.ACTION_FETCH_FAILED) {
                Toast.makeText(context, intent.getStringExtra("reason"), Toast.LENGTH_SHORT).show();
            } else if (intent.getAction() == Service.ACTION_FETCH_DONE) {
                refreshUI();
            }
        }
    };
    private RecyclerView lvOrders;

    private RecyclerViewClickListener mOrderClickListener;
    private OrderAdapter mOrderAdapter;

    private List<Order> mOrderList;
    private HashSet<Integer> mSelectedItems = new HashSet<>();
    private HashSet<String> mDeliverers = new HashSet<>(); //deliverer_id

    private TabLayout addrTabLayout;
    private TabLayout typeTabLayout;
    private Button btnShipping;
    private Button btnReset;
    private Order.OrderType mSelectedOrderType;
    private String mSelectedAddr;
    private String[] addrList = {"All", "한화", "푸르지오", "아이파크"};
    private List<String> typeList = new LinkedList<>();
    private Map<String, Integer> mAddrCounter = new HashMap<>();
    private Map<String, Integer> mTypeCounter = new HashMap<>();
    private Button btnDeliverer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initConstants();
        initView();
        initControl();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Service.ACTION_FETCH_FAILED);
        filter.addAction(Service.ACTION_FETCH_DONE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }

    private void initConstants() {
        typeList.add("All");
        Order.OrderType[] values = Order.OrderType.values();
        for (int i = 0; i < values.length; i++) {
            typeList.add(values[i].name());
        }
    }

    private void initView() {
        addrTabLayout = (TabLayout) findViewById(R.id.addr_tabs);
        for(int i = 0; i < addrList.length; i++) {
            TabLayout.Tab tab = addrTabLayout.newTab();
            tab.setTag(addrList[i]);
            tab.setText(addrList[i]);
            addrTabLayout.addTab(tab);
        }

        typeTabLayout = (TabLayout) findViewById(R.id.type_tabs);
        for(int i = 0; i < typeList.size(); i++) {
            String t = typeList.get(i);
            TabLayout.Tab tab = typeTabLayout.newTab();
            typeTabLayout.addTab(tab.setText(t).setTag(t));
        }

        lvOrders = (RecyclerView) findViewById(R.id.order_list);
        btnShipping = (Button) findViewById(R.id.shipping_button);
        btnReset = (Button) findViewById(R.id.reset_button);
        btnDeliverer = (Button) findViewById(R.id.deliverer_add_button);
    }

    private void initControl() {
        btnShipping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postShipping();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedItems.clear();
                refreshUI();
            }
        });
        btnDeliverer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDeliverer();
            }
        });

        mOrderClickListener = new RecyclerViewClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                int id = mOrderList.get(position).id;
                if(mSelectedItems.contains(id)) {
                    mSelectedItems.remove(id);
                } else {
                    mSelectedItems.add(id);
                }

                refreshUI();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                Order item = mOrderList.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                try {
                    builder.setMessage(item.json.toString(2));
                } catch (JSONException e) {
                    builder.setMessage(e.getMessage());
                    e.printStackTrace();
                }
                builder.show();
            }
        };

        addrTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tag = (String) tab.getTag();
                if (tag == "All") {
                    mSelectedAddr = null;
                } else {
                    mSelectedAddr = tag;
                }

                refreshUI();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        typeTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String name = (String) tab.getTag();
                if (name == "All") {
                    mSelectedOrderType = null;
                } else {
                    mSelectedOrderType = Order.OrderType.valueOf(name);
                }
                refreshUI();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        lvOrders.setLayoutManager(new LinearLayoutManager(getActivity()));

        mOrderAdapter = new OrderAdapter(this,mOrderClickListener);
        lvOrders.setAdapter(mOrderAdapter);
    }

    private void addDeliverer() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("배달자 추가");
        final EditText input = new EditText(this);
        alert.setView(input);
        alert.setPositiveButton("저장", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(input.getText().length() > 0) {
                    mDeliverers.add(input.getText().toString());
                }
            }
        });
        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    private Context getActivity() {
        return this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    private void refreshUI() {
        mTypeCounter.clear();
        mAddrCounter.clear();

        LinkedList<Order> fullList = new LinkedList<>(Service.orderList);
        LinkedList<Order> filteredList = new LinkedList<>();
        for(Order order : fullList) {
            String addr = order.addr;
            for (int i = 0; i < addrList.length; i++) {
                if(addr != null && addr.contains(addrList[i])) {
                    if(!mAddrCounter.containsKey(addrList[i])) {
                        mAddrCounter.put(addrList[i],0);
                    }
                    mAddrCounter.put(addrList[i],mAddrCounter.get(addrList[i]) + 1);
                }
            }

            if (mSelectedAddr == null) {
                if (mSelectedOrderType == null) {
                    filteredList.add(order);
                } else {
                    if (order.orderType == mSelectedOrderType) {
                        filteredList.add(order);
                    }
                }
                String name = order.orderType.name();
                if(!mTypeCounter.containsKey(name)) {
                    mTypeCounter.put(name,0);
                }
                mTypeCounter.put(name,mTypeCounter.get(name) + 1);
            } else {
                if (order.addr != null && order.addr.contains(mSelectedAddr)) {
                    if (mSelectedOrderType == null) {
                        filteredList.add(order);
                    } else {
                        if (order.orderType == mSelectedOrderType) {
                            filteredList.add(order);
                        }
                    }
                    String name = order.orderType.name();
                    if(!mTypeCounter.containsKey(name)) {
                        mTypeCounter.put(name,0);
                    }
                    mTypeCounter.put(name,mTypeCounter.get(name) + 1);
                }
            }
        }
        int tcSum = 0;
        for(int v : mTypeCounter.values()) {
            tcSum += v;
        }
        mTypeCounter.put("All",tcSum);
        mAddrCounter.put("All",fullList.size());

        mOrderList = new LinkedList<>(filteredList);
        mOrderAdapter.setList(mOrderList);
        mOrderAdapter.setSelectedIdList(mSelectedItems);
        mOrderAdapter.notifyDataSetChanged();

        btnShipping.setEnabled(mSelectedItems.size() > 0);
        btnShipping.setText("SHIP " + mSelectedItems.size());

        for(int i = 0; i < addrTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = addrTabLayout.getTabAt(i);
            String key = addrList[i];
            tab.setText(key + (mAddrCounter.containsKey(key) ? "\n" + mAddrCounter.get(key): ""));
        }
        for(int i = 0; i < typeTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = typeTabLayout.getTabAt(i);
            String key = typeList.get(i);
            tab.setText(key + (mTypeCounter.containsKey(key) ? "\n" + mTypeCounter.get(key): ""));
        }
    }

    private void postShipping() {
        if(mDeliverers.size() == 0) {
            addDeliverer();
            return;
        }
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        builderSingle.setTitle("배달자 선택");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.select_dialog_singlechoice);
        for (String each : mDeliverers) {
            arrayAdapter.add(each);
        }

        builderSingle.setNegativeButton(
                "배달자 불필요",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mSelectedItems.clear();
                        refreshUI();
                    }
                });

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);
                        Toast.makeText(getActivity(),"SHIP " + mSelectedItems.size() + " by " + strName,Toast.LENGTH_SHORT).show();
                        mSelectedItems.clear();
                        refreshUI();
                    }
                });
        builderSingle.show();
    }

}
