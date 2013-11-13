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

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Patrik on 06.11.13.
 */
public class GetInfoWorker extends AsyncTask<Void,JSONObject, Void >
{

    private static final String TAG = "GetInfoWorker";
	private boolean isRunning = true;
    private int iConnectionTimeout = 5000;
    private GetInfoWorkerCallback getInfoWorkerCallback;
    private int sleepTime = 60000; // 1 min

    public String getUrlToGiveMeCoins() {
        return urlToGiveMeCoins;
    }

    public void setUrlToGiveMeCoins(String urlToGiveMeCoins) {
        this.urlToGiveMeCoins = urlToGiveMeCoins;
    }

    String urlToGiveMeCoins = null;

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public int getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    public GetInfoWorker(GetInfoWorkerCallback para_getInfoWorkerCallback) {
        this.getInfoWorkerCallback = para_getInfoWorkerCallback;
    }

    @Override
    protected Void doInBackground(Void... params) {

        while( isRunning )
        {
            if(urlToGiveMeCoins != null )
            {
                try {
                    URL currentUrl = new URL(urlToGiveMeCoins);
                    // bring it to UI
                    JSONObject currentJson = getJSONFromUrl(currentUrl);
                    if( currentJson != null )
                    {
                      //  Log.d(TAG,currentJson.toString() );
                        publishProgress( currentJson );
                    }
                    else
                    {
                        Log.e(TAG, "failed to get a valid json");
                    }

                } catch (MalformedURLException e) {
                    Log.e(TAG,e.toString());
                }
                
                try {
                    Thread.sleep(sleepTime);
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
            getInfoWorkerCallback.refreshValues(currentCoinsInfo);
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
}
