package com.saladgram.assemble;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yns on 5/31/16.
 */

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.SimpleViewHolder> {

    private final Context mContext;
    private final RecyclerViewClickListener mListener;
    private List<Order> mList = new LinkedList<>();
    private int mSelectedId;

    public void setList(List<Order> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void setSelectedId(int selectedId) {
        mSelectedId = selectedId;
    }


    public static class SimpleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final RecyclerViewClickListener mListener;
        private final TextView tv;


        public SimpleViewHolder(View view, RecyclerViewClickListener listener) {
            super(view);
            tv = (TextView) view.findViewById(R.id.tv);
            mListener = listener;
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mListener != null) {
                mListener.onItemClick(v, getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(mListener != null) {
                mListener.onItemLongClick(v, getAdapterPosition());
            }
            return false;
        }
    }

    public OrderAdapter(Context context, RecyclerViewClickListener listner) {
        mContext = context;
        mListener = listner;
    }

    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.order_listitem, parent, false);
        return new SimpleViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {
        Order item = mList.get(position);
        holder.tv.setText("" + item.id + (mSelectedId == item.id ? " v" : ""));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

}
