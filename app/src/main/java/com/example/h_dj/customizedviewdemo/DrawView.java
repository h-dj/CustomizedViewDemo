package com.example.h_dj.customizedviewdemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Created by H_DJ on 2017/4/23.
 */

public class DrawView extends View {
    private final static String TAG = "DrawView";

    /**
     * 屏幕的宽高
     */
    private int screenWidth;
    private int screenHeight;

    private static final int DRAW_RECT = 1; //矩形
    private static final int DRAW_OVAL = 2; //椭圆
    private static final int DRAW_PATH = 3; //画路径
    private static final int DRAW_ARROW = 4; //画箭头
    private float currentX;//当前X坐标
    private float currentY;//当前Y坐标
    private int startX; //开始坐标
    private int startY; //开始坐标


    private Paint mPaint;//画笔
    private Path mPath;//路径

    private Paint mBitmapPaint; //暂时画布中的画笔
    private Bitmap mBitmap; //暂时画布
    private Canvas mCanvas;


    private DrawPath mDrawPath;

    private List<DrawPath> savePath; //保存
    private List<DrawPath> deletePath; //删除

    /**
     * 保存画笔路径实体类
     */
    private class DrawPath {
        Paint paint;
        Path path;
    }

    private int currentGraphics = DRAW_PATH; //当前画笔图形
    private int currentStyle = 1;  //画笔的当前样式；1：为正常画笔；否则为橡皮差
    private float currentSize = 15;//画笔大小
    /**
     * 保存颜色集合
     */

    private int currentColor = Color.BLACK;//当前画笔颜色;默认黑色


    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        /**
         * 获取屏幕的宽高
         */
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        initCanvesBitmap();
        savePath = new ArrayList<DrawPath>();
        deletePath = new ArrayList<DrawPath>();
    }

    /**
     * 撤销
     */
    public void undo() {

        if (savePath != null && savePath.size() > 0) {
            Log.i(TAG, "undo: ");
//            //初始化画布
            initCanvesBitmap();
            //把保存的路径中的一个删除 ；
            DrawPath drawPath = savePath.get(savePath.size() - 1);
            deletePath.add(drawPath);
            savePath.remove(savePath.size() - 1);
            //在重新画上去
            Iterator<DrawPath> iterator = savePath.iterator();
            while (iterator.hasNext()) {
                DrawPath next = iterator.next();
                mCanvas.drawPath(next.path, next.paint);
            }
            //重新绘制
            invalidate();
        }
    }

    /**
     * 恢复
     */
    public void redo() {
        if (deletePath.size() > 0) {
            //把删除路径的最后一个拿出来；重新加入保存路径中
            DrawPath drawPath = deletePath.get(deletePath.size() - 1);
            savePath.add(drawPath);
            deletePath.remove(deletePath.size() - 1);
            mCanvas.drawPath(drawPath.path, drawPath.paint);
        }
        invalidate();//重新绘制
    }

    /**
     * 清除画板
     */
    public void clear() {
        initCanvesBitmap();
        invalidate();
        savePath.clear();
        deletePath.clear();

    }

    /**
     * 保存为图片
     */
    public void saveToPicture() {
        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName = new SimpleDateFormat("yy-MM-dd hh-mm-ss").format(new Date()) + ".png";
        String savePath = sdPath + File.separator + fileName;
        File file = new File(savePath);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 创建单独的画布bitmap
     */
    private void initCanvesBitmap() {
        setPaintStyle();
        //new 一个暂时的画布中的画笔
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        //暂时画布;与屏幕一样大
        mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);//所有mCanvas画的东西都被保存在了mBitmap中
        mCanvas.drawColor(Color.WHITE);
    }

    /**
     * 初始化画笔的样式
     */
    private void setPaintStyle() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        /**
         *设置外边缘
         *  set the paint's Join, used whenever the paint's style is Stroke or StrokeAndFill.
         */
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);//设置形状
        mPaint.setAntiAlias(true); //设置抗锯齿
        mPaint.setDither(true);  //抖动
        if (currentStyle == 1) {
            mPaint.setStrokeWidth(currentSize); //设置当前画笔大小
            mPaint.setColor(currentColor); //设置画笔颜色
        } else {
            //橡皮檫模式
            mPaint.setAlpha(0); //设置透明度
            /**
             * setXfermode: 设置混合模式   （只在源图像和目标图像相交的地方绘制目标图像）
             */
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
            mPaint.setColor(Color.TRANSPARENT); //设置透明色
            mPaint.setStrokeWidth(50);  //设置画笔大小
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.i(TAG, "onDraw: ");

        /**
         * 把暂时画布的东西显示出来
         */
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

        //实时显示
        if (mPath != null) {
            canvas.drawPath(mPath, mPaint);
        }
        super.onDraw(canvas);
    }


    /**
     * 设置画笔颜色
     *
     * @param currentColor int
     */
    public void setCurrentColor(int currentColor) {
        this.currentColor = currentColor;
    }

    /**
     * @param style 1 为画笔；否则为橡皮擦
     */
    public void setCurrentStyle(int style) {
        currentStyle = style;
    }

    /**
     * 画矩形
     */
    public void draw_rect() {
        currentGraphics = DRAW_RECT;
    }

    /**
     * 画椭圆
     */
    public void draw_oval() {
        currentGraphics = DRAW_OVAL;
    }

    /**
     * 画路径
     */
    public void draw_path() {
        currentGraphics = DRAW_PATH;
    }

    /**
     * 画路径
     */
    public void draw_arr() {
        currentGraphics = DRAW_ARROW;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float X = event.getX();
        float Y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = (int) event.getX();
                startY = (int) event.getY();
                mPath = new Path();
                setPaintStyle();

                //画笔实体类
                mDrawPath = new DrawPath();
                mDrawPath.path = mPath;
                mDrawPath.paint = mPaint;

                drawStart(X, Y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                drawMove(X, Y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                draw_up();
                invalidate();
                break;
            default:
                return super.onTouchEvent(event);
        }
        return true;
    }

    /**
     * 画完
     */
    private void draw_up() {
        if (currentGraphics == DRAW_PATH) {
            mPath.lineTo(currentX, currentY);
        }
        mCanvas.drawPath(mPath, mPaint);
        savePath.add(mDrawPath);
        mPath = null;
    }

    /**
     * 正在画
     *
     * @param x
     * @param y
     */
    private void drawMove(float x, float y) {
        if (currentGraphics == DRAW_RECT) {
            mPath.reset();
            RectF rectF = new RectF(startX, startY, x, y);
            /**
             * Path.Direction.CCW　：指定闭合的方式；顺时针CW，逆时针CCW
             */
            mPath.addRect(rectF, Path.Direction.CCW);
        } else if (currentGraphics == DRAW_OVAL) {
            mPath.reset();
            RectF rectF = new RectF(startX, startY, x, y);
            mPath.addOval(rectF, Path.Direction.CCW);
        } else if (currentGraphics == DRAW_PATH) {
            mPath.lineTo(x, y);
        } else if (currentGraphics == DRAW_ARROW) {
            mPath.reset();
            drawArrow(startX, startY, x, y);
        }
        currentX = x;
        currentY = y;
    }

    private void drawArrow(int startX, int startY, float x, float y) {
        double lineLength = Math.sqrt(Math.pow(Math.abs(x - startX), 2) + Math.pow(Math.abs(y - startY), 2));
        double H = 0; //箭头高度
        double L = 0; //箭头长度
        if (lineLength < 320) {
            H = lineLength / 4;
            L = lineLength / 6;
        } else {
            H = 80;
            L = 50;
        }

        double arrowAngle = Math.atan(L / H); //箭头角度
        double arraowLen = Math.sqrt(L * L + H * H); // 箭头的长度
        double[] pointXY1 = rotateAndGetPoint((int) x - startX, (int) y - startY, arrowAngle, true, arraowLen);
        double[] pointXY2 = rotateAndGetPoint((int) x - startX, (int) y - startY, -arrowAngle, true, arraowLen);
        int x3 = (int) (x - pointXY1[0]);//(x3,y3)为箭头一端的坐标
        int y3 = (int) (y - pointXY1[1]);
        int x4 = (int) (x - pointXY2[0]);//(x4,y4)为箭头另一端的坐标
        int y4 = (int) (y - pointXY2[1]);
        // 画线
        mPath.moveTo(startX, startY);
        mPath.lineTo(x, y);
        mPath.moveTo(x3, y3);
        mPath.lineTo(x, y);
        mPath.lineTo(x4, y4);
        mPath.close();//闭合线条
    }

    /**
     * 矢量旋转函数，计算末点的位置
     *
     * @param x       x分量
     * @param y       y分量
     * @param ang     旋转角度
     * @param isChLen 是否改变长度
     * @param newLen  箭头长度长度
     * @return 返回末点坐标
     */
    private double[] rotateAndGetPoint(int x, int y, double ang, boolean isChLen, double newLen) {
        double pointXY[] = new double[2];
        double vx = x * Math.cos(ang) - y * Math.sin(ang);
        double vy = x * Math.sin(ang) + y * Math.cos(ang);
        if (isChLen) {
            double d = Math.sqrt(vx * vx + vy * vy);
            pointXY[0] = vx / d * newLen;
            pointXY[1] = vy / d * newLen;
        }
        return pointXY;
    }

    /**
     * 开始画
     *
     * @param x
     * @param y
     */
    private void drawStart(float x, float y) {
        mPath.reset();
        currentX = x;
        currentY = y;
        mPath.moveTo(currentX, currentY);
    }
}
