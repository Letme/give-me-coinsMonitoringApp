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

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Patrik on 06.11.13.
 */
public class GiveMeCoinsWorkerInfo {


  //  {"analpet.1":{"alive":"1","hashrate":"128","last_share_timestamp":"1383696561","username":"analpet.1"}
    private String username = "N/A";
    private boolean alive = false;
    private int hashrate = 0;
    private long last_share_timestamp = 0;

    String getUsername() {
        return username;
    }

    private void setUsername(String username) {
        this.username = username;
    }

    boolean isAlive() {
        return alive;
    }

    private void setAlive(boolean alive) {
        this.alive = alive;
    }

    int getHashrate() {
        return hashrate;
    }

    private void setHashrate(int hashrate) {
        this.hashrate = hashrate;
    }

    long getLast_share_timestamp() {
        return last_share_timestamp;
    }

    private void setLast_share_timestamp(long last_share_timestamp) {
        this.last_share_timestamp = last_share_timestamp;
    }

    public GiveMeCoinsWorkerInfo( JSONObject para_workerInfos) {

    	if( para_workerInfos != null )
    	{
	        username = JSONHelper.getVal(para_workerInfos,"username", "N/A");
	        alive = ( JSONHelper.getVal(para_workerInfos, "alive", 0) == 1);
	        hashrate = JSONHelper.getVal(para_workerInfos,"hashrate",0);
	        if( alive )
	        {
	        	last_share_timestamp = JSONHelper.getVal(para_workerInfos,"last_share_timestamp",(long)0);
	        }
	        else
	        {
	        	last_share_timestamp = 0;
	        }
       }

    }

}
