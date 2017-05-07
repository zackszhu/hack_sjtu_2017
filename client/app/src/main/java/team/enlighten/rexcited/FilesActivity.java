package team.enlighten.rexcited;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class FilesActivity extends AppCompatActivity {
    ViewPager pager;
    TabLayout tabs;
    TabImport tabImport;
    TabAllFiles tabAllFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

        tabs = (TabLayout) findViewById(R.id.files_tabs);
        pager = ((ViewPager) findViewById(R.id.files_pager));
        tabImport = new TabImport();
        tabImport.type = getIntent().getStringExtra("type");
        tabAllFiles = new TabAllFiles();
        tabAllFiles.type = getIntent().getStringExtra("type");

        tabs.addTab(tabs.newTab().setText(R.string.import_file));
        tabs.addTab(tabs.newTab().setText(R.string.all_files));

        pager.setAdapter(
                new FragmentStatePagerAdapter(getSupportFragmentManager()) {
                    @Override
                    public int getCount() {
                        return 2;
                    }

                    @Override
                    public Fragment getItem(int position) {
                        switch (position) {
                            case 0:
                                return tabImport;
                            case 1:
                                return tabAllFiles;
                            default:
                                return null;
                        }
                    }
                });
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
        tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
