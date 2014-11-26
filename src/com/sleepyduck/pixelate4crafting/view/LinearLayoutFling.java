package com.sleepyduck.pixelate4crafting.view;

import com.sleepyduck.pixelate4crafting.BetterLog;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

public class LinearLayoutFling extends LinearLayout {
	private LayoutTransition mLayoutTransition;
	private View mSelectedChild;
	private float mStartDragPos;
	private ViewConfiguration mViewConfig;
	private OnItemFlungListener mOnItemFlungListener;

	public LinearLayoutFling(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup();
	}

	public LinearLayoutFling(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup();
	}

	public LinearLayoutFling(Context context) {
		super(context);
		setup();
	}

	private void setup() {
		mLayoutTransition = new LayoutTransition();
		mLayoutTransition.enableTransitionType(LayoutTransition.APPEARING);
		mLayoutTransition.enableTransitionType(LayoutTransition.DISAPPEARING);
		setLayoutTransition(mLayoutTransition);
		mViewConfig = ViewConfiguration.get(getContext());
	}

	public void setOnItemFlungListener(OnItemFlungListener listener) {
		mOnItemFlungListener = listener;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				for (int i = 0; i < getChildCount(); ++i) {
					View view = getChildAt(i);
					Rect childRect = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
					if (childRect.contains((int) ev.getX(), (int) ev.getY())) {
						mSelectedChild = view;
						mStartDragPos = ev.getX();
						return false;
					}
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (mSelectedChild != null) {
					float distance = Math.abs(ev.getX() - mStartDragPos);
					if (distance >= mViewConfig.getScaledTouchSlop()) {
						requestDisallowInterceptTouchEvent(true);
						return true;
					}
					return false;
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mSelectedChild = null;
				break;
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		float distance = ev.getX() - mStartDragPos;
		switch (ev.getAction()) {
			case MotionEvent.ACTION_MOVE:
				if (mSelectedChild != null) {
					mSelectedChild.setX(mSelectedChild.getLeft() + distance);
					float alpha = 1f - 2f * Math.min(0.4f, Math.abs(distance) / (float) getWidth());
					mSelectedChild.setAlpha(alpha);
				}
				break;
			case MotionEvent.ACTION_UP:
				if (mOnItemFlungListener != null && Math.abs(distance) > 0.4f * (float) getWidth()) {
					BetterLog.d(this, "distance = " + distance);
					boolean deleted = mOnItemFlungListener.onItemFlung(mSelectedChild);
					if (deleted) {
						BetterLog.d(this, "mSelectedChild.getParent() != this");
						mSelectedChild = null;
						break;
					}
				}
			case MotionEvent.ACTION_CANCEL:
				mSelectedChild.animate().translationX(0).alpha(1).start();
				mSelectedChild = null;
				break;
		}
		return true;
	}

}
