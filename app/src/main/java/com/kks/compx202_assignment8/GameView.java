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
    //球当前坐标
    private int mCurBallX;
    private int mCurBallY;
    //板子所在左上坐标
    private int mBoardX1;
    private int mBoardY1;
    //板子所在右下坐标
    private int mBoardX2;
    private int mBoardY2;
    private SensorManager mSensorManager;
    private SurfaceHolder mHolder;
    //板子画笔
    private Paint mBoardPaint;
    //球画笔
    private Paint mBallPaint;
    //砖画笔
    private Paint mBrickPaint;
    public boolean isEnd;
    //当前坐标
    private int mCurBoardX1;
    private int mCurBoardX2;
    //板子移动速度
    private final int mBoardSpeed = 15;
    //球移动的速度
    private final int mBallSpeed = 20;
    private static final String TAG = GameView.class.getSimpleName();
    public int mOrientation;
    private GameThread mGameThread;
    //球当前的运行状态
    private boolean isDown, isRight;
    private int state = -1;
    //砖块的列数
    private final int mBricksCol = 5;
    //砖块的行数
    private final int mBricksRow = 5;
    //砖块的宽、高
    private final int mBrickWidth = 100;
    private final int mBrickHeight = 80;
    private final int mBrickSpace = 80;
    private int mBrick1X;
    private List<List<Brick>> mBricks;
    private boolean isSuccess;
    //砖块一开始距离屏幕顶端的高度
    private final int mBrickInitHeight = 0;
    //板子状态
    private int mBrickState;


    public GameView(Context context) {
        super(context);
        //初始化画笔
        initPaint();
        mHolder = getHolder();
        mHolder.addCallback(this);
        mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        mAccelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //加速度传感器向左转动X轴获取值为负反之为负
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
                mOrientation = 0;//右
            else if (values[0] < 0)
                mOrientation = 1;//左

            if (values[1] > 0)
                mBrickState = 0;//板子向下收缩
            else
                mBrickState = 1;//板子向上伸长
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void initPaint() {
        mBoardPaint = new Paint();
        mBoardPaint.setColor(Color.BLACK);
        mBoardPaint.setStyle(Paint.Style.FILL);
        mBallPaint = new Paint();
        mBallPaint.setColor(Color.RED);
        mBallPaint.setStyle(Paint.Style.FILL);
        mBrickPaint = new Paint();
        mBrickPaint.setColor(Color.BLUE);
        mBrickPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * 球
     *
     * @param canvas
     */
    public void drawBall(Canvas canvas) {
        //小球临界点判断
        if (mCurBallX <= mBallRedit && isDown) { //小球以左下角的方式
            state = 0;
            isDown = true;
            isRight = true;
        } else if (mCurBallX <= mBallRedit && !isDown) {//小球以左上角的方式
            state = 1;
            isDown = false;
            isRight = true;
        } else if (mCurBallY <= mBallRedit && isRight) {//小球以右上的方式
            state = 2;
            isDown = true;
            isRight = true;
        } else if (mCurBallY <= mBallRedit && !isRight) {//小球以左上的方式
            state = 3;
            isDown = true;
            isRight = false;
        } else if (mCurBallX >= getWidth() - mBallRedit && isDown) {//小球以右下角的方式
            state = 4;
            isDown = true;
            isRight = false;
        } else if (mCurBallX >= getWidth() - mBallRedit && !isDown) {//小球以右上角的方式
            state = 5;
            isDown = false;
            isRight = false;
        } else if (mCurBallY >= getHeight() - mBallRedit) {//小球出界
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
     * 板子
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
            if (mOrientation == 0) {//右
                mCurBoardX1 -= mBoardSpeed;
                mCurBoardX2 -= mBoardSpeed;
            } else if (mOrientation == 1) {//左
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
     * 砖块
     *
     * @param canvas
     */
    public void drawBrick(Canvas canvas) {
        isSuccess = true;
        //绘制砖块
        for (List<Brick> brick : mBricks) {
            for (Brick b : brick) {
                if (mCurBallX >= b.mBrickX1 && mCurBallX <= b.mBrickX2 && mCurBallY >= b.mBrickY1 && mCurBallY <= b.mBrickY2) {
                    b.mBrickX1 = 0;
                    b.mBrickX2 = 0;
                    b.mBrickY1 = 0;
                    b.mBrickY2 = 0;
                    if (!isDown && !isRight) {          //以左上角撞击砖块
                        state = 3;
                        isDown = true;
                        isRight = false;
                    } else if (!isDown && isRight) {   //以右上角撞击砖块
                        state = 2;
                        isDown = true;
                        isRight = true;
                    } else if (isDown && !isRight) {   //以左下角撞击砖块
                        state = 9;
                        isDown = false;
                        isRight = false;
                    } else if (isDown && isRight) {     //以右下角撞击砖块
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
        for (int i = 0; i < mBricksRow; i++) {//砖块的行列数
            List<Brick> mBrickRow = new ArrayList<>();
            for (int j = 0; j < mBricksCol; j++) {
                mBrickRow.add(new Brick(mBrick1X + j * (mBrickWidth + mBrickSpace), mBrickInitHeight + i * (mBrickSpace + mBrickHeight), mBrick1X + j * (mBrickWidth + mBrickSpace) + mBrickWidth, mBrickInitHeight + mBrickHeight + i * (mBrickSpace + mBrickHeight)));
            }
            mBricks.add(mBrickRow);
        }
    }

    /**
     * 重绘View
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
