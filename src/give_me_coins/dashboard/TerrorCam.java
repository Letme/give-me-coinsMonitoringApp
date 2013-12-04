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

import java.io.ByteArrayOutputStream;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

class TerrorCam implements Callback, PreviewCallback
{

	private static final String TAG = "QRCodeReaderCam";
    private static final boolean DEBUG=true;
    
    
	private final SurfaceHolder oPreviewHolder;
	private Camera oCamera = null;
	private boolean inPreview = false;
	private boolean cameraConfigured = false;
	private final Activity oAct;
	private QRCodeReturnListener qrcl = null;


	void addQRCodeReturnListener(QRCodeReturnListener para_qr)
	{
		qrcl = para_qr;
	}
	
	interface QRCodeReturnListener
	{
		/**
		 * packet received
		 * @param head - function name
		 * @param params - parameters
		 */
		void validQRcode(String QRText);
	}
	
	TerrorCam(SurfaceView para_prev, Activity para_act)
	{
		oPreviewHolder = para_prev.getHolder();

		oAct = para_act;
	}

	
	private Camera.Size getBestPreviewSize( int width, int height,
			Camera.Parameters parameters )
	{
		if(DEBUG) Log.d( TAG, "getBestPreviewSize" );
		Camera.Size result = null;

		for ( Camera.Size size : parameters.getSupportedPreviewSizes() )
		{
			if ( size.width <= width && size.height <= height )
			{
				if ( result == null )
				{
					result = size;
				} else
				{
					int resultArea = result.width * result.height;
					int newArea = size.width * size.height;

					if ( newArea > resultArea )
					{
						result = size;
					}
				}
			}
		}
		
		return ( result );
	}

	private void initPreview( int width, int height )
	{
		if(DEBUG) Log.d( TAG, "initPreview " );
		if ( oCamera != null && oPreviewHolder.getSurface() != null )
		{
			try
			{
				oCamera.setPreviewDisplay( oPreviewHolder );
			} catch ( Throwable t )
			{
				Log.e( "PreviewDemo-surfaceCallback",
						"Exception in setPreviewDisplay()", t );
				Toast.makeText( oAct, t.getMessage(), Toast.LENGTH_LONG )
						.show();
			}
			
			if ( !cameraConfigured )
			{
				Camera.Parameters parameters = oCamera.getParameters();
				Camera.Size size = getBestPreviewSize( width, height, parameters );
				
				if ( size != null )
				{
					parameters.setPreviewSize( size.width, size.height );
					oCamera.setParameters( parameters );
					
					/* stupid fps rates beeing stupid ...
					try{
						parameters.setPreviewFpsRange(5000, 12000);
						oCamera.setParameters( parameters );
					
					}
					catch(Exception e)
					{
						if(DEBUG)Log.d(TAG, "doens't like to make fps range ...");
						parameters = oCamera.getParameters();
						parameters.setPreviewSize(size.width, size.height);
						oCamera.setParameters(parameters);
					}
					*/
					Display display = ((WindowManager) oAct.getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
					if(DEBUG)Log.d(TAG, "ORIENT"+oAct.getResources().getConfiguration().orientation );
					
					if (oAct.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
			        {
						if(DEBUG)Log.d(TAG, "ORIENTATION_PORTRAIT Rot: "+display.getRotation());
						if( display.getRotation() == Surface.ROTATION_0 )
						{
							oCamera.setDisplayOrientation(90);
						}
						else
						{
							oCamera.setDisplayOrientation(270);				
						}
			        }
					else if (oAct.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
					{
						if(DEBUG)Log.d(TAG, "ORIENTATION_LANDSCAPE Rot: "+display.getRotation());
						if( display.getRotation() == Surface.ROTATION_90 )
						{
							oCamera.setDisplayOrientation(0);
						}
						else
						{
							oCamera.setDisplayOrientation(180);
						}
					}
					
					cameraConfigured = true;
				}
			}
		}
	}

	void start()
	{
		if(DEBUG) Log.d( TAG, "start" );
		try
		{
			oCamera = Camera.open();
		} catch ( Exception e )
		{
			// TODO Auto-generated catch block
			Log.e(TAG,"Camera not found");
			Toast.makeText( oAct, "No Camera Found", Toast.LENGTH_LONG ).show();
			return;
		}
		oPreviewHolder.addCallback( this );
		oPreviewHolder.setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS );
		
		//oPreviewHolder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
		
		startPreview();
		oCamera.setPreviewCallback( this );
	}

	void stop()
	{
		if(DEBUG) Log.d( TAG, "stop" );
		if ( inPreview )
		{
			if(DEBUG) Log.d( TAG, "stop prev" );
			oCamera.stopPreview();
			oCamera.setPreviewCallback( null );
		}
		oPreviewHolder.removeCallback( this );
		inPreview = false;
		cameraConfigured = false;
		if(oCamera != null)
		{
			oCamera.release();
			oCamera = null;
		}
	}
	private void startPreview()
	{
		if ( cameraConfigured && oCamera != null )
		{
			oCamera.startPreview();
			inPreview = true;
		}
	}

    @Override
	public void onPreviewFrame( byte[] data, Camera camera )
	{
		try
		{
			camera.autoFocus(null);
			// Convert to JPG
			Size previewSize = camera.getParameters().getPreviewSize();
			YuvImage yuvimage = new YuvImage( data, ImageFormat.NV21,
					previewSize.width, previewSize.height, null );
			
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			yuvimage.compressToJpeg( new Rect( 0, 0, previewSize.width,
					previewSize.height ), 80, baos );
			byte[] jdata = baos.toByteArray();

			Bitmap oBitmap = BitmapFactory.decodeByteArray( jdata, 0,
					jdata.length );// ,opts);

			LuminanceSource source = new RGBLuminanceSource( oBitmap );
			BinaryBitmap oBbitmap = new BinaryBitmap( new HybridBinarizer(
					source ) );
			//Log.d( TAG, "blubb" );
			try
			{
				QRCodeReader oReader = new QRCodeReader();
				String sDecoded = oReader.decode( oBbitmap ).getText();
				if(DEBUG) Log.d( TAG, "TEXT: " + sDecoded );
				if(qrcl != null)
				{
					qrcl.validQRcode( sDecoded );
				}
			} 
			catch ( Exception e1 )
			{
				// Log.e(TAG,e1.toString());
				// didnt find qrcode ...
			}

		} 
		catch ( Exception e )
		{
			 Log.e(TAG,e.toString());
		}
	}

	@Override
	public void surfaceCreated( SurfaceHolder holder )
	{
		// no-op -- wait until surfaceChanged()
	}

	@Override
	public void surfaceChanged( SurfaceHolder holder, int format, int width,
			int height )
	{
		if( cameraConfigured )
		{
			oCamera.stopPreview();
			cameraConfigured = false;
		}
		initPreview( width, height );
		startPreview();
	}

	@Override
	public void surfaceDestroyed( SurfaceHolder holder )
	{
		// no-op
	}

}
