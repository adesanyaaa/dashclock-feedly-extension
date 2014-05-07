package in.co.madhur.dashclockfeedlyextension;

import com.infospace.android.oauth2.WebApiHelper;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

public class LoginActivity extends FragmentActivity
{
	WebApiHelper apiHelper;
	AppPreferences appPreferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		appPreferences=new AppPreferences(this);
		
		setContentView(R.layout.activity_login);
		
		WebApiHelper.register(this);
		
		
		apiHelper=WebApiHelper.getInstance();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
