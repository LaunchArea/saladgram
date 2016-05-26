package com.saladgram.pos;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {


    private static final int BUTTONS_PER_ROW = 6;
    private RecyclerView mMenuRecyclerView;
    private MenuAdapter mMenuAdapter;
    private List<MenuItem> mMenuList = new LinkedList<MenuItem>();
    private List<Integer> mSectionPositions = new LinkedList<Integer>();
    private List<SaleItem> mSaleList = new LinkedList<SaleItem>();
    private RecyclerView mSaleRecyclerView;
    private RecyclerViewClickListener mMenuClickListener;
    private RecyclerViewClickListener mSaleClickListener;
    private SaleAdapter mSaleAdapter;
    private int mCashReceived = 0;
    private double mDiscount = 5;
    private int mPoint = 0;
    private int mSubTotal;
    private int mTotal;

    enum PaymentType {CARD, CASH}
    private PaymentType mPaymentType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initListeners();
        new MenuListFetchTask(getActivity()).execute();

        findViewById(R.id.complete).setEnabled(false);
        refreshSaleAmount();
    }

    void addSaleItem(SaleItem saleItem) {
        if(saleItem.menuItem.type == MenuItem.Type.SALAD) {
            mSaleList.add(saleItem);
        } else {
            boolean quantityIncreased = false;
            for(SaleItem each : mSaleList) {
                if(each.menuItem.data.hashCode() == saleItem.menuItem.data.hashCode()) {
                    each.quantity++;
                    quantityIncreased = true;
                    break;
                }
            }
            if (!quantityIncreased) {
                mSaleList.add(saleItem);
            }
        }
        mSaleAdapter.notifyDataSetChanged();
        refreshSaleAmount();
        mSaleRecyclerView.scrollToPosition(mSaleList.size()-1);
    }


    private void initListeners() {
        mMenuClickListener = new RecyclerViewClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MenuItem item = mMenuList.get(getItemPosition(position));
                SaleItem saleItem = new SaleItem();
                saleItem.menuItem = item;
                if (item.checkWeight) {
                    checkWeight(saleItem);
                } else if (item.checkToGo) {
                    checkToGo(saleItem);
                } else {
                    addSaleItem(saleItem);
                }
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
                setPoint();
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

    private void setPoint() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("조회했다 치고, 사용할 포인트를 입력");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Put actions for OK button here
                if(input.getText().length() > 0) {
                    mPoint = Integer.parseInt(input.getText().toString());
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


    private void checkToGo(final SaleItem saleItem) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("드시고가세요?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                addSaleItem(saleItem);
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                saleItem.takeout = true;
                addSaleItem(saleItem);
            }
        });
        alert.show();
    }

    private void checkWeight(final SaleItem saleItem) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("무게 입력 (단위:g)");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);
        alert.setView(input);
        alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Put actions for OK button here
                if(input.getText().length() > 0) {
                    saleItem.amount = Integer.parseInt(input.getText().toString());
                    addSaleItem(saleItem);
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    private void placeOrder() {
        PlaceOrderTask task = new PlaceOrderTask(getActivity(), mSaleList) {
            @Override
            protected void onPostExecute(Integer result) {
                super.onPostExecute(result);
                Log.d("yns", ""+result);
                if(result == 200) {
                    mSaleList.clear();
                    mSaleAdapter.notifyDataSetChanged();
                    findViewById(R.id.complete).setEnabled(false);
                    mCashReceived = 0;
                    mDiscount = 5;
                    mPoint = 0;
                    mPaymentType = null;
                    refreshSaleAmount();
                } else {
                    Toast.makeText(getActivity(),""+result,Toast.LENGTH_SHORT).show();
                }
            }
        };
        task.execute();
    }

    private void refreshSaleAmount() {
        findViewById(R.id.pay_card).setEnabled(mSaleList.size()>0);
        findViewById(R.id.pay_cash).setEnabled(mSaleList.size()>0);
        findViewById(R.id.set_discount).setEnabled(mSaleList.size()>0);
        findViewById(R.id.set_point).setEnabled(mSaleList.size()>0);


        mSubTotal = 0;
        mTotal = 0;
        int change = -1;

        for(SaleItem each : mSaleList) {
            mSubTotal += each.getPrice();
        }

        mTotal = (int) (mSubTotal * ( 1 - mDiscount/100));
        mTotal -= mPoint;
        change = mCashReceived > 0 ? -1 * (mTotal - mCashReceived):0;

        ((TextView)findViewById(R.id.subtotal)).setText(String.valueOf(mSubTotal) + "원");
        ((TextView)findViewById(R.id.discount)).setText(String.valueOf(mDiscount) + "%");
        ((TextView)findViewById(R.id.point)).setText(String.valueOf(mPoint));
        ((TextView)findViewById(R.id.total)).setText(String.valueOf(mTotal) + "원");
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

    class PlaceOrderTask extends ProgressAsyncTask<Void,Void,Integer> {

        private final List<SaleItem> mItems;

        public PlaceOrderTask(Context context, List<SaleItem> items) {
            super(context);
            mItems = items;
        }

        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        @Override
        protected Integer doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            try {
                JSONObject signInJson = new JSONObject();

                signInJson.put("id","real3333");
                signInJson.put("password", "1234");
                RequestBody body = RequestBody.create(JSON, signInJson.toString());

                String url = "https://saladgram.com/api/sign_in.php";
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                Response response = null;
                response = client.newCall(request).execute();
                if (response.code() != 200) {
                    return response.code();
                }
                String jwt = new JSONObject(response.body().string()).getString("jwt");
                JSONObject orderJson = new JSONObject();

                orderJson.put("type", 1);
                orderJson.put("id", "real3333");
                orderJson.put("total_price", mSubTotal);
                orderJson.put("discount", (int)mDiscount);
                orderJson.put("reward_use", mPoint);
                orderJson.put("payment_type", mPaymentType == PaymentType.CARD ? 1 : 2);
                orderJson.put("order_time", System.currentTimeMillis()/1000);
                orderJson.put("reservation_time", 0);

                JSONArray items = new JSONArray();
                for(SaleItem each : mItems) {
                    JSONObject item = new JSONObject();
                    switch(each.menuItem.type) {
                        case SALAD:
                            item.put("type", 1);
                            item.put("salad_items", each.menuItem.data.get("salad_items"));
                            break;
                        case SOUP:
                            item.put("type",2);
                            break;
                        case OTHER:
                            item.put("type",3);
                            break;
                        case BEVERAGE:
                            item.put("type",4);
                            break;
                    }
                    item.put("item_id", each.menuItem.data.get("item_id"));
                    item.put("quantity", each.quantity);
                    item.put("price",each.menuItem.data.get("price"));
                    item.put("calories", each.menuItem.data.get("calories"));

                    items.put(item);
                }
                orderJson.put("order_items", items);

                body = RequestBody.create(JSON, orderJson.toString());

                url = "https://www.saladgram.com/api/place_order.php";
                request = new Request.Builder()
                        .header("jwt",jwt)
                        .url(url)
                        .post(body)
                        .build();

                response = null;
                response = client.newCall(request).execute();
                Log.d("yns",response.body().string());
                return response.code();

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return -1;
        }
    }


    class MenuListFetchTask extends ProgressAsyncTask<Void,Void,String> {

        public MenuListFetchTask(Context context) {
            super(context);
        }

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
//                if (each.containsKey("hide") && ((Double)each.get("hide")).intValue() == 1) {
//                    continue;
//                }
                MenuItem item = new MenuItem();
                item.data = each;
                item.name = (String) each.get("name");
                item.price = each.containsKey("price") ? ((Double)each.get("price")).intValue() : -1;
                item.available = ((Double)each.get("available")).intValue() == 1;
                item.type = type;
                item.amount = (String) each.get("amount");

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
        mMenuRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), BUTTONS_PER_ROW));

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

        MenuItem.Type currentType = null;
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
