package give_me_coins.dashboard;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Patrik on 07.11.13.
 */
public class JSONHelper {

    private static final String TAG = "JSONHelper";
	private static final int iConnectionTimeout = 10000;
    
    public static JSONObject getJSONFromUrl(URL para_url)
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
            Log.d(TAG, "Timeout");
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

	public static String getVal(JSONObject para_jsonObject, String para_name, String para_defaultValue)
    {
           String retString = para_defaultValue;
           try
           {
               retString = para_jsonObject.getString(para_name);

           } catch (JSONException e) {
               Log.d(TAG, "String json error "+e.toString());
           }
            return retString;
    }

    public static int getVal(JSONObject para_jsonObject, String para_name, int para_defaultValue)
    {
        int retInteger = para_defaultValue;
        try
        {
            retInteger = para_jsonObject.getInt(para_name);

        } catch (JSONException e) {
            Log.d(TAG, "int json error "+e.toString());
        }
        return retInteger;
    }

    public static double getVal(JSONObject para_jsonObject, String para_name, double para_defaultValue)
    {
        double retDouble = para_defaultValue;
        try
        {
            retDouble =  para_jsonObject.getDouble(para_name);

        } catch (JSONException e) {
            Log.d(TAG, "double json error "+e.toString());
        }
        return retDouble;
    }

    public static long getVal(JSONObject para_jsonObject, String para_name, long para_defaultValue)
    {
        long retLong = para_defaultValue;
        try
        {
            retLong =  para_jsonObject.getLong(para_name);

        } catch (JSONException e) {
            Log.d(TAG, "long json error "+e.toString());
        }
        return retLong;
    }

    public static JSONObject getVal(JSONObject para_jsonObject, String para_name, JSONObject para_defaultValue) {

        JSONObject retObj = para_defaultValue;
        try
        {
            retObj =  para_jsonObject.getJSONObject(para_name);

        } catch (JSONException e) {
            Log.d(TAG, "object json error "+e.toString());
        }
        return retObj;

    }
}
