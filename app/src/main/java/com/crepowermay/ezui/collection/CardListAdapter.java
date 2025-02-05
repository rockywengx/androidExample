package com.crepowermay.ezui.collection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class CardListAdapter<T> extends RecyclerView.Adapter<CardListAdapter.ViewHolder> {

    private final List<CardModelInterface<T>> data = new ArrayList<>();
    private int textColor;

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardModelInterface<T> card = data.get(position);
        holder.textView.setText(card.getTitle());
        holder.textView.setTextColor(textColor);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<CardModelInterface<T>> data) {
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    public void setData(Function<Integer, CardModelInterface<T>> funs) {
        this.data.clear();
        for (int i = 0; ; i++) {
            CardModelInterface<T> card = funs.apply(i);
            if (card == null) {
                break;
            }
            this.data.add(card);
        }
        notifyDataSetChanged();
    }


    public void appendData(List<CardModelInterface<T>> newData) {
        int startPosition = data.size();
        data.addAll(newData);
        notifyItemRangeInserted(startPosition, newData.size());
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
