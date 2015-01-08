package com.freedom.gridview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.freedom.gridview.adapter.GridViewAdapter;
import com.freedom.gridview.bean.Data;

/**
 * @ClassName: FreedomGridView
 * @author victor_freedom (x_freedom_reddevil@126.com)
 * @createddate 2015-1-4 下午10:25:52
 * @Description: 可移动item内容的gridView
 */
@SuppressLint("NewApi")
public class FreedomGridView extends GridView {

	// 是否在移动中
	private boolean isMove = false;
	// 是否是第一次移动
	private boolean isFirst = true;
	// 是否是长按
	private boolean isLongClick = false;
	// 图形
	private Bitmap bitmap;
	// 移动的视图
	private ImageView moveView = null;
	// 偏移量
	private int offsetX, offsetY;
	// 在屏幕中触摸的位置
	private int touchPositionInScreen;
	// 移动的目的位置
	private int moveToPosition;
	// 在ITEM中触摸的坐标
	private int touchPositionInItemX, touchPositionInItemY;
	// 移动速度
	private int scaledTouchSlop;
	// 移动过程中，上下边距判定自动滑动距离
	private int upScrollBounce;
	private int downScrollBounce;
	// 窗体管理者，用于添加视图
	private WindowManager windowManager = null;
	private WindowManager.LayoutParams layoutParams = null;
	private GridViewAdapter adapter;

	public FreedomGridView(Context context) {
		super(context);
		scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
	}

	public FreedomGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FreedomGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * 长按判定
	 */
	private Runnable longPressRun = new Runnable() {
		@Override
		public void run() {
			isLongClick = true;
		}
	};

	/**
	 * @Title: contains
	 * @Description: 判断是否触摸坐标是否在视图里面
	 * @param v
	 * @param xInView
	 * @param yInView
	 * @return
	 * @throws
	 */
	private boolean contains(View v, int xInView, int yInView) {
		if (v instanceof ImageView) {
			return ((ImageView) v).getDrawable().getBounds()
					.contains(xInView, yInView);
		}
		return v.getBackground().getBounds().contains(xInView, yInView);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// 拿到适配器
		if (null == adapter || adapter.isEmpty()) {
			adapter = (GridViewAdapter) getAdapter();
		}
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// 拿到相对于触摸视图的坐标
			int x = (int) ev.getX();
			int y = (int) ev.getY();
			// 拿到触摸位置
			touchPositionInScreen = moveToPosition = this.pointToPosition(x, y);

			// 判断位置是否有效
			if (moveToPosition == AdapterView.INVALID_POSITION) {
				break;
			}

			// 拿到当前触摸的可见item
			ViewGroup itemView = (ViewGroup) this.getChildAt(moveToPosition
					- this.getFirstVisiblePosition());
			// 拿到点击位置相对于ITEM视图的偏移量
			touchPositionInItemY = y - itemView.getTop();
			touchPositionInItemX = x - itemView.getLeft();

			// 拿到item视图里面的控件
			View view = itemView.findViewById(R.id.desk_back);
			// 判断点击位置是否在视图里面
			if (this.contains(view, touchPositionInItemX, touchPositionInItemX)) {
				try {
					int[] locationInScreen = new int[2];
					view.getLocationOnScreen(locationInScreen);
				} catch (NullPointerException e) {
					break;
				}
			}
			// 移动视图时候的偏移量
			this.offsetX = (int) (ev.getRawX() - x);
			this.offsetY = (int) (ev.getRawY() - y);

			// 获取触发当拖动视图到最顶端或者最底端自动滚动视图的边距
			upScrollBounce = Math.min(y - scaledTouchSlop, getHeight() / 3);
			downScrollBounce = Math.max(y + scaledTouchSlop,
					getHeight() * 2 / 3);
			itemView.setDrawingCacheEnabled(true);
			// 拿item视图的bitmap
			bitmap = Bitmap.createBitmap(itemView.getDrawingCache());
			itemView.destroyDrawingCache();
			postDelayed(longPressRun, 1000);
			break;
		case MotionEvent.ACTION_MOVE:
			if (isLongClick) {
				int mx = (int) ev.getX();
				int my = (int) ev.getY();
				// 第一次移动，创建移动视图
				if (isFirst)
					initWindowManager(bitmap, mx, my);
				onMove(mx, my);
				// 移除之前的runable
				removeCallbacks(longPressRun);
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			int upY = (int) ev.getY();
			int upX = (int) ev.getX();
			if (isMove && isLongClick) {
				stopMove();
				completeMove(upX, upY);
				isMove = false;
				isLongClick = false;
				break;
			}
			removeCallbacks(longPressRun);
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * @Title: initWindowManager
	 * @Description: 创建移动视图
	 * @param bm
	 * @param x
	 * @param y
	 * @throws
	 */
	public void initWindowManager(Bitmap bm, int x, int y) {
		stopMove();
		isFirst = false;
		layoutParams = new WindowManager.LayoutParams();
		layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
		layoutParams.horizontalMargin = layoutParams.verticalMargin = 0;
		layoutParams.x = x - touchPositionInItemX + offsetX;
		layoutParams.y = y - touchPositionInItemY + offsetY;
		layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		layoutParams.format = PixelFormat.TRANSLUCENT;
		layoutParams.windowAnimations = 0;
		windowManager = (WindowManager) this.getContext().getSystemService(
				"window");

		ImageView moveViewTemp = new ImageView(getContext());
		moveViewTemp.setImageBitmap(bm);

		windowManager = (WindowManager) this.getContext().getSystemService(
				"window");
		windowManager.addView(moveViewTemp, layoutParams);
		moveView = moveViewTemp;

	}

	/**
	 * @Title: stopMove
	 * @Description: 停止移动
	 * @throws
	 */
	public void stopMove() {
		if (moveView != null) {
			windowManager.removeView(moveView);
			moveView = null;
		}
	}

	/**
	 * @Title: onMove
	 * @Description: 视图移动的时候触发的方法
	 * @param x
	 * @param y
	 * @throws
	 */
	public void onMove(int x, int y) {
		isMove = true;
		// 避免拖动到无效区域
		int tempPosition = this.pointToPosition(x, y);
		if (tempPosition != FreedomGridView.INVALID_POSITION) {
			this.moveToPosition = tempPosition;
		}

		// 移动的时候更新视图位置
		if (moveView != null) {
			layoutParams.alpha = 0.8f;
			layoutParams.y = y - touchPositionInItemY + offsetY;
			layoutParams.x = x - touchPositionInItemX + offsetX;
			windowManager.updateViewLayout(moveView, layoutParams);
		}

		int scrollHeight = 0;
		if (y < upScrollBounce) {
			scrollHeight = 30;
		} else if (y > downScrollBounce) {
			scrollHeight = -30;
		}
		// 触发自动滚动
		if (scrollHeight != 0) {
			smoothScrollToPositionFromTop(moveToPosition,
					getChildAt(moveToPosition - getFirstVisiblePosition())
							.getTop() + scrollHeight, 1);
		}
	}

	/**
	 * @Title: completeMove
	 * @Description: 移动完成时
	 * @param x
	 * @param y
	 * @throws
	 */
	public void completeMove(int x, int y) {
		isFirst = true;
		// 拿到停止的位置
		int tempPosition = this.pointToPosition(x, y);
		if (tempPosition != FreedomGridView.INVALID_POSITION) {
			this.moveToPosition = tempPosition;
		}

		if (y < getChildAt(0).getTop()) {
			return;
		} else if (y > getChildAt(getChildCount() - 1).getBottom()) {
			moveToPosition = getAdapter().getCount() - 1;
			return;
		} else {
			// 如果在有效位置
			if (moveToPosition >= 0 && moveToPosition < getAdapter().getCount()) {

				ViewGroup itemView = (ViewGroup) this.getChildAt(moveToPosition
						- this.getFirstVisiblePosition());
				if (itemView != null) {

					ImageView imaveView = (ImageView) itemView
							.findViewById(R.id.desk_back);
					// 判断是否移入了有效视图里面
					boolean isIn = this.contains(imaveView,
							x - itemView.getLeft(), y - itemView.getTop());

					// 如果已经移入了，并且不是触摸时的起始位置
					if (isIn) {
						if (moveToPosition != touchPositionInScreen) {
							itemView.startAnimation(AnimationUtils
									.loadAnimation(getContext(),
											R.anim.desk_scale));
							Data touchData = ((Data) adapter
									.getItem(touchPositionInScreen));
							if (touchData.getNum() == 0) {
								Toast.makeText(getContext(), "数目为0不可变化",
										Toast.LENGTH_SHORT).show();
								return;
							}

							Data toData = (Data) adapter
									.getItem(moveToPosition);
							if (toData.getNum() == 2) {
								Toast.makeText(getContext(), "数目为2不可变化",
										Toast.LENGTH_SHORT).show();
								return;
							}

							touchData.setNum(touchData.getNum() - 1);
							toData.setNum(toData.getNum() + 1);
							adapter.notifyDataSetChanged();
						}
					}
				}
			}
		}
	}

}
