package info.papdt.express.helper.ui.fragment.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.prefs.MaterialListPreference;
import com.rengwuxian.materialedittext.MaterialEditText;

import info.papdt.express.helper.R;
import info.papdt.express.helper.support.Settings;
import info.papdt.express.helper.ui.SettingsActivity;

public class SettingsMain extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

	public static SettingsMain newInstance() {
		return new SettingsMain();
	}

	public SettingsMain() {

	}

	private Settings mSets;

	private Preference pref_version, pref_donate, pref_os_license, pref_api_provider,
		pref_weibo, pref_github, pref_token_custom;
	private SwitchPreference pref_swipe_back;
	private SwitchPreference pref_auto_refresh;
	private MaterialListPreference pref_token_choose;

	private MaterialDialog dialog_custom_token;
	private MaterialEditText et_secret, et_id;

	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		addPreferencesFromResource(R.xml.pref_main);

		mSets = Settings.getInstance(getActivity().getApplicationContext());

		getActivity().setTitle(R.string.title_settings);

		pref_version = findPreference("application_version");
		pref_weibo = findPreference("sina_weibo");
		pref_os_license = findPreference("open_source_license");
		pref_api_provider = findPreference("api_provider");
		pref_donate = findPreference("donate");
		pref_github = findPreference("github_repo");
		pref_token_choose = (MaterialListPreference) findPreference("api_token_choose");
		pref_token_custom = findPreference("api_token_custom");
		pref_swipe_back = (SwitchPreference) findPreference("swipe_back");
		pref_auto_refresh = (SwitchPreference) findPreference("auto_refresh_first");

		String version = "Unknown";
		try {
			version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
			version += " (" + getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionCode + ")";
		} catch (Exception e) {
			// Keep the default value
		}
		pref_version.setSummary(version);
		pref_swipe_back.setChecked(mSets.getBoolean(Settings.KEY_SWIPE_BACK, true));
		pref_auto_refresh.setChecked(mSets.getBoolean(Settings.KEY_AUTO_REFRESH_FIRST, false));
		pref_token_custom.setDefaultValue(mSets.getInt(Settings.KEY_TOKEN_CHOOSE, 0));
		pref_token_custom.setEnabled(mSets.getInt(Settings.KEY_TOKEN_CHOOSE, 0) == 2);
		String[] values = getResources().getStringArray(R.array.item_token_list_values);
		int index, target = mSets.getInt(Settings.KEY_TOKEN_CHOOSE, 0);
		for (index = 0; index < values.length; index++) {
			if (values[index].equals(String.valueOf(target))) break;
		}
		pref_token_choose.setSummary(
				getResources().getStringArray(R.array.item_token_list)
				[index]
		);

		pref_weibo.setOnPreferenceClickListener(this);
		pref_os_license.setOnPreferenceClickListener(this);
		pref_api_provider.setOnPreferenceClickListener(this);
		pref_donate.setOnPreferenceClickListener(this);
		pref_github.setOnPreferenceClickListener(this);
		pref_token_custom.setOnPreferenceClickListener(this);
		pref_token_choose.setOnPreferenceChangeListener(this);
		pref_swipe_back.setOnPreferenceChangeListener(this);
		pref_auto_refresh.setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference p) {
		if (p == pref_weibo) {
			openWebsite(getString(R.string.item_weibo_author_url));
			return true;
		}
		if (p == pref_os_license) {
			SettingsActivity.launchActivity(getActivity(), SettingsActivity.FLAG_LICENSE);
			return true;
		}
		if (p == pref_api_provider) {
			openWebsite(getString(R.string.api_provider_home));
			return true;
		}
		if (p == pref_donate) {
			showDonateDialog();
			return true;
		}
		if (p == pref_github) {
			openWebsite(getString(R.string.item_github_url));
			return true;
		}
		if (p == pref_token_custom) {
			showCustomTokenDialog();
			return true;
		}
		return false;
	}

	private void openWebsite(String url) {
		Uri uri = Uri.parse(url);
		startActivity(new Intent(Intent.ACTION_VIEW, uri));
	}

	private void showDonateDialog() {
		View v = View.inflate(
				new ContextThemeWrapper(
						getActivity().getApplicationContext(),
						R.style.Theme_AppCompat_Light_Dialog
				),
				R.layout.dialog_donate,
				null
		);
		new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog)
				.setTitle(R.string.item_donate)
				.setView(v)
				.setNegativeButton(android.R.string.ok, null)
				.show();
	}

	private void showCustomTokenDialog() {
		if (dialog_custom_token == null) {
			dialog_custom_token = new MaterialDialog.Builder(getActivity())
					.title(R.string.dialog_token_custom_title)
					.customView(R.layout.dialog_custom_token, false)
					.positiveText(android.R.string.ok)
					.negativeText(android.R.string.cancel)
					.neutralText(R.string.dialog_token_custom_how_to_get_it)
					.callback(new MaterialDialog.ButtonCallback() {
						@Override
						public void onNegative(MaterialDialog dialog) {
							super.onNegative(dialog);
						}

						@Override
						public void onPositive(MaterialDialog dialog) {
							super.onPositive(dialog);
							mSets.putString(Settings.KEY_CUSTOM_SECRET, et_secret.getText().toString().trim());
							mSets.putString(Settings.KEY_CUSTOM_ID, et_id.getText().toString().trim());
						}

						@Override
						public void onNeutral(MaterialDialog dialog) {
							super.onNeutral(dialog);
							openWebsite("http://feng.moe/?p=111");
						}
					})
					.build();

			et_secret = (MaterialEditText) dialog_custom_token.getCustomView().findViewById(R.id.et_secret);
			et_id = (MaterialEditText) dialog_custom_token.getCustomView().findViewById(R.id.et_app_id);
		}

		et_secret.setText(mSets.getString(Settings.KEY_CUSTOM_SECRET, ""));
		et_id.setText(mSets.getString(Settings.KEY_CUSTOM_ID, ""));

		dialog_custom_token.show();
	}

	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		if (pref == pref_token_choose) {
			int value = Integer.parseInt((String) newValue);
			mSets.putInt(Settings.KEY_TOKEN_CHOOSE, value);
			pref_token_custom.setEnabled(value == 2);
			String[] values = getResources().getStringArray(R.array.item_token_list_values);
			int index;
			for (index = 0; index < values.length; index++) {
				if (values[index].equals(newValue)) break;
			}
			pref_token_choose.setSummary(getResources().getStringArray(R.array.item_token_list)[index]);
			return true;
		}
		if (pref == pref_swipe_back) {
			Boolean b = (Boolean) newValue;
			mSets.putBoolean(Settings.KEY_SWIPE_BACK, b);
			pref_swipe_back.setChecked(b);
			showRestartTips();
			return true;
		}
		if (pref == pref_auto_refresh) {
			Boolean b = (Boolean) newValue;
			mSets.putBoolean(Settings.KEY_AUTO_REFRESH_FIRST, b);
			pref_auto_refresh.setChecked(b);
			return true;
		}
		return false;
	}

	private void showRestartTips() {
		Toast.makeText(
				getActivity().getApplicationContext(),
				R.string.toast_you_need_restart,
				Toast.LENGTH_SHORT
		).show();
	}

}
