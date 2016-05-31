package com.saladgram.ready;

import android.view.View;

/**
 * Created by yns on 5/27/16.
 */
public interface RecyclerViewClickListener {
    public void onItemClick(View view, int position);
    public void onItemLongClick(View view, int position);
}