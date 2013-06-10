package com.phonebank.ripplewallet;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import net.sourceforge.zbar.android.CameraTest.CameraPreview;
import android.content.Context;
import android.content.res.TypedArray;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Shows what the camera sees
 * @author Tony Gaitatzis
 *
 */
public class View_CameraPreview extends LinearLayout {
	View_CameraPreview thisview;
	private Context context;
	private Camera mCamera;
	private CameraPreview mPreview;
	private Handler autoFocusHandler;

	private TextView scanText;
	private Button scanButton, cancelButton;
	private FrameLayout preview;

	private ImageScanner scanner;

	private ScanListener scanListener;

	private boolean barcodeScanned = false;
	private boolean previewing = true;

	static {
		System.loadLibrary("iconv");
	}

	public interface ScanListener {
		public void handleScan(String message);
	}

	public View_CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.context = context;

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.View_CameraPreview, 0, 0);
		a.recycle();

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_camerapreview, this, true);

		preview = (FrameLayout) findViewById(R.id.cameraPreview);
		scanText = (TextView) findViewById(R.id.scanText);
		scanButton = (Button) findViewById(R.id.ScanButton);
		cancelButton = (Button) findViewById(R.id.CancelButton);
		
		thisview = this;
		
		scanButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (barcodeScanned) {
					barcodeScanned = false;
					scanText.setText("Scanning...");
					mCamera.setPreviewCallback(previewCallback);
					mCamera.startPreview();
					previewing = true;
					mCamera.autoFocus(autoFocusCB);
				}
			}
		});

		cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				thisview.stopPreview();
			}
		});

	}

	public View_CameraPreview(Context context) {
		this(context, null);
	}

	public void startPreview(ScanListener listener) {
		setScanListener(listener);
		/* Instance barcode scanner */
		scanner = new ImageScanner();
		scanner.setConfig(0, Config.X_DENSITY, 3);
		scanner.setConfig(0, Config.Y_DENSITY, 3);

		autoFocusHandler = new Handler();
		mCamera = getCameraInstance();
		mCamera.startPreview(); 
		
		mPreview = new CameraPreview(context, mCamera, previewCallback,
				autoFocusCB);
		preview.addView(mPreview);

		expand();
	}

	public void setScanListener(ScanListener listener) {
		scanListener = listener;
	}
	public ImageScanner getScanner() {
		return scanner;
	}

	public void setBarcodeScanned() {
		barcodeScanned = true;
	}

	public void stopPreview() {
		collapse();
		releaseCamera();
		//mCamera.stopPreview();
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

	public void closeScanner() {
		releaseCamera();
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception e) {
		}
		return c;
	}

	private void releaseCamera() {
		if (mCamera != null) {
			previewing = false;
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

	private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if (previewing)
				mCamera.autoFocus(autoFocusCB);
		}
	};

	PreviewCallback previewCallback = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			Camera.Parameters parameters = camera.getParameters();
			Size size = parameters.getPreviewSize();

			Image barcode = new Image(size.width, size.height, "Y800");
			barcode.setData(data);
			

			int result = getScanner().scanImage(barcode);

			if (result != 0) {

				SymbolSet syms = getScanner().getResults();
				

				
				for (Symbol sym : syms) {
					Log.v("Wallet","SCANNED IMAGE!: "+sym.getData());
					scanListener.handleScan(sym.getData());
					setBarcodeScanned();
					
				}

				stopPreview();
			} 
		}
	};

	// Mimic continuous auto-focusing
	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};
}
