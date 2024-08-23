package com.crepowermay.ezui.collection;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CardList extends RecyclerView {

    private CardListAdapter adapter;
    private boolean isLoading = false;
    private OnLoadMoreListener onLoadMoreListener;

    public CardList(Context context) {
        super(context);
        init(context);
    }

    public CardList(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CardList(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        setLayoutManager(new LinearLayoutManager(context));
        adapter = new CardListAdapter();
        setAdapter(adapter);
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
                if (!isLoading && layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == adapter.getItemCount() - 1) {
                    if (onLoadMoreListener != null) {
                        isLoading = true;
                        onLoadMoreListener.onLoadMore(data -> {
                            adapter.appendData(data);
                            isLoading = false;
                        });
                    }
                }
            }
        });
    }

    public void setCustomHeight(int height) {
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = height;
        setLayoutParams(params);
    }

    public void setCustomStyle(int backgroundColor, int textColor) {
        setBackgroundColor(backgroundColor);
        adapter.setTextColor(textColor);
    }

    public void setData(List<String> data) {
        adapter.setData(data);
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.onLoadMoreListener = listener;
    }

    public void setLoading(boolean loading) {
        this.isLoading = loading;
    }

    public interface OnLoadMoreListener {
        void onLoadMore(DataCallback callback);
    }

    public interface DataCallback {
        void onDataLoaded(List<String> data);
    }
}
