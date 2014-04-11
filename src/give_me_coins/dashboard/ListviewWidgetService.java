package give_me_coins.dashboard;

/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.util.ArrayList;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class ListviewWidgetService extends RemoteViewsService {
    
	public static int realCount = 10;
	
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
    	StackRemoteViewsFactory.mCount = realCount;
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
	private static final boolean DEBUG = true;
	private static final String TAG = "LIST_WIDGET_SERVICE";
	static int mCount = 10;
    private final Context oContext;
    private final int mCurrency;

    
    StackRemoteViewsFactory(Context context, Intent intent) {
        oContext = context;
        int mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        mCurrency = intent.getIntExtra(Widget_mainLarge.CURRENCY,0);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {
      
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.
    	if(DEBUG)Log.d(TAG,"getViewAt "+position);

        // We construct a remote views item based on our widget item xml file, and set the
        // text based on the position.
    	RemoteViews rv = new RemoteViews(oContext.getPackageName(), R.layout.worker_table_layout);
    	if( Widget_mainLarge.oGmcService != null)
    	{
	    	GiveMeCoinsInfo gmcInfo = Widget_mainLarge.oGmcService.getBTCInfo();
	    	if( mCurrency == 1)
	    	{
	    		gmcInfo = Widget_mainLarge.oGmcService.getLTCInfo();
	    	}
	    	else if( mCurrency == 2 )
	    	{
	    		gmcInfo = Widget_mainLarge.oGmcService.getFTCInfo();
	    	}
	    	else if( mCurrency == 3 )
	    	{
	    		gmcInfo = Widget_mainLarge.oGmcService.getVTCInfo();
	    	}
	    	else if( mCurrency == 4 )
	    	{
	    		gmcInfo = Widget_mainLarge.oGmcService.getPPCInfo();
	    	}
	    	if(DEBUG)Log.d(TAG,"gmcInfo ");
	    	
	    	if( gmcInfo != null )
	    	{
		    	ArrayList<GiveMeCoinsWorkerInfo> gmcWorkers = gmcInfo.getGiveMeCoinWorkers();
		    	mCount = gmcWorkers.size();
		    	if( position < gmcWorkers.size() )
		    	{
			    	GiveMeCoinsWorkerInfo currentWorker = gmcWorkers.get(position);
			    	
		        	//float percentage = (float)currentWorker.getHashrate()/(float)gmcInfo.getTotal_hashrate();
		        	rv.setTextViewText(R.id.worker_name, currentWorker.getUsername());
		        	if( currentWorker.isAlive() )
		        	{
		        		rv.setTextColor(R.id.status, oContext.getResources().getColor( R.color.light_green ));
				        rv.setTextViewText(R.id.status, "Online" );
		        	}
		        	else
		        	{
			        	rv.setTextColor(R.id.status, oContext.getResources().getColor( R.color.light_red ));
				        rv.setTextViewText(R.id.status, "Offline" );
		        	}
		        	
		        	rv.setTextViewText(R.id.hash_rate_val, MainScreen.readableHashSize( currentWorker.getHashrate() ));
			        // rv.setImageViewBitmap(R.id.hash_rate_percentage, getBitmapPercentageCircle( percentage ) );
			        // Next, we set a fill-intent which will be used to fill-in the pending intent template
			        /* which is set on the collection view in StackWidgetProvider.
			        Bundle extras = new Bundle();
			        extras.putInt(Widget_mainLarge.EXTRA_ITEM, position);
			        Intent fillInIntent = new Intent();
			        fillInIntent.putExtras(extras);
			        rv.setOnClickFillInIntent(R.id.hash_rate_percentage, fillInIntent);
			        */
		    	}
	    	}
    	}
        // Return the remote views object.
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        // You can create a custom loading view (for instance when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
    	if(DEBUG)Log.d(TAG,"getLoadingView");
        return null;
    }

    @Override
    public int getViewTypeCount() {
    	//if(DEBUG)Log.d(TAG,"getViewTypeCount");
        return 1;
    }

    @Override
    public long getItemId(int position) {
    	//if(DEBUG)Log.d(TAG,"getItemId");
        return position;
    }

    @Override
    public boolean hasStableIds() {
    	//if(DEBUG)Log.d(TAG,"hasStableIds");
        return true;
    }

    @Override
    public void onDataSetChanged() {
        // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
        // on the collection view corresponding to this factory. You can do heaving lifting in
        // here, synchronously. For example, if you need to process an image, fetch something
        // from the network, etc., it is ok to do it here, synchronously. The widget will remain
        // in its current state while work is being done here, so you don't need to worry about
        // locking up the widget.
    	if(DEBUG)Log.d(TAG,"onDataSetChanged");
    }
   
}