package redroid.widget;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * zhangyao
 * 16/9/4
 * zhangyao@jiandanxinli.com
 */
public class LoadMoreRecyclerView extends RecyclerView {
    private Context mContext;

    private LoadMoreListener mLoadMoreListener;
    //是否可加载更多
    private boolean mLoadMoreEnabled = true;

    private Adapter mAdapter;

    private Adapter mFooterAdapter;

    public boolean isLoadingData = false;
    //加载更多布局
    private FooterView footerView;

    public LoadMoreRecyclerView(Context context) {
        this(context, null);
    }

    public LoadMoreRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadMoreRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        FooterView footView = new FooterView(mContext);
        addFootView(footView);
        footView.setGone();
    }


    //点击监听
    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        if (mFooterAdapter != null && mFooterAdapter instanceof FooterAdapter) {
            ((FooterAdapter) mFooterAdapter).setOnItemClickListener(onItemClickListener);
        }
    }


    //长按监听
    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
        if (mFooterAdapter != null && mFooterAdapter instanceof FooterAdapter) {
            ((FooterAdapter) mFooterAdapter).setOnItemLongClickListener(listener);
        }
    }

    /**
     * 底部加载更多视图
     *
     * @param view
     */
    public void addFootView(FooterView view) {
        footerView = view;
    }

    //设置底部加载中效果
    public void setFooterLoadingView(View view) {
        if (footerView != null) {
            footerView.addFootLoadingView(view);
        }
    }

    //设置底部到底了布局
    public void setFootEndView(View view) {
        if (footerView != null) {
            footerView.addFootEndView(view);
        }
    }

    //下拉刷新后初始化底部状态
    public void refreshComplete() {
        if (footerView != null) {
            footerView.setGone();
        }
        isLoadingData = false;
    }

    public void loadMoreComplete() {
        if (footerView != null) {
            footerView.setGone();
        }
        isLoadingData = false;
    }


    //到底了
    public void loadMoreEnd() {
        if (footerView != null) {
            footerView.setEnd();
        }
    }

    //设置是否可加载更多
    public void enableLoadMore(boolean flag) {
        mLoadMoreEnabled = flag;
    }

    //设置加载更多监听
    public void setLoadMoreListener(LoadMoreListener listener) {
        mLoadMoreListener = listener;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mAdapter = adapter;
        mFooterAdapter = new FooterAdapter(this, footerView, adapter);
        super.setAdapter(mFooterAdapter);
        mAdapter.registerAdapterDataObserver(mDataObserver);
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == RecyclerView.SCROLL_STATE_IDLE && mLoadMoreListener != null && !isLoadingData && mLoadMoreEnabled) {
            LayoutManager layoutManager = getLayoutManager();
            int lastVisibleItemPosition;
            if (layoutManager instanceof GridLayoutManager) {
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                ((StaggeredGridLayoutManager) layoutManager).findLastCompletelyVisibleItemPositions(into);
                lastVisibleItemPosition = last(into);
            } else {
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
            }

            if (layoutManager.getChildCount() > 0
                    && lastVisibleItemPosition >= layoutManager.getItemCount() - 1) {
                if (footerView != null) {
                    footerView.setVisible();
                }
                isLoadingData = true;
                mLoadMoreListener.onLoadMore();
            }
        }
    }


    //取到最后的一个节点
    private int last(int[] lastPositions) {
        int last = lastPositions[0];
        for (int value : lastPositions) {
            if (value > last) {
                last = value;
            }
        }
        return last;
    }


    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            mFooterAdapter.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mFooterAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mFooterAdapter.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mFooterAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mFooterAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mFooterAdapter.notifyItemMoved(fromPosition, toPosition);
        }
    };

    static class FooterView extends LinearLayout {
        private LinearLayout loading_view_layout;
        private LinearLayout end_layout;

        public FooterView(Context context) {
            super(context);
            initView(context);
        }

        /**
         * @param context
         * @param attrs
         */
        public FooterView(Context context, AttributeSet attrs) {
            super(context, attrs);
            initView(context);
        }

        public void initView(Context context) {
            setGravity(Gravity.CENTER);
            setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View view = layoutInflater.inflate(R.layout.view_footer,
                    null);
            loading_view_layout = view.findViewById(R.id.loading_view_layout);
            end_layout = view.findViewById(R.id.end_layout);


            addFootLoadingView(new ProgressBar(context, null, android.R.attr.progressBarStyle));

            TextView textView = new TextView(context);
            textView.setText("已经到底啦~");
            addFootEndView(textView);

            addView(view);
        }


        //设置底部加载中效果
        public void addFootLoadingView(View view) {
            loading_view_layout.removeAllViews();
            loading_view_layout.addView(view);
        }

        //设置底部到底了布局
        public void addFootEndView(View view) {
            end_layout.removeAllViews();
            end_layout.addView(view);
        }


        //设置已经没有更多数据
        public void setEnd() {
            setVisibility(VISIBLE);
            loading_view_layout.setVisibility(GONE);
            end_layout.setVisibility(VISIBLE);
        }


        public void setVisible() {
            setVisibility(VISIBLE);
            loading_view_layout.setVisibility(VISIBLE);
            end_layout.setVisibility(GONE);
        }


        public void setGone() {
            setVisibility(GONE);
        }

    }

    public class FooterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

        private RecyclerView.Adapter adapter;

        private LoadMoreRecyclerView recyclerView;

        private FooterView loadingMoreFooter;

        private static final int DEFAULT = 0;
        private static final int FOOTER = -1;

        public FooterAdapter(LoadMoreRecyclerView loadMoreRecyclerView, FooterView loadingMoreFooter, RecyclerView.Adapter adapter) {
            this.recyclerView = loadMoreRecyclerView;
            this.adapter = adapter;
            this.loadingMoreFooter = loadingMoreFooter;
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) manager);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return (getItemViewType(position) == RecyclerView.INVALID_TYPE || getItemViewType(position) == RecyclerView.INVALID_TYPE - 1)
                                ? gridManager.getSpanCount() : 1;
                    }
                });
            }
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null
                    && lp instanceof StaggeredGridLayoutManager.LayoutParams
                    && isFooter(holder.getLayoutPosition())) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
        }

        /**
         * 当前布局是否为Footer
         *
         * @param position
         * @return
         */
        public boolean isFooter(int position) {
            return position < getItemCount() && position >= getItemCount() - 1;
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == FOOTER) {
                return new SimpleViewHolder(loadingMoreFooter);
            }
            return adapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            holder.itemView.setOnClickListener(this);
            holder.itemView.setOnLongClickListener(this);
            if (adapter != null) {
                int count = adapter.getItemCount();
                if (position < count) {
                    adapter.onBindViewHolder(holder, position);
                    return;
                }
            }
        }

        @Override
        public int getItemCount() {
            if (adapter != null) {
                return 1 + adapter.getItemCount();
            } else {
                return 1;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (isFooter(position)) {
                return FOOTER;
            }
            if (adapter != null) {
                int count = adapter.getItemCount();
                if (position < count) {
                    return adapter.getItemViewType(position);
                }
            }
            return DEFAULT;
        }

        @Override
        public long getItemId(int position) {
            if (adapter != null && position >= 0) {
                int adapterCount = adapter.getItemCount();
                if (position < adapterCount) {
                    return adapter.getItemId(position);
                }
            }
            return -1;
        }

        @Override
        public void onClick(View view) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(null, view, recyclerView.getChildAdapterPosition(view), 0);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (onItemLongClickListener != null) {
                onItemLongClickListener.onItemLongClick(null, view, recyclerView.getChildAdapterPosition(view), 0);
            }
            return true;
        }

        //点击
        AdapterView.OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        //长按
        AdapterView.OnItemLongClickListener onItemLongClickListener;

        public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener onItemLongClickListener) {
            this.onItemLongClickListener = onItemLongClickListener;
        }

        private class SimpleViewHolder extends RecyclerView.ViewHolder {
            public SimpleViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

    public interface LoadMoreListener {

        /**
         * loading more
         */
        void onLoadMore();
    }
}
