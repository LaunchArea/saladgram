package com.saladgram.assemble;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

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
        private final TextView id;
        private final TextView type;
        private final TextView time;
        private final TextView items;
        private final View bg;


        public SimpleViewHolder(View view, RecyclerViewClickListener listener) {
            super(view);
            bg = view.findViewById(R.id.bg);
            id = (TextView) view.findViewById(R.id.id);
            type = (TextView) view.findViewById(R.id.type);
            time = (TextView) view.findViewById(R.id.time);
            items = (TextView) view.findViewById(R.id.items);
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

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.KOREA);
    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {
        Order item = mList.get(position);
        holder.id.setText("" + item.id);
        holder.type.setText(item.type.name().toLowerCase());
        holder.time.setText(sdf.format(item.order_time));
        holder.items.setText(item.getOrderItemSummary());
        if(mSelectedId == item.id) {
            holder.bg.setBackgroundResource(android.R.color.darker_gray);
        } else {
            holder.bg.setBackgroundResource(android.R.color.white);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

}
