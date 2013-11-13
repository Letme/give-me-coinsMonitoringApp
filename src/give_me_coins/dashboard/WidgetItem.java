/**
 * 	Copyrights reserved to authors of this code (available from GitHub
 * 	repository https://github.com/Letme/give-me-coinsMonitoringApp
 * 
 *  This file is part of Give-me-coins.com Dashboard Android App
 * 
 *	Give-me-coins.com Dashboard is free software: you can redistribute it 
 *	and/or modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation, either version 3 of the 
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


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