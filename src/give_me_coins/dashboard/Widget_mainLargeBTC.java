package give_me_coins.dashboard;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;



public class Widget_mainLargeBTC extends Widget_mainLarge{

		protected String getApiKey(Context context)
		{
			
			SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
			String key = sp.getString(context.getString(R.string.saved_api_key),null);
			
			
			return key.replace("ltc?api_key", "btc?api_key");
		}
		
		@Override
		protected ComponentName getComponentName(Context context) {
			// TODO Auto-generated method stub
			return new ComponentName(context, Widget_mainLargeBTC.class);
		}

}