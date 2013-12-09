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
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
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
import android.view.KeyEvent;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
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
    
	private static String API_key_saved;
    private static final int GET_API_KEY = 1;
	private static final String URL = "https://give-me-coins.com";

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
	private final SystemUiHider mSystemUiHider = null;
	
	/**
	 * Set stuff for Service handler to communicate with UI
	 */
	private static final GMCService mService = null;
	private static GmcStickyService oStickyService = null;
	static GMCPoolService mPoolService = null;
	private static final int DATA_FAILED=1;
	static final int DATA_READY=2;
	static final int DATA_PROGRESS=3;
	static final int POOL_DATA_READY=4;
	
	/**
	 * ProgressBar stuff
	 */
	static int Progress=5;
	private final static float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
	
	
	private static final int MAX_WORKER_NUMBER=20;
	//int i=0;

	private static final String PROGRESS = "Progress";
	
	static final String[] worker_alive = new String[MAX_WORKER_NUMBER];
	static final String[] worker_hashrate = new String[MAX_WORKER_NUMBER];
	static final String[] worker_name = new String[MAX_WORKER_NUMBER];
	static final String[] worker_timestamp= new String[MAX_WORKER_NUMBER];
    static String username = null;
    static String round_estimate= null;
    static String total_hashrate= null;
    static String round_shares= null;
    static String confirmed_rewards= null;
    static String pool_total_hashrate= null;
    static String pool_workers=null;
    static String pool_round_shares=null;
    static String pool_last_block=null;
    static String pool_last_block_shares=null;
    static String pool_last_block_finder=null;
    static String pool_last_block_reward=null;
    static String pool_difficulty=null;

	private static AppSectionsPagerAdapter mAppSectionsPagerAdapter;
	private ViewPager mViewPager;
	private AsyncTask asyncService;
	private AsyncTask asyncPoolService;
	private boolean isRunning=true;

	private static int coin_select = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_screen);

		//create file for shared preference
		sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

		API_key_saved=sharedPref.getString(getString(R.string.saved_api_key),"");
		if( sharedPref.getBoolean(getString(R.string.show_ltc), true) )
		{
			coin_select = 1;
		}
		else if( sharedPref.getBoolean(getString(R.string.show_btc), true) )
		{
			coin_select = 2;
		}
		else if( sharedPref.getBoolean(getString(R.string.show_ftc), true) )
		{
			coin_select = 3;
		}


		// Start service to receive data
		//if(mService==null) mService= new GMCService(this,mHandler);
		if(mPoolService==null) mPoolService = new GMCPoolService(mHandler);
		
		  // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
       
	    // Specify that tabs should be displayed in the action bar.
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

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
	    
	 // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
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

       // mViewPager.setCurrentItem(0);
		if(API_key_saved != null && !API_key_saved.equals("")) {
    		if (!"No api key found".equals(API_key_saved)) {
    			startService();
    			// change to summary tab if api key is set and everything
    			mViewPager.setCurrentItem(1);
    		}
    	}
        
	}
	
	private void startService() {
		asyncService = new AsyncTask<Void, Void, Void>() {
		    @Override
		    protected Void doInBackground(Void... params) {
		    		if(DEBUG) Log.d("asyncService","Starting oStickyService AsyncTask");
					startService(new Intent(MainScreen.this, GmcStickyService.class));
		    		return null;
		    }
		}.execute();
		asyncPoolService = new AsyncTask<Void, Void, Void>() {
		    @Override
		    protected Void doInBackground(Void... params) {
		    		if(DEBUG) Log.d("asyncPoolService","Starting PoolService AsyncTask");
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
	private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	private final Handler mHideHandler = new Handler();
	private final Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};
	private ProgressDialog oLoadingProgress;

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
	private void clearPoolServiceVars(){
		pool_total_hashrate=null;
		pool_workers=null;
		pool_round_shares=null;
		pool_last_block=null;
		pool_last_block_shares=null;
		pool_last_block_finder=null;
		pool_last_block_reward=null;
		pool_difficulty=null;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
		ScrollView summary = (ScrollView) findViewById(R.id.summary_layout);
		ScrollView dashBoard = (ScrollView) findViewById(R.id.dashboard_layout);

        boolean change=false;
	    switch (item.getItemId()) {
	        case R.id.ltc_menu:
    	 		coin_select=1;
    	 		Toast.makeText(this, "Coin changed to LTC", Toast.LENGTH_LONG).show();
				if(API_key_saved.contains("api-btc")) {
					API_key_saved=API_key_saved.replace("api-btc", "api-ltc");
					change=true;
				} else if (API_key_saved.contains("api-ftc")) {
					API_key_saved=API_key_saved.replace("api-ftc", "api-ltc");
					change=true;
				}
    			GMCPoolService.url_fixed=URL+"/pool/api-ltc";
    			clearPoolServiceVars();
    	 		mAppSectionsPagerAdapter.notifyDataSetChanged();
    	 		int ltcColor = getResources().getColor(R.color.ltc);
    	 		if(dashBoard != null)
    	 		{
    	 			dashBoard.setBackgroundColor(ltcColor);
    	 		}
    	 		if( summary != null )
    	 		{
    	 			summary.setBackgroundColor(ltcColor);
    	 		}
	            return true;
	        case R.id.btc_menu:
	        	coin_select=2;
    	 		Toast.makeText(this, "Coin changed to BTC", Toast.LENGTH_LONG).show();
				if(API_key_saved.contains("api-ltc")) {
					API_key_saved=API_key_saved.replace("api-ltc", "api-btc");
					change=true;
				} else if (API_key_saved.contains("api-ftc")) {
					API_key_saved=API_key_saved.replace("api-ftc", "api-btc");
					change=true;
				}
				GMCPoolService.url_fixed=URL+"/pool/api-btc";
				clearPoolServiceVars();
            	mAppSectionsPagerAdapter.notifyDataSetChanged();
    	 		int btcColor = getResources().getColor(R.color.btc);
    	 		if(dashBoard != null)
    	 		{
    	 			dashBoard.setBackgroundColor(btcColor);
    	 		}
    	 		if( summary != null )
    	 		{
    	 			summary.setBackgroundColor(btcColor);
    	 		}
	            return true;
	        case R.id.ftc_menu:
	        	coin_select=3;
     			Toast.makeText(this, "Coin changed to FTC", Toast.LENGTH_LONG).show();
    			if(API_key_saved.contains("api-ltc")) {
    				API_key_saved=API_key_saved.replace("api-ltc", "api-ftc");
    				change=true;
    			} else if (API_key_saved.contains("api-btc")) {
    				API_key_saved=API_key_saved.replace("api-btc", "api-ftc");
    				change=true;
    			}
    			GMCPoolService.url_fixed=URL+"/pool/api-ftc";
    			clearPoolServiceVars();
            	mAppSectionsPagerAdapter.notifyDataSetChanged();
    	 		int ftcColor = getResources().getColor(R.color.ftc);
    	 		if(dashBoard != null)
    	 		{
    	 			dashBoard.setBackgroundColor(ftcColor);
    	 		}
    	 		if( summary != null )
    	 		{
    	 			summary.setBackgroundColor(ftcColor);
    	 		}
    	 		return true;
	        default:
            	mAppSectionsPagerAdapter.notifyDataSetChanged();
	            return super.onOptionsItemSelected(item);
	    }
		/*
		 *  case R.id.refresh:
	        	updateNow();
	        	return true;
		 */
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);

        showIfEnabled(R.string.show_btc, R.id.btc_menu, menu);
        showIfEnabled(R.string.show_ltc, R.id.ltc_menu, menu);
        showIfEnabled(R.string.show_ftc, R.id.ftc_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

    private void showIfEnabled(int key, int itemId, Menu menu) {
        boolean isEnabled = sharedPref.getBoolean(getString(key), true);
        MenuItem item = menu.findItem(itemId);
        item.setVisible(isEnabled);
    }

	 private void updateNow() {
		// TODO Auto-generated method stub
     	if( oStickyService != null )
     	{
     		oLoadingProgress = new ProgressDialog(this);
     		oLoadingProgress.setTitle("Loading");
     		oLoadingProgress.setMessage("Wait while loading...");
     		oLoadingProgress.show();

     		oStickyService.forceUpdate();
     	}
	}

	/**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    static class AppSectionsPagerAdapter extends FragmentPagerAdapter {
        private final Fragment barcode = new BarCodeReaderFragment();
        private final Fragment dashboard = new DashBoardFragment();
        private final Fragment summary = new SummaryFragment();

        AppSectionsPagerAdapter(FragmentManager fm) {
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
                    return barcode;
                case 1:
                	// Summary Fragment
                	return summary;
                case 2:
                	// Dashboard fragment
                	return dashboard;
                default:
                	return summary;
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
    	private View rootView;
        private EditText apikeyoutput;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	rootView = inflater.inflate(R.layout.settings, container, false);
        	
            // Lunching barcode reader activity.
            rootView.findViewById(R.id.lunch_barcode_reader)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                           Activity activity = getActivity();
                           Intent intent = new Intent(activity, BarCodeReaderActivity.class);
                           startActivityForResult(intent, GET_API_KEY);
                        }
                    });
            
            //Check if we have something in field
            apikeyoutput = (EditText) rootView.findViewById(R.id.api_key_value);

            // set show stuff
            CheckBox show_btc = (CheckBox) rootView.findViewById(R.id.show_btc);
            CheckBox show_ltc = (CheckBox) rootView.findViewById(R.id.show_ltc);
            CheckBox show_ftc = (CheckBox) rootView.findViewById(R.id.show_ftc);
            CheckBox show_notification = (CheckBox) rootView.findViewById(R.id.show_notification);
            
            show_btc.setChecked(sharedPref.getBoolean(getString(R.string.show_btc), true ) );
            show_ltc.setChecked(sharedPref.getBoolean(getString(R.string.show_ltc), true ) );
            show_ftc.setChecked(sharedPref.getBoolean(getString(R.string.show_ftc), true ) );
            show_notification.setChecked(sharedPref.getBoolean(getString(R.string.show_notification), true ) );
            
            
            
            setViewToTime(rootView.findViewById(R.id.update_times), sharedPref.getInt(getString(R.string.update_interval), 60000));

            String API_key = sharedPref.getString(getString(R.string.saved_api_key),"No api key found");
            if (!"No api key found".equals(API_key)) {
            	apikeyoutput.setText(API_key);
            } else if (apikeyoutput.getText().length() > 0) {
                apikeyoutput.setText(R.string.add_api_key_text);
                API_key_saved=apikeyoutput.getText().toString();
            }

            final Activity activity = getActivity();

            // Save settings for further usage
            rootView.findViewById(R.id.save_settings_button)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        		if(apikeyoutput.getText().length()>0)
                        			API_key_saved=apikeyoutput.getText().toString();
                        		//strip the api key of everything you can remember - you need just  /pool/... to remain
                        		int instring=API_key_saved.indexOf("/pool");
                        		
                                CheckBox show_btc = (CheckBox) rootView.findViewById(R.id.show_btc);
                                CheckBox show_ltc = (CheckBox) rootView.findViewById(R.id.show_ltc);
                                CheckBox show_ftc = (CheckBox) rootView.findViewById(R.id.show_ftc);
                                CheckBox show_notification = (CheckBox) rootView.findViewById(R.id.show_notification);
                        		
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean(getString(R.string.show_btc), show_btc.isChecked());
                                editor.putBoolean(getString(R.string.show_ftc), show_ftc.isChecked());
                                editor.putBoolean(getString(R.string.show_ltc), show_ltc.isChecked());
                                editor.putBoolean(getString(R.string.show_notification), show_notification.isChecked());
                                editor.putInt(getString(R.string.update_interval), getMillisecondsFromView(rootView.findViewById(R.id.update_times)));
                                
                        		if(instring !=1) {
                        			//Toast.makeText(context, "Removing http://",Toast.LENGTH_LONG).show();
                        			if(instring==-1) {
	                                    Toast.makeText(activity, "You need to add /pool/ in front of your API-key", Toast.LENGTH_LONG).show();
	                                    API_key_saved="No api key found";
	                                } else if(API_key_saved.length()>0) {
	                                    API_key_saved=API_key_saved.substring(API_key_saved.indexOf("/pool/"),API_key_saved.length());
	                                    editor.putString(getString(R.string.saved_api_key), API_key_saved);      
	                                    if(DEBUG) Log.i(TAG,"Saving API_key_save:" + API_key_saved);
	                                    editor.commit();
	                                    Toast.makeText(activity, "Settings have been saved.", Toast.LENGTH_LONG).show();
	                                }
                        		}
                        		if(show_notification.isChecked()) {
                        			editor.commit();
                                    ((MainScreen) activity).startService();
		                        	if(oStickyService != null)
		                        		oStickyService.forceUpdate();
                        		} else {
                        			editor.commit();
                        			if(oStickyService != null)
                        				oStickyService.forceUpdate();
                        		}

	                        	activity.invalidateOptionsMenu();
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
	                        	apikeyoutput.setText("");
	                        	editor.commit();
	                        	Toast.makeText(activity, "Settings cleared.", Toast.LENGTH_LONG).show();
	                        	mAppSectionsPagerAdapter.notifyDataSetChanged();
	                        	//mAppSectionsPagerAdapter.getItemPosition(dashboard);
                        }
                    });
            	ProgressBar displayProgress=(ProgressBar) rootView.findViewById(R.id.progressBarSettings);
            	// Define a shape with rounded corners
                ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners,     null, null));
            
            	//determine what color it needs to be
	    		int color;
	    		switch(coin_select) {
	    			case 1:
	    				color = getResources().getColor(R.color.ltc);
	    				break;
	    			case 2:
	    				color = getResources().getColor(R.color.btc);
						break;
	    			case 3:
	    				 color = getResources().getColor(R.color.ftc);
						break;
	    			default:
	    				 color = getResources().getColor(R.color.ltc);
	    		}
	    		pgDrawable.getPaint().setColor(color);
	    		//actionBar.setTitle("Settings");

	    		// Adds the drawable to your progressBar
	    	    ClipDrawable progressDrawable = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
	    	    displayProgress.setProgressDrawable(progressDrawable);
	    	    displayProgress.setProgress(Progress);

	    		
            return rootView;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == GET_API_KEY && resultCode == Activity.RESULT_OK) {
                // trying to get stuff from qrcode reader activity
                Bundle extras = data.getExtras();
                if (extras != null) {
                    String sApiKey = extras.getString("API_KEY");
                    if(DEBUG)Log.d(TAG,sApiKey);

                    // if got api key from QR activity directly save it
                    if( sApiKey != null )
                    {
                        apikeyoutput.setText(sApiKey);

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.saved_api_key), sApiKey);
                        if(DEBUG) Log.d(TAG,"Saving sApiKey:" + sApiKey);
                        editor.commit();

                        if (isAdded()) {
                            Toast.makeText(getActivity(), "Settings have been saved.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                else
                {

                    if(DEBUG) Log.d(TAG,"No Extras");
                }
            }
        }

		private int getMillisecondsFromView(View para_spinner) {
			if( para_spinner != null )
			{
				Spinner timeSpinner = (Spinner) para_spinner;
				
				switch( timeSpinner.getSelectedItemPosition() ){
					case 0:
						return 30000;
					case 1:
						return 60000;
					case 2:
						return 120000;
					case 3:
						return 60000*5;
					case 4:
						return 600000;
					case 5:
						return 60000*15;
					case 6:
						return 1200000;
					case 7:
						return 60000*30;
					case 8:
						return 60000*60;
					
				}
			}
			return 60000;
		}
        
        private void setViewToTime(View para_spinner, int milliseconds)
        {
		
        	if( para_spinner != null )
        	{
        		Spinner timeSpinner = (Spinner) para_spinner;
		    	switch( milliseconds ){
					case 30000:
						timeSpinner.setSelection(0);
						break;
					case 60000:
						timeSpinner.setSelection(1);
						break;
					case 120000:
						timeSpinner.setSelection(2);
						break;
					case 60000*5:
						timeSpinner.setSelection(3);
						break;
					case 600000:
						timeSpinner.setSelection(4);
						break;
					case 60000*15:
						timeSpinner.setSelection(5);
						break;
					case 1200000:
						timeSpinner.setSelection(6);
						break;
					case 60000*30:
						timeSpinner.setSelection(7);
						break;
					case 60000*60:
						timeSpinner.setSelection(8);
						break;
					default:
						timeSpinner.setSelection(1);
						break;
					
		    	}		
        	}
        	
        }
        
        @Override
        public void update() {
        	
        	// do whatever you want to update your data
        	// Define a shape with rounded corners
            ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners,     null, null));
        
        	//determine what color it needs to be
            int color;
    		switch(coin_select) {
    			case 1:
    				color = getResources().getColor(R.color.ltc);
    				break;
    			case 2:
    				color = getResources().getColor(R.color.btc);
					break;
    			case 3:
    				 color = getResources().getColor(R.color.ftc);
					break;
    			default:
    				 color = getResources().getColor(R.color.ltc);
    		}
            pgDrawable.getPaint().setColor(color);

            ProgressBar displayProgress=(ProgressBar) rootView.findViewById(R.id.progressBarSettings);
    		displayProgress.setProgress(Progress);
    		displayProgress.invalidate();
    		
    		BackKeyExit=0;
        }
    }
    
    /*
     * Dashboard fragment function
     */
    public static class DashBoardFragment extends Fragment implements UpdateableFragment{
    	private View rootView;
    	@Override
		public void onResume() {
			// TODO Auto-generated method stub
			super.onResume();
			update();
		}

		@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    			
	    		 rootView = inflater.inflate(R.layout.dashboard, container, false);
	    		 getNewGMCInfo();
	    		 /*Intent intent = new Intent(getActivity(), DashBoardActivity.class);
	             startActivity(intent);*/	            
	         	
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
	    	    ProgressBar displayProgress=(ProgressBar) rootView.findViewById(R.id.progressBarDashBoard);
	    	    displayProgress.setProgressDrawable(progressDrawable);
	    	    displayProgress.setProgress(Progress);
	    	    pgDrawable.getPaint().setColor(currentColor);
				ScrollView dashBoard = (ScrollView) rootView.findViewById(R.id.dashboard_layout);
				dashBoard.setBackgroundColor(currentColor);
				
				// make hints so when they are null they get what -> could be done in layoutXML
        		TextView hashrateTV = (TextView) rootView.findViewById(R.id.pool_hashrate);
        		hashrateTV.setHint("...");
        		TextView workersTV = (TextView) rootView.findViewById(R.id.pool_workers);
        		workersTV.setHint("...");
        		TextView sharesTV = (TextView) rootView.findViewById(R.id.pool_shares);
        		sharesTV.setHint("...");
        		TextView blockTV = (TextView) rootView.findViewById(R.id.pool_lastblock);
        		blockTV.setHint("...");
        		TextView sblockTV = (TextView) rootView.findViewById(R.id.pool_lastblock_shares);
        		sblockTV.setHint("...");
        		TextView fblockTV = (TextView) rootView.findViewById(R.id.pool_lastblock_finder);
        		fblockTV.setHint("...");
        		TextView rblockTV = (TextView) rootView.findViewById(R.id.pool_lastblock_reward);
        		rblockTV.setHint("...");
        		TextView sifTV = (TextView) rootView.findViewById(R.id.pool_difficulty);
        		sifTV.setHint("...");
				
				
	        	//Read data from settings and write them here
	        	return rootView;
    		}
    	
        @Override
		public void onDestroy() {
			// TODO Auto-generated method stub
			super.onDestroy();
		}
            @Override
            public void update() {
            	
            		// Define Views
            		TextView hashrateTV = (TextView) rootView.findViewById(R.id.pool_hashrate);
            		TextView workersTV = (TextView) rootView.findViewById(R.id.pool_workers);
            		TextView sharesTV = (TextView) rootView.findViewById(R.id.pool_shares);
            		TextView blockTV = (TextView) rootView.findViewById(R.id.pool_lastblock);
            		TextView sblockTV = (TextView) rootView.findViewById(R.id.pool_lastblock_shares);
            		TextView fblockTV = (TextView) rootView.findViewById(R.id.pool_lastblock_finder);
            		TextView rblockTV = (TextView) rootView.findViewById(R.id.pool_lastblock_reward);
            		TextView sifTV = (TextView) rootView.findViewById(R.id.pool_difficulty);
            		
            		// Write data to views
	    		 	if(pool_total_hashrate!=null) {
	            		hashrateTV.setText(readableHashSize(Long.valueOf(pool_total_hashrate.split("\\.")[0])));
	            	} else {
	            		hashrateTV.setText("");
	            	}
	            		workersTV.setText(pool_workers);
	            		sharesTV.setText(pool_round_shares);
	            		blockTV.setText(pool_last_block);
	            		sblockTV.setText(pool_last_block_shares);
	            		fblockTV.setText(pool_last_block_finder);
	            		rblockTV.setText(pool_last_block_reward);
	            	if(pool_difficulty!=null) {
	            		sifTV.setText(pool_difficulty.split("\\.")[0]);
	            	} else {
	            		sifTV.setText("");
	            	}
		        
		        ProgressBar displayProgress=(ProgressBar) rootView.findViewById(R.id.progressBarDashBoard);
		     // Define a shape with rounded corners
                ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners,     null, null));
            
            	//determine what color it needs to be
                int color;
	    		switch(coin_select) {
	    			case 1:
	    				color = getResources().getColor(R.color.ltc);
	    				break;
	    			case 2:
	    				color = getResources().getColor(R.color.btc);
						break;
	    			case 3:
	    				 color = getResources().getColor(R.color.ftc);
						break;
	    			default:
	    				 color = getResources().getColor(R.color.ltc);
	    		}
                pgDrawable.getPaint().setColor(color);
	    		// Adds the drawable to your progressBar
	    	    ClipDrawable progressDrawable = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
	    	    displayProgress.setProgressDrawable(progressDrawable);
	    		displayProgress.setProgress(Progress);
	    		displayProgress.invalidate();
	    		
	    		BackKeyExit=0;
    	} 	
    }
    
    /*
     * Summary fragment function
     */
    public static class SummaryFragment extends Fragment implements UpdateableFragment{
        private Activity activity;
        private ActionBar actionBar;
    	private View rootView;

    	@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    		 activity = getActivity();
	    		 actionBar = activity.getActionBar();
   
	    		 rootView = inflater.inflate(R.layout.summary, container, false);
	    		 getNewGMCInfo();

		        	SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
		        	String API_key_text=sharedPref.getString(getString(R.string.saved_api_key),"No api key found");
		        	
		        	//safeguard to get data - NEED TO INFORM USER
		        	if (!"No api key found".equals(API_key_text)) {
		        		API_key_saved=API_key_text;
		        		//API_key_saved="/pool/api-ltc?api_key=5ccbdb20d6e50838fdce14aeba0727f9e995f798ee618f1c31b2eb2790ba0cec";
		        	}
		        	
	                ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners,     null, null));

	                int currentColor = 0;
	            	//determine what color it needs to be
		    		switch(coin_select) {
		    			case 1:
		    				currentColor = getResources().getColor(R.color.ltc);
		    				actionBar.setTitle("LTC");
		    				actionBar.setDisplayShowTitleEnabled(true);
		    				//getNewGMCInfo();
		    				//mAppSectionsPagerAdapter.notifyDataSetChanged();
		    				break;
		    			case 2:
		    				currentColor = getResources().getColor(R.color.btc);
		    				actionBar.setTitle("BTC");
		    				actionBar.setDisplayShowTitleEnabled(true);
		    				//getNewGMCInfo();
		    				//mAppSectionsPagerAdapter.notifyDataSetChanged();
							break;
		    			case 3:
		    				currentColor =  getResources().getColor(R.color.ftc);
		    				actionBar.setTitle("FTC");
		    				actionBar.setDisplayShowTitleEnabled(true);
		    				//getNewGMCInfo();
		    				//mAppSectionsPagerAdapter.notifyDataSetChanged();
							break;
		    			default:
		    				currentColor = getResources().getColor(R.color.ltc);
		    				 actionBar.setTitle("LTC");
		    				 actionBar.setDisplayShowTitleEnabled(true);
		    				 //getNewGMCInfo();
		    				// mAppSectionsPagerAdapter.notifyDataSetChanged();
		    				 break;
		    		}
		        	
		        	/*if(API_key_saved != null) {
		        		if(API_key_saved.matches("No api key found")) {
		        			
		        		}
		        		else {
		        			mService.start(URL+API_key_saved);
		        		}
		        	}*/
		    		
		        	/*TextView usernameH = new TextView(getActivity());
					usernameH.setText(username + "with hashrate: " + total_hashrate);
					usernameH.setTextColor(Color.RED);
					main_layout.addView(usernameH);
					*/
		        	
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
			        View line = new View(activity);
			        line.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,2));
			        line.setBackgroundColor(getResources().getColor(R.color.table_border));
			        
			        tl.addView( line );
			        if(DEBUG) Log.d(TAG,"Table header ended");
			        int green = getResources().getColor(R.color.light_green);
			        int red = getResources().getColor(R.color.light_red);
			        //TODO: we really need to pack this in a function ... 
			        //------------- KONEC TABLE HEADERJA ---------------------
			        for(int current=0;worker_alive[current]!=null;current++)
			        {
			        	// Check if we have already the line on screen
			        	if(rootView.findViewById(1000+current) != null) {
			        		//What do we want to change?
			        		TableRow tr=(TableRow) rootView.findViewById(1000+current);
			        		TextView Worker_Alive=(TextView) rootView.findViewById(3000+current);
			        		if ("1".equals(worker_alive[current])) {
						        Worker_Alive.setText("Online");
						        Worker_Alive.setTextColor(green);
						        //tr.setBackgroundColor(green);
			        		}
						    else {
						        Worker_Alive.setText("Offline");
						        Worker_Alive.setTextColor(red);
						       // tr.setBackgroundColor(red);
						    }
			        		TextView Worker_HashRate =(TextView) rootView.findViewById(4000+current);
			        		Worker_HashRate.setText(worker_hashrate[current]);
			        		TextView Worker_Name =(TextView) rootView.findViewById(2000+current);
			        		Worker_Name.setText(worker_name[current]);
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
				        if ("1".equals(worker_alive[current])) {
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
				        View line1 = new View(activity);
				        line1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,2));
				        line1.setBackgroundColor(getResources().getColor(R.color.table_border));
				        tl.addView(line1);
			        	}
			        }
			        if(DEBUG) Log.d(TAG,"Table data ended");
	    		 
			        // Define a shape with rounded corners

		    		
    				//pgDrawable.getPaint().setColor(currentColor);
    				
    			//	LinearLayout dashBoard = (LinearLayout) oAct.findViewById(R.id.dashboard_layout);
    			//	dashBoard.setBackgroundColor(currentColor);
    				
    				//LinearLayout summary = (LinearLayout) rootView.findViewById(R.id.summary_layout);
		    		ScrollView main_layout = (ScrollView) (rootView.findViewById(R.id.summary_layout));
		    		main_layout.setBackgroundColor(currentColor);
    				
		    		// Adds the drawable to your progressBar
		    	    ClipDrawable progressDrawable = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);

		    	    ProgressBar displayProgress=(ProgressBar) rootView.findViewById(R.id.progressBarSummary);
		    	    displayProgress.setProgressDrawable(progressDrawable);
		    	    displayProgress.setProgress(Progress);
		    	    
	        	return rootView;
    	}
    	
        @Override
		public void onDestroy() {
			// TODO Auto-generated method stub
			super.onDestroy();
		}

		@Override
        public void update() {
			
		            int currentColor = 0;
		        	//determine what color it needs to be
		    		switch(coin_select) {
		    		case 1:
		    			currentColor = getResources().getColor(R.color.ltc);
						actionBar.setTitle("LTC");
						getNewGMCInfo();
						break;
					case 2:
						currentColor = getResources().getColor(R.color.btc);
						actionBar.setTitle("BTC");
						getNewGMCInfo();
						break;
					case 3:
						currentColor =  getResources().getColor(R.color.ftc);
						actionBar.setTitle("FTC");
						getNewGMCInfo();
						break;
					default:
						currentColor =  getResources().getColor(R.color.ltc);
						 actionBar.setTitle("LTC");
						 getNewGMCInfo();
						 break;
		    		}
			
        	
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
	        	ScrollView main_layout = (ScrollView) (rootView.findViewById(R.id.summary_layout));
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
		        		if("1".equals(worker_alive[current])){
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
		        		TextView Worker_Name = (TextView) rootView.findViewById(2000+current);
		        		Worker_Name.setText(worker_name[current]);
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
			        if("1".equals(worker_alive[current])) {
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
			        View line1 = new View(activity);
			        line1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,2));
			        line1.setBackgroundColor(getResources().getColor(R.color.table_border));
		        	tl.addView(line1);
		        	}

		        }            
		        if(DEBUG) Log.d(TAG,"Summary updated");
		        
		        ProgressBar displayProgress=(ProgressBar) rootView.findViewById(R.id.progressBarSummary);
		        // Define a shape with rounded corners

	    		
				//pgDrawable.getPaint().setColor(currentColor);
				/*
				LinearLayout dashBoard = (LinearLayout) oAct.findViewById(R.id.dashboard_layout);
				dashBoard.setBackgroundColor(currentColor);
				*/
				ScrollView summary = main_layout;
				summary.setBackgroundColor(currentColor);
	    		
	    		// Adds the drawable to your progressBar
	    	    ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null, null));
	    	    ClipDrawable progressDrawable = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
	    	    displayProgress.setProgressDrawable(progressDrawable);
	    	    displayProgress.setProgress(Progress);
	    	    displayProgress.invalidate();
	    	    
	    	    BackKeyExit=0;
    	} 	
    }

    private final Handler mHandler = new Handler() {
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

	private static void getNewGMCInfo() {
		// new info ...
		 if( oStickyService != null)
		 {
			 switch( coin_select){
			 	case 1:
			 		if( oStickyService.getLTCInfo() != null)
			 			setToLocalGMCInfo(oStickyService.getLTCInfo());
			 		break;
			 	case 2:
			 		if( oStickyService.getBTCInfo() != null)
			 			setToLocalGMCInfo(oStickyService.getBTCInfo());
			 		break;
			 	case 3:
			 		if( oStickyService.getFTCInfo() != null)
			 			setToLocalGMCInfo(oStickyService.getFTCInfo());
			 		break;
			 	default:
			 		if( oStickyService.getLTCInfo() != null)
			 			setToLocalGMCInfo(oStickyService.getLTCInfo());
			 		break;
			 }
		 }
		 else 
		 {
			 // It can happen that we do not have the service running
			 //startService();
			 //if(DEBUG) Log.e(TAG,"oStickyService==null");
		 } 
		
	}

	@Override
	protected void onPause() {
		if(DEBUG) Log.d(TAG,"onPause");
		isRunning=false;
		try
		{
			if( oStickyService != null )
			{
				oStickyService.detachListener(btc_callback, ltc_callback, ftc_callback);
			}
			asyncService.cancel(true);
			asyncPoolService.cancel(true);
			mPoolService.timer.cancel();
			mPoolService.stop();
		}
		catch(Exception e)
		{
			Log.e(TAG,"error while trying to pause "+e.toString());
		}
		super.onPause();

	}
	
	private final GetInfoWorkerCallback btc_callback = new GetInfoWorkerCallback() {
		
		@Override
		public void refreshValues(GiveMeCoinsInfo para_giveMeCoinsInfo) {
			if( oStickyService == null)
				oStickyService = GmcStickyService.getInstance(btc_callback, ltc_callback, ftc_callback);
			//TODO: need some defines for coin select stuff
			if( coin_select == 2 )
			{
				setToLocalGMCInfo(para_giveMeCoinsInfo);
				mAppSectionsPagerAdapter.notifyDataSetChanged();
				if(oLoadingProgress != null)oLoadingProgress.dismiss();
			}
			
		}
	};
	
	private final GetInfoWorkerCallback ltc_callback = new GetInfoWorkerCallback() {
		
		@Override
		public void refreshValues(GiveMeCoinsInfo para_giveMeCoinsInfo) {
			if( oStickyService == null)
				oStickyService = GmcStickyService.getInstance(btc_callback, ltc_callback, ftc_callback);
			//TODO: need some defines for coin select stuff
			if( coin_select == 1 )
			{
				setToLocalGMCInfo(para_giveMeCoinsInfo);
				mAppSectionsPagerAdapter.notifyDataSetChanged();
				if(oLoadingProgress != null)oLoadingProgress.dismiss();
			}
			
		}
	};
	
	private final GetInfoWorkerCallback ftc_callback = new GetInfoWorkerCallback() {
		
		@Override
		public void refreshValues(GiveMeCoinsInfo para_giveMeCoinsInfo) {
			if( oStickyService == null)
				oStickyService = GmcStickyService.getInstance(btc_callback, ltc_callback, ftc_callback);
			//TODO: need some defines for coin select stuff
			if( coin_select == 3 )
			{
				setToLocalGMCInfo(para_giveMeCoinsInfo);
				mAppSectionsPagerAdapter.notifyDataSetChanged();
				if(oLoadingProgress != null)oLoadingProgress.dismiss();
			}
			
			
		}
	};
	
	private static void setToLocalGMCInfo(GiveMeCoinsInfo para_giveMeCoinsInfo)
	{
		if(para_giveMeCoinsInfo!=null) {
			username = para_giveMeCoinsInfo.getUsername();
			
			total_hashrate = String.valueOf( para_giveMeCoinsInfo.getTotal_hashrate() );
			round_shares = String.valueOf(para_giveMeCoinsInfo.getRound_shares());
			
			DecimalFormat df = new DecimalFormat("#,##0.########");
			round_estimate = df.format( para_giveMeCoinsInfo.getRound_estimate() );
			
			//round_shares = String.valueOf( para_giveMeCoinsInfo.getRound_shares() );
			confirmed_rewards = df.format( para_giveMeCoinsInfo.getConfirmed_rewards() );
			int i = 0;
			for(GiveMeCoinsWorkerInfo worker : para_giveMeCoinsInfo.getGiveMeCoinWorkers() )
			{
				if(  worker.isAlive() )
				{
					worker_alive[i] = "1";
				}
				else
				{
					worker_alive[i] = "0";
				}
				worker_hashrate[i] = String.valueOf( worker.getHashrate() );
				worker_name[i] = worker.getUsername();
				worker_timestamp[i] = String.valueOf(worker.getLast_share_timestamp());
	
				i++;
	
				//TODO: refactor ... maybe to Arraylist so we can put infite workers in list
				if( i >= 10 )
				{
					break;
				}
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(DEBUG) Log.d(TAG,"onDestroy");
		isRunning=false;
		getActionBar().removeAllTabs();
		try
		{
			if( oStickyService != null)
			{
				oStickyService.detachListener(btc_callback, ltc_callback, ftc_callback);
				oStickyService.stop();
				oStickyService = null;
			}
			asyncService.cancel(true);
			asyncPoolService.cancel(true);
			mPoolService.timer.cancel();
			mPoolService.stop();
		}
		catch(Exception e)
		{
			Log.e(TAG,"error while trying to stop "+e.toString());
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(DEBUG) Log.e(TAG,"onResume");
		isRunning=true;
		oStickyService = GmcStickyService.getInstance(btc_callback, ltc_callback, ftc_callback);
		if( oStickyService == null)
		{
			if(DEBUG)Log.e(TAG,"oStickyService == null onResume");
			startService();
		}
		else
		{
			oStickyService.forceUpdate();
			switch(coin_select)
			{
				case(1):
					setToLocalGMCInfo(oStickyService.getLTCInfo());
					break;
				case(2):
					setToLocalGMCInfo(oStickyService.getBTCInfo());
					break;
				case(3):
					setToLocalGMCInfo(oStickyService.getFTCInfo());
					break;
				default:
					setToLocalGMCInfo(oStickyService.getLTCInfo());
					break;
			}
					
		}
		if( mPoolService==null) startService();
	}
	
	static String readableHashSize(long size) {
	    if(size <= 0) return String.valueOf(size);
	    final String[] units = new String[] { "Kh/s", "Mh/s", "Gh/s", "Th/s","Ph/s","Eh/s" }; //we left ouh h/s because API puts dot at kh/s!!
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1000));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1000, digitGroups)) + " " + units[digitGroups];
	}

	private static int BackKeyExit=0;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch(keyCode) {
			case KeyEvent.KEYCODE_HOME:
				BackKeyExit=0;
				onPause();
				return true;
			case KeyEvent.KEYCODE_BACK:
				if(event.getRepeatCount() == 0) {
					if(BackKeyExit == 0) {
						Toast.makeText(this, "Press BACK twice to fully exit", Toast.LENGTH_LONG).show();
						BackKeyExit=1;
					}
					else {
						if(DEBUG) Log.d(TAG,"Back pressed twice - EXITING");
						BackKeyExit=0;
						finish();
					}
				}
				if(event.getRepeatCount() == 1) {
					if(DEBUG) Log.d(TAG,"Back pressed twice - EXITING");
					finish();
				}
				return true;
			default:
				BackKeyExit=0;
		}
		if(DEBUG) Log.d(TAG,"Pressed: " + keyCode);
		return super.onKeyDown(keyCode, event);
	}
    
	
}
