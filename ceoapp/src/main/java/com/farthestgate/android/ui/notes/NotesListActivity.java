package com.farthestgate.android.ui.notes;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.farthestgate.android.R;

public class NotesListActivity extends Activity {

    int observationNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_notes_list);

        observationNumber = getIntent().getIntExtra("obs",0);

        if (savedInstanceState == null) {

           /* getFragmentManager().beginTransaction()
                    .add(R.id.container, new NoteListFragment())
                    .commit();*/

            getFragmentManager().beginTransaction()
                    .add(R.id.container, NoteListFragment.newInstance(observationNumber))
                    .commit();
        }
    }



}
