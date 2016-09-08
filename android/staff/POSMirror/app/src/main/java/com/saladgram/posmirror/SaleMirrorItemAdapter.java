package com.saladgram.posmirror;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.saladgram.model.SaleMirrorItem;

import java.util.List;

/**
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class SaleMirrorItemAdapter extends RecyclerView.Adapter<SaleMirrorItemAdapter.SimpleViewHolder> {

    private final Context mContext;
    private final RecyclerViewClickListener mListener;
    private List<SaleMirrorItem> mList;

    public void setList(List<SaleMirrorItem> list) {
        this.mList = list;
    }


    public static class SimpleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public final TextView name;
        private final TextView price;
        private final TextView takeout;
        private final TextView amount;

        private final RecyclerViewClickListener mListener;
        private final TextView quantity;


        public SimpleViewHolder(View view, RecyclerViewClickListener listener) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            price = (TextView) view.findViewById(R.id.price);
            takeout = (TextView) view.findViewById(R.id.takeout);
            amount = (TextView) view.findViewById(R.id.amount);
            quantity = (TextView) view.findViewById(R.id.quantity);
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

    public SaleMirrorItemAdapter(Context context, RecyclerViewClickListener listner) {
        mContext = context;
        mListener = listner;
    }

    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.sale_item, parent, false);
        return new SimpleViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {
        SaleMirrorItem item = mList.get(position);
        holder.name.setText(item.name);
        holder.price.setText(String.valueOf(item.price)+"원");
        holder.takeout.setVisibility(item.takeout?View.VISIBLE:View.GONE);
        if (item.amount > 0) {
            holder.amount.setText(String.valueOf(item.amount) + "g");
            holder.amount.setVisibility(View.VISIBLE);
        } else {
            holder.amount.setVisibility(View.GONE);
        }
        if (item.quantity > 1) {
            holder.quantity.setText("x " + String.valueOf(item.quantity));
            holder.quantity.setVisibility(View.VISIBLE);
        } else {
            holder.quantity.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}