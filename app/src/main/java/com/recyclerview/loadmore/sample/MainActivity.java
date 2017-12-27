package com.recyclerview.loadmore.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.gigamole.library.navigationtabstrip.NavigationTabStrip;
import com.recyclerview.loadmore.sample.adapter.FragmentAdapter;
import com.recyclerview.loadmore.sample.fragment.FragmentOne;
import com.recyclerview.loadmore.sample.fragment.FragmentThree;
import com.recyclerview.loadmore.sample.fragment.FragmentTwo;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.vp)
    ViewPager viewPager;
    @BindView(R.id.nts_bottom)
    NavigationTabStrip mBottomNavigationTabStrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();
    }

    public void init() {

        ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();

        fragmentList.add(new FragmentOne());
        fragmentList.add(new FragmentTwo());
        fragmentList.add(new FragmentThree());

        viewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager(), fragmentList));
        viewPager.setOffscreenPageLimit(2);
        mBottomNavigationTabStrip.setViewPager(viewPager, 0);
    }
}
