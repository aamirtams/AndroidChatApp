package com.applozic.mobicomkit.sample;


/**
 * Created by aamir on 21-Mar-17.
 */


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ObservableWebView;
import com.github.ksoichiro.android.observablescrollview.ScrollState;


public class GoogleWebClass extends AppCompatActivity {


    ObservableWebView wv;
   // EditText et;
    FloatingActionButton gotozam;
    ProgressBar progressB;
    SwipeRefreshLayout swipeLayout;
   // ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.google_web);
        wv = (ObservableWebView) findViewById(R.id.webView1);


       /* final ViewGroup actionBarLaout = (ViewGroup) getLayoutInflater().inflate(R.layout.browser_action_bar, null);
        actionBar = getSupportActionBar();
        //actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionBarLaout);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));
        final EditText et = (EditText) findViewById(R.id.action_bar_text);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);*/

        String url = "https://www.google.com/?gws_rd=ssl#q=";
        progressB = (ProgressBar) findViewById(R.id.progressBar);
        progressB.setMax(100);


       wv.loadUrl(url);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setDisplayZoomControls(false);
        wv.setWebViewClient(new webCont());
        wv.getSettings().setLoadsImagesAutomatically(true);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setVerticalScrollBarEnabled(false);
        wv.setHorizontalScrollBarEnabled(false);
        wv.getSettings().setLoadWithOverviewMode(true);
        wv.getSettings().setUseWideViewPort(true);
        wv.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        wv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        /*et.setPadding(10, 0, 0, 5);
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String url = et.getText().toString();
                    closeContextMenu();
                    wv.loadUrl("http://www.google.com/?gws_rd=ssl#q=" + url);

                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(et.getWindowToken(), 0);



                    return true;
                }

                return false;
            }
        });*/

        wv.setScrollViewCallbacks(new ObservableScrollViewCallbacks() {
            @Override
            public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

            }

            @Override
            public void onDownMotionEvent() {

            }

            @Override
            public void onUpOrCancelMotionEvent(ScrollState scrollState) {

                if (scrollState == ScrollState.UP) {
                   /* if (actionBar.isShowing()) {


                        actionBar.hide();
                    }*/


                } else if (scrollState == ScrollState.DOWN) {
                    /*if (!actionBar.isShowing()) {
                        actionBar.show();
                    }*/
                }
            }
        });

        gotozam = (FloatingActionButton) findViewById(R.id.gotozam);
        gotozam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });


        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(false);
                    }
                }, 2000);
                wv.reload();
            }
        });


        wv.requestFocus(View.FOCUS_DOWN);
        wv.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // TODO Auto-generated method stub
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });


        wv.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100 && progressB.getVisibility() == ProgressBar.GONE) {
                    progressB.setVisibility(ProgressBar.VISIBLE);
                }
                progressB.setProgress(progress);
                if (progress == 100) {
                    progressB.setVisibility(ProgressBar.GONE);

                }
            }

        });
    }

   /* @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (wv.canGoBack()) {
                        wv.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }*/



    @Override
    public void onBackPressed(){
        if (wv.canGoBack()){
            wv.goBack();
        }else
        {
            finish();
        }

    }

    private class webCont extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub
            view.loadUrl(url);

            return true;
        }
        @Override
        public void onPageFinished(WebView view, String url){
            super.onPageFinished(view, url);
            try {
                //et.setText(url);
            }catch (Throwable e){

            }

        }
    }

  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.browser_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.broser_home:
                Intent intent = new Intent(GoogleWebClass.this, GoogleWebClass.class);
                startActivity(intent);
                return true;

            case R.id.browser_next:
                wv.goForward();
                return true;

            case R.id.browser_back:

                    wv.goBack();

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }*/
}
