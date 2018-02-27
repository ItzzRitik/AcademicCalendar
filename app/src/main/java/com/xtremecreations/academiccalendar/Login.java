package com.xtremecreations.academiccalendar;

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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
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

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity {
    Animation anim;
    ImageView ico_splash;
    RelativeLayout login_div,logo_div,splash_cover,title,adminPane;
    EditText email;
    TextView signin,heading,upload,data;
    ProgressBar nextLoad;
    ArrayList<String> dates;
    ArrayList<String> events;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        ico_splash=findViewById(R.id.ico_splash);
        login_div=findViewById(R.id.login_div);
        logo_div=findViewById(R.id.logo_div);
        splash_cover=findViewById(R.id.splash_cover);
        title=findViewById(R.id.title);
        adminPane=findViewById(R.id.adminPane);
        nextLoad=findViewById(R.id.nextLoad);
        data=findViewById(R.id.data);
        dates = new ArrayList<>();
        events = new ArrayList<>();

        heading=findViewById(R.id.heading);
        heading.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/vdub.ttf"));

        email=findViewById(R.id.email);
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
    public void performSignIn()
    {
        if(isStudent(email.getText().toString())==1)
        {
            showKeyboard(email,false);scaleY(login_div,93,300,new AccelerateDecelerateInterpolator());
            email.setVisibility(View.GONE);title.setVisibility(View.VISIBLE);heading.setText("STUDENT LOGIN");
        }
        else if(isStudent(email.getText().toString())==0)
        {
            showKeyboard(email,false);scaleY(login_div,198,300,new AccelerateDecelerateInterpolator());
            email.setVisibility(View.GONE);title.setVisibility(View.VISIBLE);heading.setText("ADMIN LOGIN");
            adminPane.setVisibility(View.VISIBLE);
        }
        else
        {
            Toast.makeText(this, "Please Enter Correct ID", Toast.LENGTH_SHORT).show();
        }
    }

    public void getRawFromPDF(String path)
    {
        String parsedText="";
        try {
            PdfReader reader = new PdfReader(path);
            int n = reader.getNumberOfPages();
            for (int i = 0; i <n ; i++) {
                parsedText   = parsedText+ PdfTextExtractor.getTextFromPage(reader, i+1).trim()+"\n";
            }
            scaleY(login_div,750,300,new AccelerateDecelerateInterpolator());
            reader.close();
        }
        catch (Exception e) {Toast.makeText(Login.this, e.toString(), Toast.LENGTH_LONG).show();}
        getDataFromRaw(parsedText);
    }
    public void getDataFromRaw(String text)
    {
        int len=text.length();
        int i=0,serial=1;
        /*while(i<len)
        {
            if(i+8>len){break;}
            String date=text.substring(i,i+8);
            if(Pattern.compile("^[0-9]{2}[/][0-9]{2}[/][0-9]{2}$",Pattern.CASE_INSENSITIVE).matcher(date).find())
            {
                dates.add(date);
                for (int j=i+10;j<len;j++)
                {
                    String event="";
                    if(j+8<len)
                    {
                        if(Pattern.compile("^[0-9]{2}[/][0-9]{2}[/][0-9]{2}$",Pattern.CASE_INSENSITIVE).matcher(text.substring(j,j+8)).find()) {
                            event=(text.substring(i+10,j)).replace('\n',' ');
                            event=event.substring(0,event.length()-4);
                            events.add(event);break;
                        }
                    }
                    else
                    {
                        event=(text.substring(i+10,len)).replace('\n',' ');
                        events.add(event);break;
                    }
                }
            }
            i++;
        }*/
        i=text.indexOf(serial+".");
        while(i<len)
        {
            if(i==-1){break;}
            if(i+8>len){break;}
            String date=text.substring(i,i+8);
            if(Pattern.compile("^[0-9]{2}[/][0-9]{2}[/][0-9]{2}$",Pattern.CASE_INSENSITIVE).matcher(date).find())
            {
                dates.add(date);
                for (int j=i+10;j<len;j++)
                {
                    String event="";
                    if(j+8<len)
                    {
                        if(Pattern.compile("^[0-9]{2}[/][0-9]{2}[/][0-9]{2}$",Pattern.CASE_INSENSITIVE).matcher(text.substring(j,j+8)).find()) {
                            event=(text.substring(i+10,j)).replace('\n',' ');
                            event=event.substring(0,event.length()-4);
                            events.add(event);serial++;Log.d(date,""+serial);i=text.indexOf(serial+".");break;
                        }
                    }
                    else
                    {
                        event=(text.substring(i+10,len)).replace('\n',' ');
                        events.add(event);break;
                    }
                }
            }
            i++;
        }
        String print="";
        for (int k=0;k<dates.size()-1;k++)
        print=print+dates.get(k)+" - "+events.get(k)+"\n";

        data.setText(print+"\n"+dates.size()+"\n"+serial);
    }

    public int isStudent(String text)
    {
        if(Pattern.compile("^[1-5][0-9][a-z]{3}[1-2][0-9]{3}$", Pattern.CASE_INSENSITIVE) .matcher(text).find()) {return 1;}
        else if(Pattern.compile("^[5][0][0-9]{3}$", Pattern.CASE_INSENSITIVE) .matcher(text).find()) {return 0;}
        return -1;
    }
    public void ButtonLoading(Boolean loading)
    {
        if(loading)
        {
            scaleX(upload,35,300,new AnticipateInterpolator());upload.setBackgroundResource(R.drawable.signin_disabled);
            upload.setText(" ");
            new Handler().postDelayed(new Runnable() {@Override public void run() {
                nextLoad.setVisibility(View.VISIBLE);
            }},300);
        }
        else
        {
            nextLoad.setVisibility(View.GONE);scaleX(signin,85,300,new OvershootInterpolator());
            new Handler().postDelayed(new Runnable()
            {@Override public void run() {upload.setText("✔");}},300);
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
            new Handler().postDelayed(new Runnable() {@Override public void run() {ButtonLoading(true);}},500);
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
        finally {if (cursor != null) {cursor.close();}}
    }
}
