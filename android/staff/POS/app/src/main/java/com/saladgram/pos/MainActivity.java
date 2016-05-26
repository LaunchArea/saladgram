package com.saladgram.pos;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.*;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {


    private RecyclerView mMenuRecyclerView;
    private MenuAdapter mMenuAdapter;
    private List<MenuItem> mMenuList = new LinkedList<MenuItem>();
    private List<Integer> mSectionPositions = new LinkedList<Integer>();
    private List<MenuItem> mSaleList = new LinkedList<MenuItem>();
    private RecyclerView mSaleRecyclerView;
    private RecyclerViewClickListener mMenuClickListener;
    private RecyclerViewClickListener mSaleClickListener;
    private SaleAdapter mSaleAdapter;
    private int mCashReceived = 0;
    private double mDiscount = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initListeners();
        new MenuListFetchTask().execute();

        findViewById(R.id.set_point).setEnabled(false);
        findViewById(R.id.complete).setEnabled(false);
        refreshSaleAmount();
    }

    private void initListeners() {
        mMenuClickListener = new RecyclerViewClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MenuItem item = mMenuList.get(getItemPosition(position));
                if (item.checkToGo) {

                } else if (item.checkWeight) {

                } else {
                    mSaleList.add(item);
                    mSaleAdapter.notifyDataSetChanged();
                }
                refreshSaleAmount();
                mSaleRecyclerView.scrollToPosition(mSaleList.size()-1);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                MenuItem item = mMenuList.get(getItemPosition(position));

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                try {
                    builder.setMessage((new JSONObject(item.data)).toString(2));
                } catch (JSONException e) {
                    builder.setMessage(e.getMessage());
                    e.printStackTrace();
                }
                builder.show();
            }
        };

        mSaleClickListener = new RecyclerViewClickListener() {
            @Override
            public void onItemClick(View view, int position) {
            }

            @Override
            public void onItemLongClick(View view, int position) {
                mSaleList.remove(position);
                mSaleAdapter.notifyDataSetChanged();
                refreshSaleAmount();
            }
        };


        findViewById(R.id.pay_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptCard();
            }
        });
        findViewById(R.id.pay_cash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptCash();
            }
        });
        findViewById(R.id.set_point).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
            }
        });
        findViewById(R.id.set_discount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDiscount();
            }
        });
        findViewById(R.id.complete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeOrder();
            }
        });
    }

    private void acceptCard() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("카드결제 진행");
        alert.setPositiveButton("완료", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                findViewById(R.id.complete).setEnabled(true);

                refreshSaleAmount();

            }
        });
        alert.show();
    }

    private void acceptCash() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("현금 입력 (단위:원)");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Put actions for OK button here
                if(input.getText().length() > 0) {
                    mCashReceived = Integer.parseInt(input.getText().toString());
                    findViewById(R.id.complete).setEnabled(true);

                    refreshSaleAmount();
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Put actions for CANCEL button here, or leave in blank
            }
        });
        alert.show();
    }

    private void setDiscount() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("할인율 입력 (단위:%)");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Put actions for OK button here
                if(input.getText().length() > 0) {
                    mDiscount = Double.parseDouble(input.getText().toString());
                    refreshSaleAmount();
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Put actions for CANCEL button here, or leave in blank
            }
        });
        alert.show();
    }

    private void placeOrder() {
        mSaleList.clear();
        mSaleAdapter.notifyDataSetChanged();
        findViewById(R.id.complete).setEnabled(false);
        mCashReceived = 0;
        mDiscount = 5;
        refreshSaleAmount();
    }

    private void refreshSaleAmount() {
        findViewById(R.id.pay_card).setEnabled(mSaleList.size()>0);
        findViewById(R.id.pay_cash).setEnabled(mSaleList.size()>0);
        findViewById(R.id.set_discount).setEnabled(mSaleList.size()>0);


        int subtotal = 0;
        int point = 0;
        int total = 0;
        int change = -1;

        for(MenuItem each : mSaleList) {
            subtotal += each.price;
        }

        total = (int) (subtotal * ( 1 - mDiscount/100));
        change = mCashReceived > 0 ? -1 * (total - mCashReceived):0;

        ((TextView)findViewById(R.id.subtotal)).setText(String.valueOf(subtotal) + "원");
        ((TextView)findViewById(R.id.discount)).setText(String.valueOf(mDiscount) + "%");
        ((TextView)findViewById(R.id.point)).setText(String.valueOf(point));
        ((TextView)findViewById(R.id.total)).setText(String.valueOf(total) + "원");
        ((TextView)findViewById(R.id.cash_received)).setText(String.valueOf(mCashReceived) + "원");
        ((TextView)findViewById(R.id.change)).setText(String.valueOf(change) + "원");

    }

    public Activity getActivity() {
        return this;
    }

    private int getItemPosition(int adapterPosition) {
        int position = adapterPosition;

        for(Integer i : mSectionPositions) {
            if(position > i) {
                position--;
            }
        }
        return position;
    }

    class MenuListFetchTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();


            String url = "https://saladgram.com/api/menu_list.php";
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                HashMap<String, ArrayList<HashMap<String, Object>>> map = new Gson().fromJson(result, new TypeToken<HashMap<String, ArrayList<HashMap<String, Object>>>>() {
                }.getType());
                buildMenuList(map.get("salads"), MenuItem.Type.SALAD);
                buildMenuList(map.get("soups"), MenuItem.Type.SOUP);
                buildMenuList(map.get("others"), MenuItem.Type.OTHER);
                buildMenuList(map.get("beverages"), MenuItem.Type.BEVERAGE);

                initializeUI();
            }
        }

        private void buildMenuList(ArrayList<HashMap<String, Object>> list, MenuItem.Type type) {
            for(HashMap<String, Object> each : list) {
                MenuItem item = new MenuItem();
                item.data = each;
                item.name = (String) each.get("name");
                item.price = each.containsKey("price") ? ((Double)each.get("price")).intValue() : -1;
                item.available = ((Double)each.get("available")).intValue() == 1;
                item.type = type;

                item.checkToGo = (type == MenuItem.Type.SALAD);
                item.checkWeight = (item.price == -1);
                mMenuList.add(item);
            }
        }
    }

    private void initializeUI() {
        findViewById(R.id.content_layout).setVisibility(View.VISIBLE);

        //Your RecyclerView
        mMenuRecyclerView = (RecyclerView) getActivity().findViewById(R.id.menu_list);
        mMenuRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 5));

        //Your RecyclerView.Adapter
        mMenuAdapter = new MenuAdapter(getActivity(), mMenuClickListener);
        mMenuAdapter.setList(mMenuList);

        mSaleRecyclerView = (RecyclerView) getActivity().findViewById(R.id.sale_list);
        mSaleRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mSaleAdapter = new SaleAdapter(getActivity(), mSaleClickListener);
        mSaleAdapter.setList(mSaleList);

        mSaleRecyclerView.setAdapter(mSaleAdapter);

        //This is the code to provide a sectioned grid
        List<SectionedGridRecyclerViewAdapter.Section> sections =
                new ArrayList<SectionedGridRecyclerViewAdapter.Section>();

        MenuItem.Type currentType = MenuItem.Type.NONE;
        for(int i = 0; i < mMenuList.size(); i++) {
            MenuItem now = mMenuList.get(i);
            if(now.type != currentType) {
                sections.add(new SectionedGridRecyclerViewAdapter.Section(i, now.type.name()));
                mSectionPositions.add(i);
            }
            currentType = now.type;
        }

        //Add your adapter to the sectionAdapter
        SectionedGridRecyclerViewAdapter.Section[] dummy = new SectionedGridRecyclerViewAdapter.Section[sections.size()];
        SectionedGridRecyclerViewAdapter mSectionedAdapter = new
                SectionedGridRecyclerViewAdapter(getActivity(), R.layout.menu_section, R.id.section_text, mMenuRecyclerView, mMenuAdapter);
        mSectionedAdapter.setSections(sections.toArray(dummy));

        //Apply this adapter to the RecyclerView
        mMenuRecyclerView.setAdapter(mSectionedAdapter);
    }
}
