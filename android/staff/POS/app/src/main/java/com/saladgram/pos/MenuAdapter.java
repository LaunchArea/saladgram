package com.saladgram.pos;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.SimpleViewHolder> {

    private final Context mContext;
    private final RecyclerViewClickListener mListener;
    private List<MenuItem> mList;

    public void setList(List<MenuItem> list) {
        this.mList = list;
    }


    public static class SimpleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public final TextView name;
        private final TextView price;
        private final View na;
        private final RecyclerViewClickListener mListener;
        private final TextView amount;

        public SimpleViewHolder(View view, RecyclerViewClickListener listener) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            price = (TextView) view.findViewById(R.id.price);
            na = view.findViewById(R.id.not_available);
            amount = (TextView) view.findViewById(R.id.amount);
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

    public MenuAdapter(Context context, RecyclerViewClickListener listner) {
        mContext = context;
        mListener = listner;
    }

    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.menu_item, parent, false);
        return new SimpleViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {
        MenuItem item = mList.get(position);
        holder.name.setText(item.name);
        holder.price.setText(String.valueOf(item.price)+"원");
        holder.na.setVisibility(!item.available ? View.VISIBLE : View.GONE);
        holder.amount.setText(item.amount);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}