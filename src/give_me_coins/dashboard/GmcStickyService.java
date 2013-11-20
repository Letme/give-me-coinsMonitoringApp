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
	
	private static GmcStickyService oInstace = null;
	
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
	
	private boolean showBTC = false;
	private boolean showFTC = true;
	private boolean showLTC = true;
	
	private String btcHashRate = "0 kh/s";
	private String ltcHashRate = "0 kh/s";
	private String ftcHashRate = "0 kh/s";
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
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
    	
    	oInstace = this;
    	
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        oContext = this;
        // Display a notification about us starting.  We put an icon in the status bar.
        // and start foreground
        showNotification();
        SharedPreferences sp = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		String key = sp.getString(getString(R.string.saved_api_key),null);
        
        // start getting info
		if( key != null )
		{
			
			if(DEBUG)Log.d(TAG,"new coin workers");
			// here switch if we want btc or not ...

			oGiveMeCoinsWorker = new GetInfoWorker( btc_callback, ltc_callback, ftc_callback );
			oGiveMeCoinsWorker.setUrlToGiveMeCoins( URL_STRING+key );
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
		return oInstace;	

	}
	
	/**
     * Show a notification while this service is running.
     */
    private void showNotification() {

    	String currentTextToShow = "";
    	if( showBTC )
    		currentTextToShow += "BTC: "+btcHashRate+" ";
    	if( showFTC )
    		currentTextToShow += "FTC: "+ftcHashRate+" ";
    	if( showLTC )
    		currentTextToShow += "LTC: "+ltcHashRate+" ";
    	
        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.ic_launcher, currentTextToShow,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainScreen.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, oContext.getText(R.string.app_name), currentTextToShow, contentIntent);
        
       // Start in foreground - so we dont get killed
        startForeground(NOTIFICATION, notification);
       // Send the notification.
       
        // mNM.notify(NOTIFICATION, notification);
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
				showNotification();
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
				showNotification();
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
				showNotification();
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

}
