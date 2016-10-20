package com.joe.foldingtabbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.joe.foldingtabbar.view.FoldingTabBar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FoldingTabBar foldingTabBar1 = (FoldingTabBar) findViewById(R.id.folding_tab_bar1);
        FoldingTabBar foldingTabBar2 = (FoldingTabBar) findViewById(R.id.folding_tab_bar2);
        FoldingTabBar foldingTabBar3 = (FoldingTabBar) findViewById(R.id.folding_tab_bar3);
        FoldingTabBar foldingTabBar4 = (FoldingTabBar) findViewById(R.id.folding_tab_bar4);

        foldingTabBar1.setItemsIcons(R.drawable.search_icon, R.drawable.nearby_icon, R.drawable.me_icon, R.drawable.settings_icon);
        foldingTabBar2.setItemsIcons(R.drawable.search_icon, R.drawable.nearby_icon, R.drawable.me_icon);
        foldingTabBar3.setItemsIcons(R.drawable.search_icon, R.drawable.nearby_icon, R.drawable.me_icon, R.drawable.settings_icon, R.drawable.new_chat_icon);
        foldingTabBar4.setItemsIcons(R.drawable.search_icon, R.drawable.nearby_icon, R.drawable.me_icon, R.drawable.settings_icon, R.drawable.new_chat_icon, R.drawable.chats_icon);

        foldingTabBar1.setOnTabItemClickListener(tabItemClickListener1);
        foldingTabBar2.setOnTabItemClickListener(tabItemClickListener2);
        foldingTabBar3.setOnTabItemClickListener(tabItemClickListener3);
        foldingTabBar4.setOnTabItemClickListener(tabItemClickListener4);

    }

    FoldingTabBar.OnTabItemClickListener tabItemClickListener1 = new FoldingTabBar.OnTabItemClickListener() {
        @Override
        public void onItemClick(int position) {
            Toast.makeText(MainActivity.this, "tab1 position" + position + " clicked", Toast.LENGTH_SHORT).show();
        }
    };

    FoldingTabBar.OnTabItemClickListener tabItemClickListener2 = new FoldingTabBar.OnTabItemClickListener() {
        @Override
        public void onItemClick(int position) {
            Toast.makeText(MainActivity.this, "tab2 position" + position + " clicked", Toast.LENGTH_SHORT).show();
        }
    };

    FoldingTabBar.OnTabItemClickListener tabItemClickListener3 = new FoldingTabBar.OnTabItemClickListener() {
        @Override
        public void onItemClick(int position) {
            Toast.makeText(MainActivity.this, "tab3 position" + position + " clicked", Toast.LENGTH_SHORT).show();
        }
    };

    FoldingTabBar.OnTabItemClickListener tabItemClickListener4 = new FoldingTabBar.OnTabItemClickListener() {
        @Override
        public void onItemClick(int position) {
            Toast.makeText(MainActivity.this, "tab4 position" + position + " clicked", Toast.LENGTH_SHORT).show();
        }
    };

}
