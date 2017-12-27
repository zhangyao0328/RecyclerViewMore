package com.recyclerview.loadmore.sample.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

/**
 * zhangyao
 * 16/9/24
 * zhangyao@jiandanxinli.com
 */

public abstract class BaseFrgment extends Fragment{

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mRootView = inflater.inflate(getLayoutId(), container, false);
        ButterKnife.bind(this, mRootView);
        init();
        return mRootView;
    }

    protected abstract int getLayoutId();

    public abstract void init();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.bind(getActivity()).unbind();
    }
}
