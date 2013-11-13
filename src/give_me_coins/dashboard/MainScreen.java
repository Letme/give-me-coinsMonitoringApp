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

import java.text.DecimalFormat;
import give_me_coins.dashboard.util.SystemUiHider;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TableLayout.LayoutParams;


/**
 * Give-me-coins.com public api for ltc,btc,ftc is
 * Give-me-coins.com/pool/api-ltc
 * Give-me-coins.com/pool/api-btc
 * Give-me-coins.com/pool/api-ftc
 */

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MainScreen extends FragmentActivity implements ActionBar.TabListener {
	// Debugging
    private static final String TAG = "MainScreen";
    private static final boolean DEBUG=true;
    
	static String API_key_saved;
	static String URL="https://give-me-coins.com";
	private final static int REFRESH_RATE=100;
	static boolean change=false;
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;
	private static SharedPreferences sharedPref;
	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	
	/**
	 * Set stuff for Service handler to communicate with UI
	 */
	static GMCService mService = null;
	static GMCPoolService mPoolService = null;
	public static final int DATA_FAILED=1;
	public static final int DATA_READY=2;
	public static final int DATA_PROGRESS=3;
	public static final int POOL_DATA_READY=4;
	
	/**
	 * ProgressBar stuff
	 */
	static int Progress=5;
	final static float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
	
	
	static final int MAX_WORKER_NUMBER=20;
	//int i=0;

	public static final String PROGRESS = "Progress";
	
	public static String[] worker_alive = new String[MAX_WORKER_NUMBER];
	public static String[] worker_hashrate = new String[MAX_WORKER_NUMBER];
	public static String[] worker_name = new String[MAX_WORKER_NUMBER];
	public static String[] worker_timestamp= new String[MAX_WORKER_NUMBER];
    public static String username = null,
    		round_estimate= null,
    		total_hashrate= null,
    		round_shares= null,
    		confirmed_rewards= null,
    		pool_total_hashrate= null,
    		pool_workers=null,
    		pool_round_shares=null,
    		pool_last_block=null,
    		pool_difficulty=null;
	
	static AppSectionsPagerAdapter mAppSectionsPagerAdapter;
	ViewPager mViewPager;
	static Context context;
	static Fragment barcode;
	static Fragment dashboard;
	static Fragment summary;
	static ActionBar actionBar;
	static AsyncTask asyncService;
	static AsyncTask asyncPoolService;
	private static boolean isRunning=true;
	private static Activity oAct;
	
	static int coin_select=1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_screen);

		//create file for shared preference
		context = this;
		sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		
		oAct = this;
		
		// trying to get stuff from qrcode reader activity 		
		Bundle extras = getIntent().getExtras(); 
		String sApiKey = null;

		if(extras != null) {
			sApiKey = extras.getString("API_KEY");
			if(DEBUG)Log.d(TAG,sApiKey);
			
			// if got api key from QR activity directly save it
			if( sApiKey != null )
			{
				SharedPreferences.Editor editor = sharedPref.edit();
            	editor.putString(getString(R.string.saved_api_key), sApiKey);
            	if(DEBUG) Log.d(TAG,"Saving sApiKey:" + sApiKey);
            	editor.commit();
            	Toast.makeText(context, "Settings have been saved.",Toast.LENGTH_LONG).show();
			}
		}
		else
		{
			
			if(DEBUG) Log.d(TAG,"No Extras");
		}



		API_key_saved=sharedPref.getString(getString(R.string.saved_api_key),"");
		


		// Start service to receive data
		if(mService==null) mService= new GMCService(this,mHandler);
		if(mPoolService==null) mPoolService=new GMCPoolService(this,mHandler);
		
		  // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        actionBar = getActionBar();
       
	    // Specify that tabs should be displayed in the action bar.
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    actionBar.setDisplayShowTitleEnabled(false);
	    
	 // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        
        actionBar.setBackgroundDrawable(new ColorDrawable(R.color.menu_background));
        // For each of the sections in the app, add a tab to the action bar.
        // Create a tab with text corresponding to the page title defined by the adapter.
        // Also specify this Activity object, which implements the TabListener interface, as the
        // listener for when this tab is selected.
        actionBar.addTab(
                actionBar.newTab()
                        .setText("Settings")
                        //.setIcon(R.drawable.settings)
                        .setTabListener(this));
        actionBar.addTab(
                    actionBar.newTab()
                            .setText("Summary")
                           // .setIcon(R.drawable.dashboard)
                            .setTabListener(this));
        actionBar.addTab(
                actionBar.newTab()
                        .setText("Pool")
                        //.setIcon(R.drawable.news)
                        .setTabListener(this));
        
       // mViewPager.setCurrentItem(0);
		if(API_key_saved != null && API_key_saved != "") {
    		if(API_key_saved.matches("No api key found")) {
    			
    		}
    		else {
    			startService();
    			// change to summary tab if api key is set and everything
    			mViewPager.setCurrentItem(1);
    		}
    	}
        
	}
	
	public static void startService() {
		asyncService = new AsyncTask<Void, Void, Void>() {
		    @Override
		    protected Void doInBackground(Void... params) {
		    		if(DEBUG) Log.d("asyncService","Starting AsyncTask");
		        	mService.start(URL+API_key_saved);
					return null;
		    }
		}.execute();
		asyncPoolService = new AsyncTask<Void, Void, Void>() {
		    @Override
		    protected Void doInBackground(Void... params) {
		    		if(DEBUG) Log.d("asyncPoolService","Starting AsyncTask");
		        	mPoolService.start(URL+"/pool/api-ltc");
					return null;
		    }
		}.execute();
	}

/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)  {
		// TODO Auto-generated method stub
		mViewPager.setCurrentItem(tab.getPosition());
		
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_activity_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
		int currentColor = 0;
		LinearLayout summary = (LinearLayout) oAct.findViewById(R.id.summary_layout);
		LinearLayout dashBoard = (LinearLayout) oAct.findViewById(R.id.dashboard_layout);
		
	    switch (item.getItemId()) {
	        case R.id.ltc_menu:
    	 		coin_select=1;
    	 		Toast.makeText(context, "Coin changed to LTC",Toast.LENGTH_LONG).show();
				if(API_key_saved.contains("api-btc")) {
					API_key_saved=API_key_saved.replace("api-btc", "api-ltc");
					change=true;
				}
				if(API_key_saved.contains("api-ftc")) {
					API_key_saved=API_key_saved.replace("api-ftc", "api-ltc");
					change=true;
				}
    			GMCService.url_fixed=URL+API_key_saved;
    			GMCPoolService.url_fixed=URL+"/pool/api-ltc";
    	 		mAppSectionsPagerAdapter.notifyDataSetChanged();
    	 		currentColor = getResources().getColor(R.color.ltc);
    	 		if(dashBoard != null)
    	 		{
    	 			dashBoard.setBackgroundColor(currentColor); 			
    	 		}
    	 		if( summary != null )
    	 		{
    	 			summary.setBackgroundColor(currentColor);    	 			
    	 		}
	            return true;
	        case R.id.btc_menu:
	        	coin_select=2;
    	 		Toast.makeText(context, "Coin changed to BTC",Toast.LENGTH_LONG).show();
				if(API_key_saved.contains("api-ltc")) {
					API_key_saved=API_key_saved.replace("api-ltc", "api-btc");
					change=true;
				}
				if(API_key_saved.contains("api-ftc")) {
					API_key_saved=API_key_saved.replace("api-ftc", "api-btc");
					change=true;
				}
				GMCService.url_fixed=URL+API_key_saved;
				GMCPoolService.url_fixed=URL+"/pool/api-btc";
            	mAppSectionsPagerAdapter.notifyDataSetChanged();
    	 		currentColor = getResources().getColor(R.color.btc);
    	 		if(dashBoard != null)
    	 		{
    	 			dashBoard.setBackgroundColor(currentColor); 			
    	 		}
    	 		if( summary != null )
    	 		{
    	 			summary.setBackgroundColor(currentColor);    	 			
    	 		}
	            return true;
	        case R.id.ftc_menu:
	        	coin_select=3;
     			Toast.makeText(context, "Coin changed to FTC",Toast.LENGTH_LONG).show();
    			if(API_key_saved.contains("api-ltc")) {
    				API_key_saved=API_key_saved.replace("api-ltc", "api-ftc");
    				change=true;
    			}
    			if(API_key_saved.contains("api-btc")) {
    				API_key_saved=API_key_saved.replace("api-btc", "api-ftc");
    				change=true;
    			}
    			GMCService.url_fixed=URL+API_key_saved;
    			GMCPoolService.url_fixed=URL+"/pool/api-ftc";
            	mAppSectionsPagerAdapter.notifyDataSetChanged();
    	 		currentColor = getResources().getColor(R.color.ftc);
    	 		if(dashBoard != null)
    	 		{
    	 			dashBoard.setBackgroundColor(currentColor); 			
    	 		}
    	 		if( summary != null )
    	 		{
    	 			summary.setBackgroundColor(currentColor);    	 			
    	 		}
	        default:
	            return super.onOptionsItemSelected(item);
	    }
		
	}
	
	 /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

	
        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public int getItemPosition(Object object) {
            if (object instanceof UpdateableFragment) {
                ((UpdateableFragment) object).update();
            }
            //don't return POSITION_NONE, avoid fragment recreation. 
            return super.getItemPosition(object);
        }
        
        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                	barcode= new BarCodeReaderFragment(); 
                    return barcode;
                case 1:
                	// Summary Fragment
                	summary=new SummaryFragment();
                	return summary;
                case 2:
                	// Dashboard fragment
                	dashboard= new DashBoardFragment();
                	return dashboard;
                	
                default:
                    // The other sections of the app are dummy placeholders.
                    Fragment dummy = new DummySectionFragment();
                    Bundle args = new Bundle();
                    args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
                    dummy.setArguments(args);
                    return dummy;
            }
        }
        
        @Override
        public int getCount() {
            return 3;
        }
    }
    
    /**
     * A Barcode reader fragment
     */
    public static class BarCodeReaderFragment extends Fragment implements UpdateableFragment {
    	 EditText apikeyoutput;
    	 View rootView;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	rootView = inflater.inflate(R.layout.settings, container, false);
        	
            // Lunching barcode reader activity.
            rootView.findViewById(R.id.lunch_barcode_reader)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                           Intent intent = new Intent(getActivity(), BarCodeReaderActivity.class);
                           startActivity(intent);
                           oAct.finish();
                        }
                    });
            
            //Check if we have something in field
            apikeyoutput = (EditText) rootView.findViewById(R.id.api_key_value);
            String API_key = sharedPref.getString(getString(R.string.saved_api_key),"No api key found");
            if(!API_key.matches("No api key found")) {
            	apikeyoutput.setText(API_key);
            }
            else {
            	if(apikeyoutput.getText().length()>0) {
            		apikeyoutput.setText(R.string.add_api_key_text);
            		API_key_saved=apikeyoutput.getText().toString();
            	}
            }
            
            
            // Save settings for further usage
            rootView.findViewById(R.id.save_settings_button)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        		if(apikeyoutput.getText().length()>0)
                        			API_key_saved=apikeyoutput.getText().toString();
                        		SharedPreferences.Editor editor = sharedPref.edit();
	                        	editor.putString(getString(R.string.saved_api_key), API_key_saved);
	                        	if(DEBUG) Log.d(TAG,"Saving API_key_save:" + API_key_saved);
	                        	editor.commit();
	                        	Toast.makeText(context, "Settings have been saved.",Toast.LENGTH_LONG).show();
	                        	startService();
	                        	mAppSectionsPagerAdapter.notifyDataSetChanged();
	                        	
	                        	//mAppSectionsPagerAdapter.getItemPosition(dashboard);
                        }
                    });
            // delete settings for further usage
            rootView.findViewById(R.id.delete_settings_button)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        		SharedPreferences.Editor editor = sharedPref.edit();
	                        	editor.remove(getString(R.string.saved_api_key));
	                        	if(DEBUG) Log.d(TAG,"Removing API_key_save");
	                        	editor.commit();
	                        	Toast.makeText(context, "Settings cleared.",Toast.LENGTH_LONG).show();
	                        	mAppSectionsPagerAdapter.notifyDataSetChanged();
	                        	//mAppSectionsPagerAdapter.getItemPosition(dashboard);
                        }
                    });
            	ProgressBar displayProgress=(ProgressBar) rootView.findViewById(R.id.progressBarSettings);
            	// Define a shape with rounded corners
                ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners,     null, null));
            
            	//determine what color it needs to be
	    		switch(coin_select) {
	    			case 1:
	    				pgDrawable.getPaint().setColor(getResources().getColor(R.color.ltc));
	    				break;
	    			case 2:
	    				pgDrawable.getPaint().setColor(getResources().getColor(R.color.btc));
						break;
	    			case 3:
	    				 pgDrawable.getPaint().setColor(getResources().getColor(R.color.ftc));
						break;
	    			default:
	    				 pgDrawable.getPaint().setColor(getResources().getColor(R.color.ltc));
	    		}
	    		//actionBar.setTitle("Settings");
	    		actionBar.setDisplayShowTitleEnabled(true);
	    		// Adds the drawable to your progressBar
	    	    ClipDrawable progressDrawable = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
	    	    displayProgress.setProgressDrawable(progressDrawable);
	    	    displayProgress.setProgress(Progress);

	    		
            return rootView;
        }
        
        @Override
        public void update() {
        	
        	// do whatever you want to update your data
        	ProgressBar displayProgress=(ProgressBar) rootView.findViewById(R.id.progressBarSettings);
        	// Define a shape with rounded corners
            ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners,     null, null));
        
        	//determine what color it needs to be
    		switch(coin_select) {
    			case 1:
    				pgDrawable.getPaint().setColor(getResources().getColor(R.color.ltc));
    				break;
    			case 2:
    				pgDrawable.getPaint().setColor(getResources().getColor(R.color.btc));
					break;
    			case 3:
    				 pgDrawable.getPaint().setColor(getResources().getColor(R.color.ftc));
					break;
    			default:
    				 pgDrawable.getPaint().setColor(getResources().getColor(R.color.ltc));
    		}
    		//actionBar.setTitle("Settings");
    		actionBar.setDisplayShowTitleEnabled(true);
    		displayProgress.setProgress(Progress);
    		displayProgress.invalidate();
        }
    }
    
    /*
     * Dashboard fragment function
     */
    public static class DashBoardFragment extends Fragment implements UpdateableFragment{
    	View rootView;
    	@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
   
	    		 rootView = inflater.inflate(R.layout.dashboard, container, false);
	    		 /*Intent intent = new Intent(getActivity(), DashBoardActivity.class);
	             startActivity(intent);*/	            
	         	
		        ProgressBar displayProgress=(ProgressBar) rootView.findViewById(R.id.progressBarDashBoard);
		     // Define a shape with rounded corners
                ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners,     null, null));
            
                int currentColor = 0;
            	//determine what color it needs to be
	    		switch(coin_select) {
	    			case 1:
	    				currentColor = getResources().getColor(R.color.ltc);
	    				break;
	    			case 2:
	    				currentColor = getResources().getColor(R.color.btc);
						break;
	    			case 3:
	    				currentColor = getResources().getColor(R.color.ftc);
						break;
	    			default:
	    				currentColor = getResources().getColor(R.color.ltc);
	    		}
	    		// Adds the drawable to your progressBar
	    	    ClipDrawable progressDrawable = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
	    	    displayProgress.setProgressDrawable(progressDrawable);
	    	    displayProgress.setProgress(Progress);
	    	    pgDrawable.getPaint().setColor(currentColor);
				LinearLayout dashBoard = (LinearLayout) rootView.findViewById(R.id.dashboard_layout);
				dashBoard.setBackgroundColor(currentColor);
				
	        	//Read data from settings and write them here
	        	return rootView;
    		}
            @Override
            public void update() {
	    		 if(pool_total_hashrate!=null) {
	            		TextView hashrateTV = (TextView) rootView.findViewById(R.id.pool_hashrate);
	            		hashrateTV.setText(readableHashSize(Long.valueOf(pool_total_hashrate.split("\\.")[0])));
	            	}
	            	if(pool_workers!=null) {
	            		TextView workersTV = (TextView) rootView.findViewById(R.id.pool_workers);
	            		workersTV.setText(pool_workers);
	            	}
	            	if(pool_round_shares!=null) {
	            		TextView sharesTV = (TextView) rootView.findViewById(R.id.pool_shares);
	            		sharesTV.setText(pool_round_shares);
	            	}
	            	if(pool_last_block!=null) {
	            		TextView blockTV = (TextView) rootView.findViewById(R.id.pool_lastblock);
	            		blockTV.setText(pool_last_block);
	            	}
	            	if(pool_difficulty!=null) {
	            		TextView sifTV = (TextView) rootView.findViewById(R.id.pool_difficulty);
	            		sifTV.setText(pool_difficulty.split("\\.")[0]);
	            	}
		        
		        ProgressBar displayProgress=(ProgressBar) rootView.findViewById(R.id.progressBarDashBoard);
		     // Define a shape with rounded corners
                ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners,     null, null));
            
            	//determine what color it needs to be
	    		switch(coin_select) {
	    			case 1:
	    				pgDrawable.getPaint().setColor(getResources().getColor(R.color.ltc));
	    				break;
	    			case 2:
	    				pgDrawable.getPaint().setColor(getResources().getColor(R.color.btc));
						break;
	    			case 3:
	    				 pgDrawable.getPaint().setColor(getResources().getColor(R.color.ftc));
						break;
	    			default:
	    				 pgDrawable.getPaint().setColor(getResources().getColor(R.color.ltc));
	    		}
	    		// Adds the drawable to your progressBar
	    	    ClipDrawable progressDrawable = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
	    	    displayProgress.setProgressDrawable(progressDrawable);
	    		displayProgress.setProgress(Progress);
	    		displayProgress.invalidate();
    	} 	
    }
    
    /*
     * Summary fragment function
     */
    public static class SummaryFragment extends Fragment implements UpdateableFragment{
    	View rootView;
    	@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
   
	    		 rootView = inflater.inflate(R.layout.summary, container, false);

	    		 TextView api_text = (TextView) rootView.findViewById(R.id.api_key_url_text);
	    		 api_text.setPadding(0, 0, 0, 2);
		        	SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
		        	String API_key_text=sharedPref.getString(getString(R.string.saved_api_key),"No api key found");
		        	
		        	//safeguard to get data - NEED TO INFORM USER
		        	if(API_key_text.matches("No api key found")) {
		        		//API_key_saved="/pool/api-ltc?api_key=5ccbdb20d6e50838fdce14aeba0727f9e995f798ee618f1c31b2eb2790ba0cec";
		        		//return rootView;
		            }
		        	else {
		        		API_key_saved=API_key_text;
		        		//API_key_saved="/pool/api-ltc?api_key=5ccbdb20d6e50838fdce14aeba0727f9e995f798ee618f1c31b2eb2790ba0cec";
		        	}
		        	
		        	api_text.setText(API_key_saved);
		        	/*if(API_key_saved != null) {
		        		if(API_key_saved.matches("No api key found")) {
		        			
		        		}
		        		else {
		        			mService.start(URL+API_key_saved);
		        		}
		        	}*/
		        	
		        	LinearLayout main_layout = (LinearLayout) (rootView.findViewById(R.id.summary_layout));
		        	/*TextView usernameH = new TextView(getActivity());
					usernameH.setText(username + "with hashrate: " + total_hashrate);
					usernameH.setTextColor(Color.RED);
					main_layout.addView(usernameH);
					*/
					TableLayout tl = (TableLayout)rootView.findViewById(R.id.myTableLayout);
					//----------------- Dodaj header-----------------
					TableRow trH = new TableRow(getActivity());
					trH.setBackgroundResource(R.drawable.shape);
					//trH.setBackgroundColor(Color.LTGRAY);
					trH.setPadding(5, 5, 5, 5);
			        trH.setLayoutParams(new TableLayout.LayoutParams(
			        		LayoutParams.MATCH_PARENT,
			                LayoutParams.WRAP_CONTENT));
			        // Create first column
			        TextView Worker_NameH = new TextView(getActivity());
			        Worker_NameH.setText("Worker Name"); //+1 tukaj ker gledamo 2 polje
			        Worker_NameH.setTextColor(Color.BLACK);
			       // Worker_NameH.setBackgroundColor(Color.LTGRAY);
			        Worker_NameH.setPadding(5,2,40,2);
			        Worker_NameH.setGravity(Gravity.LEFT);
			        Worker_NameH.setLayoutParams(new TableRow.LayoutParams(
			        		LayoutParams.WRAP_CONTENT));
			        trH.addView(Worker_NameH);
			        
			        // Create second column
			        TextView Worker_AliveH = new TextView(getActivity());
			        Worker_AliveH.setText("Worker status"); //+1 tukaj ker gledamo 2 polje
			        Worker_AliveH.setTextColor(Color.BLACK);
			       // Worker_AliveH.setBackgroundColor(Color.LTGRAY);
			        Worker_AliveH.setGravity(Gravity.CENTER);
			        Worker_AliveH.setPadding(0,2,40,2);
			        Worker_AliveH.setLayoutParams(new TableRow.LayoutParams(
			        		LayoutParams.WRAP_CONTENT));
			        trH.addView(Worker_AliveH);
			        
			        // Create third column
			        TextView Worker_HashRateH = new TextView(getActivity());
			        Worker_HashRateH.setText("HashRate"); //+1 tukaj ker gledamo 2 polje
			        Worker_HashRateH.setTextColor(Color.BLACK);
			        //Worker_HashRateH.setBackgroundColor(Color.LTGRAY);
			        Worker_HashRateH.setGravity(Gravity.RIGHT);
			        Worker_HashRateH.setLayoutParams(new TableRow.LayoutParams(
			        		LayoutParams.WRAP_CONTENT));
			       // Worker_HashRateH.setPadding(10,2,0,2);
			        trH.addView(Worker_HashRateH);
			        /* Add row to TableLayout. */
			        tl.addView(trH,new TableLayout.LayoutParams(
			                  LayoutParams.MATCH_PARENT,
			                  LayoutParams.WRAP_CONTENT));  
			        View line = new View(oAct);
			        line.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,2));
			        line.setBackgroundColor(getResources().getColor(R.color.table_border));
			        
			        tl.addView( line );
			        if(DEBUG) Log.d(TAG,"Table header ended");
			        int green = getResources().getColor(R.color.light_green);
			        int red = getResources().getColor(R.color.light_red);
			        //------------- KONEC TABLE HEADERJA ---------------------
			        for(int current=0;worker_alive[current]!=null;current++)
			        {
			        	// Check if we have already the line on screen
			        	if(rootView.findViewById(1000+current) != null) {
			        		//What do we want to change?
			        		TableRow tr=(TableRow) rootView.findViewById(1000+current);
			        		TextView Worker_Alive=(TextView) rootView.findViewById(3000+current);
			        		if(worker_alive[current].equals("1")) {
						        Worker_Alive.setText("Online");
						        //tr.setBackgroundColor(green);
			        		}
						    else {
						        Worker_Alive.setText("Offline");
						       // tr.setBackgroundColor(red);
						    }
			        		TextView Worker_HashRate =(TextView) rootView.findViewById(4000+current);
			        		Worker_HashRate.setText(worker_hashrate[current]);
			        	}
			        	else {
			        		/* Create a new row to be added. */
				        TableRow tr = new TableRow(getActivity());
				        tr.setBackgroundResource(R.drawable.shape);
				        tr.setId(1000+current);
				        tr.setLayoutParams(new TableLayout.LayoutParams(
				        		LayoutParams.MATCH_PARENT,
				                LayoutParams.WRAP_CONTENT));
				        
				        // First column
				        TextView Worker_Name = new TextView(getActivity());
				        Worker_Name.setId(2000+current);
				        Worker_Name.setGravity(Gravity.LEFT);
				        Worker_Name.setText(worker_name[current]);
				        Worker_Name.setTextColor(Color.BLACK);
				        Worker_Name.setLayoutParams(new TableRow.LayoutParams(
				        		LayoutParams.WRAP_CONTENT));
				        tr.addView(Worker_Name);
				        
				        // Second column
				        TextView Worker_Alive = new TextView(getActivity());
				        Worker_Alive.setId(3000+current);
				        if(worker_alive[current].equals("1")) {
				        	Worker_Alive.setText("Online");
				        	Worker_Alive.setTextColor(green);
				        	//tr.setBackgroundColor(green);
				        }
				        else {
				        	Worker_Alive.setText("Offline");
				        	Worker_Alive.setTextColor(red);
				        	//tr.setBackgroundColor(red);
				        }
				        Worker_Alive.setTextColor(Color.BLACK);
				        Worker_Alive.setGravity(Gravity.CENTER);
				        Worker_Alive.setLayoutParams(new TableRow.LayoutParams(
				        		LayoutParams.WRAP_CONTENT));
				        tr.addView(Worker_Alive);
				        
				        // Third column
				        TextView Worker_HashRate = new TextView(getActivity());
				        Worker_HashRate.setId(4000+current);
				        Worker_HashRate.setText(worker_hashrate[current]);
				        Worker_HashRate.setGravity(Gravity.RIGHT);
				        Worker_HashRate.setTextColor(Color.BLACK);
				        Worker_HashRate.setLayoutParams(new TableRow.LayoutParams(
				        		LayoutParams.WRAP_CONTENT));
				        tr.addView(Worker_HashRate);
				        /* Add row to TableLayout. */
				        tl.addView(tr,new TableLayout.LayoutParams(
				                  LayoutParams.MATCH_PARENT,
				                  LayoutParams.WRAP_CONTENT));
				        tl.setPadding(5, 5, 5, 5);
				        View line1 = new View(oAct);
				        line1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,2));
				        line1.setBackgroundColor(getResources().getColor(R.color.table_border));
				        tl.addView(line1);
			        	}
			        }
			        if(DEBUG) Log.d(TAG,"Table data ended");
	    		 
			        ProgressBar displayProgress=(ProgressBar) rootView.findViewById(R.id.progressBarSummary);
			        // Define a shape with rounded corners
	                ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners,     null, null));
	            
	                int currentColor = 0;
	            	//determine what color it needs to be
		    		switch(coin_select) {
		    			case 1:
		    				currentColor = getResources().getColor(R.color.ltc);
		    				actionBar.setTitle("LTC");
		    				actionBar.setDisplayShowTitleEnabled(true);
		    				break;
		    			case 2:
		    				currentColor = getResources().getColor(R.color.btc);
		    				actionBar.setTitle("BTC");
		    				actionBar.setDisplayShowTitleEnabled(true);
							break;
		    			case 3:
		    				currentColor =  getResources().getColor(R.color.ftc);
		    				actionBar.setTitle("FTC");
		    				actionBar.setDisplayShowTitleEnabled(true);
							break;
		    			default:
		    				currentColor = getResources().getColor(R.color.ltc);
		    				 actionBar.setTitle("LTC");
		    				 actionBar.setDisplayShowTitleEnabled(true);
		    		}
		    		
    				//pgDrawable.getPaint().setColor(currentColor);
    				
    			//	LinearLayout dashBoard = (LinearLayout) oAct.findViewById(R.id.dashboard_layout);
    			//	dashBoard.setBackgroundColor(currentColor);
    				
    				//LinearLayout summary = (LinearLayout) rootView.findViewById(R.id.summary_layout);
		    		main_layout.setBackgroundColor(currentColor);
    				
		    		// Adds the drawable to your progressBar
		    	    ClipDrawable progressDrawable = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		    	    displayProgress.setProgressDrawable(progressDrawable);
		    	    displayProgress.setProgress(Progress);
		    	    
	        	return rootView;
    	}
        @Override
        public void update() {
	        	TextView api_text = (TextView) rootView.findViewById(R.id.api_key_url_text);
	        	api_text.setText(API_key_saved);
	        	
            	if(username!=null) {
            		TextView usernameTV = (TextView) rootView.findViewById(R.id.summary_username);
            		usernameTV.setText(username);
            	}
            	if(confirmed_rewards!=null) {
            		TextView confrewardsTV = (TextView) rootView.findViewById(R.id.summary_confirmedrewards);
            		confrewardsTV.setText(confirmed_rewards);
            	}
            	if(total_hashrate!=null) {
            		TextView hashrateTV = (TextView) rootView.findViewById(R.id.summary_totalhash);
            		hashrateTV.setText(readableHashSize(Long.valueOf(total_hashrate.split("\\.")[0])));
            	}
            	if(round_estimate!=null) {
            		TextView estimateTV = (TextView) rootView.findViewById(R.id.summary_roundestimate);
            		estimateTV.setText(round_estimate);
            	}
            	if(round_shares!=null) {
            		TextView sharesTV = (TextView) rootView.findViewById(R.id.summary_roundshares);
            		sharesTV.setText(round_shares);
            	}
            	
            	 // do whatever you want to update your data
	        	LinearLayout main_layout = (LinearLayout) (rootView.findViewById(R.id.summary_layout));
	        	TableLayout tl = (TableLayout)rootView.findViewById(R.id.myTableLayout);
	        	
		       // View line = new View(oAct);
		        // line.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,2));
		       // line.setBackgroundColor(Color.GRAY);
		        
		       // tl.addView( line );
		        int green = getResources().getColor(R.color.light_green);
		        int red = getResources().getColor(R.color.light_red);
		        for(int current=0;worker_alive[current]!=null;current++)
		        {
		        	// Check if we have already the line on screen
		        	if(rootView.findViewById(1000+current) != null) {
		        		//What do we want to change?
		        		TableRow tr=(TableRow) rootView.findViewById(1000+current);
		        		TextView Worker_Alive=(TextView) rootView.findViewById(3000+current);
		        		if(worker_alive[current].equals("1")){
		        			//tr.setBackgroundColor(Color.GREEN);
					        Worker_Alive.setText("Online");
					        Worker_Alive.setTextColor(green);
		        		}
					    else {
					    	Worker_Alive.setText("Offline");
					    	Worker_Alive.setTextColor(red);
					    	//tr.setBackgroundColor(Color.RED);
					    }
					        	
		        		TextView Worker_HashRate =(TextView) rootView.findViewById(4000+current);
		        		Worker_HashRate.setText(worker_hashrate[current]);
		        	}
		        	else {
		        		/* Create a new row to be added. */
			        TableRow tr = new TableRow(getActivity());
			        tr.setBackgroundResource(R.drawable.shape);
			        tr.setId(1000+current);
			        tr.setPadding(5, 5, 5, 5);
			        tr.setLayoutParams(new TableLayout.LayoutParams(
			        		LayoutParams.MATCH_PARENT,
			                LayoutParams.WRAP_CONTENT));
			        
			        // First column
			        TextView Worker_Name = new TextView(getActivity());
			        Worker_Name.setId(2000+current);
			        Worker_Name.setText(worker_name[current]);
			        Worker_Name.setGravity(Gravity.LEFT);
			        Worker_Name.setTextColor(Color.BLACK);
			        Worker_Name.setLayoutParams(new TableRow.LayoutParams(
			        		LayoutParams.WRAP_CONTENT));
			        tr.addView(Worker_Name);
			        
			        // Second column
			        TextView Worker_Alive = new TextView(getActivity());
			        Worker_Alive.setId(3000+current);
			        if(worker_alive[current].equals("1")) {
			        	Worker_Alive.setText("Online");
			        	Worker_Alive.setTextColor(green);
			        	//tr.setBackgroundColor(Color.GREEN);
			        }
			        else {
			        	Worker_Alive.setText("Offline");
			        	Worker_Alive.setTextColor(red);
			        	//tr.setBackgroundColor(Color.RED);
			        }
			        Worker_Alive.setTextColor(Color.BLACK);
			        Worker_Alive.setGravity(Gravity.CENTER);
			        Worker_Alive.setLayoutParams(new TableRow.LayoutParams(
			        		LayoutParams.WRAP_CONTENT));
			        tr.addView(Worker_Alive);
			        
			        // Third column
			        TextView Worker_HashRate = new TextView(getActivity());
			        Worker_HashRate.setId(4000+current);
			        Worker_HashRate.setText(worker_hashrate[current]);
			        Worker_HashRate.setGravity(Gravity.RIGHT);
			        Worker_HashRate.setTextColor(Color.BLACK);
			        Worker_HashRate.setLayoutParams(new TableRow.LayoutParams(
			        		LayoutParams.WRAP_CONTENT));
			        tr.addView(Worker_HashRate);
			        /* Add row to TableLayout. */
			        tl.addView(tr,new TableLayout.LayoutParams(
			                  LayoutParams.MATCH_PARENT,
			                  LayoutParams.WRAP_CONTENT));
			        View line1 = new View(oAct);
			        line1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,2));
			        line1.setBackgroundColor(getResources().getColor(R.color.table_border));
		        	tl.addView(line1);
		        	}

		        }            
		        if(DEBUG) Log.d(TAG,"Summary updated");
		        
		        ProgressBar displayProgress=(ProgressBar) rootView.findViewById(R.id.progressBarSummary);
		        // Define a shape with rounded corners
                ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners,     null, null));
                int currentColor = 0;
            	//determine what color it needs to be
	    		switch(coin_select) {
	    		case 1:
	    			currentColor = getResources().getColor(R.color.ltc);
    				actionBar.setTitle("LTC");
    				break;
    			case 2:
    				currentColor = getResources().getColor(R.color.btc);
    				actionBar.setTitle("BTC");
					break;
    			case 3:
    				currentColor =  getResources().getColor(R.color.ftc);
    				actionBar.setTitle("FTC");
					break;
    			default:
    				currentColor =  getResources().getColor(R.color.ltc);
    				 actionBar.setTitle("LTC");
	    		}
	    		
				//pgDrawable.getPaint().setColor(currentColor);
				/*
				LinearLayout dashBoard = (LinearLayout) oAct.findViewById(R.id.dashboard_layout);
				dashBoard.setBackgroundColor(currentColor);
				*/
				LinearLayout summary = main_layout;
				summary.setBackgroundColor(currentColor);
	    		
	    		// Adds the drawable to your progressBar
	    	    ClipDrawable progressDrawable = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
	    	    displayProgress.setProgressDrawable(progressDrawable);
	    	    displayProgress.setProgress(Progress);
	    	    displayProgress.invalidate();
    	} 	
    }
    
    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_dummy, container, false);
            Bundle args = getArguments();
            ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                    getString(R.string.dummy_section_text, args.getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }
    
    private static final Handler mHandler = new Handler() {
    	 @Override
         public void handleMessage(Message msg) {
    		 // so it doesnt try to update if not running
    		 if( isRunning )
    		 {
	    		 switch(msg.what) {
	    		 	case DATA_FAILED:
	    		 		mAppSectionsPagerAdapter.notifyDataSetChanged();
	    		 		break;
	    		 	case DATA_READY:
	    		 		mAppSectionsPagerAdapter.notifyDataSetChanged();
	    		 		break;
	    		 	case DATA_PROGRESS:
	    		 		//Progress=msg.getData().getInt(PROGRESS);
	    		 		mAppSectionsPagerAdapter.notifyDataSetChanged();
	    		 		break;
	    		 	case POOL_DATA_READY:
	    		 		//Progress=msg.getData().getInt(PROGRESS);
	    		 		mAppSectionsPagerAdapter.notifyDataSetChanged();
	    		 		break;
	    		 
	    		 }
    		 }
    	 }
    };


	@Override
	protected void onStop() {
		super.onStop();
		
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		isRunning=false;
		try
		{
			asyncService.cancel(true);
			asyncPoolService.cancel(true);
			mService.timer.cancel();
			mPoolService.timer.cancel();
			mService.stop();
			mPoolService.stop();
		}
		catch(Exception e)
		{
			Log.e(TAG,"error while trying to pause "+e.toString());
		}

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		isRunning=true;
		if(mService==null || mPoolService==null) startService();
	}
	
	public static String readableHashSize(long size) {
	    if(size <= 0) return String.valueOf(size);
	    final String[] units = new String[] { "Kh/s", "Mh/s", "Gh/s", "Th/s","Ph/s","Eh/s" }; //we left ouh h/s because API puts dot at kh/s!!
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1000));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1000, digitGroups)) + " " + units[digitGroups];
	}
    
}
