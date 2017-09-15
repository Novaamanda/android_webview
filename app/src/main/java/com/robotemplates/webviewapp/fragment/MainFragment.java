package com.robotemplates.webviewapp.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.MailTo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.robotemplates.webviewapp.R;
import com.robotemplates.webviewapp.WebViewAppConfig;
import com.robotemplates.webviewapp.utility.DownloadUtility;
import com.robotemplates.webviewapp.utility.Logcat;
import com.robotemplates.webviewapp.utility.NetworkManager;
import com.robotemplates.webviewapp.utility.webappinterface;
import com.robotemplates.webviewapp.view.ViewState;

import java.io.File;
import java.io.IOException;


public class MainFragment extends TaskFragment implements SwipeRefreshLayout.OnRefreshListener
{
	private static final String ARGUMENT_URL = "url";
	private static final String ARGUMENT_SHARE = "share";
	private static final int REQUEST_FILE_PICKER = 1;

	private boolean mActionBarProgress = false;
	private ViewState mViewState = null;
	private View mRootView;
	private String mUrl = "about:blank";
	private String mShare;
	private boolean mLocal = false;
	private ValueCallback<Uri> mFilePathCallback4;
	private ValueCallback<Uri[]> mFilePathCallback5;

	// variables for camera and choosing files methods
	private static final int FILECHOOSER_RESULTCODE = 1;
	private ValueCallback<Uri> mUploadMessage;
	private Uri mCapturedImageURI = null;

	// the same for Android 5.0 methods only
	private ValueCallback<Uri[]> mFilePathCallback;
	private String mCameraPhotoPath;

	// error handling
	private static final String TAG = MainFragment.class.getSimpleName();

	public static MainFragment newInstance(String url, String share)
	{
		MainFragment fragment = new MainFragment();

		// arguments
		Bundle arguments = new Bundle();
		arguments.putString(ARGUMENT_URL, url);
		arguments.putString(ARGUMENT_SHARE, share);
		fragment.setArguments(arguments);

		return fragment;
	}


	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
		setRetainInstance(true);

		// handle fragment arguments
		Bundle arguments = getArguments();
		if(arguments != null)
		{
			handleArguments(arguments);
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		mRootView = inflater.inflate(R.layout.fragment_main, container, false);
		return mRootView;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		// restore webview state
		if(savedInstanceState!=null)
		{
			WebView webView = (WebView) mRootView.findViewById(R.id.fragment_main_webview);
			webView.restoreState(savedInstanceState);

			webView.addJavascriptInterface(new webappinterface(getActivity().getApplicationContext()), "Android");
		}

		// setup webview
		renderView();

		// pull to refresh
		SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.container_swipe_refresh);
		swipeRefreshLayout.setOnRefreshListener(this);

		// load and show data
		if(mViewState==null || mViewState==ViewState.OFFLINE)
		{
			loadData();
		}
		else if(mViewState==ViewState.CONTENT)
		{
			showContent();
		}
		else if(mViewState==ViewState.PROGRESS)
		{
			showProgress();
		}
		else if(mViewState==ViewState.EMPTY)
		{
			showEmpty();
		}

		// progress in action bar
		showActionBarProgress(mActionBarProgress);
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
	public void onDestroyView()
	{
		super.onDestroyView();
		mRootView = null;
	}


	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}


	@Override
	public void onDetach()
	{
		super.onDetach();
	}


	// return here when file selected from camera or from SD Card
	@Override
	public void onActivityResult (int requestCode, int resultCode, Intent data) {

		// code for all versions except of Lollipop
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

			if(requestCode==FILECHOOSER_RESULTCODE) {
				if (null == this.mUploadMessage) {
					return;
				}

				Uri result=null;

				try{
					if (resultCode != Activity.RESULT_OK) {
						result = null;
					} else {
						// retrieve from the private variable if the intent is null
						result = data == null ? mCapturedImageURI : data.getData();
					}
				}
				catch(Exception e) {
					Toast.makeText(getActivity(), R.string.global_offline_toast, Toast.LENGTH_LONG).show();
				}

				mUploadMessage.onReceiveValue(result);
				mUploadMessage = null;
			}

		} // end of code for all versions except of Lollipop

		// start of code for Lollipop only
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

			if (requestCode != FILECHOOSER_RESULTCODE || mFilePathCallback == null) {
				super.onActivityResult(requestCode, resultCode, data);
				return;
			}

			Uri[] results = null;

			// check that the response is a good one
			if (resultCode == Activity.RESULT_OK) {
				if (data == null || data.getData() == null) {
					// if there is not data, then we may have taken a photo
					if (mCameraPhotoPath != null) {
						results = new Uri[]{Uri.parse(mCameraPhotoPath)};
					}
				} else {
					String dataString = data.getDataString();
					if (dataString != null) {
						results = new Uri[]{Uri.parse(dataString)};
					}
				}
			}

			mFilePathCallback.onReceiveValue(results);
			mFilePathCallback = null;

		} // end of code for Lollipop only

	}


	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		// save current instance state
		super.onSaveInstanceState(outState);
		setUserVisibleHint(true);

		// save webview state
		WebView webView = (WebView) mRootView.findViewById(R.id.fragment_main_webview);
		webView.saveState(outState);

		webView.addJavascriptInterface(new webappinterface(getActivity().getApplicationContext()), "Android");

	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		// action bar menu
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_main, menu);

		// show or hide share button
		MenuItem share = menu.findItem(R.id.menu_share);
		share.setVisible(mShare!=null && !mShare.trim().equals(""));
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// action bar menu behaviour
		switch(item.getItemId())
		{
			case R.id.menu_share:
				startShareActivity(getString(R.string.app_name), mShare);
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public void onRefresh()
	{
		runTaskCallback(new Runnable()
		{
			@Override
			public void run()
			{
				refreshData();
			}
		});
	}


	private void handleArguments(Bundle arguments)
	{
		if(arguments.containsKey(ARGUMENT_URL))
		{
			mUrl = arguments.getString(ARGUMENT_URL);
			mLocal = mUrl.contains("file://");
		}
		if(arguments.containsKey(ARGUMENT_SHARE))
		{
			mShare = arguments.getString(ARGUMENT_SHARE);
		}
	}


	private void loadData()
	{
		if(NetworkManager.isOnline(getActivity()) || mLocal)
		{
			// show progress
			showProgress();

			// load web url
			WebView webView = (WebView) mRootView.findViewById(R.id.fragment_main_webview);
			webView.addJavascriptInterface(new webappinterface(getActivity().getApplicationContext()), "Android");
			webView.loadUrl(mUrl);


		}
		else
		{
			showOffline();
		}
	}


	public void refreshData()
	{
		if(NetworkManager.isOnline(getActivity()) || mLocal)
		{
			// show progress in action bar
			showActionBarProgress(true);

			// load web url
			WebView webView = (WebView) mRootView.findViewById(R.id.fragment_main_webview);
			webView.addJavascriptInterface(new webappinterface(getActivity().getApplicationContext()), "Android");
			webView.loadUrl(webView.getUrl());
		}
		else
		{
			showActionBarProgress(false);
			Toast.makeText(getActivity(), R.string.global_offline_toast, Toast.LENGTH_LONG).show();
		}
	}


	private void showActionBarProgress(boolean visible)
	{
		// show pull to refresh progress bar
		SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.container_swipe_refresh);

		swipeRefreshLayout.setRefreshing(visible);
		swipeRefreshLayout.setEnabled(!visible);

		mActionBarProgress = visible;
	}


	private void showContent()
	{
		// show content container
		ViewGroup containerContent = (ViewGroup) mRootView.findViewById(R.id.container_content);
		ViewGroup containerProgress = (ViewGroup) mRootView.findViewById(R.id.container_progress);
		ViewGroup containerOffline = (ViewGroup) mRootView.findViewById(R.id.container_offline);
		ViewGroup containerEmpty = (ViewGroup) mRootView.findViewById(R.id.container_empty);
		containerContent.setVisibility(View.VISIBLE);
		containerProgress.setVisibility(View.GONE);
		containerOffline.setVisibility(View.GONE);
		containerEmpty.setVisibility(View.GONE);
		mViewState = ViewState.CONTENT;
	}


	private void showContent(final long delay)
	{
		final Handler timerHandler = new Handler();
		final Runnable timerRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				runTaskCallback(new Runnable()
				{
					public void run()
					{
						if(getActivity()!=null && mRootView!=null)
						{
							Logcat.d("Fragment.timerRunnable()");
							showContent();
						}
					}
				});
			}
		};
		timerHandler.postDelayed(timerRunnable, delay);
	}


	private void showProgress()
	{
		// show progress container
		ViewGroup containerContent = (ViewGroup) mRootView.findViewById(R.id.container_content);
		ViewGroup containerProgress = (ViewGroup) mRootView.findViewById(R.id.container_progress);
		ViewGroup containerOffline = (ViewGroup) mRootView.findViewById(R.id.container_offline);
		ViewGroup containerEmpty = (ViewGroup) mRootView.findViewById(R.id.container_empty);
		containerContent.setVisibility(View.GONE);
		containerProgress.setVisibility(View.VISIBLE);
		containerOffline.setVisibility(View.GONE);
		containerEmpty.setVisibility(View.GONE);
		mViewState = ViewState.PROGRESS;
	}


	private void showOffline()
	{
		// show offline container
		ViewGroup containerContent = (ViewGroup) mRootView.findViewById(R.id.container_content);
		ViewGroup containerProgress = (ViewGroup) mRootView.findViewById(R.id.container_progress);
		ViewGroup containerOffline = (ViewGroup) mRootView.findViewById(R.id.container_offline);
		ViewGroup containerEmpty = (ViewGroup) mRootView.findViewById(R.id.container_empty);
		containerContent.setVisibility(View.GONE);
		containerProgress.setVisibility(View.GONE);
		containerOffline.setVisibility(View.VISIBLE);
		containerEmpty.setVisibility(View.GONE);
		mViewState = ViewState.OFFLINE;
	}


	private void showEmpty()
	{
		// show empty container
		ViewGroup containerContent = (ViewGroup) mRootView.findViewById(R.id.container_content);
		ViewGroup containerProgress = (ViewGroup) mRootView.findViewById(R.id.container_progress);
		ViewGroup containerOffline = (ViewGroup) mRootView.findViewById(R.id.container_offline);
		ViewGroup containerEmpty = (ViewGroup) mRootView.findViewById(R.id.container_empty);
		containerContent.setVisibility(View.GONE);
		containerProgress.setVisibility(View.GONE);
		containerOffline.setVisibility(View.GONE);
		containerEmpty.setVisibility(View.VISIBLE);
		mViewState = ViewState.EMPTY;
	}


	private void renderView()
	{
		// reference
		final WebView webView = (WebView) mRootView.findViewById(R.id.fragment_main_webview);
		webView.addJavascriptInterface(new webappinterface(getActivity().getApplicationContext()), "Android");

		// webview settings
		webView.getSettings().setBuiltInZoomControls(false);
		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setBackgroundColor(getResources().getColor(R.color.global_bg_front));
		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY); // fixes scrollbar on Froyo
		webView.setWebChromeClient(new WebChromeClient()
		{
			public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
				callback.invoke(origin, true, false);
			}
			// for Lollipop, all in one
			public boolean onShowFileChooser(
					WebView webView, ValueCallback<Uri[]> filePathCallback,
					WebChromeClient.FileChooserParams fileChooserParams) {
				if (mFilePathCallback != null) {
					mFilePathCallback.onReceiveValue(null);
				}
				mFilePathCallback = filePathCallback;

				Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

				if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {

					// create the file where the photo should go
					File photoFile = null;
					try {
						photoFile = createImageFile();
						takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
					} catch (IOException ex) {
						// Error occurred while creating the File
						Log.e(TAG, "Unable to create Image File", ex);
					}

					// continue only if the file was successfully created
					if (photoFile != null) {
						mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
						takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
								Uri.fromFile(photoFile));
					} else {
						takePictureIntent = null;
					}
				}

				Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
				contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
				contentSelectionIntent.setType("image/*");

				Intent[] intentArray;
				if (takePictureIntent != null) {
					intentArray = new Intent[]{takePictureIntent};
				} else {
					intentArray = new Intent[0];
				}

				Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
				chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
				chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
				chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

				startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);

				return true;
			}

			// creating image files (Lollipop only)
			private File createImageFile() throws IOException {

				File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "SRS-PHOTO");

				if (!imageStorageDir.exists()) {
					imageStorageDir.mkdirs();
				}

				// create an image file name
				imageStorageDir  = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
				return imageStorageDir;
			}

			// openFileChooser for Android 3.0+
			public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
				mUploadMessage = uploadMsg;

				try {
					File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "SRS-PHOTO");

					if (!imageStorageDir.exists()) {
						imageStorageDir.mkdirs();
					}

					File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");

					mCapturedImageURI = Uri.fromFile(file); // save to the private variable

					final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
					captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
					// captureIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

					Intent i = new Intent(Intent.ACTION_GET_CONTENT);
					i.addCategory(Intent.CATEGORY_OPENABLE);
					i.setType("image/*");

					// Create file chooser intent
					Intent chooserIntent = Intent.createChooser(i, "Image Chooser");

					// Set camera intent to file chooser
					chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
							, new Parcelable[] { captureIntent });

					// On select image call onActivityResult method of activity
					startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
				} catch (Exception e) {
					Toast.makeText(getActivity(), R.string.global_offline_toast, Toast.LENGTH_LONG).show();
				}

			}

			// openFileChooser for Android < 3.0
			public void openFileChooser(ValueCallback<Uri> uploadMsg) {
				openFileChooser(uploadMsg, "");
			}

			// openFileChooser for other Android versions
            /* may not work on KitKat due to lack of implementation of openFileChooser() or onShowFileChooser()
               https://code.google.com/p/android/issues/detail?id=62220
               however newer versions of KitKat fixed it on some devices */
			public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
				openFileChooser(uploadMsg, acceptType);
			}

		});
		webView.setWebViewClient(new WebViewClient()
		{
			private boolean mSuccess = true;


			@Override
			public void onPageFinished(final WebView view, final String url)
			{
				runTaskCallback(new Runnable()
				{
					public void run()
					{
						if(getActivity()!=null && mSuccess)
						{
							showContent(); // hide progress bar with delay to show webview content smoothly
							showActionBarProgress(false);
						}
					}
				});
			}


			@Override
			public void onReceivedError(final WebView view, final int errorCode, final String description, final String failingUrl)
			{
				runTaskCallback(new Runnable()
				{
					public void run()
					{
						if(getActivity()!=null)
						{
							mSuccess = false;
							showEmpty();
							showActionBarProgress(false);
						}
					}
				});
			}


			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				if(DownloadUtility.isDownloadableFile(url))
				{
					Toast.makeText(getActivity(), R.string.fragment_main_downloading, Toast.LENGTH_LONG).show();
					DownloadUtility.downloadFile(getActivity(), url, DownloadUtility.getFileName(url));
					return true;
				}
				else if(url!=null && (url.startsWith("http://") || url.startsWith("https://")))
				{
					// determine for opening the link externally or internally
					boolean external = isLinkExternal(url);
					boolean internal = isLinkInternal(url);
					if(!external && !internal)
					{
						external = WebViewAppConfig.OPEN_LINKS_IN_EXTERNAL_BROWSER;
					}

					// open the link
					if(external)
					{
						startWebActivity(url);
						return true;
					}
					else
					{
						showActionBarProgress(true);
						return false;
					}
				}
				else if(url!=null && url.startsWith("mailto:"))
				{
					MailTo mailTo = MailTo.parse(url);
					startEmailActivity(mailTo.getTo(), mailTo.getSubject(), mailTo.getBody());
					return true;
				}
				else if(url!=null && url.startsWith("tel:"))
				{
					startCallActivity(url);
					return true;
				}
				else if(url!=null && url.startsWith("sms:"))
				{
					startSmsActivity(url);
					return true;
				}
				else if(url!=null && url.startsWith("geo:"))
				{
					startMapSearchActivity(url);
					return true;
				}
				else
				{
					return false;
				}
			}
		});
		webView.setOnKeyListener(new View.OnKeyListener()
		{
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				if(event.getAction() == KeyEvent.ACTION_DOWN)
				{
					WebView webView = (WebView) v;

					switch(keyCode)
					{
						case KeyEvent.KEYCODE_BACK:
							if(webView.canGoBack())
							{
								webView.goBack();
								return true;
							}
							break;
					}
				}

				return false;
			}
		});
		webView.requestFocus(View.FOCUS_DOWN); // http://android24hours.blogspot.cz/2011/12/android-soft-keyboard-not-showing-on.html
		webView.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				switch(event.getAction())
				{
					case MotionEvent.ACTION_DOWN:
					case MotionEvent.ACTION_UP:
						if(!v.hasFocus())
						{
							v.requestFocus();
						}
						break;
				}

				return false;
			}
		});


	}


	private void controlBack()
	{
		final WebView webView = (WebView) mRootView.findViewById(R.id.fragment_main_webview);
		if(webView.canGoBack()) webView.goBack();
	}


	private void controlForward()
	{
		final WebView webView = (WebView) mRootView.findViewById(R.id.fragment_main_webview);
		if(webView.canGoForward()) webView.goForward();
	}


	private void controlStop()
	{
		final WebView webView = (WebView) mRootView.findViewById(R.id.fragment_main_webview);
		webView.stopLoading();
	}


	private void controlReload()
	{
		final WebView webView = (WebView) mRootView.findViewById(R.id.fragment_main_webview);
		webView.reload();
	}


	private void startWebActivity(String url)
	{
		try
		{
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
		}
		catch(ActivityNotFoundException e)
		{
			// can't start activity
		}
	}


	private void startEmailActivity(String email, String subject, String text)
	{
		try
		{
			StringBuilder builder = new StringBuilder();
			builder.append("mailto:");
			builder.append(email);

			Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(builder.toString()));
			intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
			intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
			startActivity(intent);
		}
		catch(ActivityNotFoundException e)
		{
			// can't start activity
		}
	}


	private void startCallActivity(String url)
	{
		try
		{
			Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
			startActivity(intent);
		}
		catch(ActivityNotFoundException e)
		{
			// can't start activity
		}
	}


	private void startSmsActivity(String url)
	{
		try
		{
			Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
			startActivity(intent);
		}
		catch(ActivityNotFoundException e)
		{
			// can't start activity
		}
	}


	private void startMapSearchActivity(String url)
	{
		try
		{
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
		}
		catch(android.content.ActivityNotFoundException e)
		{
			// can't start activity
		}
	}


	private void startShareActivity(String subject, String text)
	{
		try
		{
			Intent intent = new Intent(android.content.Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
			intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
			startActivity(intent);
		}
		catch(android.content.ActivityNotFoundException e)
		{
			// can't start activity
		}
	}


	private boolean isLinkExternal(String url)
	{
		for(String rule : WebViewAppConfig.LINKS_OPENED_IN_EXTERNAL_BROWSER)
		{
			if(url.contains(rule)) return true;
		}
		return false;
	}


	private boolean isLinkInternal(String url)
	{
		for(String rule : WebViewAppConfig.LINKS_OPENED_IN_INTERNAL_WEBVIEW)
		{
			if(url.contains(rule)) return true;
		}
		return false;
	}
}
