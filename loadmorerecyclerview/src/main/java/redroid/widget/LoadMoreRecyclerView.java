package redroid.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.IntDef;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
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
    private static final String TAG = "LoadMoreRecyclerView";
    private Context mContext;

    private LoadMoreListener mLoadMoreListener;
    //是否可加载更多
    private boolean mLoadMoreEnabled = true;

    private Adapter mAdapter;

    private Adapter mFooterAdapter;

    public boolean isLoadingData = false;
    //加载更多布局
    private FooterView mFooterView;

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
    }

    public void enableLoadMore(boolean flag) {
        mLoadMoreEnabled = flag;
    }

    public void refreshComplete() {
        if (mFooterView != null) {
            mFooterView.setVisibility(GONE);
        }
        isLoadingData = false;
    }

    public void loadMoreComplete() {
        if (mFooterView != null) {
            mFooterView.setVisibility(GONE);
        }
        isLoadingData = false;
    }

    public void showNoMoreDataView() {
        if (mFooterView != null) {
            mFooterView.showNoMoreDataView();
        }
        isLoadingData = false;
    }

    /**
     * 底部加载更多视图
     *
     * @param view
     */
    public void addFootView(FooterView view) {
        mFooterView = view;
    }

    /**
     * set load more event callback.
     *
     * @param listener
     */
    public void setLoadMoreListener(LoadMoreListener listener) {
        mLoadMoreListener = listener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        if (mFooterAdapter != null && mFooterAdapter instanceof FooterAdapter) {
            ((FooterAdapter) mFooterAdapter).setOnItemClickListener(onItemClickListener);
        }
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
        if (mFooterAdapter != null && mFooterAdapter instanceof FooterAdapter) {
            ((FooterAdapter) mFooterAdapter).setOnItemLongClickListener(listener);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mAdapter = adapter;
        mFooterAdapter = new FooterAdapter(this, mFooterView, adapter);
        super.setAdapter(mFooterAdapter);
        mAdapter.registerAdapterDataObserver(mDataObserver);
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);

        Log.d(TAG, "onScrollStateChanged: " + (state == RecyclerView.SCROLL_STATE_IDLE && mLoadMoreListener != null && !isLoadingData && mLoadMoreEnabled));
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
                if (mFooterView != null) {
                    mFooterView.showLoadingMoreView();
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
        private static final int VIEW_STATE_LOADING_MORE = 1;
        private static final int VIEW_STATE_NO_MORE_DATA = 2;

        @IntDef({VIEW_STATE_LOADING_MORE, VIEW_STATE_NO_MORE_DATA})
        public @interface ViewState {}

        private Context mContext;
        private LinearLayout mLoadingMoreView;
        private LinearLayout mNoMoreDataView;

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

        protected void initView(Context context) {
            mContext = context;

            //default hidden.
            setVisibility(GONE);

            setGravity(Gravity.CENTER);
            setOrientation(HORIZONTAL);
            setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        protected void showLoadingMoreView() {
            setUpDefaultLoadingMoreView();
            setViewState(VIEW_STATE_LOADING_MORE);
            setVisibility(VISIBLE);
        }

        protected void showNoMoreDataView() {
            setUpDefaultNoMoreDataView();
            setViewState(VIEW_STATE_NO_MORE_DATA);
            setVisibility(VISIBLE);

            Log.d(TAG, "showNoMoreDataView: 111");
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "showNoMoreDataView: 222");
                    mNoMoreDataView.animate().translationY(getHeight())
                            .setDuration(300)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    //mNoMoreDataView.setTranslationY(0);
                                    setVisibility(GONE);
                                }
                            });
                }
            }, 2000);
        }

        private void setViewState(@ViewState int viewState) {
            if (null != mNoMoreDataView)
                mNoMoreDataView.setVisibility(viewState == VIEW_STATE_NO_MORE_DATA ? VISIBLE : GONE);

            if (null != mLoadingMoreView)
                mLoadingMoreView.setVisibility(viewState == VIEW_STATE_LOADING_MORE ? VISIBLE : GONE);
        }

        protected void setLoadingMoreView(View view) {
            if (view instanceof LinearLayout) {
                mLoadingMoreView = (LinearLayout) view;
            } else {
                mLoadingMoreView.removeAllViews();
                mLoadingMoreView.addView(view);
            }
        }

        protected void setNoMoreDataView(View view) {
            if (view instanceof LinearLayout) {
                mNoMoreDataView = (LinearLayout) view;
            } else {
                mNoMoreDataView.removeAllViews();
                mNoMoreDataView.addView(view);
            }
        }

        /**
         * setup the default loading-more view if necessary.
         */
        protected void setUpDefaultLoadingMoreView() {
            if (null == mLoadingMoreView) {
                mLoadingMoreView = new LinearLayout(mContext);

                ProgressBar progressBar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleSmall);
                TextView textView = new TextView(mContext);
                LinearLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(getResources().getDimensionPixelOffset(R.dimen.loading_text_margin), 0, 0, 0);
                params.gravity = Gravity.CENTER_VERTICAL;
                textView.setLayoutParams(params);
                textView.setBackgroundColor(Color.RED);
                textView.setText(getResources().getString(R.string.loading_more));
                mLoadingMoreView.addView(progressBar);
                mLoadingMoreView.addView(textView);

                addView(mLoadingMoreView);
            }
        }


        /**
         * setup the default no-more-data view if necessary.
         */
        protected void setUpDefaultNoMoreDataView() {
            if (null == mNoMoreDataView) {
                mNoMoreDataView = new LinearLayout(mContext);
                TextView textView = new TextView(mContext);
                textView.setText(getResources().getString(R.string.no_more_data));
                mNoMoreDataView.addView(textView);
                addView(mNoMoreDataView);
            }
        }
    }


    public static class FooterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {
        private RecyclerView.Adapter adapter;
        private LoadMoreRecyclerView recyclerView;
        private FooterView mFooterView;
        // TODO: 2017/12/26 get/set
        public boolean mVisibleFlag;

        private static final int DEFAULT = 0;
        private static final int FOOTER = -1;

        public FooterAdapter(LoadMoreRecyclerView loadMoreRecyclerView, FooterView footerView, RecyclerView.Adapter adapter) {
            this.recyclerView = loadMoreRecyclerView;
            this.adapter = adapter;
            this.mFooterView = footerView;
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
                return new SimpleViewHolder(mFooterView);
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
            int itemCount = mVisibleFlag ? 1 : 0;
            if (adapter != null) {
                return itemCount + adapter.getItemCount();
            } else {
                return itemCount;
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

        AdapterView.OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

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
        void onLoadMore();
    }
}
