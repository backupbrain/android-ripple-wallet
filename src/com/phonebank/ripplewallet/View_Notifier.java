package com.phonebank.ripplewallet;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Self-defined Notifier.
 * @author Tony Gaitatzis
 *
 */
public class View_Notifier extends LinearLayout implements OnClickListener {
	private Context context;
	private TextView message;
	private Button closeButton;

	public View_Notifier(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.View_Notifier, 0, 0);
		a.recycle();

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_notifier, this, true);

		message = (TextView)findViewById(R.id.message);
		closeButton = (Button) findViewById(R.id.closeButton);

		closeButton.setOnClickListener(this);
		
	}

	public View_Notifier(Context context) {
		this(context, null);
	}

	public void setMessage(String messageText) {
		message.setText(messageText);
	}
	// http://stackoverflow.com/questions/4946295/android-expand-collapse-animation
	public void expand() {
		final View v = (View) this;

		// only expand if it is not expanded
		if (v.getVisibility() != View.VISIBLE) {
			v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			final int targtetHeight = v.getMeasuredHeight();

			v.getLayoutParams().height = 0;
			v.setVisibility(View.VISIBLE);
			Animation a = new Animation() {
				@Override
				protected void applyTransformation(float interpolatedTime,
						Transformation t) {
					v.getLayoutParams().height = interpolatedTime == 1 ? LayoutParams.WRAP_CONTENT
							: (int) (targtetHeight * interpolatedTime);
					v.requestLayout();
				}

				@Override
				public boolean willChangeBounds() {
					return true;
				}
			};

			// 1dp/ms
			a.setDuration((int) (targtetHeight / v.getContext().getResources()
					.getDisplayMetrics().density) * 2);
			v.startAnimation(a);
		}
	}

	public void collapse() {
		final View v = (View) this;
		// only collapse if it is already visible
		if (v.getVisibility() != View.GONE) {
			final int initialHeight = v.getMeasuredHeight();

			Animation a = new Animation() {
				@Override
				protected void applyTransformation(float interpolatedTime,
						Transformation t) {
					if (interpolatedTime == 1) {
						v.setVisibility(View.GONE);
					} else {
						v.getLayoutParams().height = initialHeight
								- (int) (initialHeight * interpolatedTime);
						v.requestLayout();
					}
				}

				@Override
				public boolean willChangeBounds() {
					return true;
				}
			};

			// 1dp/ms
			a.setDuration((int) (initialHeight / v.getContext().getResources()
					.getDisplayMetrics().density) * 2);
			v.startAnimation(a);
		}
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.closeButton) {
			this.collapse();
		}
	}
}
