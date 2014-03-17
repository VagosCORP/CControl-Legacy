package com.vagoscorp.ccontrol;

import libraries.vagoscorp.comunication.android.Comunic;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements OnTouchListener,SensorEventListener {

	SurfaceView acel;
	SurfaceView incli;
	ToggleButton turbo;
	Button freno;
	TextView textView1;
	TextView textView2;
	TextView textView3;
	Canvas acelCanvas;
	Canvas incliCanvas;
	SurfaceHolder acelHolder;
	SurfaceHolder incliHolder;
	Thread acelThread;
	Thread incliThread;
	Comunic comunic;
	
	boolean runn = false;
	float x = 0;
	float y = 0;
	float dx = 0;
	float dy = 0;
	float dead = 0;
	float width;
	float height;
	
	float ax = 0;
	float ay = 0;
	float az = 0;
	
	boolean frenando = false;
	
	Paint touch = new Paint();
	Paint surfPaint = new Paint();
	Paint msurfPaint = new Paint();
	Paint hsurfPaint = new Paint();
	Paint acelPaint = new Paint();
	Bitmap border;
	
	SensorManager sensorManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		acel = (SurfaceView)findViewById(R.id.acel);
		incli = (SurfaceView)findViewById(R.id.incli);
		turbo = (ToggleButton)findViewById(R.id.turbo);
		freno = (Button)findViewById(R.id.freno);
		textView1 = (TextView)findViewById(R.id.textView1);
		textView2 = (TextView)findViewById(R.id.textView2);
		textView3 = (TextView)findViewById(R.id.textView3);
		
		touch.setColor(Color.BLUE);
		touch.setAntiAlias(true);
		touch.setStrokeWidth(2);
		touch.setStrokeCap(Paint.Cap.ROUND);
		touch.setStyle(Paint.Style.STROKE);
		acelPaint.setColor(Color.BLACK);
		surfPaint.setColor(Color.YELLOW);
		msurfPaint.setARGB(255, 255, 111, 0);
		hsurfPaint.setColor(Color.RED);
		
		border = BitmapFactory.decodeResource(getResources(), R.drawable.border);
		
		acelHolder = acel.getHolder();
		incliHolder = incli.getHolder();
		
		acel.setOnTouchListener(this);
		freno.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				frenando = true;
				x = width/2;
				y = dead;
				v.setBackgroundColor(Color.rgb(89, 126, 163));
//				v.setBackgroundResource(android.R.drawable.button_onoff_indicator_off);
				if(event.getAction() == MotionEvent.ACTION_UP) {
					v.setBackgroundResource(android.R.drawable.btn_default/*btn_default_holo_light*/);
					frenando = false;
				}
				return true;
			}
		});
		sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(
        		Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        comunic = new Comunic(this, "10.0.0.20", 2000);
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
		if(y < dead) {
			canvas.drawRect(0, y, width, dead, surfPaint);
			if(y < 300*dy) {
				canvas.drawRect(0, y, width, dead, msurfPaint);
			}
			if(y < 100*dy) {
				canvas.drawRect(0, y, width, dead, hsurfPaint);
			}
		}else {
			canvas.drawRect(0, dead, width, y, msurfPaint);
		}
//		canvas.drawRect(0, y-25, width, y+25, acelPaint);
		canvas.drawBitmap(border, null, new RectF(0, y-(float)25*dy, width, y+(float)25*dy), null);
//		canvas.drawCircle(x, y, 50, touch);
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            ax=event.values[0];
            ay=event.values[1];
            az=event.values[2];
            textView1.setText("X = "+ax);
            textView2.setText("X = "+ay);
            textView3.setText("X = "+az);
        }
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
	
	public void turbo(View view) {
		
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(!frenando) {
			x = event.getX();
			y = event.getY();
		}
		if(event.getAction() == MotionEvent.ACTION_UP) {
			x = width/2;
			y = dead;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		
		super.onDestroy();
	}
	
	public class runAcel extends Thread {

		@Override
		public void run() {
			boolean first = true;
			while (runn) {
				if (!acelHolder.getSurface().isValid())
					continue;
				acelCanvas = acelHolder.lockCanvas();
				if(first) {
					width = acelCanvas.getWidth();
					height = acelCanvas.getHeight();
					dx = (float)width/1000;
					dy = (float)height/1000;
					x = width/2;
					dead = (float)666*dy;
					y = dead;
					first =  false;
				}
				acelCanvas.drawColor(Color.DKGRAY);
//				acelCanvas.drawARGB(255, 89, 126, 163); 
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
				float w = incliCanvas.getWidth();
				float h = incliCanvas.getHeight();
				float ws = (float)w/20;
//				incliCanvas.drawColor(Color.GREEN);
				incliCanvas.drawARGB(255, 89, 126, 163);
				if(ay < 0) {
					incliCanvas.drawRect((float)w/2 + (float)ws*ay, 0, (float)w/2, h, surfPaint);
					if(ay < -2) {
						incliCanvas.drawRect((float)w/2 + (float)ws*ay, 0, (float)w/2-(float)ws*2, h, msurfPaint);
					}
				}
				else if(ay > 0) {
					incliCanvas.drawRect((float)w/2, 0, (float)w/2 + (float)ws*ay, h, surfPaint);
					if(ay > 2) {
						incliCanvas.drawRect((float)w/2+(float)ws*2, 0, (float)w/2 + (float)ws*ay, h, msurfPaint);
					}
				}
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
