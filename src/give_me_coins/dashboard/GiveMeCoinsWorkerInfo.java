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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getHashrate() {
        return hashrate;
    }

    public void setHashrate(int hashrate) {
        this.hashrate = hashrate;
    }

    public long getLast_share_timestamp() {
        return last_share_timestamp;
    }

    public void setLast_share_timestamp(long last_share_timestamp) {
        this.last_share_timestamp = last_share_timestamp;
    }

    public GiveMeCoinsWorkerInfo( JSONObject para_workerInfos) {

    	if( para_workerInfos != null )
    	{
	        username = JSONHelper.getVal(para_workerInfos,"username", "N/A");
	        alive = ( JSONHelper.getVal(para_workerInfos, "alive", 0) == 1);
	        hashrate = JSONHelper.getVal(para_workerInfos,"hashrate",0);
	        last_share_timestamp = JSONHelper.getVal(para_workerInfos,"last_share_timestamp",(long)0);
    	}

    }

}
