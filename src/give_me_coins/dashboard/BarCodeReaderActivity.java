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

import give_me_coins.dashboard.TerrorCam.QRCodeReturnListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class BarCodeReaderActivity extends Activity{

	    private static final String TAG = "BarCodeReaderActivity";
	    private static final boolean DEBUG=true;
	    
	  private Activity oAct = null;
	  private TerrorCam oTerrorCam = null;
	  
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.barcodefragment);
	    

	    SurfaceView preview = (SurfaceView)findViewById(R.id.cameraPrev);
	    
	   // int width = preview.getWidth();
	   // int height = preview.getHeight();
	    
	    
	    oTerrorCam = new TerrorCam(preview,this);
	    oTerrorCam.addQRCodeReturnListener( oQRCodeListener );
//	    previewHolder = preview.getHolder();
//	    previewHolder.addCallback(surfaceCallback);
//	    previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    oAct = this;
	    
	  }

	  private final QRCodeReturnListener oQRCodeListener = new QRCodeReturnListener()
	  {
			@Override
			public void validQRcode( String QRText )
			{
				if(DEBUG) Log.d(TAG, "Valid QRText found");
				Intent mainScreen = new	Intent(oAct,give_me_coins.dashboard.MainScreen.class);
				mainScreen.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
				// adding  /pool/api-ftc?api_key= -> in front of key
				mainScreen.putExtra("API_KEY", "/pool/api-ltc?api_key="+QRText);
				oAct.startActivity(mainScreen);
				oAct.finish();
			}
	  };
	  
	  @Override
	  public void onResume() {
	    super.onResume();

	    oTerrorCam.start();
	    
	  }
	    
	  
	  
	  @Override
	  public void onPause() {
		  super.onPause();
		  oTerrorCam.stop();
	  }

	  @Override
	  public void onBackPressed() {
	    	if(DEBUG) Log.d(TAG, "onBackPressed Called");
	        finish(); 
	  }
}