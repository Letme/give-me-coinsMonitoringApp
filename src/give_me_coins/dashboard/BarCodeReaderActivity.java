package give_me_coins.dashboard;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class BarCodeReaderActivity extends FragmentActivity {
	 @Override
     public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             setContentView(R.layout.barcodefragment);
             //BarCodeFragment fragment = (BarCodeFragment) getSupportFragmentManager()
             //        .findFragmentById(R.id.barCode);
             //	fragment.setmCallBack((IResultCallback) this);
     }
}
