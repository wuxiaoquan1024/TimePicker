package cn.wxq.timepicker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by Administrator on 2018/5/29.
 */

public class CircleTimePicker extends View {

    private final static int TOUCH_HANDLE_SLEEP = 1;

    private final static int TOUCH_HANDLE_WAKEUP = 2;

    private final static int TOUCH_HANDLE_MINUTE = 3;

    private final static int TOUCH_HANDLE_NON = -1;

    /**
     * 时间选择模式
     */
    private final static int PICKER_MODEL_TIME = 1;

    /**
     * 区间选择模式
     */
    private final static int PICKER_MODEL_RANGE = 2;

    //动画重复执行次数
    private final static int REPEAT_COUNT = 4;

    private static final long DURATION_SCALE = 200;

    //最大旋转角度
    private static final float MAX_ROTATE_ANGLE = 5.5f;

    //内圆时间半径
    private float mHourRadius = 200;

    //內圆时间进度半径
    private float mHourProgressRadius;

    //分钟半径
    private float mMinuteRadius;

    //时间进度背景画笔
    private Paint mHourBackgroundPaint = new Paint();

    //时间刻度画笔
    private Paint mHourPaint = new Paint();

    //时间文字画笔
    private Paint mTextPaint = new Paint();

    //时间进度画笔
    private Paint mProgressPaint;

    //手势手柄画笔
    private Paint mTouchHandlePaint;

    //时间背景颜色
    private int mHourBackgroundColor;

    //时间刻度颜色
    private int mHourColor = Color.BLACK;

    //分钟时间刻度颜色
    private int mMinuteColor = Color.GRAY;

    //文字颜色
    private int mTextColor;

    //进度颜色
    private int mProgressColor;

    //时间画笔粗细度
    private float mHourStrokeWidth;

    //时间背景画笔粗细度
    private float mHourBackgroundStrokeWidth;

    /**分钟刻度宽度*/
    private float mMinuteWidth;

    private float mMinuteTextWidth;

    //时间刻度长度
    private float mCalibrantionLenght;

    private float mStartAngle = 270;

    private float mEndUpAngle = 45;

    private float mMeasureOffset;

    //手柄位置
    private RectF mSleepRectF = new RectF();

    //手柄位置
    private RectF mWakeupRectF = new RectF();

    private RectF mMinuteFectF = new RectF();

    private int mTouchHanlerType = TOUCH_HANDLE_NON;

    private GestureDetector mGestureDetector;

    private boolean mDoubleState;

    private ViewConfiguration mViewConfiguration;

    //双击展开、收回动画
    private ValueAnimator mScaleAnimator;

    //缩放比例
    private float mScaleValue = 1f;

    private Camera mCamera;

    private Matrix mMatrix = new Matrix();

    private float mDownX;

    private float mDownY;

    private float mLastX;

    private float mLastY;

    private float mRotateX;

    private float mRotateY;

    private Bitmap mBitmap;

    private Canvas mDrawCanvas;

    private AnimatorSet mRotateAnimator;

    //分钟时间区域是否接受Touch时间
    private boolean mMintueAcceptTouch;

    //分钟时间的角度
    private float mTouchDownAngle;

    private OnTimePickerListener mTimePickerListener;

    private OnTimeRangePickerListener mTimeRangePickerListener;

    private int mPickerModel;

    public CircleTimePicker(Context context) {
        this(context, null);
    }

    public CircleTimePicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleTimePicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircleTimePicker, defStyleAttr, 0);
        mMeasureOffset = ta.getDimension(R.styleable.CircleTimePicker_measure_offset, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, displayMetrics));
        mCalibrantionLenght = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, displayMetrics);
        mHourBackgroundStrokeWidth = ta.getDimension(R.styleable.CircleTimePicker_background_stroke_size, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, displayMetrics));
        mMinuteWidth = ta.getDimension(R.styleable.CircleTimePicker_minute_stroke_size, mHourBackgroundStrokeWidth);
        mHourStrokeWidth = ta.getDimension(R.styleable.CircleTimePicker_hour_stroke_size, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f, displayMetrics));
        mMinuteTextWidth  = ta.getDimension(R.styleable.CircleTimePicker_text_size, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, displayMetrics));
        mHourRadius = ta.getDimension(R.styleable.CircleTimePicker_hour_redius, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 68, displayMetrics));
        mHourColor = ta.getColor(R.styleable.CircleTimePicker_hour_color, Color.BLACK);
        mHourBackgroundColor = ta.getColor(R.styleable.CircleTimePicker_progress_background_color, Color.WHITE);
        mTextColor = ta.getColor(R.styleable.CircleTimePicker_text_color, Color.BLACK);
        mProgressColor = ta.getColor(R.styleable.CircleTimePicker_progress_color, Color.GRAY);
        mEndUpAngle = ta.getInt(R.styleable.CircleTimePicker_end_angle, 90);
        mStartAngle = ta.getInt(R.styleable.CircleTimePicker_start_angle, 270);
        mPickerModel = ta.getInt(R.styleable.CircleTimePicker_picker_model, PICKER_MODEL_TIME);

        mGestureDetector = new GestureDetector(context, mGestrueListener);
        mGestureDetector.setOnDoubleTapListener(mGestureDoubleTapListener);  //设置双击事件监听
        mViewConfiguration = ViewConfiguration.get(context);
        mCamera = new Camera();
        initPaint(mHourBackgroundPaint, mHourBackgroundStrokeWidth, mHourBackgroundColor);
        initPaint(mHourPaint, mHourStrokeWidth, mHourColor);
        mMinuteRadius = mHourRadius + mHourBackgroundStrokeWidth + mMinuteWidth;
        mHourProgressRadius = mHourRadius + mHourBackgroundStrokeWidth / 2f;

        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(ta.getDimension(R.styleable.CircleTimePicker_text_size, 30));
        mTextPaint.setColor(mTextColor);

        mProgressPaint = new Paint(mHourBackgroundPaint);
        mProgressPaint.setColor(mProgressColor);
        mTouchHandlePaint = new Paint(mProgressPaint);
        mTouchHandlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTouchHandlePaint.setColor(Color.YELLOW);
        final float halfStrokeWidth = mHourBackgroundStrokeWidth / 2f;
        mSleepRectF.set(-halfStrokeWidth, -halfStrokeWidth, halfStrokeWidth, halfStrokeWidth);
        mWakeupRectF.set(mSleepRectF);
        ta.recycle();
    }

    private void initPaint(Paint paint, float strokeWidth, int color) {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(color);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        final int defaultSize = (int)((mMinuteRadius + mMeasureOffset) * 2 + mMinuteTextWidth);
        int width;
        if (layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT || widthSize < defaultSize) {
            width = defaultSize;
        } else {
            width = widthSize;
        }

        int height;
        if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT || heightSize < defaultSize) {
            height = width;
        } else {
            height = heightSize;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            mDrawCanvas = new Canvas(mBitmap);
        }

        if (mDoubleState) {
            canvas.save();
            canvas.concat(mMatrix);
        }

        if (mDoubleState || (mScaleAnimator != null && mScaleAnimator.isRunning())) {
            drawMinuteCalibrant(canvas);
        }
        drawCalibration(mDrawCanvas);
        canvas.drawBitmap(mBitmap, 0, 0, null);
        if (mPickerModel == PICKER_MODEL_RANGE) {
            drawProgress(canvas);
        }
        drawTouchHandle(canvas);

        if (mDoubleState) {
            drawSelectedMintueText(canvas);
            canvas.restore();
        }
    }

    /**
     * 绘制时间刻度
     * @param canvas
     */
    private void drawCalibration(Canvas canvas) {
        final float calibrantionRadius = mHourRadius - mCalibrantionLenght;
        canvas.save();
        float cx = getWidth() / 2.f;
        float cy = getHeight() / 2.f;
        mHourPaint.setColor(mHourColor);
        canvas.translate(cx, cy);
        if (mPickerModel == PICKER_MODEL_RANGE) {
            canvas.drawCircle(0, 0, mHourRadius + mHourBackgroundStrokeWidth / 2, mHourBackgroundPaint);
        }
        canvas.drawCircle(0, 0, mHourRadius, mHourPaint);
        final int step = 30;
        for (int i = 0; i < 360;) {
            final Point inner = angleConvertPoint(i, (int)calibrantionRadius);
            final Point outer = angleConvertPoint(i, (int) mHourRadius);
            canvas.drawLine(inner.x, inner.y, outer.x, outer.y, mHourPaint);
            final float angle90Multiple = i % 90f;
            if (angle90Multiple == 0) {
                //90 度的倍数绘制文字
                String text = String.valueOf((i + 90) / step);
                Point textPoint = caculateTextPoint(text, (int)calibrantionRadius, (i + 90));
                canvas.drawText(text, textPoint.x, textPoint.y, mTextPaint);
            }
            i += step;
        }
        canvas.restore();
    }

    private Point angleConvertPoint(float angle, int radius) {
        return new Point(caculateX(angle, radius), caculateY(angle, radius));
    }

    private int caculateX(float angle, float lenght) {
        return (int) (Math.cos(angle * Math.PI / 180f) * lenght);
    }

    private int caculateY(float angle, float lenght) {
        return (int) (Math.sin(angle * Math.PI / 180f) * lenght);
    }

    /**
     * 计算时间刻度位置
     * @param text
     * @param radius
     * @param angle
     * @return
     */
    private Point caculateTextPoint(String text, int radius, int angle) {
        final int space = 20;
        Rect rect = new Rect();
        mTextPaint.getTextBounds(text, 0, text.length(), rect);   //获取字条大小
        final int textHalfHeight = rect.height() / 2;
        final int textHalfWidth = rect.width() / 2;
        if (angle % 180 == 0) {
            //竖向
            if (angle / 180 == 1) {
                return new Point(-textHalfWidth, radius - textHalfHeight);
            } else {
                return new Point(-textHalfWidth, -radius + space + textHalfHeight);
            }
        } else {
            float x = angle < 180 ? radius - space - textHalfWidth : -radius  + space - textHalfWidth;
            return new Point((int)x, textHalfHeight);
        }
    }

    /**
     * 绘制时间进度
     * @param canvas
     */
    private void drawProgress(Canvas canvas) {
        canvas.save();
        float width = mHourRadius * 2 + mHourBackgroundStrokeWidth;
        final float halfSWidth = (getWidth() - width) / 2;
        final float halfSHeight = (getHeight() - width) / 2;
        canvas.translate(halfSWidth, halfSHeight);
        RectF rectF = new RectF(0, 0, width, width);
        float start = mStartAngle % 360;
        float end = mEndUpAngle % 360;
        float sweep;
        if (start >= 270 && end >= 270 && end > start) {
            sweep = end - start;
        } else if (end > start) {
            sweep = end - start;
        } else {
            sweep = 360 - (start - end);
        }
        canvas.drawArc(rectF, start, sweep, false, mProgressPaint);
        canvas.restore();
    }

    /**
     * 绘制分钟刻度
     * @param canvas
     */
    private void drawMinuteCalibrant(Canvas canvas) {
        final float calibrantionRadius = mMinuteRadius - mCalibrantionLenght;
        canvas.save();
        mHourPaint.setColor(mMinuteColor);
        float cx = getWidth() / 2.f;
        float cy = getHeight() / 2.f;
        canvas.translate(cx, cy);
        canvas.scale(mScaleValue, mScaleValue);
        canvas.drawCircle(0, 0, mMinuteRadius, mHourPaint);
        final int step = 6;
        for (int i = 0; i < 360;) {
            final Point outer = angleConvertPoint(i, (int) mMinuteRadius);
            Point inner;
            if (i % 45 == 0) {
                inner = angleConvertPoint(i, (int)(calibrantionRadius -  mCalibrantionLenght));
            } else {
                inner = angleConvertPoint(i, (int)calibrantionRadius);
            }
            canvas.drawLine(inner.x, inner.y, outer.x, outer.y, mHourPaint);
            i += step;
        }

        canvas.restore();
    }

    /**
     * 绘制手柄
     * @param canvas
     */
    private void drawTouchHandle(Canvas canvas) {
        canvas.save();
        float cx = getWidth() / 2.f;
        float cy = getHeight() / 2.f;
        float haldStrokeWidth = mHourBackgroundStrokeWidth / 2;
        canvas.translate(cx, cy);
        Point sleepPoint = caculateTouchHandlePoint(mStartAngle);
        mSleepRectF.set(-haldStrokeWidth + cx, -haldStrokeWidth + cy, haldStrokeWidth + cx, haldStrokeWidth + cy);
        mSleepRectF.offset(sleepPoint.x, sleepPoint.y);
        mTouchHandlePaint.setColor(Color.RED);
        canvas.drawCircle(sleepPoint.x, sleepPoint.y, mHourStrokeWidth, mTouchHandlePaint);

        if (mPickerModel == PICKER_MODEL_RANGE) {
            Point wakeupPoint = caculateTouchHandlePoint(mEndUpAngle);
            mWakeupRectF.set(-haldStrokeWidth + cx, -haldStrokeWidth + cy, haldStrokeWidth + cx, haldStrokeWidth + cy);
            mWakeupRectF.offset(wakeupPoint.x, wakeupPoint.y);
            mTouchHandlePaint.setColor(Color.GREEN);
            canvas.drawCircle(wakeupPoint.x, wakeupPoint.y, mHourStrokeWidth, mTouchHandlePaint);
        }
        canvas.restore();
    }

    /**
     * 绘制选择的分钟
     * @param canvas
     */
    private void drawSelectedMintueText(Canvas canvas) {
        canvas.save();
        float cx = getWidth() / 2.f;
        float cy = getHeight() / 2.f;
        float angle = getMinuteAngle();
        Log.e("Tag", "minute angel : " + angle);
        String minute = String.valueOf(minuteAngleConvertTime(angle));
        Point point = angleConvertPoint(angle - 90, (int) (mMinuteRadius + mMinuteWidth));
        mHourPaint.setColor(mMinuteColor);
        canvas.translate(cx + point.x, cy + point.y);
        Rect out = new Rect();
        mTextPaint.getTextBounds(minute, 0, minute.length(), out);
        canvas.drawCircle(0, 0, 40, mHourPaint);
        canvas.drawText(minute, -out.width() / 2f, out.height() / 2f, mTextPaint);
//        canvas.rotate( minuteAngle / 360f * 120f, out.width() / 2f, out.height() / 2f);
//        canvas.drawPath(mMinuteTextFramePath, mHourPaint);
        mMinuteFectF.set(-40, -40, 40, 40);
        mMinuteFectF.offset(cx + point.x, cy + point.y);
        canvas.restore();
    }

    /**
     * 获得选择的时间
     * @return
     * @param hourAngle
     */
    private int getSelectedMinute(float hourAngle) {
        float angle = hourAngle % 30f * (360 / 30);
        return minuteAngleConvertTime(angle);
    }

    /**
     * 获得选择的时间
     * @param hourAngle
     * @return
     */
    private int getSelectedHour(float hourAngle) {
        int hour = (int)((hourAngle + 90) / 30f) % 12;
        if (hour == 0) {
            return 12;
        }
        return hour;
    }

    //获取小时表盘设置的分钟角度
    private float getMinuteAngle() {
        float hourAngle;
        if (mPickerModel == PICKER_MODEL_TIME
                || mTouchHanlerType == TOUCH_HANDLE_SLEEP) {
            hourAngle = mStartAngle;
        } else {
            hourAngle = mEndUpAngle;
        }

        return hourAngle % 30f * (360 / 30);
    }

    //将分钟角度转换成时间
    private int minuteAngleConvertTime(float angle) {
        return (int) (60 / 360f * angle);
    }

    private Point caculateTouchHandlePoint(float angle) {
        float radius = mHourProgressRadius;
        return new Point(caculateX(angle, radius), caculateY(angle, radius));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        if (action == MotionEvent.ACTION_CANCEL
                || action == MotionEvent.ACTION_UP) {
            mDownY = mDownX = -1;
            mLastY = mLastX = -1;
            mMintueAcceptTouch = false;
            mTouchDownAngle = -1;
            //执行恢复动画
            if (mRotateY != 0 || mRotateX != 0) {
                executeRotateAnimator(mRotateX, -mRotateX, mRotateY, -mRotateY);
            }

            if (action == MotionEvent.ACTION_UP
                    && (mTouchHanlerType != TOUCH_HANDLE_NON || mMintueAcceptTouch)) {
                if (mPickerModel == PICKER_MODEL_TIME && mTimePickerListener != null) {
                    mTimePickerListener.onTime(getSelectedHour(mStartAngle), getSelectedHour(mStartAngle));
                } else if (mTimeRangePickerListener != null){
                    mTimeRangePickerListener.onRangeTime(getSelectedHour(mStartAngle), getSelectedMinute(mStartAngle), getSelectedHour(mEndUpAngle), getSelectedMinute(mEndUpAngle));
                }
            }

            if (!mDoubleState) {
                mTouchHanlerType = TOUCH_HANDLE_NON;
            }
        }
        return mGestureDetector.onTouchEvent(event);
    }

    /**
     * 根据点计算角度
     * @param x x 坐标
     * @param y y 坐标
     * @return
     */
    private float caculateAngleByPoint(float x, float y) {
        float cx = getWidth() / 2.f;
        float cy = getHeight() / 2.f;
        return (float) (Math.atan(Math.abs(y - cy) / Math.abs(x - cx)) * 180 / Math.PI);
    }

    /**
     * 先求出这两点间的弦长（设为d）：d＝根号下[(x2－x1)²＋(y2－y1)²]
     圆心角θ＝2arcsin(d/2r)
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    private float caculatePointAngle(float x1, float y1, float x2, float y2, float angleOffset) {
        float cx = getWidth() / 2.f;
        float cy = getHeight() / 2.f;
        x1 -= cx;
        x2 -= cx;
        y1 -= cy;
        y2 -= cy;
        double sqrt = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        float angle = (float) (2 *  Math.asin(sqrt / (2 * mHourProgressRadius)) * 180 / Math.PI) ;
        if (angle == Float.NaN) {
            if (x2 < cx) {
                return 180;
            } else {
                return 0;
            }
        }
        Log.d("Tag", "@@@@@@@@@两点之间的角度：" + angle);
        return angle;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    private boolean checkTouchInMinuteArea(float x, float y) {
        /*float centX = getWidth() / 2f;
        float centY = getHeight() / 2f;
        float xl = Math.abs(x - centX);
        float yl = Math.abs(y - centY);
        float lenght = (float) Math.sqrt(xl * xl + yl * yl);
        return lenght >= mHourProgressRadius + mHourBackgroundStrokeWidth / 2 && lenght <= mMinuteRadius;*/
        return mMinuteFectF.contains(x, y);
    }

    /**
     * 坐标系象限
     * x>0,y>0时在第一象限
     x<0,y>0时在第二象限
     x<0,y<0时在第三象限
     x>0,y<0时在第四象限
     */
    private float getAngleFromPoint(float x, float y) {
        float angle = caculateAngleByPoint(x, y);
        float cx = getWidth() / 2.f;
        float cy = getHeight() / 2.f;
        if( x > cx && y >= cy) {//第一象限
            Log.d("Tag", "第一象限两点之间的角度 ： " + angle);
//                    angle = 90 - angle;
        } else if (x < cx && y >= cy) {   //第二象限
            Log.d("Tag", "第二象限两点之间的角度 ： " + angle);
            angle = 180 - angle;
        } else if (x <= cx && y < cy) { //第三象限
            Log.d("Tag", "第三象限两点之间的角度 ： " + angle);
            angle += 180;
        } else if (x >= cx && y < cy){//第四象限
            Log.d("Tag", "第四象限两点之间的角度 ： " + angle);
            angle = 270 + (90 - angle);
        }
        return angle;
    }

    private GestureDetector.OnGestureListener mGestrueListener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            final float x = e.getX();
            final float y = e.getY();
            Log.d("Tag", "Wakeup Rectf : " + mWakeupRectF + "\t   Sleep rectF : " + mSleepRectF + "\t x = " + x + " \t   y = " + y);

            mTouchDownAngle = getAngleFromPoint(x, y);
            if (mDoubleState) {
                //判断是否在分钟触发点上
                mMintueAcceptTouch = checkTouchInMinuteArea(x, y);
                mLastX = mDownX = x;
                mLastY = mDownY = y;

                //停止动画
                if (mRotateAnimator != null && mRotateAnimator.isRunning()) {
                    mRotateAnimator.cancel();
                }
            }

            if (mWakeupRectF.contains(x, y)) {
                mTouchHanlerType = TOUCH_HANDLE_WAKEUP;
            } else if (mSleepRectF.contains(x, y)) {
                mTouchHanlerType = TOUCH_HANDLE_SLEEP;
            }
            invalidate();
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            final float x = e2.getX();
            final float y = e2.getY();
            float angle = getAngleFromPoint(x, y);
            float dx = angle - mTouchDownAngle;
            float finalAngle = mTouchDownAngle + dx;
            if (mDoubleState) {

                if (mMintueAcceptTouch) {
                    float addA = dx / 360f * 30;
                    if (mTouchDownAngle > 270 && finalAngle < 90) {
                        //从第四象限滑动到第一象限，加一个单位
                        addA += 30;
                    } else if (mTouchDownAngle < 90 && finalAngle > 270) {
                        //从第一象限滑动到滴四现象，减一个单位
                        addA -= 30;
                    }

                    if (mTouchHanlerType == TOUCH_HANDLE_SLEEP
                            || mPickerModel == PICKER_MODEL_TIME) {
                        mStartAngle = (mStartAngle + addA + 360) % 360;
                    } else {
                        mEndUpAngle = (mEndUpAngle + addA + 360) % 360;
                    }
                    Log.d("Tag", "分钟时间区域接受到了Touch事件mMinuteAngle : " + mTouchDownAngle + "--angle--"
                            + angle + "::;:angle = " + addA + ";;;;finalAngle = " + finalAngle + "----mEndUpAngle = " + mEndUpAngle);
                    mTouchDownAngle = finalAngle % 360;
                } else {
                    final float step = 0.9f;
                    float xd = Math.abs(x - mLastX);
                    float yd = Math.abs(y - mLastY);
                    float lenght = (float) Math.sqrt(xd * xd + yd * yd);
                    float yDeg = yd / lenght * step;
                    float xDeg = xd / lenght * step;
                    if (x < mDownX) {
                        xDeg = -xDeg;
                    }

                    if ( y > mDownY) {
                        yDeg = -yDeg;
                    }
                    mRotateX += xDeg;
                    mRotateY += yDeg;
                    mRotateX = checkRotateDegressValid(mRotateX);
                    mRotateY = checkRotateDegressValid(mRotateY);
                    setMatrixRotate();
                    Log.d("Tag", "旋转角度 mRotateX：" + mRotateX + "\t mRotateY ： " + mRotateY);
                }
            } else if (mTouchHanlerType != TOUCH_HANDLE_NON) {

                if (mTouchDownAngle > 270 && finalAngle < 90) {
                    //从第一象限滑动到滴四现象
                    if (mTouchHanlerType == TOUCH_HANDLE_SLEEP) {
                        mStartAngle = 360 - dx;
                    } else if (mTouchHanlerType == TOUCH_HANDLE_WAKEUP) {
                        mEndUpAngle = 360 -dx;
                    }
                } else {
                    if (mTouchHanlerType == TOUCH_HANDLE_SLEEP) {
                        mStartAngle = (mStartAngle + dx + 360) % 360;
                    } else if (mTouchHanlerType == TOUCH_HANDLE_WAKEUP) {
                        mEndUpAngle = (mEndUpAngle + dx + 360) % 360;
                    }
                }

                mTouchDownAngle = finalAngle % 360;
                Log.d("Tag", "touch angle : " + angle);
            }
            mLastX = x;
            mLastY = y;
            invalidate();
            return true;
        }

        private float checkRotateDegressValid(float degress) {
            if (degress > MAX_ROTATE_ANGLE) {
                return MAX_ROTATE_ANGLE;
            } else if (degress < -MAX_ROTATE_ANGLE) {
                return -MAX_ROTATE_ANGLE;
            }
            return degress;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return true;
        }
    };

    private void setMatrixRotate() {
        float cx = getWidth() / 2.f;
        float cy = getHeight() / 2.f;
        mCamera.save();   //保存先前状态后再设置旋转值
        mCamera.rotateX(mRotateY);
        mCamera.rotateY(mRotateX);
        mCamera.getMatrix(mMatrix);
        mMatrix.preTranslate(-cx, -cy);   //居中旋转
        mMatrix.postTranslate(cx,cy);
        mCamera.restore();
    }

    /**
     * 执行展开动画
     */
    private void executeOpenAnimator() {
        float startScale = (mHourRadius + mHourBackgroundStrokeWidth) / mMinuteRadius;
        executeScaleAnimator(startScale, 1f);
    }

    /**
     * 执行关闭动画
     */
    private void executeCloseAnimator() {
        float endScale = (mHourRadius + mHourBackgroundStrokeWidth) / mMinuteRadius;
        executeScaleAnimator(1f, endScale);
    }

    /**
     * 执行缩放动画
     * @param startScale
     * @param endScale
     */
    private void executeScaleAnimator(float startScale, float endScale) {
        mScaleAnimator = ValueAnimator.ofFloat(startScale, endScale);
        mScaleAnimator.setDuration(DURATION_SCALE);
        mScaleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mScaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mScaleValue = value;
                postInvalidate();
            }
        });
        mScaleAnimator.start();
    }

    private void setAnimatorRotateAndInvalidate(float rotateX, float rotateY) {
        mRotateX = rotateX;
        mRotateY = rotateY;
        setMatrixRotate();
        postInvalidate();
    }

    private ValueAnimator buildRotateAnimator(float start, float end, int repeatCount, ValueAnimator.AnimatorUpdateListener listener) {
        ValueAnimator animator = ValueAnimator.ofFloat(start, end);
        if (repeatCount > 0) {
            animator.setRepeatCount(repeatCount);
        }
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(listener);
        return animator;
    }

    private AnimatorSet buildAnimatorSet(Animator... items) {
        AnimatorSet set = new AnimatorSet();
        set.setDuration(DURATION_SCALE);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.playTogether(items);
        return set;
    }

    private void executeRotateAnimator(final float startRotateX, final float endRotateX, float startRotateY, float endRotateY) {
        if (mRotateAnimator == null) {
            mRotateAnimator = new AnimatorSet();
            ValueAnimator rotateX = buildRotateAnimator(startRotateX, endRotateX, REPEAT_COUNT, new RotateAnimatorListener(RotateAnimatorListener.AXIS_X));
            ValueAnimator rotateY = buildRotateAnimator(startRotateY, endRotateY, REPEAT_COUNT, new RotateAnimatorListener(RotateAnimatorListener.AXIS_Y));
            ValueAnimator resetRotateX = buildRotateAnimator(endRotateX, 0, 0, new RotateAnimatorListener(RotateAnimatorListener.AXIS_X));
            ValueAnimator resetRotateY = buildRotateAnimator(endRotateY, 0, 0, new RotateAnimatorListener(RotateAnimatorListener.AXIS_Y));

            rotateY.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    mRotateY = mRotateX = 0;
                    mCamera.rotateX(mRotateX);
                    mCamera.rotateY(mRotateY);
                    mMatrix.reset();
                    invalidate();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mRotateY = mRotateX = 0;
                    mCamera.rotateX(mRotateX);
                    mCamera.rotateY(mRotateY);
                    mMatrix.reset();
                    invalidate();
                }
            });

            AnimatorSet resetSet = buildAnimatorSet(resetRotateX, resetRotateY);
            AnimatorSet executeSet = buildAnimatorSet(rotateX, rotateY);

            mRotateAnimator.playSequentially(executeSet, resetSet);
        }
        mRotateAnimator.start();
    }

    private GestureDetector.OnDoubleTapListener mGestureDoubleTapListener = new GestureDetector.OnDoubleTapListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            final float x = e.getX();
            final float y = e.getY();
            boolean state = !mWakeupRectF.contains(x, y) && !mSleepRectF.contains(x, y);
            if (state) {
                mDoubleState = !mDoubleState;
                if (mDoubleState) {
                    executeOpenAnimator();
                } else {
                    executeCloseAnimator();
                    mTouchHanlerType = TOUCH_HANDLE_NON;
                }
                invalidate();
            }
            Log.d("Tag", "双击事件：" + mDoubleState);
            return mDoubleState;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
    };

    private class RotateAnimatorListener implements ValueAnimator.AnimatorUpdateListener {

        static final int AXIS_X = 1;

        static final int AXIS_Y = 2;

        private int axis;

        private RotateAnimatorListener(int axis) {
            this.axis = axis;
        }


        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float value = (float) animation.getAnimatedValue();
            if (axis == AXIS_X) {
                setAnimatorRotateAndInvalidate(value, mRotateY);
            } else if (axis == AXIS_Y) {
                setAnimatorRotateAndInvalidate(mRotateX, value);
            }
        }
    }

    public int getHourBackgroundColor() {
        return mHourBackgroundColor;
    }

    public void setHourBackgroundColor(int hourBackgroundColor) {
        this.mHourBackgroundColor = hourBackgroundColor;
    }

    public int getHourColor() {
        return mHourColor;
    }

    public void setHourColor(int hourColor) {
        this.mHourColor = hourColor;
    }

    public int getMinuteColor() {
        return mMinuteColor;
    }

    public void setMinuteColor(int minuteColor) {
        this.mMinuteColor = minuteColor;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
    }

    public int getProgressColor() {
        return mProgressColor;
    }

    public void setProgressColor(int progressColor) {
        this.mProgressColor = progressColor;
    }

    public float getHourStrokeWidth() {
        return mHourStrokeWidth;
    }

    public void setHourStrokeWidth(float hourStrokeWidth) {
        this.mHourStrokeWidth = hourStrokeWidth;
    }

    public float getBackgroundStrokeWidth() {
        return mHourBackgroundStrokeWidth;
    }

    public void setBackgroundStrokeWidth(float strokeWidth) {
        this.mHourBackgroundStrokeWidth = strokeWidth;
    }

    public float getMinuteWidth() {
        return mMinuteWidth;
    }

    public void setMinuteWidth(float minuteWidth) {
        this.mMinuteWidth = minuteWidth;
    }

    public float getMinuteTextWidth() {
        return mMinuteTextWidth;
    }

    public void setMinuteTextWidth(float minuteTextWidth) {
        this.mMinuteTextWidth = minuteTextWidth;
    }

    public float getStartAngle() {
        return mStartAngle;
    }

    public void setStartAngle(float angle) {
        this.mStartAngle = angle;
    }

    public float getEndAngle() {
        return mEndUpAngle;
    }

    public void setEndAngle(float angle) {
        this.mEndUpAngle = angle;
    }

    /**
     * 设置时间选择器回调接口
     * @param listener
     */
    public void setOnTimePickerListner(OnTimePickerListener listener) {
        this.mTimePickerListener = listener;
    }

    public OnTimePickerListener getOnTimePickerListener() {
        return mTimePickerListener;
    }

    public void setOnTimeRangePickerListener(OnTimeRangePickerListener listener) {
        mTimeRangePickerListener = listener;
    }

    public OnTimeRangePickerListener getOnTimeRangePickerListener() {
         return mTimeRangePickerListener;
    }

    /**
     * 时间选择回调接口
     */
    public static interface OnTimePickerListener {
        void onTime(int hour, int minute);
    }

    /**
     * 时间区间选择器
     */
    public static interface OnTimeRangePickerListener {
        void onRangeTime(int startHour, int startMinute, int endHour, int endMinute);
    }
}
