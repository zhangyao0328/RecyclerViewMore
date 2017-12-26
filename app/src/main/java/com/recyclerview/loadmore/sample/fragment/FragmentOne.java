package com.recyclerview.loadmore.sample.fragment;

import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Gravity;

import redroid.widget.LoadMoreRecyclerView;

import com.recyclerview.loadmore.sample.R;
import com.recyclerview.loadmore.sample.adapter.FragmentOneAdapter;
import com.recyclerview.loadmore.sample.base.BaseFrgment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

import static android.content.ContentValues.TAG;
import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

/**
 * zhangyao
 * 16/9/24
 * zhangyao@jiandanxinli.com
 */

public class FragmentOne extends BaseFrgment implements LoadMoreRecyclerView.LoadMoreListener, SwipeRefreshLayout.OnRefreshListener{

    @BindView(R.id.recyclerview)
    LoadMoreRecyclerView mRecyclerView;
    @BindView(R.id.swiprefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    FragmentOneAdapter adapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_one;
    }

    @Override
    public void init() {

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new FragmentOneAdapter(getActivity());
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), VERTICAL));
        adapter.addItems(initData());
        mRecyclerView.setLoadMoreListener(this);
    }

    private int page = 0;

    @Override
    public void onLoadMore() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mSwipeRefreshLayout.isRefreshing()){
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                Log.d(TAG, "run: abc");

                if (page < 3) {
                    adapter.addItems(initData());
                    mRecyclerView.loadMoreComplete();
                    page++;
                } else {
                    mRecyclerView.showNoMoreDataView();
                }
            }
        }, 2000);
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mRecyclerView.isLoadingData){
                    mRecyclerView.loadMoreComplete();
                }
                adapter.resetItems(initData());
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    private List initData(){
        List<String> mList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            mList.add(i + "");
        }
        return mList;
    }
}
