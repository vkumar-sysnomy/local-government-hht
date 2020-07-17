package com.farthestgate.android.ui.dialogs.vehicle_logging;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.model.PCN;
import com.farthestgate.android.ui.components.NoSwipeViewPager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Hanson Aboagye on 29/01/14.
 */


public class VehicleMakesActivity extends FragmentActivity implements MakesAdapter.MakeModelAdapterListener,
                                                                      ModelAdapter.ModelAdapterListener,
                                                                      ColoursAdapter.ColourAdapterListener
{

    private FragmentManager fragmentManager;
    private NoSwipeViewPager    makeModelPager;
    private ListPagerAdapter    listPagerAdapter;

    public static PCN currentPCN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //overridePendingTransition(R.anim.anim_slide_in_left, R.anim.fadeout_long);  // slide animation not needed ...
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_make_model);
        fragmentManager = getSupportFragmentManager();
        makeModelPager = (NoSwipeViewPager) findViewById(R.id.listPager);

        try {
            Field scrollerField = ViewPager.class.getDeclaredField("mScroller");
            FixedSpeedScroller slowScroller = new FixedSpeedScroller(this, new DecelerateInterpolator());
            scrollerField.set(makeModelPager, slowScroller);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        makeModelPager.setOffscreenPageLimit(2);
        //Could use account manager to capture preferences
        listPagerAdapter = new ListPagerAdapter(fragmentManager);
        makeModelPager.setAdapter(listPagerAdapter);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }



    public static JSONArray sortJsonArray(JSONArray array) throws JSONException
    {
        List<JSONObject> jsons = new ArrayList<JSONObject>();
        for (int i = 0; i < array.length(); i++) {
            jsons.add(array.getJSONObject(i));
        }
        Collections.sort(jsons, new Comparator<JSONObject>()
        {
            @Override
            public int compare(JSONObject lhs, JSONObject rhs)
            {
                try
                {
                    String leftName = lhs.getString("name");
                    String rightName = rhs.getString("name");

                    return leftName.compareTo(rightName);
                } catch (JSONException jex)
                {
                    jex.printStackTrace();

                    return 0;
                }
            }
        });
        return new JSONArray(jsons);
    }

    /**
     * Interface implementations from the adapter
     *
     */
    @Override
    public void onSelect(Integer manufacturerID, MakeRowItem.ITEM_MODES mode, int position, Boolean isUserFavourite) {
        ((ModelsSelectFragment) listPagerAdapter.getItem(1)).loadModels(manufacturerID, this, false);
        if (mode == MakeRowItem.ITEM_MODES.EXPAND_VIEW) {
            makeModelPager.setCurrentItem(1, true);
        }
        currentPCN.manufacturer = MakesSelectFragment.manufacturerList.get(position);
    }

    @Override
    public void onBackPressed() {

      /*  if (ModelsSelectFragment.colourList != null)
            ModelsSelectFragment.colourList.clear();
*/
        int index = makeModelPager.getCurrentItem();
        if (index != 0) {
                makeModelPager.setCurrentItem(makeModelPager.getCurrentItem()-1);

            if (index == 1)
                ModelsSelectFragment.clearList();
        }
        else
        {
            setResult(CeoApplication.RESULT_CODE_VEHICLE_DIALOG_CANCEL);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onSelectModel(String makeID, ModelRowItem.ITEM_MODES mode, int position, Boolean isUserFavourite)
    {
        makeModelPager.setCurrentItem(2, true);
        currentPCN.model = ModelsSelectFragment.modelList.get(position);


    }

    @Override
    public void onSelectColour(String colourName, int colourValue)
    {
        currentPCN.colourName = colourName;
        setResult(CeoApplication.RESULT_CODE_VEHICLE_DIALOG);
        finish();
    }

    private class ListPagerAdapter extends FragmentStatePagerAdapter
    {

        private String manufacturerID;
        private String vehicleMakeName;


        public ListPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return MakesSelectFragment.newInstance(VehicleMakesActivity.this);
                case 1:
                    return ModelsSelectFragment.newInstance();
                case 2:
                    return ColourSelectFragment.newInstance();
            }
            return null;
        }


    }

    private class FixedSpeedScroller extends Scroller
    {

        private int mDuration = 6000;

        public FixedSpeedScroller(Context context) {
            super(context);
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator, boolean flywheel) {
            super(context, interpolator, flywheel);
        }


        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }
    }
}
