package com.example.h_dj.customizedviewdemo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private DrawView mDrawView;
    /**
     * 保存颜色集合
     */
    private int[] colorArr = new int[]{Color.BLACK, Color.GREEN, Color.RED, Color.BLUE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
    }

    private void findView() {
        mDrawView = (DrawView) findViewById(R.id.drawview);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.draw_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.paint:
                mDrawView.setCurrentStyle(1);
                break;
            case R.id.eraser:
                mDrawView.setCurrentStyle(0);
                break;
            case R.id.color_black:
                mDrawView.setCurrentColor(colorArr[0]);
                break;
            case R.id.color_blue:
                mDrawView.setCurrentColor(colorArr[3]);
                break;
            case R.id.color_green:
                mDrawView.setCurrentColor(colorArr[1]);
                break;
            case R.id.color_red:
                mDrawView.setCurrentColor(colorArr[2]);
                break;

            case R.id.rect:
                mDrawView.draw_rect();
                break;

            case R.id.oval:
                mDrawView.draw_oval();
                break;
            case R.id.arr:
                mDrawView.draw_arr();
                break;
            case R.id.path:
                mDrawView.draw_path();
                break;
            case R.id.cancle:
                mDrawView.undo();
                break;
            case R.id.recover:
                mDrawView.redo();
                break;
            case R.id.clear:
                mDrawView.clear();
                break;
            case R.id.save:
                mDrawView.saveToPicture();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
