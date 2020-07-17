package com.farthestgate.android.ui.messages;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import com.farthestgate.android.R;
import com.farthestgate.android.ui.admin.BaseActivity;

public class MessagesActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_break);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new BriefingNotesFragment())
                    .commit();
        }
    }


    @Override
    public void onBackPressed() {
       finish();
    }
}
