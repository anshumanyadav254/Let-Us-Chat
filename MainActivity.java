package com.luciferhacker.letuschat;

import android.content.Intent;

import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity<sectionsPageAdapter> extends AppCompatActivity implements MyStringsConstant {

    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private SectionPagerAdapter mSectionPagerAdapter;
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabase;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        if( mCurrentUser != null ){

            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(strUSERS_DATABASE).child(mCurrentUser.getUid());
        }

        mToolbar = (Toolbar) findViewById(R.id.main_appbar_include);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(strLetUsChat);

        mSectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.main_tabs_viewPager);
        mViewPager.setAdapter(mSectionPagerAdapter);
        mTabLayout = (TabLayout) findViewById(R.id.main_tabs_tabLayout);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    // When initializing your Activity, check to see if the user is currently signed in.
    @Override
    public void onStart() {
        super.onStart();

        if (mCurrentUser != null) {
            /* ONLINE USER */
            mUsersDatabase.child(strONLINE).setValue(strTRUE);

        } else {
            sendToStart();
        }
    }

    protected void onStop(){
        super.onStop();

        if (mCurrentUser != null) {
            /* OFFLINE USER */
            mUsersDatabase.child(strONLINE).setValue(ServerValue.TIMESTAMP);
        }
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.mainMenu_logOut_item) {
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }
        if (item.getItemId() == R.id.mainMenu_aboutUs_item) {
            Intent aboutUsIntent = new Intent(MainActivity.this, AboutUsActivity.class);
            startActivity(aboutUsIntent);
        }
        if (item.getItemId() == R.id.mainMenu_accountSettings_item) {
            Intent settingIntent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(settingIntent);
        }
        if (item.getItemId() == R.id.mainMenu_allUsers_item) {
            Intent userIntent = new Intent(MainActivity.this, AllUserActivity.class);
            startActivity(userIntent);
        }

        return true;
    }
}