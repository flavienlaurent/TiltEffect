package com.fourmob.tilteffect;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by f.laurent on 18/07/13.
 */
public class TiltEffectAttacher implements View.OnTouchListener {

	enum TouchPart {LEFT, RIGHT, BOTTOM, TOP, TOPLEFT, TOPRIGHT, BOTTOMLEFT, MIDDLE, BOTTOMRIGHT}

	private static final String TAG = "com.fourmob.tilteffect.TiltView";

	private static int TILT_VALUE = 5;

	private TouchPart mTouchPart = TouchPart.MIDDLE;

	private ArrayList<TiltAnimation.Rotation> mLastRotations = new ArrayList<TiltAnimation.Rotation>();

	private WeakReference<View> mView;

	public static final TiltEffectAttacher attach(View view) {
		return new TiltEffectAttacher(view);
	}

	private TiltEffectAttacher(View view) {
		this.mView = new WeakReference<View>(view);
		view.setOnTouchListener(this);
	}

	public final void cleanup() {
		this.mView = null;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		final View view = mView.get();
		final int action = event.getAction();

		float x = event.getX();
		float y = event.getY();

		int height = view.getHeight();
		int width = view.getWidth();

		float cornerWidth = width * 0.20f;
		float cornerHeight = height * 0.20f;

		TouchPart oldTouchPart = mTouchPart;

		switch (action) {
			case MotionEvent.ACTION_UP:
				mTouchPart = TouchPart.MIDDLE;
				break;
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				if(x <= cornerWidth && y <= cornerHeight) {
					mTouchPart = TouchPart.TOPLEFT;
				} else if(x <= cornerWidth && y >= height-cornerHeight) {
					mTouchPart = TouchPart.BOTTOMLEFT;
				} else if(x >= width-cornerWidth && y <= cornerHeight) {
					mTouchPart = TouchPart.TOPRIGHT;
				} else if(x >= width-cornerWidth && y >= height-cornerHeight) {
					mTouchPart = TouchPart.BOTTOMRIGHT;
				} else if(x > cornerWidth && x < width-cornerWidth && y <= cornerHeight) {
					mTouchPart = TouchPart.TOP;
				} else if(x > cornerWidth && x < width-cornerWidth && y >= height-cornerHeight) {
					mTouchPart = TouchPart.BOTTOM;
				} else if(y > cornerHeight && y < height-cornerHeight && x <= cornerWidth) {
					mTouchPart = TouchPart.LEFT;
				} else if(y > cornerHeight && y < height-cornerHeight && x >= width-cornerWidth) {
					mTouchPart = TouchPart.RIGHT;
				} else {
					mTouchPart = TouchPart.MIDDLE;
				}
				break;
		}

		if(mTouchPart != oldTouchPart) {
			Log.d(TAG, "TouchPart has changed : " + mTouchPart);
			switch (mTouchPart) {
				case MIDDLE:
					applyTitlEffect(view, buildResetRotations());
					break;
				case LEFT:
					applyTitlEffect(view, buildResetRotations(TiltAnimation.ROTATE_AXIS_Y), getLastToDegreesByAxis(TiltAnimation.ROTATE_AXIS_Y), -TILT_VALUE, TiltAnimation.ROTATE_AXIS_Y);
					break;
				case RIGHT:
					applyTitlEffect(view, buildResetRotations(TiltAnimation.ROTATE_AXIS_Y), getLastToDegreesByAxis(TiltAnimation.ROTATE_AXIS_Y), TILT_VALUE, TiltAnimation.ROTATE_AXIS_Y);
					break;
				case BOTTOM:
					applyTitlEffect(view, buildResetRotations(TiltAnimation.ROTATE_AXIS_X), getLastToDegreesByAxis(TiltAnimation.ROTATE_AXIS_X), -TILT_VALUE, TiltAnimation.ROTATE_AXIS_X);
					break;
				case TOP:
					applyTitlEffect(view, buildResetRotations(TiltAnimation.ROTATE_AXIS_X), getLastToDegreesByAxis(TiltAnimation.ROTATE_AXIS_X), TILT_VALUE, TiltAnimation.ROTATE_AXIS_X);
					break;
				case TOPLEFT:
					applyTitlEffect(view,
							new TiltAnimation.Rotation(TiltAnimation.ROTATE_AXIS_X, getLastToDegreesByAxis(TiltAnimation.ROTATE_AXIS_X), TILT_VALUE),
							new TiltAnimation.Rotation(TiltAnimation.ROTATE_AXIS_Y, getLastToDegreesByAxis(TiltAnimation.ROTATE_AXIS_Y), -TILT_VALUE));
					break;
				case TOPRIGHT:
					applyTitlEffect(view,
							new TiltAnimation.Rotation(TiltAnimation.ROTATE_AXIS_X, getLastToDegreesByAxis(TiltAnimation.ROTATE_AXIS_X), TILT_VALUE),
							new TiltAnimation.Rotation(TiltAnimation.ROTATE_AXIS_Y, getLastToDegreesByAxis(TiltAnimation.ROTATE_AXIS_Y), TILT_VALUE));
					break;
				case BOTTOMLEFT:
					applyTitlEffect(view,
							new TiltAnimation.Rotation(TiltAnimation.ROTATE_AXIS_X, getLastToDegreesByAxis(TiltAnimation.ROTATE_AXIS_X), -TILT_VALUE),
							new TiltAnimation.Rotation(TiltAnimation.ROTATE_AXIS_Y, getLastToDegreesByAxis(TiltAnimation.ROTATE_AXIS_Y), -TILT_VALUE));
					break;
				case BOTTOMRIGHT:
					applyTitlEffect(view,
							new TiltAnimation.Rotation(TiltAnimation.ROTATE_AXIS_X, getLastToDegreesByAxis(TiltAnimation.ROTATE_AXIS_X), -TILT_VALUE),
							new TiltAnimation.Rotation(TiltAnimation.ROTATE_AXIS_Y, getLastToDegreesByAxis(TiltAnimation.ROTATE_AXIS_Y), TILT_VALUE));
					break;
			}
		}

		return false;
	}

	private TiltAnimation.Rotation[] buildResetRotations(int... exceptAxises) {
		if(mLastRotations == null || mLastRotations.isEmpty()) {
			return new TiltAnimation.Rotation[0];
		}
		ArrayList<TiltAnimation.Rotation> resetRotations = new ArrayList<TiltAnimation.Rotation>();
		for(TiltAnimation.Rotation rotation : mLastRotations) {
			if(! contains(exceptAxises, rotation.mRotateAxis)) {
				resetRotations.add(new TiltAnimation.Rotation(rotation.mRotateAxis, rotation.mToDegrees, 0));
			}
		}
		return resetRotations.toArray(new TiltAnimation.Rotation[0]);
	}

	private boolean contains(int[] ints, int i) {
		if(ints == null || ints.length == 0) {
			return false;
		}
		for(int ii : ints) {
			if(i == ii)  {
				return true;
			}
		}
		return false;
	}

	private float getLastToDegreesByAxis(int axis) {
		if(mLastRotations == null || mLastRotations.isEmpty()) {
			return 0;
		}
		for(TiltAnimation.Rotation rotation : mLastRotations) {
			if(rotation.mRotateAxis == axis) {
				return rotation.mToDegrees;
			}
		}
		return 0;
	}


	private void applyTitlEffect(View view, TiltAnimation.Rotation[] resetRotations, float fromDegrees, float toDegrees, int axis) {
		applyTitlEffect(view, resetRotations, new TiltAnimation.Rotation(axis, fromDegrees, toDegrees));
	}

	private void applyTitlEffect(View view, TiltAnimation.Rotation ... rotations) {
		applyTitlEffect(view, null, rotations);
	}

	private void applyTitlEffect(View view, TiltAnimation.Rotation[] resetRotations, TiltAnimation.Rotation ... rotations) {
		final float centerX = view.getWidth() / 2.0f;
		final float centerY = view.getHeight() / 2.0f;

		final TiltAnimation rotation = new TiltAnimation(centerX, centerY);
		rotation.setDuration(200);
		rotation.setFillAfter(true);
		rotation.setInterpolator(new DecelerateInterpolator());

		if(resetRotations != null) {
			rotation.addRotations(resetRotations);
		}
		rotation.addRotations(rotations);

		mLastRotations.clear();
		mLastRotations.addAll(Arrays.asList(rotations));

		view.startAnimation(rotation);
	}
}
