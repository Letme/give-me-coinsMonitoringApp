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
	private static final boolean DEBUG = true;


	private static final String URL_STRING = "https://give-me-coins.com";

	private static ArrayList<GetInfoWorkerCallback> oBtc_callbacks = null;
	private static ArrayList<GetInfoWorkerCallback> oLtc_callbacks = null;
	private static ArrayList<GetInfoWorkerCallback> oFtc_callbacks = null;
	private static ArrayList<GetInfoWorkerCallback> oVtc_callbacks = null;
	private static ArrayList<GetInfoWorkerCallback> oPpc_callbacks = null;
	
	private static GmcStickyService oInstance = null;
	private final Handler oHandler = null;
	
	private Notification oNotification;
	
    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
	/**
	 * Notification string 
	 */
    private static final int NOTIFICATION = R.string.notification;
    /**
     * Manager to show the notification for user
     */
    private NotificationManager mNM;
	private GetInfoWorker oGiveMeCoinsWorker = null;
	
	private GiveMeCoinsInfo gmcInfoFTC = null;
	private GiveMeCoinsInfo gmcInfoLTC = null;
	private GiveMeCoinsInfo gmcInfoBTC = null;
	private GiveMeCoinsInfo gmcInfoVTC = null;
	private GiveMeCoinsInfo gmcInfoPPC = null;
	
	private boolean showBTC = true;
	private boolean showFTC = true;
	private boolean showLTC = true;
	private boolean showVTC = true;
	private boolean showPPC = true;
	
	// so we can put all in (FTC, BTC, LTC) before we call the notification
	private final int alreadyUpdated = 0;
	
	private String btcHashRate = "0 kh/s";
	private String ltcHashRate = "0 kh/s";
	private String ftcHashRate = "0 kh/s";
	private String vtcHashRate = "0 kh/s";
	private String ppcHashRate = "0 kh/s";
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	
	void detachListener(GetInfoWorkerCallback para_btcCallback,
				GetInfoWorkerCallback para_ltcCallback, GetInfoWorkerCallback para_ftcCallback,
				GetInfoWorkerCallback para_vtcCallback,GetInfoWorkerCallback para_ppcCallback)
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
		if( para_vtcCallback != null )
		{
			oVtc_callbacks.remove(para_vtcCallback);
		}
		if( para_ppcCallback != null )
		{
			oPpc_callbacks.remove(para_ppcCallback);
		}
	}
	
	/**
	 * kills old thread and makes new one ... 
	 * can also be called if aki key changes ...
	 */
	void forceUpdate()
	{
		
        SharedPreferences sp = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		String key = sp.getString(getString(R.string.saved_api_key), null);
		if( key != null)
		{
			showBTC = sp.getBoolean(getString(R.string.show_btc), true);
			showLTC = sp.getBoolean(getString(R.string.show_ltc), true);
			showFTC = sp.getBoolean(getString(R.string.show_ftc), true);
			showVTC = sp.getBoolean(getString(R.string.show_vtc), true);
			showPPC = sp.getBoolean(getString(R.string.show_ppc), true);
			int sleepTime = sp.getInt(getString(R.string.update_interval), 60000);
			
			
			
			// kill old thread
			if( oGiveMeCoinsWorker != null )
			{
				oGiveMeCoinsWorker.setSleepTime(sleepTime);
				oGiveMeCoinsWorker.setUrlToGiveMeCoins( URL_STRING+key );
				oGiveMeCoinsWorker.setCoinsToShow(showBTC,showLTC, showFTC, showVTC, showPPC);
				
				oGiveMeCoinsWorker.forceUpdate();
				//oGiveMeCoinsWorker.setRunning(false);
				//oGiveMeCoinsWorker.cancel(true);
			}
			else
			{
				// make new one ... 
				oGiveMeCoinsWorker = new GetInfoWorker(btc_callback, ltc_callback, ftc_callback, vtc_callback, ppc_callback);
				oGiveMeCoinsWorker.setUrlToGiveMeCoins( URL_STRING+key );
				
				oGiveMeCoinsWorker.setCoinsToShow(showBTC,showLTC, showFTC, showVTC, showPPC);

				
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
    	if( oVtc_callbacks == null )
    		oVtc_callbacks = new ArrayList<GetInfoWorkerCallback>();
    	if( oPpc_callbacks == null )
    		oPpc_callbacks = new ArrayList<GetInfoWorkerCallback>();
    	
    	oInstance = this;
    	
        // Display a notification about us starting.  We put an icon in the status bar.
        // and start foreground
        SharedPreferences sp = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		String key = sp.getString(getString(R.string.saved_api_key),null);
		showBTC = sp.getBoolean(getString(R.string.show_btc), true);
		showLTC = sp.getBoolean(getString(R.string.show_ltc), true);
		showFTC = sp.getBoolean(getString(R.string.show_ftc), true);
		showVTC = sp.getBoolean(getString(R.string.show_vtc), true);
		showPPC = sp.getBoolean(getString(R.string.show_ppc), true);
        	
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		showStartNotification();
        
		int sleepTime = sp.getInt(getString(R.string.update_interval), 60000);
        // start getting info
		if( key != null )
		{
			if(DEBUG)Log.d(TAG,"new coin workers");
			
			oGiveMeCoinsWorker = new GetInfoWorker( btc_callback, ltc_callback, ftc_callback, vtc_callback, ppc_callback );
			oGiveMeCoinsWorker.setCoinsToShow(showBTC,showLTC, showFTC, showVTC, showPPC);
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
	static GmcStickyService getInstance(GetInfoWorkerCallback para_btc_callback,
			GetInfoWorkerCallback para_ltc_callback, GetInfoWorkerCallback para_ftc_callback,
			GetInfoWorkerCallback para_vtc_callback,GetInfoWorkerCallback para_ppc_callback)
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
		if( para_vtc_callback != null )
		{
			if( oVtc_callbacks == null )
				oVtc_callbacks = new ArrayList<GetInfoWorkerCallback>();
			oVtc_callbacks.add(para_vtc_callback);
		}
		if( para_ppc_callback != null )
		{
			if( oPpc_callbacks == null )
				oPpc_callbacks = new ArrayList<GetInfoWorkerCallback>();
			oPpc_callbacks.add(para_ppc_callback);
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
	    	if( showVTC )
	    		currentTextToShow += "VTC: "+vtcHashRate+" ";
	    	if( showPPC )
	    		currentTextToShow += "PPC: "+ppcHashRate+" ";
	    	
	        // The PendingIntent to launch our activity if the user selects this notification
	        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
	                new Intent(this, MainScreen.class), 0);
	       
	        
	        // change icon ...
	        oNotification = new Notification(R.drawable.ic_launcher, currentTextToShow,
	                System.currentTimeMillis());
	        
	        // Set the info for the views that show in the notification panel.
	        // yes deprecated ... but ...
	        oNotification.setLatestEventInfo(this, getText(R.string.app_name), currentTextToShow, contentIntent);
	        
	        
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
	    	if( showVTC )
	    		currentTextToShow += "VTC: "+vtcHashRate+" ";
	    	if( showPPC )
	    		currentTextToShow += "PPC: "+ppcHashRate+" ";
	    	
	        // Set the icon, scrolling text and timestamp
	    	oNotification = new Notification(R.drawable.ic_launcher, currentTextToShow,
	                System.currentTimeMillis());
	
	        // The PendingIntent to launch our activity if the user selects this notification
	        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
	                new Intent(this, MainScreen.class), 0);
	
	        // Set the info for the views that show in the notification panel.
	        oNotification.setLatestEventInfo(this, getText(R.string.app_name), currentTextToShow, contentIntent);
	        
	       // Start in foreground - so we dont get killed
	        startForeground(NOTIFICATION, oNotification);

	       // Send the notification.
	       
	        // mNM.notify(NOTIFICATION, notification);
    	}
    	else {
    		mNM.cancel(NOTIFICATION);
    	}
    }

	private final GetInfoWorkerCallback btc_callback = new GetInfoWorkerCallback() {
		
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
	
	private final GetInfoWorkerCallback ftc_callback = new GetInfoWorkerCallback() {
		
		

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
	
	private final GetInfoWorkerCallback ltc_callback = new GetInfoWorkerCallback() {
		
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
	
	private final GetInfoWorkerCallback vtc_callback = new GetInfoWorkerCallback() {
		
		@Override
		public void refreshValues(GiveMeCoinsInfo para_giveMeCoinsInfo) {
			gmcInfoVTC = para_giveMeCoinsInfo;
			
			// go through all 
			for(GetInfoWorkerCallback callback : oVtc_callbacks)
			{
				callback.refreshValues(para_giveMeCoinsInfo);
			}
			if( showVTC )
			{
				vtcHashRate = MainScreen.readableHashSize(gmcInfoVTC.getTotal_hashrate());
				refreshNotification();
			}
			
		}
	};
	
	private final GetInfoWorkerCallback ppc_callback = new GetInfoWorkerCallback() {
		
		@Override
		public void refreshValues(GiveMeCoinsInfo para_giveMeCoinsInfo) {
			gmcInfoPPC = para_giveMeCoinsInfo;
			
			// go through all 
			for(GetInfoWorkerCallback callback : oPpc_callbacks)
			{
				callback.refreshValues(para_giveMeCoinsInfo);
			}
			if( showPPC )
			{
				ppcHashRate = MainScreen.readableHashSize(gmcInfoPPC.getTotal_hashrate());
				refreshNotification();
			}
			
		}
	};


	GiveMeCoinsInfo getBTCInfo()
	{
		return gmcInfoBTC;		
	}
	
	GiveMeCoinsInfo getLTCInfo()
	{
		return gmcInfoLTC;		
	}
	
	GiveMeCoinsInfo getFTCInfo()
	{
		return gmcInfoFTC;		
	}
	
	GiveMeCoinsInfo getVTCInfo()
	{
		return gmcInfoVTC;		
	}
	GiveMeCoinsInfo getPPCInfo()
	{
		return gmcInfoPPC;		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		oGiveMeCoinsWorker.setRunning(false);
		oGiveMeCoinsWorker.forceUpdate();
		oInstance = null;
	}


	void stop() {
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