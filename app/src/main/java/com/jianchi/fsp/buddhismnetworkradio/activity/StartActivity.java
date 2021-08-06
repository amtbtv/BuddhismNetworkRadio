package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.tools.SharedPreferencesHelper;
import com.jianchi.fsp.buddhismnetworkradio.tools.Tools;

import java.util.ArrayList;
import java.util.List;

/**
 * 起始活动窗口
 * 包含两个节目列表：电视节目，随机点播
 * 处理启动事务，根据传过来的参数 StartWith 判断是否要直接跳转，并在启动本地或远程播放器时传入参数 StartWith
 */
public class StartActivity extends BaseActivity {

    ViewPager mViewPager;
    TabLayout mTabLayout;
    PagerAdapter mPagerAdapter;
    private String[] titles;
    private List<Fragment> fragments;

    @Override
    int getContentView() {
        return R.layout.activity_start;
    }

    @Override
    void onCreateDo() {

        setTitle(R.string.app_name);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);

        titles = new String[4];
        titles[0] = getString(R.string.bt_label_spzb);
        titles[1] = getString(R.string.bt_label_ypdb);
        titles[2] = getString(R.string.bt_label_fyxl);
        titles[3] = getString(R.string.bt_label_whjy);

        if (app.isNetworkConnected()) {
            initView();
        } else {
            Toast.makeText(getThisActivity(), R.string.no_network, Toast.LENGTH_LONG).show();
        }


        //恢复之前的播放界面
        String startWith = getIntent().getStringExtra("StartWith");
        if (startWith != null && startWith.equals("StartWith_MP3_SERVICE")) {
            startActivity(new Intent(getThisActivity(), Mp3PlayerActivity.class));
        }
    }

    void initView() {
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        fragments = new ArrayList<>();
        fragments.add(new LiveFragment());
        fragments.add(new ProgramFragment());
        fragments.add(new FaYinFragment());
        fragments.add(new WeiHuaFragment());
        mPagerAdapter.setTitles(titles);
        mPagerAdapter.setFragments(fragments);
        mViewPager.setAdapter(mPagerAdapter);
        //将TabLayout和ViewPager绑定
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
    }

    class PagerAdapter extends FragmentPagerAdapter {

        private String[] titles;

        private List<Fragment> fragments = null;

        public void setTitles(String[] titles) {
            this.titles = titles;
        }

        public void setFragments(List<Fragment> fragments) {
            this.fragments = fragments;
        }

        public PagerAdapter(FragmentManager fm) {
            super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent intent = new Intent(this, ContactActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_news) {
            Intent intent = new Intent(this, NewsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_download) {
            Intent intent = new Intent(this, DownLoadActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_zh_tw) {
            //切换语言
            if (BApplication.country.equals("ZH"))
                BApplication.country = "TW";
            else
                BApplication.country = "ZH";
            new SharedPreferencesHelper(this, "setting").putString("local", BApplication.country);
            Tools.changeAppLanguage(this);
            recreate();//刷新界面
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
