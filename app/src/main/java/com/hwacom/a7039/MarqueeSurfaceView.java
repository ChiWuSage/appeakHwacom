package com.hwacom.a7039;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

@SuppressLint("WrongCall")
public class MarqueeSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
	
	private final String TAG = "com.hwacom.hdsp";
	
	private Context context;
	private String marqueeText = "", marqueeTextColor = "", marqueeBgColor = "", marqueeDirection = "";
	private int marqueeSpeed = 0;
	private int width = 0;
	private int xMarquee = 0;
	private int marqueeWidth = 0;
	
	private RenderThread thread;
	
	public MarqueeSurfaceView(Context context, String marqueeText, String marqueeTextColor, String marqueeBgColor, int marqueeSpeed, String marqueeDirection) {
		super(context);
		this.context = context;
		this.marqueeText = marqueeText;
		this.marqueeTextColor = marqueeTextColor;
		this.marqueeBgColor = marqueeBgColor;
		this.marqueeSpeed = marqueeSpeed;
		this.marqueeDirection = marqueeDirection;
		init();
	}
	
	public MarqueeSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MarqueeSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		setZOrderOnTop(true);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		
		// Set up animation loop
		getHolder().addCallback(this);
	}
	
	public void updateState() {
		
		if (marqueeDirection.equals("left")) {
			if (xMarquee >= (0-(marqueeWidth))) {
				xMarquee = xMarquee - marqueeSpeed;
			} else {
				xMarquee = width;
			}
		} else if (marqueeDirection.equals("right")) {
			if (xMarquee <= width) {
				xMarquee = xMarquee + marqueeSpeed;
			} else {
				xMarquee = -marqueeWidth;
			}
		}
		
	}

	@Override
	public void onDraw(Canvas canvas) {
		canvas.drawColor(android.R.color.transparent, Mode.CLEAR);
		
        Paint paint = new Paint();
        
        paint.setColor(android.graphics.Color.parseColor(marqueeBgColor));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        paint.setAntiAlias(true);
        paint.setColor(android.graphics.Color.parseColor(marqueeTextColor));
        paint.setTextSize((int)(getHeight()*0.7));
        paint.setTextAlign(Paint.Align.LEFT);
        
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
        
        canvas.drawText(marqueeText, xMarquee, yPos, paint);
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		thread = new RenderThread(this);
		thread.start();
		
		width = this.getWidth();
		
		Paint paint = new Paint();
		paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);
        paint.setTextSize((int)(getHeight()*0.7));
        paint.setTextAlign(Paint.Align.LEFT);
        marqueeWidth = getTextWidth(paint, marqueeText);
		
        if (marqueeDirection.equals("left")) {
			xMarquee = width;
		} else if (marqueeDirection.equals("right")) {
			xMarquee = -marqueeWidth;
		}
        
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		thread.stopRendering();
	}
	
	private class RenderThread extends Thread {
		private boolean running = true;
		private MarqueeSurfaceView surfaceView;
		private SurfaceHolder surfaceHolder;

		public RenderThread(MarqueeSurfaceView surfaceView) {
			this.surfaceView = surfaceView;
			surfaceHolder = surfaceView.getHolder();
		}

		@Override
		public void run() {
			Canvas canvas = null;
			while(running && !interrupted()) {
				canvas = surfaceHolder.lockCanvas();
				if(canvas == null) {
					// Check for surface being destroyed while
					// we're in the loop
					continue;
				}

				try {
					synchronized (surfaceHolder) {
						surfaceView.updateState();
						surfaceView.onDraw(canvas);
					}
				} finally {
					surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}	
		}

		public void stopRendering() {
			interrupt();
			running = false;
		}
	}
	
	public int getTextWidth(Paint paint, String str) {
        int iRet = 0;  
        if (str != null && str.length() > 0) {  
            int len = str.length();  
            float[] widths = new float[len];  
            paint.getTextWidths(str, widths);  
            for (int j = 0; j < len; j++) {  
                iRet += (int) Math.ceil(widths[j]);
            }  
        }  
        return iRet;  
    }
	
}
