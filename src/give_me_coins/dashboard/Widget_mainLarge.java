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


import java.util.ArrayList;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

public abstract class Widget_mainLarge extends AppWidgetProvider implements GetInfoWorkerCallback{

    private static final String TOAST_ACTION = "com.example.givemecoinswidget.TOAST_ACTION";
    private static final String EXTRA_ITEM = "com.example.givemecoinswidget.EXTRA_ITEM";
    public static final String CURRENCY = "Currency";
	
	private static final String TAG = "GiveMeCoinsWidget";
	private static final boolean DEBUG = true;

	private AppWidgetManager oAppWidgetManager = null;
	private int[] oWidgetIds = null;
	private Context oContext = null;

	private int iCurrency;
	
	public static GmcStickyService oGmcService;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
		int[] appWidgetIds) 
	{
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		oAppWidgetManager = appWidgetManager;
		oContext = context;
		oWidgetIds = appWidgetIds;
		iCurrency = getCurrency();
		for(int app_id :appWidgetIds)
		{
			try
			{	
				oGmcService = openServiceInstance(this);
				if( oGmcService == null )
				{
					context.startService( new Intent(context, GmcStickyService.class) );
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
		        // needs to be dependend which currency ...
		        GiveMeCoinsInfo currentCoinInfo = getCurrentInfo( oGmcService );
		        if( currentCoinInfo != null )
		        {
		        	
		        	ListviewWidgetService.realCount = currentCoinInfo.getGiveMeCoinWorkers().size();
					for(GiveMeCoinsWorkerInfo worker : currentCoinInfo.getGiveMeCoinWorkers() )
					{
						if( worker.isAlive() )
						{
							countOnlineWorkers++;
						}
					}
					overview.setTextViewText(R.id.total_hash_rate, MainScreen.readableHashSize( currentCoinInfo.getTotal_hashrate() ) );
			        overview.setTextViewText(R.id.confirmed_rewards, String.valueOf( currentCoinInfo.getConfirmed_rewards() ) );
			        overview.setTextViewText(R.id.workers_online, countOnlineWorkers +"/"+currentCoinInfo.getGiveMeCoinWorkers().size() );
			
					Intent intent = new Intent(oContext, ListviewWidgetService.class);
					
		            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, app_id);
		            intent.putExtra(CURRENCY, iCurrency);
		            
		            // When intents are compared, the extras are ignored, so we need to embed the extras
		            // into the data so that the extras will not be ignored.
		            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
		            
					//RemoteViews rv = new RemoteViews(oContext.getPackageName(), R.layout.activity_widget_main);
					remoteViews.setRemoteAdapter( R.id.list_view, intent);
			        
		        }
				
				remoteViews.addView(R.id.main_view, overview);
		        oAppWidgetManager.updateAppWidget(app_id, remoteViews);
			}
			catch(Exception e)
			{
				RemoteViews remoteViews = new RemoteViews( oContext.getPackageName(), R.layout.activity_widget_main );
				remoteViews.setTextViewText(R.id.total_hash_rate, "Please choose API Key in App");
				//ComponentName watchWidget = getComponentName(oContext);
				oAppWidgetManager.updateAppWidget(app_id, remoteViews);
				Log.e(TAG, "died on update " + e.toString());
				
			}
		}
		
	}
	
	
	
	
	protected abstract int getCurrency();

	protected abstract ComponentName getComponentName(Context context);
	protected abstract GmcStickyService openServiceInstance( GetInfoWorkerCallback callback );
	protected abstract GiveMeCoinsInfo getCurrentInfo( GmcStickyService service );
	
	@Override
	public void refreshValues(GiveMeCoinsInfo para_giveMeCoinsInfo) {
		if( oGmcService == null )
			oGmcService = openServiceInstance(this);
		
		for(int app_id : oWidgetIds)
		{
			RemoteViews remoteViews = new RemoteViews( oContext.getPackageName(), R.layout.activity_widget_main );
			
	        ComponentName watchWidget = getComponentName(oContext);
	        
			if( para_giveMeCoinsInfo != null)
			{
				if(DEBUG)Log.d(TAG, "refresh");
				
		      	//  LayoutInflater workerInflater = (LayoutInflater) oContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE);
				ArrayList<GiveMeCoinsWorkerInfo> giveMeCoinWorkers = para_giveMeCoinsInfo.getGiveMeCoinWorkers();
				//remoteViews.setTextViewText(R.id.total_hash_rate, String.valueOf(para_giveMeCoinsInfo.getTotal_hashrate() ));
				ListviewWidgetService.realCount = giveMeCoinWorkers.size();
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
	
		        overview.setTextViewText(R.id.total_hash_rate, MainScreen.readableHashSize( para_giveMeCoinsInfo.getTotal_hashrate() ) );
		        overview.setTextViewText(R.id.confirmed_rewards, String.valueOf( para_giveMeCoinsInfo.getConfirmed_rewards() ));
		        overview.setTextViewText(R.id.workers_online, String.valueOf( countOnlineWorkers )+"/"+giveMeCoinWorkers.size() );
		        remoteViews.addView(R.id.main_view, overview);
		        
		       		        
				Intent intent = new Intent(oContext, ListviewWidgetService.class);
				
	            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, app_id);
	            intent.putExtra(CURRENCY, iCurrency);
	            
	            // When intents are compared, the extras are ignored, so we need to embed the extras
	            // into the data so that the extras will not be ignored.
	            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
	            
				//RemoteViews rv = new RemoteViews(oContext.getPackageName(), R.layout.activity_widget_main);
				remoteViews.setRemoteAdapter( R.id.list_view, intent);
 						      
		        
		        oAppWidgetManager.updateAppWidget(app_id, remoteViews);
		        // now listview updates ... woho
		        oAppWidgetManager.notifyAppWidgetViewDataChanged(app_id, R.id.list_view);

			}
			else
			{
				if(DEBUG)Log.d(TAG,"err ... givemecoinsInfo == null");
				
			}
		}
	}


	
/*

	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) oContext.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (GmcStickyService.class.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
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
*/

}
