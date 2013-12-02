package give_me_coins.dashboard;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Patrik on 06.11.13.
 */
public class GetInfoWorker extends AsyncTask<Void,JSONObject, Void >
{

    private static final String TAG = "GetInfoWorker";
	private boolean isRunning = true;
    private int iConnectionTimeout = 5000;
    private ArrayList<GetInfoWorkerCallback> getInfoWorkerCallbacks;
    private boolean[] showCoin = {true, true, true};
    private String[] currencySwitcher = {"btc","ltc","ftc"};
    private int sleepTime = 60000; // 1 min - default value
    private boolean isSleeping = false;


    private String getUrlToGiveMeCoins() {
        return urlToGiveMeCoins;
    }

    void setUrlToGiveMeCoins(String urlToGiveMeCoins) {
        this.urlToGiveMeCoins = urlToGiveMeCoins;
    }

    String urlToGiveMeCoins = null;
	private Thread oCurrentWorkerThread;


    private boolean isRunning() {
        return isRunning;
    }

    /**
     * 
     * @param isRunning = false stops running
     */
    void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    /**
     * 
     * @return time the process sleeps between refreshes
     */
    private int getSleepTime() {
        return sleepTime;
    }

    
    /**
     * sets time the process sleeps between refreshes
     * @param sleepTime
     */
    void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    
    public GetInfoWorker(GetInfoWorkerCallback para_getInfoWorkerCallbackBTC, GetInfoWorkerCallback para_getInfoWorkerCallbackLTC, GetInfoWorkerCallback para_getInfoWorkerCallbackFTC) {
        
    	getInfoWorkerCallbacks = new ArrayList<GetInfoWorkerCallback>();
    	
		getInfoWorkerCallbacks.add( para_getInfoWorkerCallbackBTC );
		getInfoWorkerCallbacks.add( para_getInfoWorkerCallbackLTC );
        getInfoWorkerCallbacks.add( para_getInfoWorkerCallbackFTC );
        
    }

    @Override
    protected Void doInBackground(Void... params) {
    	oCurrentWorkerThread = Thread.currentThread();
        while( isRunning )
        {
            if(urlToGiveMeCoins != null )
            {
	            for(int i = 0; i<3;i++)
	            {
	            	if( showCoin[i] && getInfoWorkerCallbacks.get(i) != null )
	            	{
	            		try {
		                	String currentUrlString = urlToGiveMeCoins;
	
	                		currentUrlString = currentUrlString.replace("ltc?api_key", currencySwitcher[i]+"?api_key");
		                	if( getInfoWorkerCallbacks.get(i) != null )
		                	{
			                    URL currentUrl = new URL(currentUrlString);
			                    // bring it to UI
			                    JSONObject currentJson = getJSONFromUrl(currentUrl);
			                    if( currentJson != null )
			                    {
			                      //  Log.d(TAG,currentJson.toString() );
			                    	try {
										currentJson.put("currency", i);
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										Log.e(TAG,"couldnt add currency");
									}
			                        publishProgress( currentJson );
			                    }
			                    else
			                    {
			                        Log.e(TAG, "failed to get a valid json");
			                    }
		                	}
		                } 
	            		catch (MalformedURLException e) 
	            		{
		                    Log.e(TAG,e.toString());
		                }
	            	} 
	            }
                try {
                	setSleeping(true);
                    Thread.sleep(sleepTime);
                    setSleeping(false);
                } catch (InterruptedException e) {
                   Log.e(TAG, "error sleeping"+e.toString());
                }
            }

        } // protected Void doInBackground(String... params)


        return null;
    }

    @Override
    protected void onProgressUpdate(JSONObject... values) {
        super.onProgressUpdate(values);
        
        GiveMeCoinsInfo currentCoinsInfo = new GiveMeCoinsInfo(values[0]);
       // Log.e(TAG,currentCoinsInfo.getUsername() );
        try
        {
        	int currentCurrencyIndex = JSONHelper.getVal(values[0],"currency",0);
            getInfoWorkerCallbacks.get(currentCurrencyIndex).refreshValues(currentCoinsInfo);
        }
        catch(Exception e)
        {
            Log.e(TAG,"troll"+e.toString());
        }
    }

    private JSONObject getJSONFromUrl(URL para_url)
    {
        //	ProgressDialog oShowProgress = ProgressDialog.show(oAct, "Loading", "Loading", true, false);
        JSONObject oRetJson = null;

        try
        {

            //Log.d(TAG,para_url.toString());
            BufferedInputStream oInput = null;

            HttpsURLConnection oConnection = (HttpsURLConnection) para_url.openConnection();
            //	HttpsURLConnection.setDefaultHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            oConnection.setConnectTimeout(iConnectionTimeout);
            oConnection.setReadTimeout(iConnectionTimeout*2);
            //		connection.setRequestProperty ("Authorization", sAuthorization);
            oConnection.connect();
            oInput = new BufferedInputStream( oConnection.getInputStream() );
            BufferedReader reader = new BufferedReader( new InputStreamReader(oInput) );
            String sReturn = reader.readLine();
            //Log.d(TAG,sReturn);


            oRetJson = new JSONObject(sReturn);

        }
        catch (SocketTimeoutException e)
        {
            Log.e(TAG, "Timeout");
        }
        catch (IOException e)
        {
            Log.e(TAG,e.toString());

        } catch (JSONException e)
        {
            Log.e(TAG,e.toString());
        }

        catch (Exception e)
        {
            Log.e(TAG,e.toString());
        }

        //para_ProgressDialog.dismiss();
        return oRetJson;

    }

    /**
     * 
     * @return tells if process is currently sleeping 
     */
	private boolean isSleeping() {
		return isSleeping;
	}

	private void setSleeping(boolean isSleeping) {
		this.isSleeping = isSleeping;
	}

	void forceUpdate() {
		oCurrentWorkerThread.interrupt();
		
	}

	void setCoinsToShow(boolean para_showBTC, boolean para_showLTC, boolean para_showFTC)
	{
		showCoin[0] = para_showBTC;
		showCoin[1] = para_showLTC;
		showCoin[2] = para_showFTC;
	}
}
