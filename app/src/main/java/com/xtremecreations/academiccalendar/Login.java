package com.xtremecreations.academiccalendar;

import android.*;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity {
    Animation anim;
    ImageView ico_splash,search_button;
    RelativeLayout login_div,logo_div,splash_cover,title,loading,adminPane,studentPane;
    EditText email,search;
    TextView signin,heading,upload,loadTitle,search_results;
    ProgressBar nextLoad;
    ArrayList<String> dates,events;
    DatabaseReference fdb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        ActivityCompat.requestPermissions(Login.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        ico_splash=findViewById(R.id.ico_splash);
        login_div=findViewById(R.id.login_div);
        logo_div=findViewById(R.id.logo_div);
        splash_cover=findViewById(R.id.splash_cover);
        title=findViewById(R.id.title);
        adminPane=findViewById(R.id.adminPane);
        studentPane=findViewById(R.id.studentPane);
        nextLoad=findViewById(R.id.nextLoad);
        loading=findViewById(R.id.loading);
        search_results=findViewById(R.id.search_results);

        dates = new ArrayList<>();
        events = new ArrayList<>();

        loadTitle=findViewById(R.id.loadTitle);
        loadTitle.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2.ttf"));

        heading=findViewById(R.id.heading);
        heading.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/vdub.ttf"));

        search_button=findViewById(R.id.search_button);
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showKeyboard(search,false);performSearch(search.getText().toString());
            }
        });

        email=findViewById(R.id.email);
        email.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2.ttf"));
        email.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            performSignIn();
                            return true;
                        default:break;
                    }
                }
                return false;
            }
        });

        search=findViewById(R.id.search);
        search.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2.ttf"));
        search.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            performSearch(search.getText().toString());
                            return true;
                        default:break;
                    }
                }
                return false;
            }
        });
        search.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                studentReset();
                return false;
            }
        });

        signin=findViewById(R.id.signin);
        signin.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/vdub.ttf"));
        signin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        signin.setBackgroundResource(R.drawable.signin_pressed);signin.setTextColor(Color.parseColor("#ffffff"));
                        break;
                    case MotionEvent.ACTION_UP:
                        signin.setBackgroundResource(R.drawable.signin);signin.setTextColor(Color.parseColor("#ff611c"));
                        performSignIn();
                        break;
                }
                return true;
            }
        });

        upload=findViewById(R.id.upload);
        upload.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2.ttf"));
        upload.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        upload.setBackgroundResource(R.drawable.signin_pressed);upload.setTextColor(Color.parseColor("#ffffff"));
                        break;
                    case MotionEvent.ACTION_UP:
                        upload.setBackgroundResource(R.drawable.signin);upload.setTextColor(Color.parseColor("#ff611c"));
                        showFileChooser();
                        break;
                }
                return true;
            }
        });


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.logo_trans);
                splash_cover.setVisibility(View.GONE);
                ico_splash.setImageResource(R.drawable.logo);
                Animation anima = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.logo_reveal);
                logo_div.setVisibility(View.VISIBLE);logo_div.startAnimation(anima);ico_splash.startAnimation(anim);
                new Handler().postDelayed(new Runnable() {@Override public void run() {
                    scaleY(login_div,48,300,new AccelerateDecelerateInterpolator());}},800);
            }},1500);
    }
    public void performSearch(final String date)
    {
        if(Pattern.compile("^[0-9]{2}[/][0-9]{2}[/][0-9]{2}$", Pattern.CASE_INSENSITIVE) .matcher(date).find())
        {
            new Handler().postDelayed(new Runnable() {@Override public void run() {
                scaleY(login_div,450,350,new OvershootInterpolator());
                search_results.setVisibility(View.VISIBLE);
                String showevent="No Results";
                try
                {
                    showevent=("● "+(events.get(dates.indexOf(date))).replace("\n","\n● "));
                    showevent=showevent.substring(0,showevent.length()-2);search_results.setGravity(Gravity.START|Gravity.TOP);
                }
                catch (Exception e){search_results.setGravity(Gravity.CENTER);}
                search_results.setText(showevent);
            }},250);
        }
        else {Toast.makeText(this, "Enter a valid date", Toast.LENGTH_SHORT).show();}
    }
    public void performSignIn()
    {
        if(isStudent(email.getText().toString())==1)
        {
            dates = new ArrayList<>();events = new ArrayList<>();showKeyboard(email,false);loading.setVisibility(View.VISIBLE);
            (FirebaseDatabase.getInstance().getReference("dates")).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){dates.add(postSnapshot.getValue().toString());}
                    (FirebaseDatabase.getInstance().getReference("events")).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){events.add(postSnapshot.getValue().toString());}
                            scaleY(login_div,127,350,new OvershootInterpolator());
                            email.setVisibility(View.GONE);title.setVisibility(View.VISIBLE);heading.setText("STUDENT LOGIN");
                            loading.setVisibility(View.GONE);studentPane.setVisibility(View.VISIBLE);
                        }
                        @Override
                        public void onCancelled(DatabaseError error) {}
                    });
                }
                @Override
                public void onCancelled(DatabaseError error) {}
            });
        }
        else if(isStudent(email.getText().toString())==0)
        {
            showKeyboard(email,false);scaleY(login_div,200,350,new OvershootInterpolator());
            email.setVisibility(View.GONE);title.setVisibility(View.VISIBLE);heading.setText("ADMIN LOGIN");
            adminPane.setVisibility(View.VISIBLE);
        }
        else {Toast.makeText(this, "Please Enter Correct ID", Toast.LENGTH_SHORT).show();}
    }

    public void getRawFromPDF(String path)
    {
        String parsedText="";
        try {
            PdfReader reader = new PdfReader(path);int n = reader.getNumberOfPages();
            for(int i=0;i<n;i++){parsedText=parsedText+PdfTextExtractor.getTextFromPage(reader,i+1).trim()+"\n";}
            getDataFromRaw(parsedText);reader.close();loadTitle.setText("Reading data");
        }
        catch (Exception e) {
            ButtonLoading(false,"Can't Find File");
            new Handler().postDelayed(new Runnable()
            {@Override public void run() {adminReset();}},1500);}
    }
    public void getDataFromRaw(String text)
    {
        int len=text.length(),i=text.indexOf("1."),serial=1;
        while(i<len)
        {
            if(serial<10){i+=3;}else{i+=4;}
            String date=text.substring(i,i+9);i=text.indexOf('\n',i);
            dates.add(date.replace(" ",""));serial++;
            if(!text.contains(serial+".")) {events.add(text.substring(i+1,len));break;}
            events.add(text.substring(i+1,text.indexOf(serial+".")));i=text.indexOf(serial+".");
        }
        loadTitle.setText("Uploading data");
        (FirebaseDatabase.getInstance().getReference("dates")).setValue(dates);
        fdb= FirebaseDatabase.getInstance().getReference("events");
        fdb.setValue(events);
        fdb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                upload.setText("✓");loadTitle.setText("Upload Complete");
                new Handler().postDelayed(new Runnable()
                {@Override public void run() {ButtonLoading(false,"ThankYou");}},1500);
                new Handler().postDelayed(new Runnable()
                {@Override public void run() {adminReset();}},3000);
            }
            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public int isStudent(String text)
    {
        if(Pattern.compile("^[1-5][0-9][a-z]{3}[1-2][0-9]{3}$", Pattern.CASE_INSENSITIVE) .matcher(text).find()) {return 1;}
        else if(Pattern.compile("^[5][0][0-9]{3}$", Pattern.CASE_INSENSITIVE) .matcher(text).find()) {return 0;}
        return -1;
    }
    public void ButtonLoading(Boolean loading,final String newText)
    {
        if(loading)
        {
            scaleX(upload,35,300,new AnticipateInterpolator());upload.setBackgroundResource(R.drawable.signin_disabled);
            upload.setText(newText);
            new Handler().postDelayed(new Runnable() {@Override public void run() {
                nextLoad.setVisibility(View.VISIBLE);loadTitle.setVisibility(View.VISIBLE);
            }},300);
        }
        else
        {
            nextLoad.setVisibility(View.GONE);loadTitle.setVisibility(View.GONE);
            upload.setBackgroundResource(R.drawable.signin);
            scaleX(upload,185,300,new OvershootInterpolator());
            new Handler().postDelayed(new Runnable()
            {@Override public void run() {upload.setText(newText);}},100);
        }
    }
    public void showKeyboard(View view,boolean what)
    {
        if(what)
        {
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInputFromWindow(view.getApplicationWindowToken(),InputMethodManager.SHOW_FORCED, 0);
        }
        else
        {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    public void adminReset()
    {
        scaleY(login_div,48,350,new AnticipateInterpolator());
        new Handler().postDelayed(new Runnable()
        {@Override public void run() {upload.setText("UPLOAD CALENDER");adminPane.setVisibility(View.GONE);
            email.setVisibility(View.VISIBLE);title.setVisibility(View.GONE);}},250);
    }
    public void studentReset()
    {
        scaleY(login_div,48,550,new AnticipateInterpolator());
        new Handler().postDelayed(new Runnable() {@Override public void run() {
            email.setVisibility(View.VISIBLE);title.setVisibility(View.GONE);search_results.setVisibility(View.GONE);
            search.setText("");search_results.setText("");studentPane.setVisibility(View.GONE);}},500);
    }
    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), 1);}
        catch (android.content.ActivityNotFoundException ex)
        {Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();}
    }
    public void scaleX(final View view,int x,int t, Interpolator interpolator)
    {
        ValueAnimator anim = ValueAnimator.ofInt(view.getMeasuredWidth(),(int)dptopx(x));anim.setInterpolator(interpolator);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.width = (Integer) valueAnimator.getAnimatedValue();
                view.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(t);anim.start();
    }
    public void scaleY(final View view,int y,int t, Interpolator interpolator)
    {
        ValueAnimator anim = ValueAnimator.ofInt(view.getMeasuredHeight(),(int)dptopx(y));anim.setInterpolator(interpolator);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = (Integer) valueAnimator.getAnimatedValue();
                view.setLayoutParams(layoutParams);view.invalidate();
            }
        });
        anim.setDuration(t);anim.start();
    }
    public float dptopx(float num)
    {return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, num, getResources().getDisplayMetrics());}

    @Override
    protected void onActivityResult(int requestCode, int resultcode, Intent intent) {
        super.onActivityResult(requestCode, resultcode, intent);
        if (requestCode == 1 && resultcode == RESULT_OK) {
            final Uri uri = intent.getData();
            new Handler().postDelayed(new Runnable() {@Override public void run() {ButtonLoading(true,"");}},500);
            new Handler().postDelayed(new Runnable() {@Override public void run() {getRawFromPDF(getRealPathFromURI(Login.this, uri));}},1500);
        }
    }
    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        catch (Exception e){ButtonLoading(false,"Can't Find File");
            new Handler().postDelayed(new Runnable()
            {@Override public void run() {adminReset();}},1500);return null;}
        finally {if (cursor != null) {cursor.close();}}
    }
}
