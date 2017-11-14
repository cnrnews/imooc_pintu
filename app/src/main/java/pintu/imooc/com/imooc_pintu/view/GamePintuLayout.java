package pintu.imooc.com.imooc_pintu.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pintu.imooc.com.imooc_pintu.R;
import pintu.imooc.com.imooc_pintu.entity.ImagePiece;
import pintu.imooc.com.imooc_pintu.utils.ImageSplitterUtils;

/**
 * @author：lihl on 2017/11/12 20:35
 * @email：1601796593@qq.com
 */
public class GamePintuLayout extends RelativeLayout implements View.OnClickListener {
    private final int TIME_CHANGED = 0x110;
    private final int LEVEL_UP = 0x111;
    private int mColumn = 3;
    /**
     * 容器内边距
     */
    private int mPadding;
    /**
     * 每张小图距离
     */
    private int mMargin = 3;
    /**
     * 面板宽度
     */
    private int mWidth;
    private ImageView[] mGamePictureItems;

    private ImageView mFirst;
    private ImageView mSecond;

    /**
     * 游戏图片
     */
    private Bitmap bitmap;
    private List<ImagePiece> mItemBitmaps;

    private int mItemWidth;
    /**
     * 等级
     */
    private int level = 1;
    /**
     * 时间
     */
    private int mTime;

    private boolean once;
    private boolean isAnim;
    /**
     * 是否暂停
     */
    private boolean isPause;
    /**
     * 游戏成功
     */
    private boolean isGameSuccess;
    /**
     * 时间可用
     */
    private boolean isTimeEnavled;
    /**
     * 游戏结束
     */
    private boolean isGameOver;

    /**
     * 动画图层
     */
    private RelativeLayout mAnimLayout;
    private GamePintuListener listener;

    public GamePintuLayout(Context context) {
        this(context, null);
    }

    public GamePintuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GamePintuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics()
        );
        mPadding = min(getPaddingLeft()
                , getPaddingRight(), getPaddingTop(), getPaddingBottom());
    }


    public void setGameLevelListener(GamePintuListener listener) {
        this.listener = listener;
    }

    public void setTimeEnavled(boolean timeEnavled) {
        isTimeEnavled = timeEnavled;
    }

    /**
     * 成功回掉,关卡
     */
    public interface GamePintuListener {
        void levelUp(int level);

        void timeChanged(int currentTime);

        void gameOver();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TIME_CHANGED:
                    if (isGameSuccess || isGameOver || isPause) {
                        return;
                    }
                    if (listener != null) {
                        listener.timeChanged(mTime);
                        if (mTime == 0) {
                            isGameOver = true;
                            listener.gameOver();
                            return;
                        }
                    }
                    mTime--;//
                    handler.sendEmptyMessageDelayed(TIME_CHANGED, 1000);
                    break;
                case LEVEL_UP:
                    level += 1;
                    if (listener != null) {
                        listener.levelUp(level);
                    } else {
                        nextLevel();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = Math.min(getMeasuredWidth(), getMeasuredHeight());
        if (!once) {
            //切图
            initBitmap();
            //设置宽高属性
            initItem();
            checkTimeEnable();
            once = true;
        }
        setMeasuredDimension(mWidth, mWidth);
    }

    /**
     * 获取最小值
     */
    private int min(int... param) {
        int min = param[0];
        for (int tmp : param) {
            if (tmp < min) {
                min = tmp;
            }
        }
        return min;
    }


    /**
     * 检测时间有效性
     */
    private void checkTimeEnable() {
        if (isTimeEnavled) {
            countTimeBaseLevel();
            handler.sendEmptyMessage(TIME_CHANGED);
        }
    }

    /**
     * 根据等级计算时间
     */
    private void countTimeBaseLevel() {
        mTime = (int) (Math.pow(2, level) * 60);
    }

    private void initItem() {
        mItemWidth = (mWidth - mPadding * 2 - mMargin * (mColumn - 1)) / mColumn;
        mGamePictureItems = new ImageView[mColumn * mColumn];
        for (int i = 0; i < mGamePictureItems.length; i++) {
            ImageView item = new ImageView(getContext());
            item.setOnClickListener(this);
            item.setImageBitmap(mItemBitmaps.get(i).getBitmap());
            mGamePictureItems[i] = item;
            item.setId(i + 1);
            item.setTag(i + "_" + mItemBitmaps.get(i).getIndex());
            RelativeLayout.LayoutParams lp = new LayoutParams(mItemWidth, mItemWidth);
            //设置item横向间隙
            if ((i + 1) % mColumn != 0) {
                lp.rightMargin = mMargin;
            }
            //不是第一列
            if (i % mColumn != 0) {
                lp.addRule(RelativeLayout.RIGHT_OF,
                        mGamePictureItems[i - 1].getId());
            }
            //纵向间距
            if ((i + 1) > mColumn) {
                lp.topMargin = mMargin;
                lp.addRule(RelativeLayout.BELOW,
                        mGamePictureItems[i - mColumn].getId());
            }
            addView(item, lp);
        }
    }

    /***切图*/
    private void initBitmap() {
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.pic);
        }
        mItemBitmaps = ImageSplitterUtils.spliteImage(
                bitmap, mColumn);
        //乱序
        Collections.sort(mItemBitmaps, new Comparator() {
            @Override
            public int compare(Object o, Object t1) {
                return Math.random() > 0.5 ? 1 : -1;
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (isAnim) {
            return;
        }
        //点击同一个View
        if (mFirst == v) {
            mFirst.setColorFilter(null);
            mFirst = null;
            return;
        }
        if (mFirst == null) {
            mFirst = (ImageView) v;
            mFirst.setColorFilter(Color.parseColor("#55FF0000"));
        } else {
            mSecond = (ImageView) v;
            exchangeView();
        }
    }


    private void exchangeView() {
        mFirst.setColorFilter(null);
        setUpAnimationLaout();
        final ImageView first = new ImageView(getContext());
        final Bitmap firstBitmap = mItemBitmaps.get(getImageIdBtTag
                ((String) mFirst.getTag())).getBitmap();
        first.setImageBitmap(firstBitmap);
        LayoutParams lp = new LayoutParams(mItemWidth, mItemWidth);
        lp.leftMargin = mFirst.getLeft() - mPadding;
        lp.topMargin = mFirst.getTop() - mPadding;

        first.setLayoutParams(lp);
        mAnimLayout.addView(first);

        //Second
        final ImageView second = new ImageView(getContext());
        final Bitmap secondBitmap = mItemBitmaps.get(getImageIdBtTag
                ((String) mSecond.getTag())).getBitmap();
        second.setImageBitmap(secondBitmap);
        LayoutParams lp2 = new LayoutParams(mItemWidth, mItemWidth);
        lp2.leftMargin = mSecond.getLeft() - mPadding;
        lp2.topMargin = mSecond.getTop() - mPadding;
        second.setLayoutParams(lp2);
        mAnimLayout.addView(second);


        //动画
        TranslateAnimation anim = new TranslateAnimation(
                0, mSecond.getLeft() - mFirst.getLeft(), 0, mSecond.getTop()
                - mFirst.getLeft());
        anim.setDuration(300);
        anim.setFillAfter(true);
        first.startAnimation(anim);


        TranslateAnimation secondAnim = new TranslateAnimation(
                0, mFirst.getLeft() - mSecond.getLeft(), 0, mFirst.getTop() - mSecond.getTop());
        secondAnim.setDuration(300);
        secondAnim.setFillAfter(true);
        second.startAnimation(secondAnim);


        //监听
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mFirst.setVisibility(INVISIBLE);
                mSecond.setVisibility(INVISIBLE);
                isAnim = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                String firstTag = (String) mFirst.getTag();
                String secondTag = (String) mSecond.getTag();

                mFirst.setImageBitmap(secondBitmap);
                mSecond.setImageBitmap(firstBitmap);
                //交换tag
                mFirst.setTag(secondTag);
                mSecond.setTag(firstTag);

                mFirst.setVisibility(VISIBLE);
                mSecond.setVisibility(VISIBLE);

                mFirst = mSecond = null;
                mAnimLayout.removeAllViews();
                isAnim = false;
                checkSuccess();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
    private int getImageIndexByTag(String tag) {
        return Integer.parseInt(tag.split("_")[1]);
    }

    private int getImageIdBtTag(String tag) {
        return Integer.parseInt(tag.split("_")[0]);
    }

    private void setUpAnimationLaout() {
        if (mAnimLayout == null) {
            mAnimLayout = new RelativeLayout(getContext());
            addView(mAnimLayout);
        }
    }

    /**
     * 是否成功
     */
    private void checkSuccess() {
        boolean isSuccess = true;
        for (int i = 0; i < mGamePictureItems.length; i++) {
            ImageView imageView = mGamePictureItems[i];
            if (getImageIndexByTag(String.valueOf(imageView.getTag())) != i) {
                isSuccess = false;
            }
        }
        if (isSuccess) {
            changeSuccess();
            handler.sendEmptyMessage(LEVEL_UP);
        }
    }

    /**
     * 重新开始
     */
    public void restart() {
        isGameOver = false;
        mColumn--;
        nextLevel();
    }

    /**暂停*/
    public void pause() {
        isPause = true;
        handler.removeMessages(TIME_CHANGED);
    }
    /**恢复*/
    public void resume() {
        if (isPause) {
            isPause = false;
            handler.sendEmptyMessage(TIME_CHANGED);
        }
    }
    public void nextLevel() {
        removeAllViews();
        mAnimLayout = null;
        mColumn++;
        isGameSuccess = false;
        checkTimeEnable();
        initBitmap();
        initItem();
    }
    public void changeSuccess() {
        isGameSuccess = true;
        handler.removeMessages(TIME_CHANGED);
    }
}
