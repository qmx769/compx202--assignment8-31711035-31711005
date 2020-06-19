package com.kks.compx202_assignment8;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    private GameView mGameView;
    public static TextView mTextView;
    private boolean mIsEnd;

    private float mX,mY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView( R.layout.activity_main );
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        FrameLayout mFrameLayout = new FrameLayout(this);
        mGameView = new GameView(this);
        mTextView = new TextView(this);
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setText("PLAY");
        mTextView.setTextSize(60);
        mGameView.setVisibility(View.GONE);
        mFrameLayout.addView(mGameView);
        mFrameLayout.addView(mTextView);
        setContentView(mFrameLayout);

//        ActionBar actionBar = getSupportActionBar();
//        actionBar.hide();
//        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
//        getWindow().getDecorView().setSystemUiVisibility( uiOptions );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mIsEnd = mGameView.isEnd;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mTextView.getText().toString().equals("PLAY")) {
                mTextView.setVisibility(View.GONE);
                mGameView.setVisibility(View.VISIBLE);
            } else if (mTextView.getText().toString().equals("GAME OVER") || mTextView.getText().toString().equals("SUCCESS")) {
                mGameView.setVisibility(View.VISIBLE);
                mTextView.setVisibility(View.GONE);
            }
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!mIsEnd) {
                Log.e("X---Y", event.getX() + " " + event.getY());
                if ((event.getX() - mX) < 0){//Scroll left
                    mGameView.mOrientation = 0;
                }else if ((event.getX() - mX) > 0){//Scroll right
                    mGameView.mOrientation = 1;
                }
                mX = event.getX();
                mY = event.getY();
            }
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE){
            if (!mIsEnd) {
                if ((event.getX() - mX) < 0){//Scroll left
                    mGameView.mOrientation = 0;
                }else if ((event.getX() - mX) > 0){//Scroll right
                    mGameView.mOrientation = 1;
                }
            }
        }
        return true;
    }
}
