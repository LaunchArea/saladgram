package com.saladgram.gramcalculator;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String selectedItem = null;
    private WeightedItem selectedStorage;
    private WeightedItem selectedStick;
    private EditText et;
    private String weightEntered;

    class WeightedItem {
        WeightedItem(String name, int weight) {
            this.name = name;
            this.weight = weight;
        }
        public String name;
        public int weight;
    }

    String[] items = new String[]{"믹스드", "파프리카", "래디쉬", "연어", "병아리콩", "오렌지",
                                  "로메인", "양파", "아스파라", "그릴치킨", "버섯", "자몽",
                                  "양상추", "방울", "브로클리", "스팀치킨", "두부", "계란",
                                  "크루통"};

    List<WeightedItem> storages = new LinkedList<>();
    List<WeightedItem> sticks = new LinkedList<>();

    List<TextView> itemViews = new LinkedList<>();
    List<TextView> storageViews = new LinkedList<>();
    List<TextView> otherViews = new LinkedList<>();
    List<TextView> calcViews = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storages.add(new WeightedItem("작은", 136));
        storages.add(new WeightedItem("연어", 320));
        storages.add(new WeightedItem("믹스드(깊)", 542));
        storages.add(new WeightedItem("플라 낮", 470+156+248));
        storages.add(new WeightedItem("올리브", 87));
        storages.add(new WeightedItem("기본스뎅", 543));

        storages.add(new WeightedItem("작고 깊", 179));
        storages.add(new WeightedItem("연어대기", 470));
        storages.add(new WeightedItem("믹스드(얕)", 408));
        storages.add(new WeightedItem("플라 높", 744+156+248));
        storages.add(new WeightedItem("크루통병", 746));
        storages.add(new WeightedItem("아몬드통", 355));

        storages.add(new WeightedItem("닭", 296));

        sticks.add(new WeightedItem("작은", 35));
        sticks.add(new WeightedItem("큰집게", 75));
        sticks.add(new WeightedItem("없음", 0));

        initItemView();
        initStorageView();
        initStickView();
        initCalc();

        et = ((EditText)findViewById(R.id.et));

        findViewById(R.id.calc_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (weightEntered != null && weightEntered.length() > 0) {
                    weightEntered = weightEntered.substring(0, weightEntered.length() - 1);
                }
                refreshUI();
            }
        });
        findViewById(R.id.undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String before = et.getText().toString();
                if(before.length() > 0) {
                    before += "\n";
                }
                if (selectedStorage == null || selectedItem == null || weightEntered == null || weightEntered.length() == 0)
                    return;
                int entered = Integer.parseInt(weightEntered);
                int stick = selectedStick != null ? selectedStick.weight : 0;
                int storage = selectedStorage.weight;

                String line = selectedItem + " " + (entered - stick - storage);
                before += line;
                et.setText(before);
                et.setSelection(et.getText().length());

                weightEntered = null;
                selectedItem = null;
                refreshUI();
            }
        });
    }

    private void initCalc() {
        calcViews.add((TextView)findViewById(R.id.calc_00));
        calcViews.add((TextView)findViewById(R.id.calc_01));
        calcViews.add((TextView)findViewById(R.id.calc_02));
        calcViews.add((TextView)findViewById(R.id.calc_03));
        calcViews.add((TextView)findViewById(R.id.calc_04));
        calcViews.add((TextView)findViewById(R.id.calc_05));
        calcViews.add((TextView)findViewById(R.id.calc_06));
        calcViews.add((TextView)findViewById(R.id.calc_07));
        calcViews.add((TextView)findViewById(R.id.calc_08));
        calcViews.add((TextView)findViewById(R.id.calc_09));

        for(int i = 0; i < calcViews.size(); i++) {
            calcViews.get(i).setOnClickListener(this);
        }
    }

    private void initStickView() {
        otherViews.add(((TextView)findViewById(R.id.other_00)));
        otherViews.add(((TextView)findViewById(R.id.other_01)));
        otherViews.add(((TextView)findViewById(R.id.other_02)));

        for(int i = 0; i < sticks.size(); i++) {
            otherViews.get(i).setText(sticks.get(i).name);
            otherViews.get(i).setVisibility(View.VISIBLE);
            otherViews.get(i).setOnClickListener(this);
            otherViews.get(i).setBackgroundColor(Color.WHITE);
        }

    }

    private void initItemView() {
        itemViews.add(((TextView)findViewById(R.id.item_00)));
        itemViews.add(((TextView)findViewById(R.id.item_01)));
        itemViews.add(((TextView)findViewById(R.id.item_02)));
        itemViews.add(((TextView)findViewById(R.id.item_03)));
        itemViews.add(((TextView)findViewById(R.id.item_04)));
        itemViews.add(((TextView)findViewById(R.id.item_05)));
        itemViews.add(((TextView)findViewById(R.id.item_06)));
        itemViews.add(((TextView)findViewById(R.id.item_07)));
        itemViews.add(((TextView)findViewById(R.id.item_08)));
        itemViews.add(((TextView)findViewById(R.id.item_09)));
        itemViews.add(((TextView)findViewById(R.id.item_10)));
        itemViews.add(((TextView)findViewById(R.id.item_11)));
        itemViews.add(((TextView)findViewById(R.id.item_12)));
        itemViews.add(((TextView)findViewById(R.id.item_13)));
        itemViews.add(((TextView)findViewById(R.id.item_14)));
        itemViews.add(((TextView)findViewById(R.id.item_15)));
        itemViews.add(((TextView)findViewById(R.id.item_16)));
        itemViews.add(((TextView)findViewById(R.id.item_17)));
        itemViews.add(((TextView)findViewById(R.id.item_18)));
        itemViews.add(((TextView)findViewById(R.id.item_19)));
        itemViews.add(((TextView)findViewById(R.id.item_20)));
        itemViews.add(((TextView)findViewById(R.id.item_21)));
        itemViews.add(((TextView)findViewById(R.id.item_22)));
        itemViews.add(((TextView)findViewById(R.id.item_23)));

        for(int i = 0; i < itemViews.size(); i++) {
            itemViews.get(i).setVisibility(View.GONE);
        }
        for(int i = 0; i < items.length; i++) {
            itemViews.get(i).setText(items[i]);
            itemViews.get(i).setVisibility(View.VISIBLE);
            itemViews.get(i).setOnClickListener(this);
            itemViews.get(i).setBackgroundColor(Color.WHITE);
        }
    }

    private void initStorageView() {
        storageViews.add(((TextView)findViewById(R.id.store_00)));
        storageViews.add(((TextView)findViewById(R.id.store_01)));
        storageViews.add(((TextView)findViewById(R.id.store_02)));
        storageViews.add(((TextView)findViewById(R.id.store_03)));
        storageViews.add(((TextView)findViewById(R.id.store_04)));
        storageViews.add(((TextView)findViewById(R.id.store_05)));
        storageViews.add(((TextView)findViewById(R.id.store_06)));
        storageViews.add(((TextView)findViewById(R.id.store_07)));
        storageViews.add(((TextView)findViewById(R.id.store_08)));
        storageViews.add(((TextView)findViewById(R.id.store_09)));
        storageViews.add(((TextView)findViewById(R.id.store_10)));
        storageViews.add(((TextView)findViewById(R.id.store_11)));
        storageViews.add(((TextView)findViewById(R.id.store_12)));
        storageViews.add(((TextView)findViewById(R.id.store_13)));
        storageViews.add(((TextView)findViewById(R.id.store_14)));
        storageViews.add(((TextView)findViewById(R.id.store_15)));
        storageViews.add(((TextView)findViewById(R.id.store_16)));
        storageViews.add(((TextView)findViewById(R.id.store_17)));

        for(int i = 0; i < storageViews.size(); i++) {
            storageViews.get(i).setVisibility(View.GONE);
        }
        for(int i = 0; i < storages.size(); i++) {
            storageViews.get(i).setText(storages.get(i).name);
            storageViews.get(i).setVisibility(View.VISIBLE);
            storageViews.get(i).setOnClickListener(this);
            storageViews.get(i).setBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public void onClick(View v) {
        for(int i = 0; i < storageViews.size(); i++) {
            TextView tv = storageViews.get(i);
            if(v == tv) {
                tv.setBackgroundColor(Color.BLUE);
                tv.setTextColor(Color.WHITE);
                selectedStorage = storages.get(i);
                for(int j = 0; j < storageViews.size(); j++) {
                    if( j == i ) continue;
                    storageViews.get(j).setBackgroundColor(Color.WHITE);
                    storageViews.get(j).setTextColor(Color.BLACK);
                }
                break;
            }
        }
        for(int i = 0; i < itemViews.size(); i++) {
            TextView tv = itemViews.get(i);
            if(v == tv) {
                tv.setBackgroundColor(Color.BLUE);
                tv.setTextColor(Color.WHITE);
                selectedItem = items[i];
                for(int j = 0; j < itemViews.size(); j++) {
                    if( j == i ) continue;
                    itemViews.get(j).setBackgroundColor(Color.WHITE);
                    itemViews.get(j).setTextColor(Color.BLACK);
                }
                break;
            }
        }
        for(int i = 0; i < otherViews.size(); i++) {
            TextView tv = otherViews.get(i);
            if(v == tv) {
                tv.setBackgroundColor(Color.BLUE);
                tv.setTextColor(Color.WHITE);
                selectedStick = sticks.get(i);
                for(int j = 0; j < otherViews.size(); j++) {
                    if( j == i ) continue;
                    otherViews.get(j).setBackgroundColor(Color.WHITE);
                    otherViews.get(j).setTextColor(Color.BLACK);
                }
                break;
            }
        }
        for(int i = 0; i < calcViews.size(); i++) {
            TextView tv = calcViews.get(i);
            if(v == tv) {
                if (weightEntered == null) {
                    weightEntered = "";
                }
                weightEntered += ("" + i);
            }
        }
        refreshUI();
    }

    private void refreshUI() {
        ((TextView)findViewById(R.id.entered)).setText(weightEntered != null ? ""+weightEntered : "0");

        int entered = weightEntered != null && weightEntered.length() > 0 ? Integer.parseInt(weightEntered) : 0;
        int stick = selectedStick != null ? selectedStick.weight : 0;
        int storage = selectedStorage != null ? selectedStorage.weight : 0;

        String line = selectedItem + " " + (entered - stick - storage);
        ((TextView)findViewById(R.id.calculated)).setText(line);

    }

}
