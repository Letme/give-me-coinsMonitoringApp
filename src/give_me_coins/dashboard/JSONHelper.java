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
class JSONHelper {

    private static final String TAG = "JSONHelper";
	private static final int iConnectionTimeout = 10000;
    
    private static JSONObject getJSONFromUrl(URL para_url)
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

	static String getVal(JSONObject para_jsonObject, String para_name, String para_defaultValue)
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

    static int getVal(JSONObject para_jsonObject, String para_name, int para_defaultValue)
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

    static double getVal(JSONObject para_jsonObject, String para_name, double para_defaultValue)
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

    static long getVal(JSONObject para_jsonObject, String para_name, long para_defaultValue)
    {
        long retLong = para_defaultValue;
        try
        {
            if (para_jsonObject.has(para_name) && !para_jsonObject.isNull(para_name)) {
                retLong =  para_jsonObject.getLong(para_name);
            }
        } catch (JSONException e) {
            Log.d(TAG, "long json error " + e.toString());
        }
        return retLong;
    }

    static JSONObject getVal(JSONObject para_jsonObject, String para_name, JSONObject para_defaultValue) {

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
