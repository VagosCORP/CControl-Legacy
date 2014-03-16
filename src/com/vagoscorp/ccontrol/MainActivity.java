package com.vagoscorp.ccontrol;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements OnTouchListener {

	SurfaceView acel;
	SurfaceView incli;
	ToggleButton turbo;
	Canvas acelCanvas;
	Canvas incliCanvas;
	SurfaceHolder acelHolder;
	SurfaceHolder incliHolder;
	Thread acelThread;
	Thread incliThread;
	
	boolean runn = false;
	float x = 0;
	float y = 0;
	
	float width;
	float height;
	
	Paint touch = new Paint();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		acel = (SurfaceView)findViewById(R.id.acel);
		incli = (SurfaceView)findViewById(R.id.incli);
		turbo = (ToggleButton)findViewById(R.id.turbo);
		
		touch.setColor(Color.DKGRAY);
		touch.setAntiAlias(true);
		touch.setStrokeWidth(2);
		touch.setStrokeCap(Paint.Cap.ROUND);
		touch.setStyle(Paint.Style.STROKE);
		
		acelHolder = acel.getHolder();
		incliHolder = incli.getHolder();
		
		acel.setOnTouchListener(this);
	}

	@Override
	protected void onPause() {
		runn = false;
		try {
			acelThread.join();
			incliThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		acelThread = null;
		incliThread = null;
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		runn = true;
		acelThread = new runAcel();
		incliThread = new runIncli();
		acelThread.start();
		incliThread.start();
		super.onResume();
	}
	
	public void drawtouch(Canvas canvas) {
		if(x <= 0)
			x = 0;
		if(y <= 0)
			y = 0;
		if(x >= width)
			x = width;
		if(y >= height)
			y = height;
		canvas.drawCircle(x, y, 50, touch);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		x = event.getX();
		y = event.getY();
		return true;
	}

	@Override
	protected void onDestroy() {
		
		super.onDestroy();
	}
	
	public class runAcel extends Thread {

		@Override
		public void run() {
			while (runn) {
				if (!acelHolder.getSurface().isValid())
					continue;
				acelCanvas = acelHolder.lockCanvas();
				width = acelCanvas.getWidth();
				height = acelCanvas.getHeight();
				acelCanvas.drawColor(Color.BLUE);
				drawtouch(acelCanvas);
				acelHolder.unlockCanvasAndPost(acelCanvas);
				try {
					sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			super.run();
		}
		
	}
	
	public class runIncli extends Thread {

		@Override
		public void run() {
			while (runn) {
				if (!incliHolder.getSurface().isValid())
					continue;
				incliCanvas = incliHolder.lockCanvas();
//				float width = incliCanvas.getWidth();
//				float height = incliCanvas.getHeight();
				incliCanvas.drawColor(Color.GREEN);
				incliHolder.unlockCanvasAndPost(incliCanvas);
				try {
					sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			super.run();
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
