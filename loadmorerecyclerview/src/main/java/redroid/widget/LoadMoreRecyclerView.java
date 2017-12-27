package redroid.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * Custom RecyclerView with load-more support.
 *
 * fork and modified from https://github.com/zhangyao0328/RecyclerViewMore
 *
 * @author RobinVanYang created at 2017-12-26 17:37.
 */

public class LoadMoreRecyclerView extends RecyclerView {
    private static final String TAG = "LoadMoreRecyclerView";
    private Context mContext;
    private LoadMoreListener mLoadMoreListener;
    private FooterAdapter mFooterAdapter;

    //a Flag, prevent call load-more multi times.
    private boolean isLoadingData;
    //whether scrolling downing, if not , do not trigger load-more event.
    private boolean mScrollingDown;
    //widget load-more on-off switch.
    private boolean mLoadMoreEnabled = true;

    public LoadMoreRecyclerView(Context context) {
        this(context, null);
    }

    public LoadMoreRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadMoreRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        setOnFlingListener(new OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                mScrollingDown = velocityY > 0;
                return false;
            }
        });
    }

    @Override
    public void setAdapter(Adapter adapter) {
        FooterView footerView = new FooterView(mContext);
        mFooterAdapter = new FooterAdapter(this, adapter, footerView);
        super.setAdapter(mFooterAdapter);
        adapter.registerAdapterDataObserver(mDataObserver);
    }

    public void notifyRefreshEvent() {
        hideFooter();
    }

    public void notifyLoadMoreFinished() {
        hideFooter();
    }

    public void showNoMoreDataView() {
        mFooterAdapter.setViewState(IFooterView.VIEW_STATE_NO_MORE_DATA, () -> setLoadingDataState(false));
    }

    private void hideFooter() {
        mFooterAdapter.setVisible(false);
        setLoadingDataState(false);
    }

    private void setLoadingDataState(boolean state) {
        isLoadingData = state;
    }

    public boolean isLoadingData() {
        return isLoadingData;
    }

    /**
     * set load more event callback.
     *
     * @param listener
     */
    public void setLoadMoreListener(LoadMoreListener listener) {
        mLoadMoreListener = listener;
    }

    public void setLoadMoreEnabled(boolean enabled) {
        mLoadMoreEnabled = enabled;
    }

    public void setLoadMoreView(View view) {
        mFooterAdapter.setLoadingMoreView(view);
    }

    public void setNoMoreDataView(View view) {
        mFooterAdapter.setNoMoreDataView(view);
    }

    @Override
    public void onScrollStateChanged(int state) {
        int lastVisibleItemPosition = 0;

        if (RecyclerView.SCROLL_STATE_IDLE == state && mScrollingDown && null != mLoadMoreListener
                && !isLoadingData && mLoadMoreEnabled) {
            LayoutManager layoutManager = getLayoutManager();
            if (layoutManager instanceof StaggeredGridLayoutManager) {
                int[] lastVisibleItemPositions = ((StaggeredGridLayoutManager) layoutManager).findLastCompletelyVisibleItemPositions(null);
                lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions);
            } else if (layoutManager instanceof GridLayoutManager) {
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
            } else if (layoutManager instanceof LinearLayoutManager) {
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
            }

            if (layoutManager.getChildCount() > 0 && lastVisibleItemPosition >= layoutManager.getItemCount() - 1) {
                mFooterAdapter.setViewState(IFooterView.VIEW_STATE_LOADING_MORE, null);
                setLoadingDataState(true);
                mLoadMoreListener.onLoadMore();
            }
        }
    }

    /**
     * get the last element position in the list.
     *
     * @param lastPositions
     * @return
     */
    public int getLastVisibleItem(int[] lastPositions) {
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

    public static class FooterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Adapter adapter;
        private IFooterView footerView;
        private boolean mVisibleFlag;
        private @IFooterView.ViewState int mViewState;
        private RecyclerView mRecyclerView;

        private static final int FOOTER = -1;

        public FooterAdapter(RecyclerView recyclerView, RecyclerView.Adapter adapter, IFooterView footerView) {
            mRecyclerView = recyclerView;
            this.adapter = adapter;
            this.footerView = footerView;
        }

        public void setVisible(boolean visible) {
            mVisibleFlag = visible;

            if (!visible) {
                notifyItemRemoved(getItemCount());
            }
        }

        public void setViewState(@IFooterView.ViewState int viewState, Runnable runnable) {
            mViewState = viewState;
            setVisible(true);
            notifyDataSetChanged();
            mRecyclerView.smoothScrollToPosition(getItemCount() - 1);
            //notifyItemChanged(getItemCount() - 1);

            if (null != runnable && IFooterView.VIEW_STATE_NO_MORE_DATA == viewState) {
                ((View) footerView).postDelayed(() -> {
                    runnable.run();
                    setVisible(false);
                }, 1500);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (FOOTER == viewType) {
                return new FooterViewHolder((View) footerView);
            }

            return adapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (position < adapter.getItemCount()) {
                adapter.onBindViewHolder(holder, position);
                return;
            }

            if (isFooter(position)) {
                ((FooterViewHolder) holder).bindView(mViewState);
            }

        }

        @Override
        public int getItemCount() {
            int count = mVisibleFlag ? 1 : 0;
            if (null != adapter) {
                return count + adapter.getItemCount();
            } else {
                return count;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (isFooter(position)) {
                return FOOTER;
            } else if (null != adapter && position < adapter.getItemCount()) {
                return adapter.getItemViewType(position);
            }
            return super.getItemViewType(position);
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

        public void setLoadingMoreView(View view) {
            footerView.setLoadingMoreView(view);
        }

        public void setNoMoreDataView(View view) {
            footerView.setNoMoreDataView(view);
        }

        private boolean isFooter(int position) {
            return mVisibleFlag && position == getItemCount() - 1;
        }


        private class FooterViewHolder extends RecyclerView.ViewHolder {
            public FooterViewHolder(View itemView) {
                super(itemView);
            }

            void bindView(int viewState) {
                if (viewState == IFooterView.VIEW_STATE_LOADING_MORE) {
                    ((IFooterView) itemView).showLoadingMoreView();
                } else if (viewState == IFooterView.VIEW_STATE_NO_MORE_DATA) {
                    ((IFooterView) itemView).showNoMoreDataView();
                }
            }
        }
    }

    class FooterView extends LinearLayout implements IFooterView {
        private Context mContext;
        private ViewGroup mLoadingMoreView;
        private ViewGroup mNoMoreDataView;

        public FooterView(Context context) {
            super(context);
            initView(context);
        }

        public FooterView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            initView(context);
        }

        private void initView(Context context) {
            mContext = context;

            setGravity(Gravity.CENTER);
            setOrientation(HORIZONTAL);
            setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelOffset(R.dimen.footer_view_height)));
        }

        @Override
        public ViewGroup getLoadingMoreView() {
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
            }

            if (mLoadingMoreView.getParent() != this) {
                addView(mLoadingMoreView);
            }

            return mLoadingMoreView;
        }

        @Override
        public ViewGroup getNoMoreDataView() {
            if (null == mNoMoreDataView) {
                mNoMoreDataView = new LinearLayout(mContext);
                TextView textView = new TextView(mContext);
                textView.setText(getResources().getString(R.string.no_more_data));
                mNoMoreDataView.addView(textView);
            }

            if (mNoMoreDataView.getParent() != this) {
                addView(mNoMoreDataView);
            }

            return mNoMoreDataView;
        }

        @Override
        public void setViewState(@ViewState int viewState) {
            getLoadingMoreView().setVisibility(viewState == VIEW_STATE_LOADING_MORE ? VISIBLE : GONE);
            getNoMoreDataView().setVisibility(viewState == VIEW_STATE_NO_MORE_DATA ? VISIBLE : GONE);
        }

        @Override
        public void showLoadingMoreView() {
            setViewState(VIEW_STATE_LOADING_MORE);
        }

        @Override
        public void showNoMoreDataView() {
            setViewState(VIEW_STATE_NO_MORE_DATA);
        }

        @Override
        public void setLoadingMoreView(View view) {
            if (view instanceof ViewGroup) {
                mLoadingMoreView = (ViewGroup) view;
            } else {
                mLoadingMoreView.removeAllViews();
                mLoadingMoreView.addView(view);
            }
        }

        @Override
        public void setNoMoreDataView(View view) {
            if (view instanceof ViewGroup) {
                mNoMoreDataView = (ViewGroup) view;
            } else {
                mNoMoreDataView.removeAllViews();
                mNoMoreDataView.addView(view);
            }
        }
    }

    public interface IFooterView {
        int VIEW_STATE_LOADING_MORE = 1;
        int VIEW_STATE_NO_MORE_DATA = 2;

        @IntDef({VIEW_STATE_LOADING_MORE, VIEW_STATE_NO_MORE_DATA})
        @interface ViewState {}

        void setViewState(@ViewState int viewState);

        ViewGroup getLoadingMoreView();

        ViewGroup getNoMoreDataView();

        void showLoadingMoreView();

        void showNoMoreDataView();

        void setLoadingMoreView(View view);

        void setNoMoreDataView(View view);
    }

    public interface LoadMoreListener {
        void onLoadMore();
    }
}
