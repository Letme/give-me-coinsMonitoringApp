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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;


public class GmcStickyService extends Service{

	private static final String TAG = "HandyTrackerService";
	private boolean DEBUG = true;


	private static final String URL_STRING = "https://give-me-coins.com";

	private Context oContext;
	private static ArrayList<GetInfoWorkerCallback> oBtc_callbacks = null;
	private static ArrayList<GetInfoWorkerCallback> oLtc_callbacks = null;
	private static ArrayList<GetInfoWorkerCallback> oFtc_callbacks = null;
	
	private static GmcStickyService oInstance = null;
	private Handler oHandler;
	
	private Notification oNotification;
	
    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
	/**
	 * Notification string 
	 */
    private int NOTIFICATION = R.string.notification;
    /**
     * Manager to show the notification for user
     */
    private NotificationManager mNM;
	private GetInfoWorker oGiveMeCoinsWorker = null;
	
	private GiveMeCoinsInfo gmcInfoFTC = null;
	private GiveMeCoinsInfo gmcInfoLTC = null;
	private GiveMeCoinsInfo gmcInfoBTC = null;
	
	private boolean showBTC = true;
	private boolean showFTC = true;
	private boolean showLTC = true;
	
	// so we can put all in (FTC, BTC, LTC) before we call the notification
	private int alreadyUpdated = 0;
	
	private String btcHashRate = "0 kh/s";
	private String ltcHashRate = "0 kh/s";
	private String ftcHashRate = "0 kh/s";
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void detachListener(GetInfoWorkerCallback para_btcCallback, 
				GetInfoWorkerCallback para_ltcCallback, GetInfoWorkerCallback para_ftcCallback)
	{
		if( para_btcCallback != null )
		{
			oBtc_callbacks.remove(para_btcCallback);
		}
		if( para_ltcCallback != null )
		{
			oLtc_callbacks.remove(para_ltcCallback);
		}
		if( para_ftcCallback != null )
		{
			oFtc_callbacks.remove(para_ftcCallback);
		}
	}
	
	/**
	 * kills old thread and makes new one ... 
	 * can also be called if aki key changes ...
	 */
	public void forceUpdate()
	{
		
        SharedPreferences sp = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		String key = sp.getString(getString(R.string.saved_api_key), null);
		if( key != null)
		{
			showBTC = sp.getBoolean(getString(R.string.show_btc), true);
			showLTC = sp.getBoolean(getString(R.string.show_ltc), true);
			showFTC = sp.getBoolean(getString(R.string.show_ftc), true);
			int sleepTime = sp.getInt(getString(R.string.update_interval), 60000);
			
			oGiveMeCoinsWorker.setSleepTime(sleepTime);
			oGiveMeCoinsWorker.setUrlToGiveMeCoins( URL_STRING+key );
			oGiveMeCoinsWorker.setCoinsToShow(showBTC,showLTC, showFTC);
			// kill old thread
			if( oGiveMeCoinsWorker != null )
			{
				
				oGiveMeCoinsWorker.forceUpdate();
				//oGiveMeCoinsWorker.setRunning(false);
				//oGiveMeCoinsWorker.cancel(true);
			}
			else
			{
				// make new one ... 
				oGiveMeCoinsWorker = new GetInfoWorker(btc_callback, ltc_callback, ftc_callback);
				oGiveMeCoinsWorker.setUrlToGiveMeCoins( URL_STRING+key );
				
				oGiveMeCoinsWorker.setCoinsToShow(showBTC,showLTC, showFTC);

				
				oGiveMeCoinsWorker.setSleepTime(sleepTime);
				oGiveMeCoinsWorker.setRunning( true );
				oGiveMeCoinsWorker.execute();
			}
	
			if(DEBUG)Log.d(TAG, "making new service ...");
		
		}

		
	}
	
	@Override
    public void onCreate() {
    	super.onCreate();
    	
    	if( oBtc_callbacks == null )
    		oBtc_callbacks = new ArrayList<GetInfoWorkerCallback>();
    	if( oLtc_callbacks == null )
    		oLtc_callbacks = new ArrayList<GetInfoWorkerCallback>();
    	if( oFtc_callbacks == null )
    		oFtc_callbacks = new ArrayList<GetInfoWorkerCallback>();
    	
    	oInstance = this;
    	
        oContext = this;
        // Display a notification about us starting.  We put an icon in the status bar.
        // and start foreground
        SharedPreferences sp = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		String key = sp.getString(getString(R.string.saved_api_key),null);
		showBTC = sp.getBoolean(getString(R.string.show_btc), true);
		showLTC = sp.getBoolean(getString(R.string.show_ltc), true);
		showFTC = sp.getBoolean(getString(R.string.show_ftc), true);
        	
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		showStartNotification();
        
		int sleepTime = sp.getInt(getString(R.string.update_interval), 60000);
        // start getting info
		if( key != null )
		{
			if(DEBUG)Log.d(TAG,"new coin workers");
			
			oGiveMeCoinsWorker = new GetInfoWorker( btc_callback, ltc_callback, ftc_callback );
			oGiveMeCoinsWorker.setCoinsToShow(showBTC,showLTC, showFTC);
			oGiveMeCoinsWorker.setUrlToGiveMeCoins( URL_STRING+key );			
			oGiveMeCoinsWorker.setSleepTime(sleepTime);
			oGiveMeCoinsWorker.setRunning( true );
			oGiveMeCoinsWorker.execute();
		}
        
	}
	
	/**
	 * callbacks for workers
	 * @param para_btc_callback -> get info about BTC stuff
	 * @param para_ltc_callback ->  get info about LTC stuff
	 * @param para_ftc_callback -> get info about FTC stuff
	 * @return
	 */
	public static GmcStickyService getInstance(GetInfoWorkerCallback para_btc_callback, GetInfoWorkerCallback para_ltc_callback, GetInfoWorkerCallback para_ftc_callback)
	{
		
		if( para_btc_callback != null )
		{
			if( oBtc_callbacks == null )
				oBtc_callbacks = new ArrayList<GetInfoWorkerCallback>();
		
			oBtc_callbacks.add(para_btc_callback);
		}
		if( para_ltc_callback != null )
		{
			if( oLtc_callbacks == null )
				oLtc_callbacks = new ArrayList<GetInfoWorkerCallback>();
			oLtc_callbacks.add(para_ltc_callback);
		}
		if( para_ftc_callback != null )
		{
			if( oFtc_callbacks == null )
				oFtc_callbacks = new ArrayList<GetInfoWorkerCallback>();
			oFtc_callbacks.add(para_ftc_callback);
		}
		return oInstance;	

	}
	
	/**
     * Refresh notification
     */
    private void refreshNotification() {
    	SharedPreferences sp = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    	if(sp.getBoolean(getString(R.string.show_notification), true)) {
	    	String currentTextToShow = "";
	    	if( showBTC )
	    		currentTextToShow += "BTC: "+btcHashRate+" ";
	    	if( showFTC )
	    		currentTextToShow += "FTC: "+ftcHashRate+" ";
	    	if( showLTC )
	    		currentTextToShow += "LTC: "+ltcHashRate+" ";
	    	
	        // The PendingIntent to launch our activity if the user selects this notification
	        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
	                new Intent(this, MainScreen.class), 0);
	       
	        
	        // change icon ...
	        oNotification = new Notification(R.drawable.ic_launcher, currentTextToShow,
	                System.currentTimeMillis());
	        
	        // Set the info for the views that show in the notification panel.
	        // yes deprecated ... but ...
	        oNotification.setLatestEventInfo(this, oContext.getText(R.string.app_name), currentTextToShow, contentIntent);
	        
	        
	      // TODO: test here if arams need to be set (kh/s dropping ... stuff like that
	        
	       
	       // Start in foreground - so we dont get killed
	       
	       // Send the notification.
	        mNM.notify(NOTIFICATION, oNotification);
    	}
    	else
    		mNM.cancel(NOTIFICATION);
    }
	
	/**
     * Show a notification while this service is running.
     */
    private void showStartNotification() {
    	SharedPreferences sp = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    	if(sp.getBoolean(getString(R.string.show_notification), true)) {
	    	String currentTextToShow = "";
	    	if( showBTC )
	    		currentTextToShow += "BTC: "+btcHashRate+" ";
	    	if( showFTC )
	    		currentTextToShow += "FTC: "+ftcHashRate+" ";
	    	if( showLTC )
	    		currentTextToShow += "LTC: "+ltcHashRate+" ";
	    	
	        // Set the icon, scrolling text and timestamp
	    	oNotification = new Notification(R.drawable.ic_launcher, currentTextToShow,
	                System.currentTimeMillis());
	
	        // The PendingIntent to launch our activity if the user selects this notification
	        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
	                new Intent(this, MainScreen.class), 0);
	
	        // Set the info for the views that show in the notification panel.
	        oNotification.setLatestEventInfo(this, oContext.getText(R.string.app_name), currentTextToShow, contentIntent);
	        
	       // Start in foreground - so we dont get killed
	        startForeground(NOTIFICATION, oNotification);

	       // Send the notification.
	       
	        // mNM.notify(NOTIFICATION, notification);
    	}
    	else {
    		mNM.cancel(NOTIFICATION);
    	}
    }

	private GetInfoWorkerCallback btc_callback = new GetInfoWorkerCallback() {
		
		@Override
		public void refreshValues(GiveMeCoinsInfo para_giveMeCoinsInfo) {
			gmcInfoBTC = para_giveMeCoinsInfo;
			// go through all 
			for(GetInfoWorkerCallback callback : oBtc_callbacks)
			{
				try{
					callback.refreshValues(para_giveMeCoinsInfo);
				}
				catch(Exception e)
				{
					// maybe delete callback ...
					Log.e(TAG, "wrong callback "+e.toString());
				}
			}
			if( showBTC )
			{
				btcHashRate = MainScreen.readableHashSize(gmcInfoBTC.getTotal_hashrate());
				refreshNotification();
			}
			
		}
	};
	
	private GetInfoWorkerCallback ftc_callback = new GetInfoWorkerCallback() {
		
		

		@Override
		public void refreshValues(GiveMeCoinsInfo para_giveMeCoinsInfo) {
			gmcInfoFTC = para_giveMeCoinsInfo;
			// go through all 
			for(GetInfoWorkerCallback callback : oFtc_callbacks)
			{
				callback.refreshValues(para_giveMeCoinsInfo);
			}
			if( showFTC )
			{
				ftcHashRate = MainScreen.readableHashSize(gmcInfoFTC.getTotal_hashrate());
				refreshNotification();
			}
		}
	};
	
	private GetInfoWorkerCallback ltc_callback = new GetInfoWorkerCallback() {
		
		@Override
		public void refreshValues(GiveMeCoinsInfo para_giveMeCoinsInfo) {
			gmcInfoLTC = para_giveMeCoinsInfo;
			
			// go through all 
			for(GetInfoWorkerCallback callback : oLtc_callbacks)
			{
				callback.refreshValues(para_giveMeCoinsInfo);
			}
			if( showLTC )
			{
				ltcHashRate = MainScreen.readableHashSize(gmcInfoLTC.getTotal_hashrate());
				refreshNotification();
			}
			
		}
	};


	public GiveMeCoinsInfo getBTCInfo()
	{
		return gmcInfoBTC;		
	}
	
	public GiveMeCoinsInfo getLTCInfo()
	{
		return gmcInfoLTC;		
	}
	
	public GiveMeCoinsInfo getFTCInfo()
	{
		return gmcInfoFTC;		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		oGiveMeCoinsWorker.setRunning(false);
		oGiveMeCoinsWorker.forceUpdate();
		oInstance = null;
	}


	public void stop() {
		oGiveMeCoinsWorker.setRunning(false);
		oGiveMeCoinsWorker.forceUpdate();
		//oInstance.stopForeground(true);
		stopSelf();
		oInstance = null;
	}
}


/*
@Override
public void refreshValues(GiveMeCoinsInfo para_giveMeCoinsInfo) {
	// TODO Auto-generated method stub
	this.gmcInfo = para_giveMeCoinsInfo;
	// Set the icon, scrolling text and timestamp
    Notification notification = new Notification(R.drawable.ic_launcher, getText(R.string.app_name),
            System.currentTimeMillis());

    // The PendingIntent to launch our activity if the user selects this notification
    PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
            new Intent(this, MainScreen.class), 0);

    // Set the info for the views that show in the notification panel.
    notification.setLatestEventInfo(oContext, getText(R.string.app_name), MainScreen.readableHashSize(gmcInfo.getTotal_hashrate()), contentIntent);
	mNM.notify(R.string.notification, notification);
}
*/

/*
private void showHashrateNotification(String para_hashRate)
{
	 Notification notification = new Notification(R.drawable.ic_launcher, getText(R.string.app_name),
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainScreen.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(oContext, getText(R.string.app_name), para_hashRate, contentIntent);
		mNM.notify(R.string.notification, notification);	
	
}
*/