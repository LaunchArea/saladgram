package com.saladgram.assemble;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.saladgram.model.OrderItem;
import com.saladgram.model.SaladItem;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yns on 5/31/16.
 */

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.SimpleViewHolder> {

    private final Context mContext;
    private final RecyclerViewClickListener mListener;
    private List<OrderItem> mList = new LinkedList<>();

    public void setList(List<OrderItem> list) {
        this.mList = list;
        notifyDataSetChanged();
    }


    public static class SimpleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final RecyclerViewClickListener mListener;
        private final TextView name;
        private final TextView type;
        private final TextView detail1;
        private final TextView quantity;
        private final TextView detail2;
        private final TextView detail3;
        private final TextView package_type;


        public SimpleViewHolder(View view, RecyclerViewClickListener listener) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            type = (TextView) view.findViewById(R.id.type);
            package_type = (TextView) view.findViewById(R.id.package_type);

            detail1 = (TextView) view.findViewById(R.id.detail1);
            detail2 = (TextView) view.findViewById(R.id.detail2);
            detail3 = (TextView) view.findViewById(R.id.detail3);

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

    public OrderItemAdapter(Context context, RecyclerViewClickListener listner) {
        mContext = context;
        mListener = listner;
    }

    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.orderitem_listitem, parent, false);
        return new SimpleViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {
        OrderItem item = mList.get(position);
        holder.name.setText(item.name);
        holder.type.setText(item.type.name().toLowerCase());
        holder.quantity.setText("x " + item.quantity);
        holder.package_type.setVisibility(item.packageType == OrderItem.PackageType.DINE_IN ? View.VISIBLE : View.GONE);

        holder.detail2.setText("");
        holder.detail3.setText("");
            switch(item.type) {
                case SALAD:
                    String[] details = new String[3];
                    for(int i = 0; i < details.length; i++) {
                        details[i] = "";
                    }
                    for(SaladItem each : item.saladItems) {
                        details[0] += (each.name + "\n");
                        details[1] += ("" + each.amount + "\n");
                        details[2] += (each.type.toString().substring(0,1) + "\n");
                    }
                    for(int i = 0; i < details.length; i++) {
                        details[i] = details[i].substring(0, details[i].length()-1);
                    }
                    holder.detail1.setText(details[0]);
                    holder.detail2.setText(details[1]);
                    holder.detail3.setText(details[2]);
                    break;
                case SOUP:
                case OTHERS:
                    holder.detail1.setText(item.amount != null ? item.amount : "");
                    break;
                case BEVERAGES:
                    holder.detail1.setText("");
                    break;
            }
        }


    @Override
    public int getItemCount() {
        return mList.size();
    }
}
