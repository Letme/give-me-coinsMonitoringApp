package give_me_coins.dashboard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;


public class WidgetItem {

    private static final boolean DEBUG = true;
	private static final String TAG = "WidgetItem";
	public String hashRate;
    public Bitmap hashRateImage;

    public WidgetItem(String hashrate, float d) {
    	//Bitmap bmp = getBitmapPercentageCircle(1);
    	//LayoutView worker = (LayoutView) findViewById(R.layout.worker_layout);
       // worker.setTextViewText(R.id.hash_rate_val, String.valueOf( para_giveMeCoinsInfo.getTotal_hashrate() ) );
       // worker.setProgressBar(R.id.hash_rate_percentage, para_giveMeCoinsInfo.getTotal_hashrate(), currentWorker.getHashrate(), false);
        //worker.setImageViewBitmap(R.id.hash_rate_percentage, bmp);
        //worker.setTextViewText(R.id.worker_name, "Total");
    	if(DEBUG)Log.d(TAG,"getViewAt");
    	this.hashRate = hashrate;
    	hashRateImage = getBitmapPercentageCircle(d);
    }
    
    private Bitmap getBitmapPercentageCircle(float percentage)
	{
		Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
		Bitmap bmp = Bitmap.createBitmap(500, 500, conf);
		
		Paint mPaint = new Paint();
		mPaint.setDither(true);
		mPaint.setColor(Color.GRAY);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(3);
		
		Path circle = new Path();
		RectF box = new RectF(10,10,bmp.getWidth()-10,bmp.getHeight()-10);
		float sweep = 360 * percentage;
		circle.addArc(box, 270, sweep);
		
		Path circleFull = new Path();
		circleFull.addArc(box, 0, 360);
		Canvas canvas = new Canvas(bmp);
		canvas.drawPath(circleFull, mPaint);
		
		mPaint.setColor(Color.BLUE);
		mPaint.setStrokeWidth(10);
		
		canvas.drawPath(circle, mPaint);
		return bmp;
	}
	
}