package com.vomaon.edookit.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class OnboardingScreenActivity extends AppCompatActivity {

    private TextView[] dots;
    private LinearLayout dotsLayout;
    private Button nextButton;
    private Button backButton;
    private int currentPage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_screen);

        ViewPager slideViewPager = findViewById(R.id.slideViewPager);
        dotsLayout = findViewById(R.id.dotsLayout);
        nextButton = findViewById(R.id.nextButton);
        backButton = findViewById(R.id.previousButton);

        int[] slideImages = {
                R.drawable.widgets_preview,
                R.drawable.browser_preview,
                R.drawable.terms_and_conditions_icon
        };

        String[] slideHeadings = {
                this.getString(R.string.header1),
                this.getString(R.string.header2),
                this.getString(R.string.header3)
        };

        String[] slideDescriptions = {
                this.getString(R.string.description1),
                this.getString(R.string.description2),
                this.getString(R.string.description3)
        };

        SliderAdapter sliderAdapter = new SliderAdapter(this, slideImages, slideHeadings, slideDescriptions);
        slideViewPager.setAdapter(sliderAdapter);

        addDotsIndicator(0);
        slideViewPager.addOnPageChangeListener(onPageChangeListener);

        nextButton.setOnClickListener(view -> {
            if(currentPage == dots.length - 1) {
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
                @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("introduced", true);
                editor.apply();

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                slideViewPager.setCurrentItem(currentPage + 1);
            }
        });

        backButton.setOnClickListener(view -> slideViewPager.setCurrentItem(currentPage - 1));
    }

    private void addDotsIndicator(int position) {
        dots = new TextView[3];
        dotsLayout.removeAllViews();

        for(int i = 0; i < dots.length; i++){
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.black));
            dotsLayout.addView(dots[i]);
        }

        if(dots.length > 0){
            dots[position].setTextColor(getResources().getColor(R.color.white));
        }
    }

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @SuppressLint("SetTextI18n")
        @Override
        public void onPageSelected(int position) {
            addDotsIndicator(position);
            currentPage = position;

            if(position == 0) {
                nextButton.setEnabled(true);
                backButton.setEnabled(false);
                backButton.setVisibility(View.INVISIBLE);

                nextButton.setText(R.string.next);
                backButton.setText("");
            } else if (position == dots.length - 1) {
                nextButton.setEnabled(true);
                backButton.setEnabled(true);
                backButton.setVisibility(View.VISIBLE);

                nextButton.setText(R.string.agree);
                backButton.setText(R.string.back);
            } else {
                nextButton.setEnabled(true);
                backButton.setEnabled(true);
                backButton.setVisibility(View.VISIBLE);

                nextButton.setText(R.string.next);
                backButton.setText(R.string.back);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
}
