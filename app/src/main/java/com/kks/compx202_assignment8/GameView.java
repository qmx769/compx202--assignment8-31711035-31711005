package com.kks.compx202_assignment8;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */

public class GameView extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener {

    private final int mBoardWidth = 500;
    private int mBoardHeight = 40;
    private final int mBallRedit = 50;
    private final Sensor mAccelSensor;
    //Sphere current coordinates
    private int mCurBallX;
    private int mCurBallY;

    private int mBoardX1;
    private int mBoardY1;
    //The lower right coordinates of the board
    private int mBoardX2;
    private int mBoardY2;
    private SensorManager mSensorManager;
    private SurfaceHolder mHolder;
    //The board brushes
    private Paint mBoardPaint;
    //The ball brush
    private Paint mBallPaint;
    //Obstacle brush
    private Paint mBrickPaint;
    public boolean isEnd;
    //The current coordinates
    private int mCurBoardX1;
    private int mCurBoardX2;
    //Board movement speed
    private final int mBoardSpeed = 15;
    //The speed at which the ball moves
    private final int mBallSpeed = 20;
    private static final String TAG = GameView.class.getSimpleName();
    public int mOrientation;
    private GameThread mGameThread;
    //The current state of the ball
    private boolean isDown, isRight;
    private int state = -1;
    //The number of columns of obstacles
    private final int mBricksCol = 5;
    //The number of rows of obstacles
    private final int mBricksRow = 5;
    //The width and height of the obstacle
    private final int mBrickWidth = 100;
    private final int mBrickHeight = 80;
    private final int mBrickSpace = 80;
    private int mBrick1X;
    private List<List<Brick>> mBricks;
    private boolean isSuccess;

    private final int mBrickInitHeight = 0;
    //
    private int mBrickState;


    public GameView(Context context) {
        super(context);
        //Initializing brush
        initPaint();
        mHolder = getHolder();
        mHolder.addCallback(this);
        mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        mAccelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        mSensorManager.registerListener(this, mAccelSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e(TAG, "surfaceChanged");
        isEnd = false;
        mCurBallX = getWidth() / 2;
        mCurBallY = getHeight() - mBoardHeight - mBallRedit;
        mBoardX1 = (getWidth() - mBoardWidth) / 2;
        mBoardY1 = getHeight() - mBoardHeight;
        mBoardX2 = (getWidth() + mBoardWidth) / 2;
        mBoardY2 = getHeight();
        mCurBoardX1 = mBoardX1;
        mCurBoardX2 = mBoardX2;
        mBrick1X = (getWidth() - mBricksCol * mBrickWidth - (mBricksCol - 1) * mBrickSpace) / 2;
        mGameThread = new GameThread();
        mGameThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "surfaceDestroyed");
        mSensorManager.unregisterListener(this);
        isEnd = true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] values = event.values;
//            Log.e(TAG, "X--->" + values[0]);
            Log.e(TAG, "Y--->" + values[1]);
            if (values[0] > 0)
                mOrientation = 0;//right
            else if (values[0] < 0)
                mOrientation = 1;//left

            if (values[1] > 0)
                mBrickState = 0;//
            else
                mBrickState = 1;//
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void initPaint() {
        mBoardPaint = new Paint();
        mBoardPaint.setColor(Color.WHITE);
        mBoardPaint.setStyle(Paint.Style.FILL);
        mBallPaint = new Paint();
        mBallPaint.setColor(Color.BLUE);
        mBallPaint.setStyle(Paint.Style.FILL);
        mBrickPaint = new Paint();
        mBrickPaint.setColor(Color.GREEN);
        mBrickPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * ball
     *
     * @param canvas
     */
    public void drawBall(Canvas canvas) {
        //Pellet critical point judgment
        if (mCurBallX <= mBallRedit && isDown) { //In the lower left hand corner
            state = 0;
            isDown = true;
            isRight = true;
        } else if (mCurBallX <= mBallRedit && !isDown) {//The upper left corner
            state = 1;
            isDown = false;
            isRight = true;
        } else if (mCurBallY <= mBallRedit && isRight) {//The upper right
            state = 2;
            isDown = true;
            isRight = true;
        } else if (mCurBallY <= mBallRedit && !isRight) {//Upper left
            state = 3;
            isDown = true;
            isRight = false;
        } else if (mCurBallX >= getWidth() - mBallRedit && isDown) {//The lower right corner
            state = 4;
            isDown = true;
            isRight = false;
        } else if (mCurBallX >= getWidth() - mBallRedit && !isDown) {//The top right corner
            state = 5;
            isDown = false;
            isRight = false;
        } else if (mCurBallY >= getHeight() - mBallRedit) {//Balls out
            state = 6;
            isDown = true;
            isRight = true;
        } else if (mCurBallY + mBallRedit >= mBoardY1 && (mCurBallX >= mCurBoardX1 && mCurBallX <= mCurBoardX2) && isRight) {
            state = 7;
            isDown = false;
            isRight = true;
        } else if (mCurBallY + mBallRedit >= mBoardY1 && (mCurBallX >= mCurBoardX1 && mCurBallX <= mCurBoardX2) && !isRight) {
            state = 8;
            isDown = false;
            isRight = false;
        }
        switch (state) {
            case 0:
                mCurBallX += mBallSpeed;
                mCurBallY += mBallSpeed;
                break;
            case 1:
                mCurBallX += mBallSpeed;
                mCurBallY -= mBallSpeed;
                break;
            case 2:
                mCurBallX += mBallSpeed;
                mCurBallY += mBallSpeed;
                break;
            case 3:
                mCurBallX -= mBallSpeed;
                mCurBallY += mBallSpeed;
                break;
            case 4:
                mCurBallX -= mBallSpeed;
                mCurBallY += mBallSpeed;
                break;
            case 5:
                mCurBallX -= mBallSpeed;
                mCurBallY -= mBallSpeed;
                break;
            case 6:
                try {
                    Thread.sleep(2000);
                    isEnd = true;
                    mHandler.sendEmptyMessage(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case 7:
                mCurBallX += mBallSpeed;
                mCurBallY -= mBallSpeed;
                break;
            case 8:
                mCurBallX -= mBallSpeed;
                mCurBallY -= mBallSpeed;
                break;
            case 9:
                mCurBallX -= mBallSpeed;
                mCurBallY -= mBallSpeed;
                break;
            case 10:
                mCurBallX += mBallSpeed;
                mCurBallY -= mBallSpeed;
                break;
            default:
                mCurBallX -= mBallSpeed;
                mCurBallY -= mBallSpeed;
                break;

        }
        canvas.drawCircle(mCurBallX, mCurBallY, mBallRedit, mBallPaint);
    }

    /**
     *
     *
     * @param canvas
     */
    public void drawBoard(Canvas canvas) {
//        Log.e(TAG, mCurBoardX1 + " " + mBoardY1 + " " + mCurBoardX2 + " " + mBoardY2);
        canvas.drawRect(mCurBoardX1, mBoardY1, mCurBoardX2, mBoardY2, mBoardPaint);
        if (mCurBoardX1 < 0) {
            mCurBoardX1 = 0;
            mCurBoardX2 = mBoardWidth;
        } else if (mCurBoardX2 > getWidth()) {
            mCurBoardX1 = getWidth() - mBoardWidth;
            mCurBoardX2 = getWidth();
        } else {
            if (mOrientation == 0) {//right
                mCurBoardX1 -= mBoardSpeed;
                mCurBoardX2 -= mBoardSpeed;
            } else if (mOrientation == 1) {//left
                mCurBoardX1 += mBoardSpeed;
                mCurBoardX2 += mBoardSpeed;
            }
        }
        if (mBrickState == 1){
            if (mBoardY1 >= 0){
                mBoardY1 -= 10;
            }
        }else if (mBrickState == 0){
            if (mBoardY1 <= getHeight() - mBoardHeight){
                mBoardY1 += 10;
            }
        }

    }

    /**
     *
     *
     * @param canvas
     */
    public void drawBrick(Canvas canvas) {
        isSuccess = true;
        //Draw an obstacle
        for (List<Brick> brick : mBricks) {
            for (Brick b : brick) {
                if (mCurBallX >= b.mBrickX1 && mCurBallX <= b.mBrickX2 && mCurBallY >= b.mBrickY1 && mCurBallY <= b.mBrickY2) {
                    b.mBrickX1 = 0;
                    b.mBrickX2 = 0;
                    b.mBrickY1 = 0;
                    b.mBrickY2 = 0;
                    if (!isDown && !isRight) {          //Top left hitting the brick
                        state = 3;
                        isDown = true;
                        isRight = false;
                    } else if (!isDown && isRight) {   //The upper right corner hits the brick
                        state = 2;
                        isDown = true;
                        isRight = true;
                    } else if (isDown && !isRight) {   //The lower left corner
                        state = 9;
                        isDown = false;
                        isRight = false;
                    } else if (isDown && isRight) {     //The lower right corner
                        state = 10;
                        isDown = false;
                        isRight = true;
                    }
                }
                canvas.drawRect(b.mBrickX1, b.mBrickY1, b.mBrickX2, b.mBrickY2, mBrickPaint);
            }
        }
        for (List<Brick> brick : mBricks) {
            for (Brick b : brick) {
                if (b.mBrickX1 == 0 && b.mBrickX2 == 0 && b.mBrickY1 == 0 && b.mBrickY2 == 0)
                    continue;
                isSuccess = false;
            }
        }
        if (isSuccess) {
            try {
                Thread.sleep(2000);
                isEnd = true;
                mHandler.sendEmptyMessage(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void initBrick() {
        mBricks = new ArrayList<>();
        for (int i = 0; i < mBricksRow; i++) {//The number of rows and rows of obstacles
            List<Brick> mBrickRow = new ArrayList<>();
            for (int j = 0; j < mBricksCol; j++) {
                mBrickRow.add(new Brick(mBrick1X + j * (mBrickWidth + mBrickSpace), mBrickInitHeight + i * (mBrickSpace + mBrickHeight), mBrick1X + j * (mBrickWidth + mBrickSpace) + mBrickWidth, mBrickInitHeight + mBrickHeight + i * (mBrickSpace + mBrickHeight)));
            }
            mBricks.add(mBrickRow);
        }
    }

    /**
     *
     */
    public class GameThread extends Thread {
        public GameThread() {
            initBrick();
        }

        @Override
        public void run() {
            while (!isEnd) {
                try {
                    Canvas mCanvas = mHolder.lockCanvas();
                    mCanvas.drawColor(Color.LTGRAY);
                    drawBall(mCanvas);
                    drawBoard(mCanvas);
                    drawBrick(mCanvas);
                    Thread.sleep(10);
                    mHolder.unlockCanvasAndPost(mCanvas);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0)
                MainActivity.mTextView.setText("GAME OVER");
            else if (msg.what == 1)
                MainActivity.mTextView.setText("SUCCESS");
            MainActivity.mTextView.setVisibility(View.VISIBLE);
            setVisibility(View.GONE);
        }
    };
}
