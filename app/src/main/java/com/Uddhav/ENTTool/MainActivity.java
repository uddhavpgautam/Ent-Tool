package com.Uddhav.ENTTool;//package name

import android.app.ProgressDialog; //says “Please wait…….”. Takes “Context” as agr.
import android.content.Intent; //
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.Uddhav.ENTTool.adapters.ListviewAdapter;
import com.Uddhav.ENTTool.database.EarthQuakes;
import com.Uddhav.ENTTool.database.LastEarthquakeDate;
import com.Uddhav.ENTTool.utils.App;
import com.Uddhav.ENTTool.utils.AppSettings;
import com.Uddhav.ENTTool.utils.EBus;
import com.Uddhav.ENTTool.utils.SyncService;
import com.Uddhav.ENTTool.utils.Tools;
import com.squareup.otto.Subscribe;
import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;

import java.util.List;

/**
 * Created by Uddhav Gautam on 7.3.2016. upgautam@ualr.edu
 */
public class MainActivity extends AppCompatActivity implements OnItemClickListener, OnScrollListener {

	private ProgressDialog	pd;
	private ListView		list;
	private int				currentScrollState, currentFirstVisibleItem, currentVisibleItemCount, currentTotalItemCount;
	private ListviewAdapter	adapter;
	private StartAppAd		startAppAd;
	private TextView		tvEmptyMessage;

	private boolean			isConnectToInternet	= true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		StartAppSDK.init(this, getString(R.string.StartApp_AccountId), getString(R.string.StartApp_AppId), true);

		ActionBar ab = getSupportActionBar(); // for icon, title etc. Retrieves a references for this activity actionbar

		ab.setDisplayShowHomeEnabled(true); // for icon. Caz, icon, here, is used as CustomView
		ab.setIcon(R.mipmap.ic_launcher);
		ab.setTitle("  " + getResources().getString(R.string.app_name)); // "string", here, is another inner class of R auto generated class

		ColorDrawable cd = new ColorDrawable(getResources().getColor(R.color.statusbar));
		ab.setBackgroundDrawable(cd);

		ab.setDisplayShowTitleEnabled(false);
		ab.setDisplayShowTitleEnabled(true);

		setContentView(R.layout.activity_main);

		AppSettings.setDefaultSettings();

		if (new LastEarthquakeDate().GetRowCount() == 0) {
			LastEarthquakeDate led = new LastEarthquakeDate();
			led.setDateMilis(606175200000l);
			led.Insert();
		}

		startAppAd = new StartAppAd(this);

		// Banner banner = (com.startapp.android.publish.banner.Banner) findViewById(R.id.startAppBanner);
		// banner.showBanner();

		startAppAd.showAd();
		startAppAd.loadAd();

		tvEmptyMessage = (TextView) findViewById(R.id.tv_empty_message);

		list = (ListView) findViewById(R.id.list2);
		list.setOnItemClickListener(this);
		list.setOnScrollListener(this);

		if (!SyncService.isServiceRunning) {
			Log.i("MainActivity", "Service Started");
			Intent intent = new Intent(getBaseContext(), SyncService.class);
			startService(intent);

			pd = new ProgressDialog(MainActivity.this);
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd.setTitle(getString(R.string.PleaseWait));
			pd.setMessage(getString(R.string.DatasLoading));
			pd.setCancelable(false);
			pd.setIndeterminate(false);
			pd.show();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (isConnectToInternet) {
			List<EarthQuakes> EarthQuakeList = new EarthQuakes().GetAllData();

			if (EarthQuakeList.size() > 0) {
				adapter = new ListviewAdapter(MainActivity.this, EarthQuakeList);
				adapter.notifyDataSetChanged();
				list.setAdapter(adapter);
				list.setSelectionFromTop(currentFirstVisibleItem, 0);
			}
		}

	}

	@Subscribe
	public void messageReceived(EBus event) {
		Log.i("MainActivity", "Tetiklendi! " + event.getStatus());

		if (event.getStatus() == 999) {
			isConnectToInternet = false;
			list.setEmptyView(tvEmptyMessage);
			list.setAdapter(null);
		}
		else {
			isConnectToInternet = true;
			List<EarthQuakes> EarthQuakeList = new EarthQuakes().GetAllData();

			if (EarthQuakeList.size() > 0) {
				adapter = new ListviewAdapter(MainActivity.this, EarthQuakeList);
				adapter.notifyDataSetChanged();
				list.setAdapter(adapter);
				list.setSelectionFromTop(currentFirstVisibleItem, 0);
			}

		}

		if (pd != null && pd.isShowing()) {
			pd.dismiss();
			pd = null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.action_main) {

			Intent i1 = new Intent(MainActivity.this, SettingsActivity.class);
			startActivity(i1);

			return true;
		}
		else if (item.getItemId() == R.id.chart_main) {
			Intent i1 = new Intent(MainActivity.this, ChartActivity.class);
			startActivity(i1);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		this.currentScrollState = scrollState;
		this.isScrollCompleted();
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		this.currentFirstVisibleItem = firstVisibleItem;
		this.currentVisibleItemCount = visibleItemCount;
		this.currentTotalItemCount = totalItemCount;
	}

	private void isScrollCompleted() {

		if (currentFirstVisibleItem + currentVisibleItemCount >= currentTotalItemCount) {
			if (this.currentVisibleItemCount > 0 && this.currentScrollState == OnScrollListener.SCROLL_STATE_IDLE) {

			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent1, View view, int position, long id) {
		try {
			EarthQuakes eq = (EarthQuakes) parent1.getAdapter().getItem(position);

			Intent i = new Intent(MainActivity.this, com.Uddhav.ENTTool.MapsActivity.class);
			i.putExtra("selectedItem", eq.getDateMilis());
			startActivity(i);
		}
		catch (Exception e) {
			System.out.println("Error catched!");
			Tools.catchException(e);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		App.bus.register(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		App.bus.unregister(this);
	}

	@Override
	public void onBackPressed() {
		startAppAd.onBackPressed();
		super.onBackPressed();
	}

}