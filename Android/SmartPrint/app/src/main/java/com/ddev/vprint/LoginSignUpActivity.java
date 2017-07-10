package com.ddev.vprint;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Home on 10/01/2017.
 */

public class LoginSignUpActivity extends AppCompatActivity {

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_sign_up);

        pref = getSharedPreferences("com.ddev.vprint", MODE_PRIVATE);
        editor = pref.edit();

        editor.putBoolean("first_run", false).commit();

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new LoginSignUpViewPagerAdapter(getSupportFragmentManager()));
    }

    class LoginSignUpViewPagerAdapter extends FragmentPagerAdapter{

        public LoginSignUpViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new LoginFragment();
                case 1:
                    return new SignUpFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
