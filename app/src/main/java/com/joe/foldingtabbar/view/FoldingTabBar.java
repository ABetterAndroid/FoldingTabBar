package com.joe.foldingtabbar.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

/**
 * Created by qiaorongzhu on 2016/10/18.
 */

public class FoldingTabBar extends View {

    private static final int STATE_OPENING = 0;
    private static final int STATE_OPENED = 1;
    private static final int STATE_CLOSING = 2;
    private static final int STATE_CLOSED = 3;
    private int tabState = STATE_CLOSED;
    private static final int VIEW_DEFAULT_WIDTH_HEIGHT = 150;
    private Bitmap[] icons;
    private int width;
    private int height;
    private Paint bgPaint;
    private float[] tabPts = new float[4];
    private float[] actionPts = new float[8];
    private float[][] iconPos;
    private RectF[] iconRectFs;
    private Paint actionPaint;
    private Paint iconPaint;
    private Paint shadowPaint;
    private RectF actionRectF;
    private OnTabItemClickListener mListener;
    private float openAnimFactor;
    private float actionRotation;
    private float iconRotation = -180;
    private float iconScale;
    private int iconAlpha;
    private TabStateListener tabStateListener;
    private int clickShadowIndex = -2;
    private int touchDownIndex = -2;
    private int touchUpIndex = -2;

    public FoldingTabBar(Context context) {
        super(context);
        initPaint();
        setLongClickable(true);
    }

    public FoldingTabBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
        setLongClickable(true);
    }

    private void initPaint() {
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#4AD6BD"));
        bgPaint.setStrokeCap(Paint.Cap.ROUND);
        bgPaint.setStrokeWidth(VIEW_DEFAULT_WIDTH_HEIGHT);

        actionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        actionPaint.setColor(Color.parseColor("#FEFFFF"));
        actionPaint.setStrokeWidth(10);

        iconPaint = new Paint();

        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.parseColor("#A8F0F0F0"));
        shadowPaint.setStrokeCap(Paint.Cap.ROUND);
        shadowPaint.setStrokeWidth(6 * VIEW_DEFAULT_WIDTH_HEIGHT / 7);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(width / 2, height / 2);
        if (icons == null || icons.length == 0 || tabState == STATE_CLOSED) {
            canvas.drawPoint(0, 0, bgPaint);
        } else {
            canvas.drawPoint(0, 0, bgPaint);
            canvas.drawLines(tabPts, bgPaint);
            for (int i = 0; i < iconPos.length; i++) {
                canvas.save();
                canvas.translate(iconPos[i][0], iconPos[i][1]);
                canvas.rotate(iconRotation);
                canvas.scale(iconScale, iconScale);
                iconPaint.setAlpha(iconAlpha);
                canvas.drawBitmap(icons[i], -icons[i].getWidth() / 2, -icons[i].getHeight() / 2, iconPaint);
                if (clickShadowIndex == i) {
                    canvas.drawPoint(0, 0, shadowPaint);
                }
                canvas.restore();
            }
        }

        if (clickShadowIndex == -1) {
            canvas.drawPoint(0, 0, shadowPaint);
        }

        canvas.save();
        canvas.rotate(actionRotation);
        canvas.drawLines(actionPts, actionPaint);
        canvas.restore();

        canvas.restore();
    }

    public FoldingTabBar setItemsIcons(Bitmap... icons) {
        this.icons = icons;
        resizeIcons(icons);
        requestLayout();
        return this;
    }

    public FoldingTabBar setItemsIcons(int... resIds) {
        Bitmap[] icons = new Bitmap[resIds.length];
        for (int i = 0; i < icons.length; i++) {
            icons[i] = BitmapFactory.decodeResource(getResources(), resIds[i]);
        }
        setItemsIcons(icons);
        return this;
    }

    /**
     * tab icon 点击监听
     *
     * @param mListener
     * @return
     */
    public FoldingTabBar setOnTabItemClickListener(OnTabItemClickListener mListener) {
        this.mListener = mListener;
        return this;
    }

    /**
     * tab 状态监听
     *
     * @param tabStateListener
     * @return
     */
    public FoldingTabBar setTabStateListener(TabStateListener tabStateListener) {
        this.tabStateListener = tabStateListener;
        return this;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            getCurrentTouchIndex(event);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            clickShadowIndex = -2;
            getCurrentTouchIndex(event);
        }
        invalidate();
        if (tabState == STATE_OPENING || tabState == STATE_CLOSING) {
            touchDownIndex = -2;
            touchUpIndex = -2;
            return super.onTouchEvent(event);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (actionRectF.contains(event.getX(), event.getY()) && touchDownIndex == touchUpIndex) {
                switchTab();
                touchDownIndex = -2;
                touchUpIndex = -2;
                return true;
            } else if (tabState == STATE_OPENED) {
                for (int i = 0; i < iconRectFs.length; i++) {
                    if (iconRectFs[i].contains(event.getX(), event.getY()) && touchDownIndex == touchUpIndex) {
                        switchTab();
                        if (mListener != null) {
                            mListener.onItemClick(i);
                        }
                        touchDownIndex = -2;
                        touchUpIndex = -2;
                        return true;
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private void getCurrentTouchIndex(MotionEvent event) {
        if (actionRectF.contains(event.getX(), event.getY())) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                touchDownIndex = clickShadowIndex = -1;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                touchUpIndex = -1;
            }

        } else if (tabState == STATE_OPENED) {
            for (int i = 0; i < iconRectFs.length; i++) {
                if (iconRectFs[i].contains(event.getX(), event.getY())) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        touchDownIndex = clickShadowIndex = i;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        touchUpIndex = i;
                    }

                }
            }
        }
    }

    private void switchTab() {

        if (tabState == STATE_OPENED) {
            performCloseAnimation();
        } else {
            performOpenAnimation();
        }

    }

    private void performCloseAnimation() {
        tabState = STATE_CLOSING;
        if (tabStateListener != null) {
            tabStateListener.onTabClosing();
        }
        ValueAnimator openAnim1 = ValueAnimator.ofFloat(1, 0);
        openAnim1.setDuration(350);
        openAnim1.setInterpolator(new DecelerateInterpolator());
        openAnim1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                openAnimFactor = animatedValue;

                actionRotation = 225 * animatedValue;
                prepareTabBg();
                iconAlpha = 0;
                invalidate();
            }
        });
        openAnim1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                tabState = STATE_CLOSED;
                if (tabStateListener != null) {
                    tabStateListener.onTabClosed();
                }
            }
        });
        openAnim1.start();
    }

    private void performOpenAnimation() {

        tabState = STATE_OPENING;
        if (tabStateListener != null) {
            tabStateListener.onTabOpening();
        }

        iconAlpha = 0;
        ValueAnimator openAnim1 = ValueAnimator.ofFloat(0, 1);
        openAnim1.setDuration(350);
        openAnim1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                openAnimFactor = (float) animation.getAnimatedValue();
                prepareTabBg();
                invalidate();
            }
        });
        openAnim1.start();

        ValueAnimator openAnim2 = ValueAnimator.ofFloat(0, 90, 180, 285, 225, 195, 245, 225);
        openAnim2.setDuration(500);
        openAnim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                actionRotation = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        openAnim2.start();

        ValueAnimator openAnim3 = ValueAnimator.ofFloat(-270, 60, -30, 20, 0);
        openAnim3.setDuration(500);
        openAnim3.setInterpolator(new LinearInterpolator());
        openAnim3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                iconRotation = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        openAnim3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                iconAlpha = 255;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                tabState = STATE_OPENED;
                if (tabStateListener != null) {
                    tabStateListener.onTabOpened();
                }
            }
        });
        openAnim3.setStartDelay(250);
        openAnim3.start();

        ValueAnimator openAnim4 = ValueAnimator.ofFloat(0, 1);
        openAnim4.setDuration(300);
        openAnim4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                iconScale = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        openAnim4.setStartDelay(250);
        openAnim4.start();

    }

    private void resizeIcons(Bitmap[] icons) {
        for (int i = 0; i < icons.length; i++) {
            Matrix matrix = new Matrix();
            float scale;
            matrix.setScale(scale = 1.3f * VIEW_DEFAULT_WIDTH_HEIGHT / 3f / icons[i].getHeight(), scale);
            icons[i] = Bitmap.createBitmap(icons[i], 0, 0, icons[i].getWidth(), icons[i].getHeight(), matrix, false);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.width = w;
        this.height = h;

        prepareTabBg();
        prepareActionIcon();
        prepareIcons();
    }

    private void prepareActionIcon() {
        actionPts[0] = -VIEW_DEFAULT_WIDTH_HEIGHT / 3 / 2;
        actionPts[1] = 0;
        actionPts[2] = VIEW_DEFAULT_WIDTH_HEIGHT / 3 / 2;
        actionPts[3] = 0;
        actionPts[4] = 0;
        actionPts[5] = -VIEW_DEFAULT_WIDTH_HEIGHT / 3 / 2;
        actionPts[6] = 0;
        actionPts[7] = VIEW_DEFAULT_WIDTH_HEIGHT / 3 / 2;
        actionRectF = new RectF(-VIEW_DEFAULT_WIDTH_HEIGHT / 2 + width / 2, 0, VIEW_DEFAULT_WIDTH_HEIGHT / 2 + width / 2, VIEW_DEFAULT_WIDTH_HEIGHT);
    }

    private void prepareIcons() {
        if (icons != null) {
            float iconUnitLength = width / ((icons.length % 2 == 0 ? icons.length : icons.length + 1) + 1);
            tabPts[2] = icons.length % 2 == 0 ? width / 2 - VIEW_DEFAULT_WIDTH_HEIGHT / 2 : width / 2 - 3 * VIEW_DEFAULT_WIDTH_HEIGHT / 2;

            iconPos = new float[icons.length][2];
            iconRectFs = new RectF[icons.length];
            int leftIconCount = icons.length % 2 == 0 ? icons.length / 2 : icons.length / 2 + 1;
            int rightIconCount = icons.length - leftIconCount;

            for (int i = 0; i < leftIconCount; i++) {
                iconPos[i][0] = -iconUnitLength * (leftIconCount - i);
                iconPos[i][1] = 0;
                iconRectFs[i] = new RectF(-iconUnitLength * (leftIconCount - i) - iconUnitLength / 2 + width / 2, 0,
                        -iconUnitLength * (leftIconCount - i) + iconUnitLength / 2 + width / 2, height);
            }
            for (int i = 0; i < rightIconCount; i++) {
                iconPos[i + leftIconCount][0] = iconUnitLength * (i + 1);
                iconPos[i + leftIconCount][1] = 0;
                iconRectFs[i + leftIconCount] = new RectF(iconUnitLength * (i + 1) - iconUnitLength / 2 + width / 2, 0,
                        iconUnitLength * (i + 1) + iconUnitLength / 2 + width / 2, height);
            }
        }
    }

    private void prepareTabBg() {
        tabPts[0] = (-width / 2 + VIEW_DEFAULT_WIDTH_HEIGHT / 2) * openAnimFactor;
        tabPts[1] = 0;
        tabPts[2] = (icons != null ? (icons.length % 2 == 0 ? width / 2 - VIEW_DEFAULT_WIDTH_HEIGHT / 2 : width / 2 - 3 * VIEW_DEFAULT_WIDTH_HEIGHT / 2) : (width / 2 - VIEW_DEFAULT_WIDTH_HEIGHT / 2)) * openAnimFactor;
        tabPts[3] = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureSelf(widthMeasureSpec, VIEW_DEFAULT_WIDTH_HEIGHT * (icons != null ? ((icons.length % 2 == 0 ? icons.length : icons.length + 1) + 1) : 1)), measureSelf(heightMeasureSpec, VIEW_DEFAULT_WIDTH_HEIGHT));
    }

    private int measureSelf(int measureSpec, int defaultDimension) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = defaultDimension;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    /**
     * tab icon 点击监听
     */
    public interface OnTabItemClickListener {
        void onItemClick(int position);
    }

    /**
     * tab 状态监听
     */
    public static abstract  class TabStateListener {
        public void onTabOpening() {
        }

        public void onTabOpened() {
        }

        public void onTabClosing() {
        }

        public void onTabClosed() {
        }
    }

}
