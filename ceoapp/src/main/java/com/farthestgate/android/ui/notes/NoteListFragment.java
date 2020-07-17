package com.farthestgate.android.ui.notes;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ListView;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.helper.NotesListAdapter;
import com.farthestgate.android.model.Note;
import com.farthestgate.android.model.PCN;
import com.farthestgate.android.model.database.NotesTable;
import com.farthestgate.android.model.database.TimerObjTable;
import com.farthestgate.android.ui.pcn.PCNStartActivity;
import com.farthestgate.android.utils.CroutonUtils;
import com.farthestgate.android.utils.ImageFileUtils;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NoteListFragment extends Fragment {

    int observationNumber;

    public static NoteListFragment newInstance(int observationNumber){
        NoteListFragment noteListFragment = new NoteListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("obs", observationNumber);
        noteListFragment.setArguments(bundle);
        return noteListFragment;
    }

    public NoteListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        observationNumber = getArguments().getInt("obs");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notes_list, container, false);

        ListView notesList = (ListView) rootView.findViewById(R.id.noteList);
        final ArrayList<String> notesPaths = new ArrayList<String>();
        final ArrayList<String> notesText = new ArrayList<String>();
        final ArrayList<String> noteListings = new ArrayList<String>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss dd MMM yy");
        List<NotesTable> notesTables;
        if(observationNumber == 0){
            String observations = "";
            List<TimerObjTable> timerObjTables = DBHelper.getTimers();
            for(TimerObjTable timerObjTable : timerObjTables){
                timerObjTable.getTimerJSON();
                PCN pcn = new GsonBuilder().create().fromJson(timerObjTable.getTimerJSON(), PCN.class);
                if(observations.isEmpty()) {
                    observations += pcn.observationNumber;
                } else{
                    observations += "," + pcn.observationNumber;
                }
            }

            notesTables = DBHelper.getNotes(observations);
        } else{
            notesTables = DBHelper.getPCNNotes(observationNumber);
        }
        for (NotesTable nt : notesTables) {
            notesPaths.add(nt.getFileName());
            notesText.add(nt.getNoteText());
            if(nt.getSubjectLine()!=null && !nt.getSubjectLine().isEmpty()){
                noteListings.add(nt.getSubjectLine() + " - " + simpleDateFormat.format(new Date(nt.getNoteDate())));
            }else{
                noteListings.add(simpleDateFormat.format(new Date(nt.getNoteDate())));
            }
        }

        NotesListAdapter notesAdapter = new NotesListAdapter(inflater);
        notesAdapter.setItems(noteListings);


        notesList.setAdapter(notesAdapter);

        final WebView mainView = (WebView) rootView.findViewById(R.id.webViewnote);

        if (notesAdapter.getCount() > 0)
        {
            String notesSourceOrPath = notesPaths.get(0);
            if (notesSourceOrPath.length() > 0) {
                mainView.loadUrl("file:///" + notesSourceOrPath);
            } else {
                if(notesText.size() > 0){
                    notesSourceOrPath = notesText.get(0);
                    StringBuilder sb = new StringBuilder("<html><body>");
                    sb.append(notesSourceOrPath);
                    sb.append("</body></html>");
                    mainView.loadData(notesSourceOrPath, "text/html", "UTF-8");
                }
            }
        }
        else
            CroutonUtils.info(getActivity(), "You have no notes at the moment in this session");
        mainView.getSettings().setBuiltInZoomControls(true);
        mainView.getSettings().setDisplayZoomControls(false);
        mainView.getSettings().setUseWideViewPort(true);

        mainView.setWebChromeClient(new WebChromeClient()
        {
            public void onProgressChanged(WebView view, int progress)
            {
            }
        });

        mainView.setWebViewClient(new WebViewClient()
        {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
                CroutonUtils.error(getActivity(), "Error loading image").show();
            }

            @Override
            public void onPageFinished(WebView webview, String url)
            {
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                mainView.loadUrl(url);
                return true;
            }
        });


        notesList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String notesSourceOrPath = notesPaths.get(position);
                if (notesSourceOrPath.length() > 0) {
                    mainView.loadUrl("file:///" + notesSourceOrPath);
                } else {
                    if(notesText.size() > 0){
                        notesSourceOrPath = notesText.get(position);
                        StringBuilder sb = new StringBuilder("<html><body>");
                        sb.append(notesSourceOrPath);
                        sb.append("</body></html>");
                        mainView.loadData(notesSourceOrPath, "text/html", "UTF-8");
                    }
                }
            }
        });

        return rootView;
    }
}

