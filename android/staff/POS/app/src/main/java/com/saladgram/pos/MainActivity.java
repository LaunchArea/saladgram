package com.saladgram.pos;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {


    private RecyclerView mRecyclerView;
    private MenuAdapter mAdapter;
    private List<MenuItem> mMenuList = new LinkedList<MenuItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new MenuListFetchTask().execute();
    }

    public Activity getActivity() {
        return this;
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
                Log.d("yns",each.toString());
                MenuItem item = new MenuItem();
                item.data = each;
                item.name = (String) each.get("name");
                item.price = each.containsKey("price") ? ((Double)each.get("price")).intValue() : -1;
                item.available = ((Double)each.get("available")).intValue() == 1;
                item.type = type;
                mMenuList.add(item);
            }
        }
    }

    private void initializeUI() {
        findViewById(R.id.content_layout).setVisibility(View.VISIBLE);

        //Your RecyclerView
        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.menu_list);
//        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 5));

        //Your RecyclerView.Adapter
        mAdapter = new MenuAdapter(getActivity());
        mAdapter.setList(mMenuList);

        //This is the code to provide a sectioned grid
        List<SectionedGridRecyclerViewAdapter.Section> sections =
                new ArrayList<SectionedGridRecyclerViewAdapter.Section>();

        MenuItem.Type currentType = MenuItem.Type.NONE;
        for(int i = 0; i < mMenuList.size(); i++) {
            MenuItem now = mMenuList.get(i);
            if(now.type != currentType) {
                sections.add(new SectionedGridRecyclerViewAdapter.Section(i, now.type.name()));
            }
            currentType = now.type;
        }

        //Add your adapter to the sectionAdapter
        SectionedGridRecyclerViewAdapter.Section[] dummy = new SectionedGridRecyclerViewAdapter.Section[sections.size()];
        SectionedGridRecyclerViewAdapter mSectionedAdapter = new
                SectionedGridRecyclerViewAdapter(getActivity(), R.layout.menu_section, R.id.section_text, mRecyclerView, mAdapter);
        mSectionedAdapter.setSections(sections.toArray(dummy));

        //Apply this adapter to the RecyclerView
        mRecyclerView.setAdapter(mSectionedAdapter);
    }
}
