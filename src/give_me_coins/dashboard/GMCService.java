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
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;

public class GMCService extends Service{
	
	private static final boolean DEBUG=false;
	
	private final Handler mHandler;
	private static final String TAG = "GMCService";
	private ReceiveDataThread mReceiveData;
	static String url_fixed;
	private Timer timer;
	
	public GMCService(Handler handler) {
		mHandler=handler;
		
	}
	
	private synchronized void start(String... urls) {
		if(urls.length==0) {
			Log.e(TAG,"Bad URL handed to service");
			return;
		}
		if(DEBUG) Log.d(TAG,"Service started");
		url_fixed = urls[0];
		if(mReceiveData==null) {
			timer = new Timer();
			TimerTask ReLoadThread = new TimerTask() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					//if(mReceiveData!=null) mReceiveData.cancel();
					Message msg = mHandler.obtainMessage(MainScreen.DATA_PROGRESS);
					MainScreen.Progress=10;
					mHandler.sendMessage(msg);
					mReceiveData= new ReceiveDataThread(url_fixed);
					mReceiveData.start();
				}			
			}; 
			timer.schedule(ReLoadThread,1,10000);
		}
	}
	
	private synchronized void stop() {
		timer.cancel();
		if(DEBUG) Log.d(TAG,"Timer cancelled");
		if(mReceiveData!=null) {
			mReceiveData.cancel();
			Log.d(TAG,"ReceiveThread cancelled");
			mReceiveData=null;
		}
	}
	
	private class ReceiveDataThread extends Thread {
		private URL url=null;
		private InputStream inputStream=null;
		private BufferedReader reader = null;
		private JsonReader jsonAll=null;
		private String url_string;
		private static final String TAG = "ReceiveDataThread";
		
		public ReceiveDataThread (String urls){
			if(DEBUG) Log.d(TAG,"public: " + urls);
			if(urls!=null) url_string=urls;
			else {
				cancel();
			}
		}
		
        @Override
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
						Message send = mHandler.obtainMessage(MainScreen.DATA_PROGRESS);
						MainScreen.Progress=20;
						mHandler.sendMessage(send);
							try {
								inputStream = url.openStream();
							} catch (Exception e1) {
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
				    	}
						send = mHandler.obtainMessage(MainScreen.DATA_PROGRESS);
						MainScreen.Progress=30;
						mHandler.sendMessage(send);
			try {
				if(jsonAll==null) jsonAll = new JsonReader(reader);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				cancel();
			}
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
									if(DEBUG)Log.d(TAG,"Main NAME");
									String name=jsonAll.nextName();
									if ("username".equals(name)) {
										MainScreen.username=jsonAll.nextString();
									} else if("confirmed_rewards".equals(name)) {
										MainScreen.confirmed_rewards=jsonAll.nextString();
									} else if("round_estimate".equals(name)) {
										MainScreen.round_estimate=jsonAll.nextString();
									} else if ("total_hashrate".equals(name)) {
										MainScreen.total_hashrate=jsonAll.nextString();
									} else if ("round_shares".equals(name)) {
										MainScreen.round_shares=jsonAll.nextString();
									} else if ("workers".equals(name)) {
										//JsonReader workerobj = new JsonReader(reader);
										ParseWorkers(jsonAll,0);
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
							send = mHandler.obtainMessage(MainScreen.DATA_PROGRESS);
							MainScreen.Progress+=5;
							mHandler.sendMessage(send);

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
					cancel();
				} catch (NullPointerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.w(TAG,"JSON MAIN hasNext failed!");
					cancel();
				}
		   			if(DEBUG) Log.d(TAG,"username: " + MainScreen.username + " |round_estimate: " + MainScreen.round_estimate +
								" |total_hashrate: " + MainScreen.total_hashrate + " |round_shares: " + MainScreen.round_shares);
					send = mHandler.obtainMessage(MainScreen.DATA_PROGRESS);
					MainScreen.Progress=90;
					mHandler.sendMessage(send);
					//Pack the data for other activity to use/get
					if(DEBUG) for(int j=0; MainScreen.worker_alive[j]!=null;++j) {
						Log.d(TAG,"worker: " + MainScreen.worker_name[j] + " |hashrate: " + MainScreen.worker_hashrate[j] + " |alive: " + String.valueOf(MainScreen.worker_alive[j]));
					}
					Message msg = mHandler.obtainMessage(MainScreen.DATA_READY);
					mHandler.sendMessage(msg);
					cancel();
		}

		private void cancel() {
	   		//Perform CLEANUP !!!!
			try {
				if(jsonAll!=null) jsonAll.close();
				if(DEBUG) Log.d(TAG,"MAIN JSON closed");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    try {
				if(reader!=null) reader.close();
				if(DEBUG) Log.d(TAG,"BufferedReader closed");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   		try {
	   			if(inputStream!=null) inputStream.close();
				if(DEBUG) Log.d(TAG,"InputStream closed");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   		Message send = mHandler.obtainMessage(MainScreen.DATA_PROGRESS);
			MainScreen.Progress=100;
			mHandler.sendMessage(send);
		}
    }
    private void ParseWorkers(JsonReader jsonAll, int i) {

    	if(DEBUG) Log.d(TAG,"Next worker");
		//going through workers
		try {
			while(jsonAll.hasNext()) {
				if(DEBUG)Log.d(TAG,"Inside worker");
				//we need to determine what comes next and act accordingly
		    	try {
					switch(jsonAll.peek()) {
						case BEGIN_OBJECT:
							jsonAll.beginObject();
							if(DEBUG) Log.d(TAG,"JSON beginObject");
							break;
						case END_OBJECT:
							jsonAll.endObject();
							if(DEBUG) Log.d(TAG,"JSON endObject");
							++i;
							//ParseWorkers(jsonAll);
							break;
						case NAME:
							String workers=jsonAll.nextName();
							if(DEBUG) Log.d(TAG,"JSON Name: " + workers + " next value: " + jsonAll.peek() +" i="+i);
							if ("alive".equals(workers)) {
									MainScreen.worker_alive[i]=jsonAll.nextString();					
							} else if("hashrate".equals(workers)) {
									MainScreen.worker_hashrate[i]=jsonAll.nextString();
							} else if ("username".equals(workers)) {
									MainScreen.worker_name[i]=jsonAll.nextString();
									jsonAll.endObject();
									if(DEBUG) Log.d(TAG,"JSON endObject");
									++i;
							} else if ("last_share_timestamp".equals(workers)) {
									if(DEBUG) Log.d(TAG,"last_share_timestamp: ");
									switch(jsonAll.peek()) {
										case STRING:
											MainScreen.worker_timestamp[i]=jsonAll.nextString();
											break;
										case NULL:
											jsonAll.nextNull();
											MainScreen.worker_timestamp[i]="0";
											break;
									}
							} else {
								//jsonAll.skipValue();
								//Log.d(TAG,"JSON Name not found: " + workers + "next value: "+ jsonAll.peek());
								ParseWorkers(jsonAll,i);
							}
							break;
						case NULL:
							if(DEBUG) Log.d(TAG,"JSON Null");
							jsonAll.skipValue();
							break;
						default:
							jsonAll.skipValue();
							//Log.d(TAG,"peek value not valid as it is " +jsonAll.peek());
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.w(TAG,"JSON Worker IOException: " + e);
				}
				Message send = mHandler.obtainMessage(MainScreen.DATA_PROGRESS);
				MainScreen.Progress+=5;
				mHandler.sendMessage(send);
			}
			//perform cleanup through this	
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.w(TAG,"JSON Worker hasNext failed!");
		}
    	
    }
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
