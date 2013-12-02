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


import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Created by Patrik on 06.11.13.
 */
public class GiveMeCoinsInfo {

   private static final String TAG = "GiveMeCoinsInfo";
   private static final boolean DEBUG = false;
	// https://give-me-coins.com/pool/api-ftc?api_key=82ca1174fe9fffb7c93e8270caa02226f720c93fa3247a82868a348019320bbd
/*
{
"username":"analpet",
"confirmed_rewards":2.68386106,
"round_estimate":"0.02926971",
"total_hashrate":"128",
"payout_history":"0",
"round_shares":"202",
"workers":{"analpet.1":{"alive":"1","hashrate":"128","last_share_timestamp":"1383696561","username":"analpet.1"},
"analpet.2":{"alive":"0","hashrate":"0","username":"analpet.2"}}}
 */
    private int total_hashrate = 0;
    private double confirmed_rewards = 0;
    private String username = "N/A";
    private double round_estimate = 0;
    private double payout_history = 0;
    private long round_shares = 0;
    private final ArrayList<GiveMeCoinsWorkerInfo> giveMeCoinWorkers;
	private final Comparator<? super GiveMeCoinsWorkerInfo> workerComparator = new Comparator<GiveMeCoinsWorkerInfo>()
    {

		@Override
		public int compare(GiveMeCoinsWorkerInfo lhs, GiveMeCoinsWorkerInfo rhs) {

			if( lhs.getHashrate() > rhs.getHashrate() )
			{
				return -1;
			}
			else if(lhs.getHashrate() == rhs.getHashrate())
			{
				return lhs.getUsername().compareTo( lhs.getUsername() ) ;
			}
			else
			{
				return 1;
			}
			
		}	
    };
    
	
    public GiveMeCoinsInfo(JSONObject para_jsonReturn) {

        total_hashrate = JSONHelper.getVal(para_jsonReturn, "total_hashrate", 0);
        confirmed_rewards = JSONHelper.getVal(para_jsonReturn, "confirmed_rewards", 0.0);
        username = JSONHelper.getVal(para_jsonReturn, "username", "N/A");
        round_estimate = JSONHelper.getVal(para_jsonReturn, "round_estimate", 0.0);
        payout_history = JSONHelper.getVal(para_jsonReturn, "payout_history", 0.0);
        round_shares = JSONHelper.getVal(para_jsonReturn, "round_shares", (long) 1);
        giveMeCoinWorkers = new ArrayList<GiveMeCoinsWorkerInfo>();
        		
        JSONObject workers = JSONHelper.getVal(para_jsonReturn, "workers", new JSONObject());
        Iterator<?> keys = workers.keys();
        
        for( ;keys.hasNext(); )
        {
        	String key = (String) keys.next();
        	if(DEBUG)Log.d(TAG,"Worker Key: "+key);
        	
            try {
				giveMeCoinWorkers.add( new GiveMeCoinsWorkerInfo( workers.getJSONObject( key ) ) );
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "error - decoding workers "+e.toString());
			}
        }
        Collections.sort(giveMeCoinWorkers, workerComparator);
       
    }
    


    public ArrayList<GiveMeCoinsWorkerInfo> getGiveMeCoinWorkers() {
        return giveMeCoinWorkers;
    }

    public void addGiveMeCoinWorker( GiveMeCoinsWorkerInfo para_NewWorker){
        giveMeCoinWorkers.add(para_NewWorker);
    }

    public int getTotal_hashrate() {
        return total_hashrate;
    }

    public void setTotal_hashrate(int total_hashrate) {
        this.total_hashrate = total_hashrate;
    }

    public double getConfirmed_rewards() {
        return confirmed_rewards;
    }

    public void setConfirmed_rewards(double confirmed_rewards) {
        this.confirmed_rewards = confirmed_rewards;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getRound_estimate() {
        return round_estimate;
    }

    public void setRound_estimate(double round_estimate) {
        this.round_estimate = round_estimate;
    }

    public double getPayout_history() {
        return payout_history;
    }

    public void setPayout_history(double payout_history) {
        this.payout_history = payout_history;
    }

    public long getRound_shares() {
        return round_shares;
    }

    public void setRound_shares(long round_shares) {
        this.round_shares = round_shares;
    }

}
