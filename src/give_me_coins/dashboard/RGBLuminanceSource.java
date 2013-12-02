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

import com.google.zxing.LuminanceSource;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.FileNotFoundException;

public final class RGBLuminanceSource extends LuminanceSource {

	private final byte[] luminances;
    private static final String TAG = "RGBLuminanceSource";
    private static final boolean DEBUG=false;

	public RGBLuminanceSource(String path) throws FileNotFoundException {
		this(loadBitmap(path));
	}

	public RGBLuminanceSource(Bitmap bitmap) {
		super(bitmap.getWidth(), bitmap.getHeight());

		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

		// In order to measure pure decoding speed, we convert the entire image
		// to a greyscale array
		// up front, which is the same as the Y channel of the
		// YUVLuminanceSource in the real app.
		luminances = new byte[width * height];
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				int pixel = pixels[offset + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;
				if (r == g && g == b) {
					// Image is already greyscale, so pick any channel.
					luminances[offset + x] = (byte) r;
				} else {
					// Calculate luminance cheaply, favoring green.
					luminances[offset + x] = (byte) ((r + g + g + b) >> 2);
				}
			}
		}
	}

    @Override
	public byte[] getRow(int y, byte[] row) {
		if (y < 0 || y >= getHeight()) {
			throw new IllegalArgumentException(
					"Requested row is outside the image: " + y);
		}
		int width = getWidth();
		if (row == null || row.length < width) {
			row = new byte[width];
		}

		System.arraycopy(luminances, y * width, row, 0, width);
		return row;
	}

	// Since this class does not support cropping, the underlying byte array
	// already contains
	// exactly what the caller is asking for, so give it to them without a copy.
    @Override
	public byte[] getMatrix() {
		return luminances;
	}

	private static Bitmap loadBitmap(String path) throws FileNotFoundException {
		Bitmap bitmap = BitmapFactory.decodeFile(path);
		if (bitmap == null) {
			throw new FileNotFoundException("Couldn�t open " + path);
		}
		return bitmap;
	}

}