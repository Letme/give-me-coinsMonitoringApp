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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;

public class GMCPoolService extends Service{
	
	private static final boolean DEBUG=false;
	
	private final Handler mHandler;
	private static final String TAG = "GMCPoolService";
	private PoolReceiveDataThread mReceiveData;
	public static String url_fixed;
	Timer timer;
	
	public GMCPoolService(Context context, Handler handler) {
		mHandler=handler;
		
	}
	public synchronized void start(String... urls) {
		if(urls.length==0) {
			Log.e(TAG,"Bad URL handed to service");
			MainScreen.mPoolService=null;
			return;
		}
		if(DEBUG) Log.d(TAG,"PoolService started");
		url_fixed=urls[0];
		if(mReceiveData==null) {
			timer = new Timer();
			TimerTask ReLoadThread = new TimerTask() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					mReceiveData= new PoolReceiveDataThread(url_fixed);
					mReceiveData.start();
				}			
			}; 
			timer.schedule(ReLoadThread,1,10000);
		}
	}
	
	public synchronized void stop() {
		timer.cancel();
		if(DEBUG) Log.d(TAG,"Timer cancelled");
		if(mReceiveData!=null) {
			mReceiveData.cancel();
			if(DEBUG) Log.d(TAG,"PoolReceiveThread cancelled");
			mReceiveData=null;
		}
	}
	
	private class PoolReceiveDataThread extends Thread {
		URL url=null;
		InputStream inputStream=null;
		BufferedReader reader = null;
		JsonReader jsonAll=null;
		String url_string;
		private static final String TAG = "PoolReceiveDataThread";
		
		public PoolReceiveDataThread (String urls){
			if(DEBUG) Log.d(TAG,"public: " + urls);
			url_string=urls;
		}
		
		public void run() {
			// TODO Auto-generated method stub
						
						if(DEBUG) Log.d(TAG,"Connecting to website: " + url_string);
						try {
							url = new URL(url_string);
						} catch (MalformedURLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							Log.e(TAG,"MalformedURLException");
						}
							try {
								inputStream = url.openStream();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
								Log.e(TAG,"InputStream IOException");
								cancel();
							}
						if(DEBUG) Log.d(TAG,"Connection should be open by now");
				    	try {
				    	    // json is UTF-8 by default
				    	    reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
				    	    if(DEBUG) Log.d(TAG,"Connected and reading JSON");
				    	} catch (Exception e) { 
				    	    // Oops
				    		Log.e(TAG,"Connecting failed!");
				    		cancel();
				    	}
			if(jsonAll==null) jsonAll = new JsonReader(reader);
			//now lets parse the output form give-me-coins
			if(DEBUG) Log.d(TAG,"Parsing json");

		   		try {
					while (jsonAll.hasNext()) {
						try {
							switch(jsonAll.peek()) {
								case BEGIN_OBJECT:
									jsonAll.beginObject();
									if(DEBUG) Log.d(TAG,"JSON beginObject");
									break;
								case END_OBJECT:
									jsonAll.endObject();
									if(DEBUG) Log.d(TAG,"JSON endObject");
									break;
								case NAME:
									if(DEBUG) Log.d(TAG,"Main NAME");
									String name=jsonAll.nextName();
									if (name.equals("hashrate")) {
										MainScreen.pool_total_hashrate=jsonAll.nextString();
									} else if(name.equals("workers")) {
										MainScreen.pool_workers=jsonAll.nextString();
									} else if(name.equals("shares_this_round")) {
										MainScreen.pool_round_shares=jsonAll.nextString();
									} else if (name.equals("last_block")) {
										MainScreen.pool_last_block=jsonAll.nextString();
									} else if (name.equals("difficulty")) {
										MainScreen.pool_difficulty=jsonAll.nextString();
									} else {
										jsonAll.skipValue();
									}
									break;
								case NULL:
									jsonAll.skipValue();
								default:
									jsonAll.skipValue();
									if(DEBUG) Log.d(TAG,"peek value main not valid as it is " +jsonAll.peek());
							}

						} catch (IllegalStateException e) {	
							Log.w(TAG,"IllegalStateException: " + e);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Log.w(TAG,"JSON MAIN IOException: " + e);
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.w(TAG,"JSON MAIN hasNext failed!");
				}	
		   			if(DEBUG) Log.d(TAG,"Total hashrate: " + MainScreen.pool_total_hashrate + " |Workers: " + MainScreen.pool_workers +
								" |Last round shares: " + MainScreen.pool_round_shares + " |last block: " + MainScreen.pool_last_block +
								" |Difficulty" + MainScreen.pool_difficulty);
					//Pack the data for other activity to use/get
					Message msg = mHandler.obtainMessage(MainScreen.POOL_DATA_READY);
					mHandler.sendMessage(msg);
					cancel();
		}

		public void cancel() {
	   		//Perform CLEANUP !!!!
			try {
				if(jsonAll != null) jsonAll.close();
				if(DEBUG) Log.d(TAG,"MAIN JSON closed");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    try {
		    	if(reader != null)reader.close();
				if(DEBUG) Log.d(TAG,"BufferedReader closed");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   		try {
	   			if(inputStream != null) inputStream.close();
				if(DEBUG) Log.d(TAG,"InputStream closed");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
