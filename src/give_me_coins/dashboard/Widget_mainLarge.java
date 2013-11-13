package give_me_coins.dashboard;


import java.util.ArrayList;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

public abstract class Widget_mainLarge extends AppWidgetProvider implements GetInfoWorkerCallback{

    public static final String TOAST_ACTION = "com.example.givemecoinswidget.TOAST_ACTION";
    public static final String EXTRA_ITEM = "com.example.givemecoinswidget.EXTRA_ITEM";
	
	private static final String TAG = "GiveMeCoinsWidget";
	private static final boolean DEBUG = true;
	private static final String URL_STRING = "https://give-me-coins.com";
	private GetInfoWorker oGiveMeCoinsWorker = null;
	private AppWidgetManager oAppWidgetManager = null;
	private Context oContext = null;
	
	// get key somewhere
	private String apiKey = null;


	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
		int[] appWidgetIds) 
	{
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		oAppWidgetManager = appWidgetManager;
		oContext = context;
		try
		{
			apiKey = getApiKey(context);
			
			if( oGiveMeCoinsWorker == null )
			{
				if( apiKey != null )
				{
					if(DEBUG)Log.d(TAG,"new coin worker");
					oGiveMeCoinsWorker = new GetInfoWorker( this );
					oGiveMeCoinsWorker.setUrlToGiveMeCoins(URL_STRING+apiKey);
					oGiveMeCoinsWorker.setRunning( true );
					oGiveMeCoinsWorker.execute();
				}
			}
			RemoteViews remoteViews = new RemoteViews( oContext.getPackageName(), R.layout.activity_widget_main );
			
	        ComponentName watchWidget = getComponentName(oContext);
	        
			remoteViews.removeAllViews(R.id.main_view);
			RemoteViews overview = (RemoteViews) new RemoteViews(oContext.getPackageName(), R.layout.overview_layout);
			
			int countOnlineWorkers = 0;
	       
	       // worker.setProgressBar(R.id.hash_rate_percentage, para_giveMeCoinsInfo.getTotal_hashrate(), currentWorker.getHashrate(), false);
			overview.setTextViewText(R.id.total_hash_rate, "...");
	        overview.setTextViewText(R.id.confirmed_rewards, "...");
	        overview.setTextViewText(R.id.workers_online, "..." );
	        remoteViews.addView(R.id.main_view, overview);
	        oAppWidgetManager.updateAppWidget(watchWidget, remoteViews);
		}
		catch(Exception e)
		{
			RemoteViews remoteViews = new RemoteViews( oContext.getPackageName(), R.layout.activity_widget_main );
			remoteViews.setTextViewText(R.id.total_hash_rate, "Please choose API Key in App");
			ComponentName watchWidget = getComponentName(oContext);
			oAppWidgetManager.updateAppWidget(watchWidget, remoteViews);
		}
	}
	
	protected abstract String getApiKey(Context context);
	protected abstract ComponentName getComponentName(Context context);
	
	@Override
	public void refreshValues(GiveMeCoinsInfo para_giveMeCoinsInfo) {
		
		RemoteViews remoteViews = new RemoteViews( oContext.getPackageName(), R.layout.activity_widget_main );
		
        ComponentName watchWidget = getComponentName(oContext);
        
		if( para_giveMeCoinsInfo != null)
		{
			if(DEBUG)Log.d(TAG, "refresh");
	      	//  LayoutInflater workerInflater = (LayoutInflater) oContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE);
			ArrayList<GiveMeCoinsWorkerInfo> giveMeCoinWorkers = para_giveMeCoinsInfo.getGiveMeCoinWorkers();
			//remoteViews.setTextViewText(R.id.total_hash_rate, String.valueOf(para_giveMeCoinsInfo.getTotal_hashrate() ));
			
			//remoteViews.setEmptyView(R.id.stack_view, R.id.empty_view);
			//remoteViews.s
			remoteViews.removeAllViews(R.id.main_view);
			RemoteViews overview = (RemoteViews) new RemoteViews(oContext.getPackageName(), R.layout.overview_layout);
			
			int countOnlineWorkers = 0;
			for(GiveMeCoinsWorkerInfo currentWorker: giveMeCoinWorkers)
			{
				if(currentWorker.isAlive())
				{
					++countOnlineWorkers;
				}
			}
			

			// worker.setProgressBar(R.id.hash_rate_percentage, para_giveMeCoinsInfo.getTotal_hashrate(), currentWorker.getHashrate(), false);
	        overview.setTextViewText(R.id.total_hash_rate, MainScreen.readableHashSize( para_giveMeCoinsInfo.getTotal_hashrate() ) );
	        overview.setTextViewText(R.id.confirmed_rewards, String.valueOf( para_giveMeCoinsInfo.getConfirmed_rewards() ));
	        overview.setTextViewText(R.id.workers_online, String.valueOf( countOnlineWorkers ) );
	        remoteViews.addView(R.id.main_view, overview);
	        oAppWidgetManager.updateAppWidget(watchWidget, remoteViews);
		}
		else
		{
			if(DEBUG)Log.d(TAG,"err ... givemecoinsInfo == null");
			
		}
		
	}
	
/*
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
*/

}
