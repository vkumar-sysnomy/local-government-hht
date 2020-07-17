package com.farthestgate.android.ui.notes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.utils.CroutonUtils;
import com.farthestgate.android.utils.StringUtil;

import android.widget.Button;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

import androidx.fragment.app.FragmentActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TextNoteActivity extends FragmentActivity {


    final String START_TEXT = "<text x=\"10\" y=\"15\">";
    final String NEW_LINE_START = "<tspan x=\"10\" y=\"";
    final String END_LINE = "</tspan>";
    final String END_TEXT = "</text></g></svg>";
    final String HEADER = "<?xml version=\"1.0\" standalone=\"yes\"?>" +
            "<svg xmlns='http://www.w3.org/2000/svg' width=\"1080px\" height=\"1920px\" version=\"1.1\">" +
            "<g transform=\"translate(0,0)\">\n";

    private EditText noteView;
    private String   pcnNumber;
    private Integer  observation;
    private TextView txtCharLeft;
    private String  subjectLine;
    private Integer remaining;
    /*private Button btnKeyboard;
    private Button btn949;
    private Button btn950;
    private Button btn951;
    private Button btn952;
    private Button btn953;
    private Button btn954;
    private Button btn955;
    private Button btn956;
    private Button btn957;
    private Button btn948;
    private Button btn913;
    private Button btn919;
    private Button btn901;
    private Button btn914;
    private Button btn916;
    private Button btn921;
    private Button btn917;
    private Button btn905;
    private Button btn911;
    private Button btn912;
    private Button btn945;
    private Button btn997;
    private Button btn915;
    private Button btn900;
    private Button btn902;
    private Button btn903;
    private Button btn904;
    private Button btn906;
    private Button btn907;
    private Button btn908;
    private Button btn946;
    private Button btn922;
    private Button btn920;
    private Button btn999;
    private String[] shortCodes;*/
    private final Integer maxChars = AppConstant.NOTE_SIZE;
    //bespoke shortcut keyboard
    LinearLayout codeLineKeyboard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_text_note);

        subjectLine = "";
//        shortCodes  = CeoApplication.getContext().getResources().getStringArray(R.array.short_codes);
        Intent data = getIntent();
        if (data != null)
        {
            //pcn disorder problem
            if(data.getStringExtra("pcn")!=null && data.getStringExtra("pcn").length()>0){
                pcnNumber = data.getStringExtra("pcn");
            }
            observation = data.getIntExtra("obs",0);
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        noteView = (EditText) findViewById(R.id.noteText);
        txtCharLeft = (TextView) findViewById(R.id.txtChars);
        txtCharLeft.append(String.valueOf(AppConstant.NOTE_SIZE));

        noteView.addTextChangedListener(tw);
        noteView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return false;
            }
        });
        noteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(noteView.getWindowToken(), 0);
            }
        });
        /*btn949 = (Button) findViewById(R.id.btn949);
        btn949.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(949);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn950 = (Button) findViewById(R.id.btn950);
        btn950.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(950);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn951 = (Button) findViewById(R.id.btn951);
        btn951.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(951);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn952 = (Button) findViewById(R.id.btn952);
        btn952.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(952);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn953 = (Button) findViewById(R.id.btn953);
        btn953.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(953);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn954 = (Button) findViewById(R.id.btn954);
        btn954.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(954);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn955 = (Button) findViewById(R.id.btn955);
        btn955.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(955);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn956 = (Button) findViewById(R.id.btn956);
        btn956.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(956);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn957 = (Button) findViewById(R.id.btn957);
        btn957.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(957);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn948 = (Button) findViewById(R.id.btn948);
        btn948.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(948);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn913 = (Button) findViewById(R.id.btn913);
        btn913.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(913);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn919 = (Button) findViewById(R.id.btn919);
        btn919.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(919);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn901 = (Button) findViewById(R.id.btn901);
        btn901.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(901);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn914 = (Button) findViewById(R.id.btn914);
        btn914.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(914);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn916 = (Button) findViewById(R.id.btn916);
        btn916.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(916);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn921 = (Button) findViewById(R.id.btn921);
        btn921.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(921);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn917 = (Button) findViewById(R.id.btn917);
        btn917.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(917);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn905 = (Button) findViewById(R.id.btn905);
        btn905.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(905);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn911 = (Button) findViewById(R.id.btn911);
        btn911.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(911);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn912 = (Button) findViewById(R.id.btn912);
        btn912.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(912);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn945 = (Button) findViewById(R.id.btn945);
        btn945.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(945);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn997 = (Button) findViewById(R.id.btn997);
        btn997.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(997);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn915 = (Button) findViewById(R.id.btn915);
        btn915.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(915);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn900 = (Button) findViewById(R.id.btn900);
        btn900.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(900);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn902 = (Button) findViewById(R.id.btn902);
        btn902.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(902);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn903 = (Button) findViewById(R.id.btn903);
        btn903.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(903);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn904 = (Button) findViewById(R.id.btn904);
        btn904.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(904);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn906 = (Button) findViewById(R.id.btn906);
        btn906.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(906);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn907 = (Button) findViewById(R.id.btn907);
        btn907.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(907);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn908 = (Button) findViewById(R.id.btn908);
        btn908.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(908);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn946 = (Button) findViewById(R.id.btn946);
        btn946.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(946);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn922 = (Button) findViewById(R.id.btn922);
        btn922.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(922);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn920 = (Button) findViewById(R.id.btn920);
        btn920.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(920);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btn999 = (Button) findViewById(R.id.btn999);
        btn999.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try{
                    String codeValue = getCode(999);
                    InsertTextAtPosition(codeValue);
                }catch(Exception e){
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during placing the text");
                }
            }
        });
        btnKeyboard = (Button) findViewById(R.id.btnKeyboard);
        btnKeyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try
                {
                    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.showSoftInput(noteView, InputMethodManager.SHOW_IMPLICIT);

                }
                catch(Exception e)
                {
                    CroutonUtils.error(TextNoteActivity.this,"An error occurred during opening the keyboard");
                }
            }
        });*/
        //bespoke shortcut keyboard
        codeLineKeyboard = (LinearLayout) findViewById(R.id.codeLineKeyboard);
        LinearLayout codeLineLayout =null;
        File keyboardJsonFile =  new File(Environment.getExternalStorageDirectory() + "/" + AppConstant.CONFIG_FOLDER + "keyboard.json");
        if (keyboardJsonFile.exists()) {
            try {
                InputStream fileStream = new FileInputStream(keyboardJsonFile);
                JSONObject keyboardJson = new JSONObject(StringUtil.getStringFromInputStream(fileStream));
                JSONArray keyboardOptions = keyboardJson.getJSONArray("keyboard");
                List<JSONObject> keyboardOptionsList = new ArrayList<JSONObject>();
                for (int i = 0; i < keyboardOptions.length(); i++) {
                    keyboardOptionsList.add(keyboardOptions.getJSONObject(i));
                }
                List<List<JSONObject>> parentList = chopped(keyboardOptionsList, 8);
                List<JSONObject> lastList = parentList.get(parentList.size()-1);
                boolean lastRowFilled = false;
                if(lastList.size()>7) {
                    lastRowFilled = true;
                }
                int layoutCount = parentList.size();
                for(List<JSONObject> layoutRows :parentList){
                    codeLineLayout = new LinearLayout(this);
                    codeLineLayout.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams codeLineLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                    codeLineLayout.setGravity(Gravity.CENTER);
                    codeLineLayoutParams.setMargins(0, 2, 0, 0);
                    codeLineLayout.setLayoutParams(codeLineLayoutParams);
                    int lineControlCount = 1;
                    for(JSONObject options : layoutRows){
                        Button keyButton = new Button(this);
                        LinearLayout.LayoutParams keyButtonParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f);
                        if (lineControlCount != 1) keyButtonParams.setMargins(2, 0, 0, 0);
                        keyButton.setLayoutParams(keyButtonParams);
                        keyButton.setBackgroundColor(Color.BLACK);
                        keyButton.setTextColor(Color.WHITE);
                        keyButton.setTextSize(9.0f);
                        keyButton.setTypeface(null, Typeface.BOLD);
                        keyButton.setText(options.getString("Code"));
                        keyButton.setTag(options.getString("Desc"));
                        codeLineLayout.addView(keyButton);
                        lineControlCount = lineControlCount + 1;
                        keyButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                try {
                                    String codeValue = String.valueOf(arg0.getTag());
                                    InsertTextAtPosition(codeValue);
                                } catch (Exception e) {
                                    CroutonUtils.error(TextNoteActivity.this, "An error occurred during placing the text");
                                }
                            }
                        });
                    }
                    if(layoutCount==1 && !lastRowFilled) {
                        Button keyboardButton = new Button(this);
                        LinearLayout.LayoutParams keyButtonParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f);
                        keyButtonParams.setMargins(2, 0, 0, 0);
                        keyboardButton.setLayoutParams(keyButtonParams);
                        keyboardButton.setBackgroundColor(Color.BLACK);
                        keyboardButton.setTextColor(Color.WHITE);
                        keyboardButton.setTextSize(10.0f);
                        keyboardButton.setTypeface(null, Typeface.BOLD);
                        keyboardButton.setText("keyboard");
                        keyboardButton.setTag("keyboard");
                        codeLineLayout.addView(keyboardButton);
                        keyboardButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                try {
                                    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    mgr.showSoftInput(noteView, InputMethodManager.SHOW_IMPLICIT);
                                } catch (Exception e) {
                                    CroutonUtils.error(TextNoteActivity.this, "An error occurred during opening the keyboard");
                                }
                            }
                        });
                    }
                    codeLineKeyboard.addView(codeLineLayout);
                    layoutCount = layoutCount - 1;
                }
                if(lastRowFilled){
                    codeLineLayout = new LinearLayout(this);
                    codeLineLayout.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams codeLineLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                    codeLineLayout.setGravity(Gravity.CENTER);
                    codeLineLayoutParams.setMargins(0, 2, 0, 0);
                    codeLineLayout.setLayoutParams(codeLineLayoutParams);

                    Button keyboardButton = new Button(this);
                    LinearLayout.LayoutParams keyButtonParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f);
                    keyboardButton.setLayoutParams(keyButtonParams);
                    keyboardButton.setBackgroundColor(Color.BLACK);
                    keyboardButton.setTextColor(Color.WHITE);
                    keyboardButton.setTextSize(10.0f);
                    keyboardButton.setTypeface(null, Typeface.BOLD);
                    keyboardButton.setText("keyboard");
                    keyboardButton.setTag("keyboard");
                    codeLineLayout.addView(keyboardButton);
                    keyboardButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            try {
                                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                mgr.showSoftInput(noteView, InputMethodManager.SHOW_IMPLICIT);
                            } catch (Exception e) {
                                CroutonUtils.error(TextNoteActivity.this, "An error occurred during opening the keyboard");
                            }
                        }
                    });
                    codeLineKeyboard.addView(codeLineLayout);
                }

            }catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    static <T> List<List<T>> chopped(List<T> list, final int L) {
        List<List<T>> parts = new ArrayList<List<T>>();
        final int N = list.size();
        for (int i = 0; i < N; i += L) {
            parts.add(new ArrayList<T>(
                            list.subList(i, Math.min(N, i + L)))
            );
        }
        return parts;
    }
    private void InsertTextAtPosition(String text){
        StringBuilder result = new StringBuilder();
        if(noteView.length()!=0){
            result.append(" ");
        }
        result.append(text);
        result.append(" ");
        text = result.toString();
        int start = noteView.getSelectionStart();
        int end = noteView.getSelectionEnd();
        noteView.getText().replace(Math.min(start, end), Math.max(start, end), text, 0, text.length());
    }
    /*private String getCode(int code)
    {
        for (String st:shortCodes)
        {
            if (st.startsWith(String.valueOf(code)))
                return st.split("-")[1].trim();
        }
        return null;
    }*/


    final TextWatcher tw = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            remaining = maxChars - s.length();
            if (remaining > 0)
                txtCharLeft.setText("Characters remaining : " + remaining);
            else
            {
                CroutonUtils.error(TextNoteActivity.this, "You have reached maximum note size");
                noteView.setSelection(start,start + count);
            }

        }

        @Override
        public void afterTextChanged(Editable s) {


        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notes_menu, menu);
        return true;
    }
    /**
     *   PCN-timestamp-note-seq.svg
     *
     *   or CEO-timestamp-note-seq.svg
     */

    private void saveNote()
    {
        /*String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String noteFolderPath = LoginActivity.CONFIG_PATH_ROOT + "/"
                + LoginActivity.CONFIG_PATH_NOTES;
        String filePath = CeoApplication.CEOLoggedIn.userId + "_" + timeStamp +".svg";


        final File noteFile = new CameraImageHelper().getFile(noteFolderPath, filePath);

        final String finalNote = writeSVG();
        try {
            OutputStream os = new FileOutputStream(noteFile, true);
            os.write(finalNote.getBytes());
            os.close();
        } catch (Exception e){
            e.printStackTrace();
        }*/
        // noteIntent.putExtra("path",noteFile.getAbsolutePath());
        Intent noteIntent = new Intent();
        noteIntent.putExtra("path","");
        noteIntent.putExtra("subject",subjectLine);

      /*  new AsyncTask()
        {
            @Override
            protected Object doInBackground(Object[] params)
            {
                try
                {
                    OutputStream os = new FileOutputStream(noteFile ,true);
                    os.write(finalNote.getBytes());
                    os.close();
                } catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }

                return null;
            }


            @Override
            protected void onPostExecute(Object o)
            {
                super.onPostExecute(o);
                finish();
            }

        }.execute(null,null,null);*/

        noteIntent.putExtra("text", noteView.getText().toString());
        setResult(CeoApplication.RESULT_CODE_NOTES, noteIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id)
        {
            case  R.id.mnuSaveNote:
            {
                subjectLine="";
                saveNote();

               /* AlertDialog.Builder builder = new AlertDialog.Builder(TextNoteActivity.this);
                final EditText tv = new EditText(TextNoteActivity.this);
                builder.setView(tv);
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        subjectLine = tv.getText().toString();
                        saveNote();
                    }
                })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setTitle("Subject Line");

                builder.create().show();*/
                break;
            }
            case R.id.mnuExitNote:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(true)
                        .setTitle("Cancel Note")
                        .setMessage(" Are you sure you want to Exit and Cancel this note ?")
                        .setPositiveButton("Yes",new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();

                break;
            }
            case R.id.mnuClearNote:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(true)
                    .setTitle("Cancel Note")
                    .setMessage(" Are you sure you want to clear this note ?")
                    .setPositiveButton("Yes",new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            noteView.setText("");
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    });
                builder.create().show();

            }
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

    private String writeSVG()
    {
        StringBuilder noteSVG = new StringBuilder();
        noteSVG.append(HEADER);
        noteSVG.append(START_TEXT);

        int x = 15;
        String textLines = noteView.getText().toString();

        for (String line: textLines.split("\n"))
        {
            noteSVG.append(line);
            if (x > 15)
                noteSVG.append(END_LINE);
            x+=15;
            noteSVG.append(NEW_LINE_START + x + "\">");
        }
        noteSVG.append(END_TEXT);
        return noteSVG.toString().replace(NEW_LINE_START + x + "\">","");
    }

}
