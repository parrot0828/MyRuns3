package kangwon.cse.jck.myruns3;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 탭과 뷰페이저를 갖는 피트니스 앱의 메인 화면.
 * 뷰페이저는 세 개의 프래그먼트를 보여준다.
 */
public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private List<Fragment> frags;

    // 세 개의 탭에 대응되는 세 개의 화면.
    private Fragment startFragment, historyFragment, settingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);   // 툴바를 액션바로 사용하도록 설정.

        // Create fragment list
        startFragment = new StartFragment();
        historyFragment = new HistoryFragment();
        settingFragment = new SettingsFragment();

        frags = new ArrayList<Fragment>();
        frags.add(startFragment);
        frags.add(historyFragment);
        frags.add(settingFragment);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), frags);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);   // 탭을 뷰페이저와 연동시킴.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // 오버플로우 메뉴(세로로 늘어선 섬 세 개)로 들어갈 메뉴아이템이 있을 때는
        // 레이아웃 파일에서 아이콘을 지정해 주지 않아도
        // 자동으로 오버플로우 메뉴가 생긴다.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        int id = item.getItemId();

        // 아무 일도 하지 않는다. (아직 미구현)
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        List<Fragment> fragments;

        public SectionsPagerAdapter(FragmentManager fm, List<Fragment> frags) {
            super(fm);
            fragments = frags;
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
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "시작";
                case 1:
                    return "기록";
                case 2:
                    return "설정";
            }
            return null;
        }
    }
}
