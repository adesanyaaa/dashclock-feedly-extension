package in.co.madhur.dashclockfeedlyextension.ui;


import in.co.madhur.dashclockfeedlyextension.LoginListener;
import in.co.madhur.dashclockfeedlyextension.R;
import in.co.madhur.dashclockfeedlyextension.service.Connection;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class SplashFragment extends Fragment implements OnClickListener
{
	Button loginButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		 View v= inflater.inflate(R.layout.splash_login, container, false);
		 loginButton=(Button) v.findViewById(R.id.login_button);
		 
		 loginButton.setOnClickListener(this);
		 
		 return v;
	}

	@Override
	public void onClick(View v)
	{
		if(loginButton==v)
		{
			if(Connection.isConnected(getActivity()))
			{
				LoginListener loginListener=(LoginListener) getActivity();
				
				loginListener.Login();
				
			}
			else
			{
				
				Toast.makeText(getActivity(), getString(R.string.internet_error), Toast.LENGTH_SHORT).show();
			}
			
		}
		
	}
	
	
	

}