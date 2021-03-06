package com.anysoftkeyboard.keyboards.views.preview;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.anysoftkeyboard.AskPrefs;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class PreviewPopup {

	private static final int[] LONG_PRESSABLE_STATE_SET = {android.R.attr.state_long_pressable};
	private static final int[] EMPTY_STATE_SET = {};

	private int mPreviewPaddingWidth = -1;
	private int mPreviewPaddingHeight = -1;

	private final ViewGroup mPreviewLayout;
	private final TextView mPreviewText;
	private final ImageView mPreviewIcon;

	private final View mParentView;
	private final PopupWindow mPopupWindow;
	private final PreviewPopupTheme mPreviewPopupTheme;

	public PreviewPopup(Context context, View parentView, PreviewPopupTheme previewPopupTheme) {
		mParentView = parentView;
		mPreviewPopupTheme = previewPopupTheme;
		mPopupWindow = new PopupWindow(context);
		LayoutInflater inflate = LayoutInflater.from(context);
		if (mPreviewPopupTheme.getPreviewKeyTextSize() > 0) {
			mPreviewLayout = (ViewGroup) inflate.inflate(R.layout.key_preview, null);
			mPreviewText = (TextView) mPreviewLayout.findViewById(R.id.key_preview_text);
			mPreviewText.setTextColor(mPreviewPopupTheme.getPreviewKeyTextColor());
			mPreviewText.setTypeface(mPreviewPopupTheme.getKeyStyle());
			mPreviewIcon = (ImageView) mPreviewLayout.findViewById(R.id.key_preview_icon);
			mPopupWindow.setBackgroundDrawable(mPreviewPopupTheme.getPreviewKeyBackground());
			mPopupWindow.setContentView(mPreviewLayout);
		} else {
			mPreviewIcon = null;
			mPreviewLayout = null;
			mPreviewText = null;
		}
		mPopupWindow.setTouchable(false);
		mPopupWindow.setAnimationStyle((AnyApplication.getConfig().getAnimationsLevel() == AskPrefs.AnimationsLevel.None) ? 0 : R.style.KeyPreviewAnimation);
	}

	public void showPreviewForKey(Keyboard.Key key, CharSequence label, Point previewPosition) {
		mPreviewIcon.setImageDrawable(null);
		mPreviewText.setTextColor(mPreviewPopupTheme.getPreviewKeyTextColor());

		mPreviewText.setText(label);
		if (label.length() > 1 && key.codes.length < 2) {
			mPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
					mPreviewPopupTheme.getPreviewLabelTextSize());
		} else {
			mPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
					mPreviewPopupTheme.getPreviewKeyTextSize());
		}

		mPreviewText.measure(
				View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
				View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

		int contentWidth = Math.max(mPreviewText.getMeasuredWidth(), key.width);
		int contentHeight = Math.max(mPreviewText.getMeasuredHeight(), key.height);
		showPopup(key, contentWidth, contentHeight, previewPosition);
	}

	public void showPreviewForKey(Keyboard.Key key, Drawable icon, Point previewPosition) {
		mPreviewIcon.setImageState(icon.getState(), false);
		// end of hack. You see, the drawable comes with a state, this state
		// is overridden by the ImageView. No more.
		mPreviewIcon.setImageDrawable(icon);
		mPreviewIcon.measure(
				View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
				View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		int contentWidth = Math.max(mPreviewIcon.getMeasuredWidth(), key.width);
		int contentHeight = Math.max(mPreviewIcon.getMeasuredHeight(), key.height);
		mPreviewText.setText(null);
		showPopup(key, contentWidth, contentHeight, previewPosition);
	}

	private void showPopup(Keyboard.Key key, int contentWidth, int contentHeight, Point previewPosition) {
		final Drawable previewKeyBackground = mPreviewPopupTheme.getPreviewKeyBackground();
		if (mPreviewPaddingHeight < 0) {
			mPreviewPaddingWidth = mPreviewLayout.getPaddingLeft() + mPreviewLayout.getPaddingRight();
			mPreviewPaddingHeight = mPreviewLayout.getPaddingTop() + mPreviewLayout.getPaddingBottom();

			if (previewKeyBackground != null) {
				Rect padding = new Rect();
				previewKeyBackground.getPadding(padding);
				mPreviewPaddingWidth += (padding.left + padding.right);
				mPreviewPaddingHeight += (padding.top + padding.bottom);
			}
		}
		contentWidth += mPreviewPaddingWidth;
		contentHeight += mPreviewPaddingHeight;

		// and checking that the width and height are big enough for the
		// background.
		if (previewKeyBackground != null) {
			contentWidth = Math.max(previewKeyBackground.getMinimumWidth(),  contentWidth);
			contentHeight = Math.max(previewKeyBackground.getMinimumHeight(),  contentHeight);
		}

		final int popupPreviewX = previewPosition.x - contentWidth/2;
		final int popupPreviewY = previewPosition.y - contentHeight;

		if (mPopupWindow.isShowing()) {
			mPopupWindow.update(popupPreviewX, popupPreviewY, contentWidth, contentHeight);
		} else {
			mPopupWindow.setWidth(contentWidth);
			mPopupWindow.setHeight(contentHeight);
			try {
				// https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/6
				// I don't understand why this should happen, and only with MIUI
				// ROMs.
				// anyhow, it easy to hide :)
				mPopupWindow.showAtLocation(mParentView, Gravity.NO_GRAVITY, popupPreviewX, popupPreviewY);
			} catch (RuntimeException e) {
				// nothing to do here. I think.
			}

		}
		mPreviewLayout.setVisibility(View.VISIBLE);

		// Set the preview background state
		if (previewKeyBackground != null) {
			previewKeyBackground.setState(key.popupResId != 0 ? LONG_PRESSABLE_STATE_SET : EMPTY_STATE_SET);
		}

		mPreviewLayout.requestLayout();
		mPreviewLayout.invalidate();
	}

	public void dismiss() {
		mPopupWindow.dismiss();
	}
}
