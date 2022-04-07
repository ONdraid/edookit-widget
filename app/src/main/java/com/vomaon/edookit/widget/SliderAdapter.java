package com.vomaon.edookit.widget;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class SliderAdapter extends PagerAdapter {

    private final Context context;
    private final int[] slideImages;
    private final String[] slideHeadings;
    private final String[] slideDescriptions;

    SliderAdapter(Context context,int[] slideImages, String[] slideHeadings, String[] slideDescriptions) {
        this.context = context;
        this.slideImages = slideImages;
        this.slideHeadings = slideHeadings;
        this.slideDescriptions = slideDescriptions;
    }



    @Override
    public int getCount() {
        return slideHeadings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slide_layout, container, false);

        ImageView slideImageView = view.findViewById(R.id.previewImageView);
        TextView slideHeading = view.findViewById(R.id.headerTitleTextView);
        TextView slideDescription = view.findViewById(R.id.descriptionTextView);

        slideImageView.setImageResource(slideImages[position]);
        slideHeading.setText(slideHeadings[position]);

        if (position == 2) {
            slideDescription.setClickable(true);
            slideDescription.setMovementMethod(LinkMovementMethod.getInstance());
            String text = "<a href='https://github.com/ONdraid/edookit-widget/blob/master/T&C.md#languages'> "+ slideDescriptions[2] + " </a>";
            slideDescription.setText(Html.fromHtml(text));
        } else {slideDescription.setText(slideDescriptions[position]);}

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout)object);
    }

}
