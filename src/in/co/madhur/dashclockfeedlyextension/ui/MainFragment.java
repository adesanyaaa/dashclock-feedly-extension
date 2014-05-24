package in.co.madhur.dashclockfeedlyextension.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.infospace.android.oauth2.WebApiHelper;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Configuration.Builder;

import in.co.madhur.dashclockfeedlyextension.App;
import in.co.madhur.dashclockfeedlyextension.AppPreferences;
import in.co.madhur.dashclockfeedlyextension.Consts;
import in.co.madhur.dashclockfeedlyextension.R;
import in.co.madhur.dashclockfeedlyextension.Consts.UPDATESOURCE;
import in.co.madhur.dashclockfeedlyextension.api.Category;
import in.co.madhur.dashclockfeedlyextension.api.Feedly;
import in.co.madhur.dashclockfeedlyextension.api.FeedlyData;
import in.co.madhur.dashclockfeedlyextension.api.Profile;
import in.co.madhur.dashclockfeedlyextension.api.Subscription;
import in.co.madhur.dashclockfeedlyextension.db.DbHelper;
import in.co.madhur.dashclockfeedlyextension.service.Alarms;
import in.co.madhur.dashclockfeedlyextension.service.Connection;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.Filter.FilterListener;

public class MainFragment extends Fragment
{

	private WebApiHelper apiHelper;
	private AppPreferences appPreferences;
	private ProgressBar progressBar;
	private ExpandableListView listView;
	private FeedlyListViewAdapter notiAdapter;
	private int LOGIN_REQUEST_CODE = 1;
	private TextView statusText;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.activity_main, container, false);

		progressBar = (ProgressBar) v.findViewById(R.id.pbHeaderProgress);
		listView = (ExpandableListView) v.findViewById(R.id.listview);
		statusText = (TextView) v.findViewById(R.id.statusMessage);

		listView.setOnGroupClickListener(new OnGroupClickListener()
		{
			@Override
			public boolean onGroupClick(ExpandableListView parent, View clickedView, int groupPosition, long rowId)
			{
				ImageView groupIndicator = (ImageView) clickedView.findViewById(R.id.group_indicator);
				if (parent.isGroupExpanded(groupPosition))
				{
					parent.collapseGroup(groupPosition);
					groupIndicator.setImageResource(R.drawable.expander_open_holo_dark);
				}
				else
				{
					parent.expandGroup(groupPosition);
					groupIndicator.setImageResource(R.drawable.expander_close_holo_dark);
				}
				return true;
			}
		});

		appPreferences = new AppPreferences(getActivity());
		if (!appPreferences.IsTokenPresent())
		{
			StartLoginProcedure();
		}
		else
		{
			WebApiHelper.register(getActivity());
			apiHelper = WebApiHelper.getInstance();

			if (apiHelper.shouldRefreshAccesToken())
			{

				apiHelper.refreshAccessTokenIfNeeded();
			}

			GetFeedlyData();

		}

		return v;
	}

	private void GetFeedlyData(boolean forceRefresh)
	{

		String token = appPreferences.GetToken();

		Feedly feedly = Feedly.getInstance(token);

		new GetFeedlyDataTask(feedly, forceRefresh).execute(0);

	}

	private void GetFeedlyData()
	{
		GetFeedlyData(false);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == 1 && requestCode == LOGIN_REQUEST_CODE)
		{
			GetFeedlyData();

		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		FeedlyListViewAdapter adapter = (FeedlyListViewAdapter) listView.getExpandableListAdapter();

		switch (item.getItemId())
		{
			case R.id.action_refresh:
				Refresh();

				break;

			case R.id.action_settings:
			//	Intent prefIntent = new Intent(getActivity(), FeedlyPreferenceFragmentActivity.class);
			//	startActivity(prefIntent);
				getFragmentManager().beginTransaction().replace(android.R.id.content, new FeedlyPreferenceFragment()).addToBackStack("settings").commit();
				break;

			case R.id.action_accept:
				SaveSelectedCategories();
				new Alarms(getActivity()).StartUpdate(UPDATESOURCE.ACCEPT_BUTTON);
				getActivity().finish();
				break;

			case R.id.action_selecteverything:
				adapter.selectAll();
				break;

			case R.id.action_selectallcategories:

				adapter.selectAllCategories();
				break;

			case R.id.action_selectallfeeds:
				adapter.selectAllFeeds();
				break;

			case R.id.action_selectnone:
				adapter.selectNone();
				break;

			case R.id.action_expandall:
				ExpandAll();
				break;

			case R.id.action_collapseall:
				CollapseAll();
				break;

			case R.id.action_logout:

				Logout();
				break;

			case R.id.action_about:
				ShowDialog();
				break;

			default:
				return super.onOptionsItemSelected(item);
		}

		return super.onOptionsItemSelected(item);

	}

	private void ShowDialog()
	{
		AboutDialog dialog = new AboutDialog();
		dialog.show(getFragmentManager(), Consts.ABOUT_TAG);

	}

	private void Logout()
	{
		new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.app_name)).setMessage(getString(R.string.logout_ques)).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				appPreferences.ClearTokens();
				StartLoginProcedure();
				new Alarms(getActivity()).StartUpdate(UPDATESOURCE.LOGOUT_BUTTON);
			}
		}).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				// do nothing
			}
		}).setIcon(android.R.drawable.ic_dialog_alert).show();

	}

	private void ExpandAll()
	{

		for (int i = 0; i < notiAdapter.getGroupCount(); ++i)
		{
			listView.expandGroup(i);
		}

	}

	private void CollapseAll()
	{
		for (int i = 0; i < notiAdapter.getGroupCount(); ++i)
		{
			listView.collapseGroup(i);
		}

	}

	private void SaveSelectedCategories()
	{
		FeedlyListViewAdapter listAdapter = (FeedlyListViewAdapter) listView.getExpandableListAdapter();

		if (listAdapter != null)
		{
			listAdapter.SaveSelectedValuestoPreferences();
		}
		else
			Log.e(App.TAG, "Adapter is null while saving selected values");
	}

	private void Refresh()
	{
		if (Connection.isConnected(getActivity()))
		{
			GetFeedlyData(true);
		}
		else
			Toast.makeText(getActivity(), getString(R.string.internet_error), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		
		inflater.inflate(R.menu.main, menu);

		SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
		MenuItem searchitem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchitem);
		SearchableInfo info = searchManager.getSearchableInfo(getActivity().getComponentName());
		searchView.setSearchableInfo(info);

		SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextChange(String newText)
			{
				FeedlyListViewAdapter adapter = (FeedlyListViewAdapter) listView.getExpandableListAdapter();
				adapter.getFilter().filter(newText, new FilterListener()
				{

					@Override
					public void onFilterComplete(int count)
					{

					}
				});

				return true;
			}

			@Override
			public boolean onQueryTextSubmit(String query)
			{
				FeedlyListViewAdapter adapter = (FeedlyListViewAdapter) listView.getExpandableListAdapter();
				adapter.getFilter().filter(query, new FilterListener()
				{

					@Override
					public void onFilterComplete(int count)
					{

					}
				});

				return true;
			}
		};
		searchView.setOnQueryTextListener(textChangeListener);

		
		super.onCreateOptionsMenu(menu, inflater);
	}
//
//	public boolean onCreateOptionsMenu(Menu menu)
//	{
//		
//		return true;
//	}
	
	private void StartLoginProcedure()
	{
		Intent loginIntent = new Intent();
		loginIntent.setClass(getActivity(), LoginActivity.class);
		loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivityForResult(loginIntent, LOGIN_REQUEST_CODE);

	}

	private class GetFeedlyDataTask extends
			AsyncTask<Integer, Integer, FeedlyData>
	{
		private DbHelper dbHelper;
		private Feedly feedly;
		private boolean forceRefresh;

		public GetFeedlyDataTask(Feedly feedly, boolean forceRefresh)
		{
			this.feedly = feedly;
			this.forceRefresh = forceRefresh;
		}

		@Override
		protected void onPreExecute()
		{

			super.onPreExecute();
			progressBar.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);

			Configuration.Builder builder = new Builder();
			builder.setDuration(Configuration.DURATION_INFINITE);

		}

		@Override
		protected FeedlyData doInBackground(Integer... params)
		{
			List<Category> categories;
			Profile profile;
			List<Subscription> subscriptions;
			Map<Category, List<Subscription>> categorySubscriptions = new HashMap<Category, List<Subscription>>();

			try
			{
				dbHelper = DbHelper.getInstance(getActivity());
				if (dbHelper.IsFetchRequired() || forceRefresh)
				{

					profile = feedly.GetProfile();
					dbHelper.TruncateProfile();
					dbHelper.WriteProfile(profile);

					categories = feedly.GetCategories();
					dbHelper.TruncateCategories();
					dbHelper.WriteCategories(categories);

					subscriptions = feedly.GetSubscriptions();
					dbHelper.TruncateSubscriptions();
					dbHelper.WriteSubscriptions(subscriptions);

					for (Category category : categories)
					{
						categorySubscriptions.put(category, dbHelper.GetSubScriptionsForCategory(category.getId()));
					}

				}
				else
				{
					profile = dbHelper.GetProfile();

					categories = dbHelper.GetCategories();

					for (Category category : categories)
					{
						categorySubscriptions.put(category, dbHelper.GetSubScriptionsForCategory(category.getId()));
					}

					subscriptions = dbHelper.GetSubscriptions();
				}

				return new FeedlyData(profile, categories, subscriptions, categorySubscriptions, null);

			}
			catch (Exception e)
			{
				Log.e(App.TAG, e.getMessage());
				e.printStackTrace();
				return new FeedlyData(e.getMessage());
			}
		}

		@Override
		protected void onPostExecute(FeedlyData result)
		{
			super.onPostExecute(result);

			UpdateUI(result);
		}

	}

	private void UpdateUI(FeedlyData result)
	{

		progressBar.setVisibility(View.GONE);

		if (result.isError())
		{
			statusText.setVisibility(View.VISIBLE);

			statusText.setText(result.getErrorMessage());
			return;
		}

		listView.setVisibility(View.VISIBLE);

		notiAdapter = new NotificationViewAdapter(result, getActivity());
		notiAdapter.GetSelectedValuesFromPreferences();
		listView.setAdapter(notiAdapter);

	}

}
