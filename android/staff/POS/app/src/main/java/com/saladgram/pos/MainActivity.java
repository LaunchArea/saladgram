package com.saladgram.pos;

import android.app.Activity;
import android.content.Context;
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
import android.text.format.DateUtils;
import android.util.Log;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.saladgram.model.*;
import com.saladgram.model.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {


    private static final int BUTTONS_PER_ROW = 4;
    private static final int WEIGHT_EGG = 50;
    private RecyclerView mMenuRecyclerView;
    private MenuAdapter mMenuAdapter;
    private List<com.saladgram.model.MenuItem> mMenuList = new LinkedList<>();
    private List<Integer> mSectionPositions = new LinkedList<Integer>();
    private List<SaleItem> mSaleList = new LinkedList<SaleItem>();
    private RecyclerView mSaleRecyclerView;
    private RecyclerViewClickListener mMenuClickListener;
    private RecyclerViewClickListener mSaleClickListener;
    private SaleAdapter mSaleAdapter;
    private int mCashReceived = 0;
    private double mDiscount = 0;
    private int mPoint = 0;
    private int mSubTotal;
    private int mTotal;
    private boolean mToGo = true;
    private static String mMirrorIP = "192.168.0.30";

    private Order.PaymentType mPaymentType;
    private View.OnClickListener mSelfClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onClickSelf(v);
        }
    };

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
                if(each.isSameKind(saleItem)) {
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
                com.saladgram.model.MenuItem item = mMenuList.get(getItemPosition(position));
                SaleItem saleItem = new SaleItem();
                saleItem.menuItem = item;
                if (item.checkWeight) {
                    checkWeight(saleItem);
                } else if (item.checkToGo) {
                    checkToGo(saleItem);
                } else if (item.checkSize) {
                    checkSize(saleItem);
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
            public void onItemClick(View view, int position) {checkAddedItem(mSaleList.get(position));
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
        findViewById(R.id.togo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mToGo = !mToGo;
                refreshSaleAmount();
            }
        });
        findViewById(R.id.togo).setVisibility(View.GONE);
        findViewById(R.id.mirror).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mirrorSetup();
            }
        });
        findViewById(R.id.self_salad).setOnClickListener(mSelfClickListener);
        findViewById(R.id.self_soup).setOnClickListener(mSelfClickListener);
        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
                refreshSaleAmount();
            }
        });

        findViewById(R.id.pickup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePickUp();
            }
        });
    }

    private void checkAddedItem(final SaleItem saleItem) {
        if(saleItem.menuItem.type == MenuItem.Type.SALAD || saleItem.menuItem.type == MenuItem.Type.SELF_SALAD) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("샐러드볼에 추가된 아이템");
            alert.setPositiveButton("계란", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    checkEggAmount(saleItem);
                }
            });
            alert.show();
        }
    }

    private void checkEggAmount(final SaleItem saleItem) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("몇개의 계란이 추가되었나요?");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);
        input.setText("1");
        alert.setView(input);
        alert.setPositiveButton("차감", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Put actions for OK button here
                if(input.getText().length() > 0) {
                    int numEggs = Integer.parseInt(input.getText().toString());
                    saleItem.amount = saleItem.amount - WEIGHT_EGG * numEggs;
                    for(MenuItem each : mMenuList) {
                        if (each.name.equals("삶은달걀")) {
                            for (int i =0; i < numEggs; i++) {
                                SaleItem item = new SaleItem();
                                item.menuItem = each;
                                addSaleItem(item);
                            }
                            break;
                        }
                    }
                    mSaleAdapter.notifyDataSetChanged();
                    refreshSaleAmount();
                }
            }
        });
        alert.show();
    }

    private void onClickSelf(View v) {
        HashMap<String, Object> data = new HashMap<String, Object>();
        MenuItem.Type type;
        if (v.getId() == R.id.self_salad) {
            data.put("name", "샐러드바");
            data.put("price", new Double(-1));
            type = MenuItem.Type.SELF_SALAD;
        } else {
            data.put("name", "스프");
            data.put("price", new Double(2000));
            type = MenuItem.Type.SELF_SOUP;
        }
        data.put("calorie", new Double(0));
        data.put("item_id", new Double(0));
        MenuItem item = new MenuItem();
        item.data = data;
        item.name = (String) data.get("name");
        item.price = ((Double)data.get("price")).intValue();
        item.available = true;
        item.type = type;
        item.amount = "";
        item.checkWeight = true;
        item.checkSize = false;
        item.checkToGo = false;

        SaleItem saleItem = new SaleItem();
        saleItem.menuItem = item;

        checkWeight(saleItem);
    }

    private void mirrorSetup() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("mirror ip");
        final EditText input = new EditText(this);
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(input.getText().length() > 0) {
                    mMirrorIP = input.getText().toString();
                }
            }
        });
        alert.show();
    }

    private void acceptCard() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("카드결제 진행");
        alert.setPositiveButton("완료", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mPaymentType = Order.PaymentType.CARD;
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
        alert.setPositiveButton("현금", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Put actions for OK button here
                if(input.getText().length() > 0) {
                    mPaymentType = Order.PaymentType.CASH;
                    mCashReceived = Integer.parseInt(input.getText().toString());
                    findViewById(R.id.complete).setEnabled(true);
                    refreshSaleAmount();
                }
            }
        });
        alert.setNegativeButton("현금영수증", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Put actions for OK button here
                if(input.getText().length() > 0) {
                    mPaymentType = Order.PaymentType.CASH_RECEIPT;
                    mCashReceived = Integer.parseInt(input.getText().toString());
                    findViewById(R.id.complete).setEnabled(true);
                    refreshSaleAmount();
                }
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
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                saleItem.takeout = false;
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
        alert.setPositiveButton("포장", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Put actions for OK button here
                if(input.getText().length() > 0) {
                    int amount = Integer.parseInt(input.getText().toString());
                    if (saleItem.menuItem.type == MenuItem.Type.SELF_SALAD) {
                        saleItem.amount = amount - 20;
                    } else if(saleItem.menuItem.type == MenuItem.Type.SELF_SOUP) {
                        saleItem.amount = amount - 10;
                    } else {
                        saleItem.amount = amount;
                    }
                    saleItem.takeout = true;
                    addSaleItem(saleItem);
                }
            }
        });
        alert.setNegativeButton("Dine in", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(input.getText().length() > 0) {
                    int amount = Integer.parseInt(input.getText().toString());
                    if (saleItem.menuItem.type == MenuItem.Type.SELF_SALAD) {
                        saleItem.amount = amount - 276;
                    } else if(saleItem.menuItem.type == MenuItem.Type.SELF_SOUP) {
                        saleItem.amount = amount - 301;
                    } else {
                        saleItem.amount = amount;
                    }
                    saleItem.takeout = false;
                    addSaleItem(saleItem);
                }
            }
        });
        alert.show();
    }

    private void checkSize(final SaleItem saleItem) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        builderSingle.setTitle("스프 크기 및 TOGO");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.select_dialog_singlechoice);
            arrayAdapter.add("small / togo");
            arrayAdapter.add("small / dine_in");
            arrayAdapter.add("large / togo");
            arrayAdapter.add("large / dine_in");

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which) {
                            case 0:
                                saleItem.amount_type = 1;
                                saleItem.amount = saleItem.menuItem.getAmount(1);
                                saleItem.takeout = true;
                                break;
                            case 1:
                                saleItem.amount_type = 1;
                                saleItem.amount = saleItem.menuItem.getAmount(1);
                                saleItem.takeout = false;
                                break;
                            case 2:
                                saleItem.amount_type = 2;
                                saleItem.amount = saleItem.menuItem.getAmount(2);
                                saleItem.takeout = true;
                                break;
                            case 3:
                                saleItem.amount_type = 2;
                                saleItem.amount = saleItem.menuItem.getAmount(2);
                                saleItem.takeout = false;
                                break;
                        }
                        addSaleItem(saleItem);
                    }
                });
        builderSingle.show();
    }

    private void placeOrder() {
        PlaceOrderTask task = new PlaceOrderTask(getActivity(), mSaleList) {
            @Override
            protected void onPostExecute(Integer result) {
                super.onPostExecute(result);
                Log.d("yns", ""+result);
                if(result == 200) {
                    reset();
                    refreshSaleAmount();
                } else {
                    Toast.makeText(getActivity(),""+result,Toast.LENGTH_SHORT).show();
                }
            }
        };
        task.execute();
    }

    private void reset() {
        mSaleList.clear();
        mSaleAdapter.notifyDataSetChanged();
        findViewById(R.id.complete).setEnabled(false);
        mCashReceived = 0;
        mDiscount = 0;
        mPoint = 0;
        mPaymentType = null;
        mToGo = true;
    }

    private void refreshSaleAmount() {
        findViewById(R.id.pay_card).setEnabled(mSaleList.size()>0);
        findViewById(R.id.pay_cash).setEnabled(mSaleList.size()>0);
        findViewById(R.id.set_discount).setEnabled(mSaleList.size()>0);
        findViewById(R.id.set_point).setEnabled(mSaleList.size()>0);
        findViewById(R.id.togo).setEnabled(mSaleList.size()>0);


        mSubTotal = 0;
        mTotal = 0;
        int change = -1;

        for(SaleItem each : mSaleList) {
            mSubTotal += each.getTotalPrice();
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
        ((TextView)findViewById(R.id.togo)).setText(mToGo?"togo? O" : "TOGO? X");

        sendToMirror();
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

                signInJson.put("id","saladgram");
                signInJson.put("password", "saladgramadmin1!");
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

                HashMap<String, Object> m = new HashMap<>();

//                m.put("order_type", mToGo ? 5 : 4);
                m.put("order_type", Order.OrderType.TAKE_OUT.ordinal() + 1);
                m.put("id", "saladgram");
                m.put("total_price", mSubTotal);
                m.put("actual_price", mTotal);
                m.put("discount", (int)mDiscount);
                m.put("reward_use", mPoint);
                m.put("payment_type", mPaymentType.ordinal() + 1);
//                m.put("order_time", System.currentTimeMillis()/1000);
//                m.put("reservation_time", 0);

                JSONArray jArray = new JSONArray();

                for(SaleItem each : mItems) {
                    JSONObject item = new JSONObject();
                    switch(each.menuItem.type) {
                        case SALAD:
                            item.put("order_item_type", 1);
                            JSONArray si = new JSONArray();
                            //for(Object o : (ArrayList)each.menuItem.data.get("salad_items")) {
                            //    si.put(new JSONObject((Map) o));
                            //}
                            item.put("salad_items", each.menuItem.jsonSaladItems);
                            item.put("package_type", each.takeout ? OrderItem.PackageType.TAKE_OUT.ordinal() + 1 : OrderItem.PackageType.DINE_IN.ordinal() + 1);
                            break;
                        case SOUP:
                            item.put("order_item_type",2);
                            item.put("amount_type", each.amount_type);
                            item.put("package_type", each.takeout ? OrderItem.PackageType.TAKE_OUT.ordinal() + 1 : OrderItem.PackageType.DINE_IN.ordinal() + 1);
                            break;
                        case OTHER:
                            item.put("order_item_type",3);
                            break;
                        case BEVERAGE:
                            item.put("order_item_type",4);
                            break;
                        case SELF_SALAD:
                            item.put("order_item_type",5);
                            item.put("package_type", each.takeout ? OrderItem.PackageType.TAKE_OUT.ordinal() + 1 : OrderItem.PackageType.DINE_IN.ordinal() + 1);
                            break;
                        case SELF_SOUP:
                            item.put("order_item_type",6);
                            item.put("package_type", each.takeout ? OrderItem.PackageType.TAKE_OUT.ordinal() + 1 : OrderItem.PackageType.DINE_IN.ordinal() + 1);
                            break;
                    }
                    item.put("item_id", ((Double)each.menuItem.data.get("item_id")).intValue());
                    item.put("quantity", each.quantity);
                    item.put("price",each.getPricePerEach());
                    item.put("calorie", each.getCaloriePerEach());

                    jArray.put(item);
                }
                boolean allSelf = true;
                for(SaleItem each : mItems) {
                    switch(each.menuItem.type) {
                        case SELF_SALAD:
                        case SELF_SOUP:
                            break;
                        default:
                            allSelf = false;
                            break;
                    }
                }
                if(allSelf) {
                    m.put("status", Order.Status.DONE.ordinal() + 1);
                }

                JSONObject orderJson = new JSONObject(m);
                orderJson.put("order_items", jArray);
                body = RequestBody.create(JSON, orderJson.toString());
                Log.d("yns", orderJson.toString(2));
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
                setJsonSaladItems(result);
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

                item.checkSize = (type == MenuItem.Type.SOUP);
                item.checkToGo = (type == MenuItem.Type.SALAD);
                if (item.price != -1) {
                    mMenuList.add(item);
                }
            }
        }

        private void setJsonSaladItems(String result) {
            try {
                JSONObject root = new JSONObject(result);
                JSONArray arr = root.getJSONArray("salads");
                for(int i = 0; i < arr.length(); i++) {
                    JSONObject salad = arr.getJSONObject(i);
                    for(MenuItem item : mMenuList) {
                        if (item.name.equals(salad.getString("name"))) {
                            item.jsonSaladItems = salad.getJSONArray("salad_items");
                            break;
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
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

    private void sendToMirror() {
        if (mMirrorIP == null) {
            return;
        }
        HashMap<String,Object> map = new HashMap<>();
        map.put("mCashReceived", mCashReceived);
        map.put("mDiscount", mDiscount);
        map.put("mPoint", mPoint);
        map.put("mSubTotal", mSubTotal);
        map.put("mTotal", mTotal);

        JSONObject jMap = new JSONObject(map);
        JSONArray jArray = new JSONArray();
        try {
            for (SaleItem each : mSaleList) {
            JSONObject jo = new JSONObject();
            jo.put("name", each.menuItem.name);
            jo.put("price", each.getTotalPrice());
            jo.put("takeout", each.takeout);
            jo.put("amount", each.amount);
            jo.put("quantity", each.quantity);
            jArray.put(jo);

        }
            jMap.put("saleMirrorItemList", jArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String message = jMap.toString();

        sendMessageToMirror(message);

    }

    private void sendMessageToMirror(final String message) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    InetAddress serverAddr = InetAddress.getByName(mMirrorIP);
                    Socket socket = new Socket(serverAddr, 6000);
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    out.println(message);
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean succ) {
                super.onPostExecute(succ);
                if (!succ) {
                    Toast.makeText(getActivity(), "Mirror failed", Toast.LENGTH_SHORT).show();
                }
            }
        };
        task.execute();
    }

    private void handlePickUp() {
        PickupListFetchTask task = new PickupListFetchTask(this){
            @Override
            protected void onPostExecute(List<Order> orders) {
                super.onPostExecute(orders);
                if(orders == null) {
                    Toast.makeText(MainActivity.this, "Can't load order list", Toast.LENGTH_SHORT).show();
                } else {
                    showPickupList(orders);
                }
            }
        };
        task.execute();
    }

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.KOREA);
    private CharSequence relativeTime(Date reservation_time) {
        long now = System.currentTimeMillis();
        return DateUtils.getRelativeTimeSpanString(reservation_time.getTime(), now, 0L, DateUtils.FORMAT_ABBREV_ALL);
    }

    private void showPickupList(final List<Order> orders) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.select_dialog_singlechoice);
        for(Order order : orders) {
            boolean reserve = order.reservation_time.getTime() != order.order_time.getTime();
            String time = (reserve ? "예약 " : "") + sdf.format(order.reservation_time) + "(" + relativeTime(order.reservation_time) + ")";
            arrayAdapter.add(order.id + "("+order.status+")" + " " + order.user_id + " " + order.phone + "\n" +time+ " " + order.getOrderItemSummary());
        }

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Order theOrder = orders.get(which);
                        processPaymentStep(theOrder);
                    }
                });
        builderSingle.show();
    }

    private void processPaymentStep(Order theOrder) {
        sendPickupPayinfoToMirror(theOrder);

        if (theOrder.paymentType == Order.PaymentType.AT_PICK_UP) {
            choosePaymentTypeAndConfirmDone(theOrder);
        } else {
            confirmDone(theOrder, theOrder.paymentType);
        }
    }


    private void sendPickupPayinfoToMirror(Order theOrder) {
        if (mMirrorIP == null) {
            return;
        }
        HashMap<String,Object> map = new HashMap<>();
        map.put("mCashReceived", 0);
        map.put("mDiscount", theOrder.discount);
        map.put("mPoint", theOrder.reward_use);
        map.put("mSubTotal", theOrder.total_price);
        map.put("mTotal", theOrder.actual_price);

        JSONObject jMap = new JSONObject(map);
        JSONArray jArray = new JSONArray();
        try {
                JSONObject jo = new JSONObject();
                jo.put("name", (theOrder.user_id != null ? (theOrder.user_id + "님 주문 - ") : "픽업 - ")
                        + theOrder.getOrderItemSummaryLong()+"");
                jo.put("price", theOrder.total_price);
                jo.put("takeout", false);
                jo.put("amount", 0);
                jo.put("quantity", 1);
                jArray.put(jo);
            jMap.put("saleMirrorItemList", jArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String message = jMap.toString();

        sendMessageToMirror(message);
    }

    private void choosePaymentTypeAndConfirmDone(final Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setNeutralButton("카드", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                confirmDone(order, Order.PaymentType.CARD);
            }
        });
        builder.setPositiveButton("현금", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                confirmDone(order, Order.PaymentType.CASH);
            }
        });
        builder.setNegativeButton("현금영수증", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                confirmDone(order, Order.PaymentType.CASH_RECEIPT);
            }
        });
        builder.show();
    }

    private void confirmDone(final Order order, final Order.PaymentType paymentType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String message = "done " + order.id + (paymentType == null ? "" : " with " + paymentType.name());
        message += "\n소계 : " + order.total_price;
        message += "\n할인 : " + order.discount;
        message += "\n리워드 : " + order.reward_use;
        message += "\n계산금액 : " + order.actual_price;
        builder.setMessage(message);
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UpdateStatusTask task = new UpdateStatusTask(getActivity(), order, paymentType) {
                    @Override
                    protected void onPostExecute(Integer code) {
                        super.onPostExecute(code);
                        Toast.makeText(getActivity(), "" + (code == 200 ? "Success" : code), Toast.LENGTH_SHORT).show();
                        reset();
                        refreshSaleAmount();
                    }
                };
                task.execute();
            }
        });
        builder.show();
    }

    class UpdateStatusTask extends ProgressAsyncTask<Void, Void, Integer> {
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        private final Order mOrder;
        private final Order.PaymentType mPaymentType;

        public  UpdateStatusTask(Context context, Order order, Order.PaymentType paymentType) {
            super(context);
            this.mOrder = order;
            this.mPaymentType = paymentType;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            try {
                JSONObject signInJson = new JSONObject();

                signInJson.put("id", "saladgram");
                signInJson.put("password", "saladgramadmin1!");
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

                HashMap<String, Object> m = new HashMap<>();

                m.put("order_id", mOrder.id);
                m.put("paid", mOrder.json.getInt("actual_price"));
                m.put("status", Order.Status.DONE.ordinal() + 1);
                if (mPaymentType != null) {
                    m.put("payment_type", mPaymentType.ordinal() + 1);
                }

                JSONObject json = new JSONObject(m);
                body = RequestBody.create(JSON, json.toString());
                Log.d("yns", json.toString(2));
                url = "https://www.saladgram.com/api/update_order.php";
                request = new Request.Builder()
                        .header("jwt", jwt)
                        .url(url)
                        .post(body)
                        .build();

                response = client.newCall(request).execute();
                Log.d("yns", response.body().string());

                return response.code();

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return -1;
        }
    }

    class PickupListFetchTask extends ProgressAsyncTask<Void,Void,List<Order>> {

        public PickupListFetchTask(Context context) {
            super(context);
        }

        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        @Override
        protected List<Order> doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            try {
                JSONObject signInJson = new JSONObject();

                signInJson.put("id","saladgram");
                signInJson.put("password", "saladgramadmin1!");
                RequestBody body = RequestBody.create(JSON, signInJson.toString());

                String url = "https://saladgram.com/api/sign_in.php";
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                Response response = null;
                response = client.newCall(request).execute();
                if (response.code() != 200) {
                    Toast.makeText(MainActivity.this, ""+response.code(), Toast.LENGTH_SHORT).show();
                    return null;
                }
                String jwt = new JSONObject(response.body().string()).getString("jwt");

                url = "https://www.saladgram.com/api/orders.php?id=saladgram&status=2&order_type=1";
                request = new Request.Builder()
                        .header("jwt",jwt)
                        .url(url)
                        .get()
                        .build();

                List<Order> orderList = new LinkedList<>();
                response = client.newCall(request).execute();
                if (response.code() == 200) {
                    String result = response.body().string();
                    JSONArray orders = new JSONArray(result);
                    for (int i = 0; i < orders.length(); i++) {
                        Order order = new Order(orders.getJSONObject(i));
                        JSONArray orderItems = orders.getJSONObject(i).getJSONArray("order_items");
                        for (int j = 0; j < orderItems.length(); j++) {
                            OrderItem orderItem = new OrderItem(orderItems.getJSONObject(j));
                            order.orderItems.add(orderItem);
                            if (orderItems.getJSONObject(j).has("salad_items")) {
                                JSONArray saladItems = orderItems.getJSONObject(j).getJSONArray("salad_items");
                                for (int k = 0; k < saladItems.length(); k++) {
                                    SaladItem saladItem = new SaladItem(saladItems.getJSONObject(k));
                                    orderItem.saladItems.add(saladItem);
                                }
                            }
                        }
                        orderList.add(order);
                    }
                    return orderList;
                } else {
                    Toast.makeText(MainActivity.this, ""+response.code(), Toast.LENGTH_SHORT).show();
                    return null;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
