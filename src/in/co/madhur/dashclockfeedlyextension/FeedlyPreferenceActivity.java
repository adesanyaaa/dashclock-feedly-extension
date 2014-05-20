package in.co.madhur.dashclockfeedlyextension;

import in.co.madhur.dashclockfeedlyextension.AppPreferences.Keys;
import in.co.madhur.dashclockfeedlyextension.service.Alarms;

import com.google.android.apps.dashclock.configuration.AppChooserPreference;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.TextUtils;

public class FeedlyPreferenceActivity extends PreferenceActivity
{
	private AppPreferences appPreferences;

	protected final OnPreferenceChangeListener listPreferenceChangeListerner = new OnPreferenceChangeListener()
	{

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue)
		{
			UpdateLabel((ListPreference) preference, newValue.toString());
			return true;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_layout);

		appPreferences = new AppPreferences(this);

	}

	@Override
	protected void onResume()
	{
		super.onResume();

		SetListeners();

		UpdateLabel((ListPreference) findPreference(Keys.SYNC_INTERVAL.key), null);
		UpdateLabel((ListPreference) findPreference(Keys.MINIMUM_UNREAD.key), null);
		UpdateLabel((ListPreference) findPreference(Keys.PICK_THEME.key), null);
	}

	protected void UpdateLabel(ListPreference listPreference, String newValue)
	{

		if (newValue == null)
		{
			newValue = listPreference.getValue();
		}

		int index = listPreference.findIndexOfValue(newValue);
		if (index != -1)
		{
			newValue = (String) listPreference.getEntries()[index];
			listPreference.setTitle(newValue);
		}

	}

	@SuppressWarnings("deprecation")
	protected void SetListeners()
	{

		final Keys clickIntentKey = Keys.CLICK_INTENT;

		findPreference(Keys.SYNC_INTERVAL.key).setOnPreferenceChangeListener(listPreferenceChangeListerner);
		findPreference(Keys.MINIMUM_UNREAD.key).setOnPreferenceChangeListener(listPreferenceChangeListerner);

		CharSequence intentSummary = AppChooserPreference.getDisplayValue(this, appPreferences.getMetadata(clickIntentKey));
		getPreferenceScreen().findPreference(clickIntentKey.key).setSummary(TextUtils.isEmpty(intentSummary)
				|| intentSummary.equals(getString(R.string.pref_shortcut_default)) ? ""
				: intentSummary);

		intentSummary = AppChooserPreference.getDisplayValue(this, appPreferences.getMetadata(Keys.NOTIFICATION_CLICK_INTENT));
		getPreferenceScreen().findPreference(Keys.NOTIFICATION_CLICK_INTENT.key).setSummary(TextUtils.isEmpty(intentSummary)
				|| intentSummary.equals(getString(R.string.pref_shortcut_default)) ? ""
				: intentSummary);

		getPreferenceScreen().findPreference(clickIntentKey.key).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				CharSequence intentSummary = AppChooserPreference.getDisplayValue(getBaseContext(), newValue.toString());
				getPreferenceScreen().findPreference(clickIntentKey.key).setSummary(TextUtils.isEmpty(intentSummary)
						|| intentSummary.equals(getResources().getString(R.string.pref_shortcut_default)) ? ""
						: intentSummary);
				return true;
			}

		});

		getPreferenceScreen().findPreference(Keys.NOTIFICATION_CLICK_INTENT.key).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				CharSequence intentSummary = AppChooserPreference.getDisplayValue(getBaseContext(), newValue.toString());
				getPreferenceScreen().findPreference(Keys.NOTIFICATION_CLICK_INTENT.key).setSummary(TextUtils.isEmpty(intentSummary)
						|| intentSummary.equals(getResources().getString(R.string.pref_shortcut_default)) ? ""
						: intentSummary);
				return true;
			}

		});

		findPreference(Keys.ENABLE_AUTOSYNC.key).setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				Boolean newVal = (Boolean) newValue;
				Alarms alarms = new Alarms(FeedlyPreferenceActivity.this);

				if (newVal)
					alarms.Schedule();
				else
					alarms.cancel();

				return true;
			}
		});
		
		findPreference(Keys.ACTION_ABOUT.key).setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				Intent aboutIntent=new Intent();
				aboutIntent.setClass(FeedlyPreferenceActivity.this, AboutActivity.class);
				startActivity(aboutIntent);
				return true;
			}
		});

		findPreference(Keys.FOLLOW_TWITTER.key).setOnPreferenceClickListener(new OnPreferenceClickListener()
		{

			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Consts.TWITTER_URL)));
				return true;
			}
		});
		
		findPreference(Keys.PICK_THEME.key).setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				// TODO Auto-generated method stub
				return false;
			}
		});
	}

}
