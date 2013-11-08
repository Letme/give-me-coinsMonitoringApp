package give_me_coins.dashboard;

import java.io.ByteArrayOutputStream;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import give_me_coins.dashboard.RGBLuminanceSource;
import give_me_coins.dashboard.TerrorCam.QRCodeReturnListener;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class BarCodeReaderActivity extends Activity{

	    private static final String TAG = "BarCodeReaderActivity";
	    private static final boolean DEBUG=true;
	    
		
	  private SurfaceView preview = null;
	  private Activity oAct = null;
	  private TerrorCam oTerrorCam = null;
	  
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.barcodefragment);
	    

	    preview = (SurfaceView)findViewById(R.id.cameraPrev);
	    
	    int width = preview.getWidth();
	    int height = preview.getHeight();
	    // bring to 16:9
	    
	    
	    oTerrorCam = new TerrorCam(preview,this);
	    oTerrorCam.addQRCodeReturnListener( oQRCodeListener );
//	    previewHolder = preview.getHolder();
//	    previewHolder.addCallback(surfaceCallback);
//	    previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    oAct = this;

	  }

	  QRCodeReturnListener oQRCodeListener = new QRCodeReturnListener()
	  {

		@Override
		public void validQRcode( String QRText )
		{
			if(DEBUG) Log.d(TAG, "Valid QRText found");
			Intent mainScreen = new	Intent(oAct,give_me_coins.dashboard.MainScreen.class);
			mainScreen.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
			// adding  /pool/api-ftc?api_key= -> in front of key
			mainScreen.putExtra("API_KEY", "/pool/api-ftc?api_key="+QRText);
			oAct.startActivity(mainScreen);
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
	  
	    public void onBackPressed() {
	    	if(DEBUG) Log.d(TAG, "onBackPressed Called");
	        finish(); 
	    }  

}