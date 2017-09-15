package com.robotemplates.webviewapp.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.robotemplates.webviewapp.Model.DataRadius;
import com.robotemplates.webviewapp.Model.DataWifi;
import com.robotemplates.webviewapp.Network.ApiConfig;
import com.robotemplates.webviewapp.Network.IAPI;
import com.robotemplates.webviewapp.R;
import com.robotemplates.webviewapp.adapter.DrawerAdapter;
import com.robotemplates.webviewapp.fragment.MainFragment;
import com.robotemplates.webviewapp.utility.DistanceUtil;
import com.robotemplates.webviewapp.utility.ScanResultComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends ActionBarActivity
{
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private ListView mDrawerListView;

	private CharSequence mTitle;
	private CharSequence mDrawerTitle;
	private String[] mTitles;

	private List<ScanResult> results = new ArrayList<>();
	private Retrofit retrofit;


	public static Intent newIntent(Context context)
	{
		Intent intent = new Intent(context, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setupActionBar();
		setupDrawer(savedInstanceState);
	}
	
	
	@Override
	public void onStart()
	{
		super.onStart();


	}
	
	
	@Override
	public void onResume()
	{
		super.onResume();
	}
	
	
	@Override
	public void onPause()
	{
		super.onPause();
	}
	
	
	@Override
	public void onStop()
	{
		super.onStop();

	}
	
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// action bar menu
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// open or close the drawer if home button is pressed
		if(mDrawerToggle.onOptionsItemSelected(item))
		{
			return true;
		}

		// action bar menu behaviour
		switch(item.getItemId())
		{
			case android.R.id.home:
				Intent intent = MainActivity.newIntent(this);
				startActivity(intent);
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}


	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}


	@Override
	public void onConfigurationChanged(Configuration newConfiguration)
	{
		super.onConfigurationChanged(newConfiguration);
		mDrawerToggle.onConfigurationChanged(newConfiguration);
	}


	@Override
	public void setTitle(CharSequence title)
	{
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}


	private void setupActionBar()
	{
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		ActionBar bar = getSupportActionBar();
		bar.setDisplayUseLogoEnabled(false);
		bar.setDisplayShowTitleEnabled(true);
		bar.setDisplayShowHomeEnabled(true);
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setHomeButtonEnabled(true);
	}


	private void setupDrawer(Bundle savedInstanceState)
	{
		mTitle = getTitle();
		mDrawerTitle = getTitle();

		// title list
		mTitles = getResources().getStringArray(R.array.navigation_title_list);

		// icon list
		TypedArray iconTypedArray = getResources().obtainTypedArray(R.array.navigation_icon_list);
		Integer[] icons = new Integer[iconTypedArray.length()];
		for(int i=0; i<iconTypedArray.length(); i++)
		{
			icons[i] = iconTypedArray.getResourceId(i, -1);
		}
		iconTypedArray.recycle();

		// reference
		mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_main_layout);
		mDrawerListView = (ListView) findViewById(R.id.activity_main_drawer);

		// set drawer
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		mDrawerListView.setAdapter(new DrawerAdapter(this, mTitles, icons));
		mDrawerListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View clickedView, int position, long id)
			{
				selectDrawerItem(position, false);
			}
		});
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close)
		{
			@Override
			public void onDrawerClosed(View view)
			{
				getSupportActionBar().setTitle(mTitle);
				supportInvalidateOptionsMenu();
			}

			@Override
			public void onDrawerOpened(View drawerView)
			{
				getSupportActionBar().setTitle(mDrawerTitle);
				supportInvalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		// show initial fragment
		if(savedInstanceState == null)
		{
			selectDrawerItem(0, true);
		}
	}


	private void selectDrawerItem(int position, boolean init)
	{
		String[] urlList = getResources().getStringArray(R.array.navigation_url_list);
		String[] shareList = getResources().getStringArray(R.array.navigation_share_list);

		Fragment fragment = MainFragment.newInstance(urlList[position], shareList[position]);
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.activity_main_container, fragment).commitAllowingStateLoss();

		mDrawerListView.setItemChecked(position, true);
		if(!init) setTitle(mTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerListView);
	}

	private void initRetrofit(){
		retrofit = new Retrofit.Builder().baseUrl(ApiConfig.BASE_URL)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

//        ambilData(retrofit);
	}

	//region WIFI.
	private List<DataWifi> buatModelWifi(List<ScanResult> results){
		List<DataWifi> listData = new ArrayList<>();

		for(ScanResult dataScan : results){
			DataWifi tmpData = new DataWifi(dataScan.SSID, dataScan.frequency, dataScan.level);

			// if untuk filter jarak disini
			double distance = DistanceUtil.calculateDistance((double) tmpData.getLevel(),
					(double) tmpData.getFrekuensi());
			tmpData.setJarak(distance);

			if(distance < 10) {
				listData.add(tmpData);
			}
		}

		return listData;
	}

//	private void scanWifi(){
//		@SuppressLint("WifiManagerLeak") final
//		WifiManager wifi = (WifiManager) getSystemService(this.getApplicationContext().WIFI_SERVICE);
//
//		final Handler handler = new Handler();
//		handler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				wifi.startScan();
//				results = wifi.getScanResults();
//				Collections.sort(results, new ScanResultComparator());
//				adapter.setData(buatModelWifi(results));
//				adapter.notifyDataSetChanged();
//
//				List<DataWifi> listData = new ArrayList<>();
//				listData = buatModelWifi(results);
//
//				kirimDataRadius(String.valueOf(listData.get(0).getJarak()), String.valueOf(listData.get(1).getJarak())
//						, String.valueOf(listData.get(2).getJarak()));
//
//
//				Log.d("LOG_RESULT", wifi.getScanResults().toString());
//				Log.d("LOG_RESULT_2", buatModelWifi(results).toString());
//				scanWifi();
//			}
//		}, 3000);
//	}
	//endregion\

	// POST data ke server
//	private void kirimDataRadius(String dataRadius1, String dataRadius2, String dataRadius3){
//		IAPI iApi = retrofit.create(IAPI.class);
//		Call<DataRadius> data = iApi.postDataRadius(dataRadius1, dataRadius2, dataRadius3);
//		data.enqueue(new Callback<DataRadius>() {
//			@Override
//			public void onResponse(Call<DataRadius> call, Response<DataRadius> response) {
//				Log.d("LOG_RESPONSE", response.toString());
//				Log.d("LOG_RESPONSE_2", response.body().getPesan());
//			}
//
//			@Override
//			public void onFailure(Call<DataRadius> call, Throwable t) {
//				t.printStackTrace();
//				Log.d("LOG_FAIL", "fail");
//
//			}
//		});
//	}
}
