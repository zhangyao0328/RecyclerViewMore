package com.recyclerviewmore.fragment;

import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;

import com.recyclerviewmore.R;
import com.recyclerviewmore.adapter.FragmentOneAdapter;
import com.recyclerviewmore.base.BaseFrgment;
import com.recyclerviewmore.widget.LoadMoreListener;
import com.recyclerviewmore.widget.RecyclerViewUpRefresh;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * zhangyao
 * 16/9/24
 * zhangyao@jiandanxinli.com
 */

public class FragmentOne extends BaseFrgment implements LoadMoreListener, SwipeRefreshLayout.OnRefreshListener{

    @BindView(R.id.recyclerview)
    RecyclerViewUpRefresh mRecyclerView;
    @BindView(R.id.swiprefreshlayout)
    SwipeRefreshLayout mSwipRefreshLayout;

    FragmentOneAdapter adapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_one;
    }

    @Override
    public void init() {

        mSwipRefreshLayout.setOnRefreshListener(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new FragmentOneAdapter(getActivity());
        mRecyclerView.setAdapter(adapter);
        adapter.addItems(initData());
        mRecyclerView.setCanloadMore(true);
        mRecyclerView.setLoadMoreListener(this);
    }
    @Override
    public void onLoadMore() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mSwipRefreshLayout.isRefreshing()){
                    mSwipRefreshLayout.setRefreshing(false);
                }
                adapter.addItems(initData());
                mRecyclerView.loadMoreComplete();
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
                mSwipRefreshLayout.setRefreshing(false);
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
