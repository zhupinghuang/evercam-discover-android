package io.evercam.connect;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

import java.util.ArrayList;
import java.util.List;

import io.evercam.connect.helper.Constants;
import io.evercam.connect.helper.PropertyReader;
import io.evercam.connect.signin.LoginActivity;
import io.evercam.connect.signin.SignUpActivity;

public class SlideActivity extends Activity implements OnPageChangeListener
{
    private SharedPreferences sharedPrefs;
    private PropertyReader propertyReader;

    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;

    private List<View> views;
    private ImageView[] dots;
    private static final int[] pics = {R.drawable.discover_intro, R.drawable.discover_feature, R
            .drawable.discover_add_camera, R.drawable.discover_next};
    private int currentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.indexslide);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(SlideActivity.this);

        propertyReader = new PropertyReader(getApplicationContext());
        // Bug Sense
        if(propertyReader.isPropertyExist(PropertyReader.KEY_BUG_SENSE))
        {
            String bugSenseCode = propertyReader.getPropertyStr(PropertyReader.KEY_BUG_SENSE);
            BugSenseHandler.initAndStartSession(SlideActivity.this, bugSenseCode);
        }

        if(LoginActivity.isUserLogged(sharedPrefs))
        {
            Intent intentMain = new Intent(SlideActivity.this, DiscoverMainActivity.class);
            startActivity(intentMain);
            finish();
        }
        else
        {
            EvercamDiscover.sendScreenAnalytics(this, getString(R.string.screen_welcome_slides));
            initSlideView();
            initDots();
            initLinks();
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if(propertyReader.isPropertyExist(PropertyReader.KEY_BUG_SENSE))
        {
            BugSenseHandler.startSession(this);
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if(propertyReader.isPropertyExist(PropertyReader.KEY_BUG_SENSE))
        {
            BugSenseHandler.closeSession(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == Constants.REQUEST_CODE_SIGN_IN || requestCode == Constants
                .REQUEST_CODE_SIGN_UP)
        {
            if(resultCode == Constants.RESULT_TRUE)
            {
                Intent intentMain = new Intent(SlideActivity.this, DiscoverMainActivity.class);
                startActivity(intentMain);
                finish();
            }
        }
    }

    private void initSlideView()
    {
        views = new ArrayList<View>();
        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(LayoutParams
                .WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        for(int index = 0; index < pics.length; index++)
        {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(mParams);
            imageView.setImageResource(pics[index]);
            views.add(imageView);
        }
        viewPager = (ViewPager) findViewById(R.id.page);

        viewPagerAdapter = new ViewPagerAdapter(views);
        viewPager.setAdapter(viewPagerAdapter);

        viewPager.setOnPageChangeListener(this);
    }

    private void initLinks()
    {
        TextView loginTextView = (TextView) findViewById(R.id.text_login);
        TextView signUpTextView = (TextView) findViewById(R.id.text_signup);
        Button skipButton = (Button) findViewById(R.id.skipButton);
        loginTextView.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                EvercamDiscover.sendEventAnalytics(SlideActivity.this, R.string
                        .category_welcome_slides, R.string.action_welcome_sign_in_out, R.string
                        .label_welcome_login);

                Intent login = new Intent(SlideActivity.this, LoginActivity.class);
                startActivityForResult(login, Constants.REQUEST_CODE_SIGN_IN);
            }
        });

        signUpTextView.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                EvercamDiscover.sendEventAnalytics(SlideActivity.this, R.string
                        .category_welcome_slides, R.string.action_welcome_sign_in_out, R.string
                        .label_welcome_sign_up);

                Intent signup = new Intent(SlideActivity.this, SignUpActivity.class);
                startActivityForResult(signup, Constants.REQUEST_CODE_SIGN_UP);
            }
        });

        skipButton.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                EvercamDiscover.sendEventAnalytics(SlideActivity.this, R.string
                        .category_welcome_slides, R.string.action_welcome_enter_app, R.string
                        .label_welcome_enter_app);

                Intent skip = new Intent(SlideActivity.this, DiscoverMainActivity.class);
                startActivity(skip);
                finish();
            }
        });
    }

    private void initDots()
    {
        LinearLayout dotLayout = (LinearLayout) findViewById(R.id.dot_layout);
        dots = new ImageView[pics.length];

        for(int index = 0; index < pics.length; index++)
        {
            dots[index] = (ImageView) dotLayout.getChildAt(index);
            dots[index].setEnabled(true);
            dots[index].setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View view)
                {
                    int position = (Integer) view.getTag();
                    setCurrentView(position);
                    setCurrentDot(position);
                }

            });
            dots[index].setTag(index);
        }
        currentIndex = 0;
        dots[currentIndex].setEnabled(false);
    }

    private void setCurrentView(int position)
    {
        if(position < 0 || position >= pics.length)
        {
            return;
        }
        viewPager.setCurrentItem(position);
    }

    private void setCurrentDot(int positon)
    {
        if(positon < 0 || positon > pics.length - 1 || currentIndex == positon)
        {
            return;
        }
        dots[positon].setEnabled(false);
        dots[currentIndex].setEnabled(true);
        currentIndex = positon;
    }

    @Override
    public void onPageScrollStateChanged(int arg0)
    {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2)
    {
    }

    @Override
    public void onPageSelected(int position)
    {
        setCurrentDot(position);
    }

    private class ViewPagerAdapter extends PagerAdapter
    {
        private List<View> views;

        public ViewPagerAdapter(List<View> views)
        {
            this.views = views;
        }

        @Override
        public void destroyItem(View view, int position, Object arg2)
        {
            ((ViewPager) view).removeView(views.get(position));
        }

        @Override
        public int getCount()
        {
            if(views != null)
            {
                return views.size();
            }
            return 0;
        }

        @Override
        public Object instantiateItem(View view, int position)
        {
            ((ViewPager) view).addView(views.get(position), 0);
            return views.get(position);
        }

        @Override
        public boolean isViewFromObject(View view, Object object)
        {
            return (view == object);
        }
    }
}