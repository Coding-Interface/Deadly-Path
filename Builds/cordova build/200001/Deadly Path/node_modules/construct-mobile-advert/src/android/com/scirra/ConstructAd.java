package com.scirra;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.RelativeLayout;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ConstructAd extends CordovaPlugin
{
	private BannerAdvert bannerAD;
	private InterstitialAdvert interstitialAD;
	private VideoAdvert videoAD;
	private RelativeLayout viewContainer;
	private UserConsent userConsent = new UserConsent();
	private String deviceID;

	private static String GMAD_TEST_APPLICATION_ID = "ca-app-pub-3940256099942544~3347511713";

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView)
	{
		super.initialize(cordova, webView);
		createLayoutContainer();
	}

	@Override
	public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext)
	{
		final ConstructAd self = this;

		cordova.getActivity().runOnUiThread(() -> {
            switch (action) {
                case "CreateBannerAdvert":
                    self.CreateBannerAdvert(args, callbackContext);
                    break;
                case "ShowBannerAdvert":
                    self.ShowBannerAdvert(callbackContext);
                    break;
                case "HideBannerAdvert":
                    self.HideBannerAdvert(callbackContext);
                    break;
                case "CreateInterstitialAdvert":
                    self.CreateInterstitialAdvert(args, callbackContext);
                    break;
                case "ShowInterstitialAdvert":
                    self.ShowInterstitialAdvert(callbackContext);
                    break;
                case "CreateVideoAdvert":
                    self.CreateVideoAdvert(args, callbackContext);
                    break;
                case "ShowVideoAdvert":
                    self.ShowVideoAdvert(callbackContext);
                    break;
                case "Configure":
                    self.Configure(args, callbackContext);
                    break;
                case "RequestConsent":
                    self.RequestConsent(callbackContext);
					break;
				case "SetUserPersonalisation":
					self.SetUserPersonalisation(args, callbackContext);
					break;
				case "SetMaxAdContentRating":
					self.SetMaxAdContentRating(args, callbackContext);
					break;
				case "TagForChildDirectedTreatment":
					self.TagForChildDirectedTreatment(args, callbackContext);
					break;
				case "TagForUnderAgeOfConsent":
					self.TagForUnderAgeOfConsent(args, callbackContext);
					break;
				case "RequestIDFA":
                    self.RequestIDFA(callbackContext);
					break;
                default:
                    callbackContext.error("invalid method");
                    break;
            }
        });

		return true;
	}

	public String getDeviceID ()
	{
		if (deviceID == null)
		{
			Activity activity = cordova.getActivity();
			String android_id = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
			deviceID = MD5(android_id).toUpperCase();
		}
		return deviceID;
	}

	private String MD5(String s)
	{
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuilder hexString = new StringBuilder();
			
			for (byte aMessageDigest : messageDigest) {
				int n = 0xFF & aMessageDigest;

				if (n == 0) hexString.append("00");
				else if (n < 16) hexString.append("0" + Integer.toHexString(n));
				else hexString.append(Integer.toHexString(n));
			}

			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	private AdRequest createAdRequest (Boolean debug)
	{
		Bundle extras = new Bundle();

		if (userConsent.status == ConsentStatus.NON_PERSONALIZED)
			extras.putString("npa", "1");

		AdRequest.Builder requestBuilder = new AdRequest.Builder();
		requestBuilder.addNetworkExtrasBundle(AdMobAdapter.class, extras);

		if (debug)
			requestBuilder.addTestDevice(getDeviceID());

		return requestBuilder.build();
	}

	private void createLayoutContainer ()
	{
		final ConstructAd self = this;

		cordova.getActivity().runOnUiThread(() -> {
            Activity activity = self.cordova.getActivity();

            self.viewContainer = new RelativeLayout(activity);
            activity.addContentView(
                    self.viewContainer,
                    new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT
                    )

            );
        });
	}

	@Override
	public void onResume (boolean multitasking) {
		Activity ctx = cordova.getActivity();
		if (videoAD != null)
			videoAD.onResume(ctx);
	}

	@Override
	public void onPause (boolean multitasking)
	{
		Activity ctx = cordova.getActivity();
		if (videoAD != null)
			videoAD.onPause(ctx);
	}

	@Override
	public void onDestroy ()
	{
		Activity ctx = this.cordova.getActivity();
		if (videoAD != null)
			videoAD.onDestroy(ctx);
	}

	private void CreateBannerAdvert(JSONArray args, CallbackContext callbackContext)
	{
		String unitID;
		String adSize;
		Boolean debug;
		String position;

		try
		{
			unitID = args.getString(0);
			adSize = args.getString(1);
			debug = args.getString(2).equals("true");
			position = args.optString(3, "bottom");
		}
		catch (JSONException e)
		{
			callbackContext.error("Invalid unitID");
			return;
		}

		if (unitID.equals(""))
		{
			callbackContext.error("Unit ID not specified");
			return;
		}

		if (adSize.equals(""))
		{
			callbackContext.error("Ad size not specified");
			return;
		}

		AdRequest request = createAdRequest(debug);
		bannerAD = new BannerAdvert(this, unitID, adSize, request, position, callbackContext);
	}

	private void SetUserPersonalisation(JSONArray args, CallbackContext callbackContext)
	{
		try
		{
			String status = args.getString(0);
			userConsent.SetStatus(status, callbackContext);
		}
		catch (JSONException e)
		{
			callbackContext.error("invalid status");
		}
	}

	private void SetMaxAdContentRating(JSONArray args, CallbackContext callbackContext)
	{
		try
		{
			/*
				Valid label values:
				"G"
				"MA"
				"PG"
				"T"
				""
			*/
			String label = args.getString(0);
			RequestConfiguration requestConfiguration = MobileAds
				.getRequestConfiguration()
				.toBuilder()
				.setMaxAdContentRating(label)
				.build();

			MobileAds.setRequestConfiguration(requestConfiguration);
			callbackContext.success("");
		}
		catch (JSONException e)
		{
			callbackContext.error("invalid label");
		}
	}

	private void TagForChildDirectedTreatment(JSONArray args, CallbackContext callbackContext)
	{
		try
		{
			/*
				Valid label values:
				0 = false
				1 = true
				-1 = unspecified
			*/
			Integer label = args.getInt(0);

			RequestConfiguration requestConfiguration = MobileAds
				.getRequestConfiguration()
				.toBuilder()
				.setTagForChildDirectedTreatment(label)
				.build();

			MobileAds.setRequestConfiguration(requestConfiguration);
			callbackContext.success("");
		}
		catch (JSONException e)
		{
			callbackContext.error("invalid label");
		}
	}

	private void TagForUnderAgeOfConsent(JSONArray args, CallbackContext callbackContext)
	{
		try
		{
			/*
				Valid label values:
				0 = false
				1 = true
				-1 = unspecified
			*/
			Integer label = args.getInt(0);

			RequestConfiguration requestConfiguration = MobileAds
				.getRequestConfiguration()
				.toBuilder()
				.setTagForUnderAgeOfConsent(label)
				.build();

			MobileAds.setRequestConfiguration(requestConfiguration);
			callbackContext.success("");
		}
		catch (JSONException e)
		{
			callbackContext.error("invalid label");
		}
	}

	private void RequestConsent(CallbackContext callbackContext)
	{
		// NOTE isRequestLocationInEeaOrUnknown is just set as true here, it's passed through to
		// the JS handler where the value is ignored
		userConsent.ShowUserConsentForm(callbackContext, this.cordova.getContext(), true);
	}

	private void RequestIDFA(CallbackContext callbackContext)
	{
		// Note: not used on Android
		callbackContext.success("not-determined");
	}

	private void ShowBannerAdvert(CallbackContext callbackContext)
	{
		bannerAD.show(viewContainer, callbackContext);
	}

	private void HideBannerAdvert(CallbackContext callbackContext)
	{
		bannerAD.hide(callbackContext);
	}

	private void CreateInterstitialAdvert(JSONArray args, CallbackContext callbackContext)
	{
		String unitID;
		Boolean debug;

		try
		{
			unitID = args.getString(0);
			debug = args.getString(1).equals("true");
		}
		catch (JSONException e)
		{
			callbackContext.error("Invalid unitID");
			return;
		}

		if (unitID.equals(""))
		{
			callbackContext.error("Unit ID not specified");
			return;
		}

		AdRequest request = createAdRequest(debug);
		interstitialAD = new InterstitialAdvert(this, unitID, request, callbackContext);
	}

	private void ShowInterstitialAdvert(CallbackContext callbackContext)
	{
		interstitialAD.show(callbackContext);
	}

	private void CreateVideoAdvert(JSONArray args, CallbackContext callbackContext)
	{
		String unitID;
		Boolean debug;

		try
		{
			unitID = args.getString(0);
			debug = args.getString(1).equals("true");
		}
		catch (JSONException e)
		{
			callbackContext.error("Invalid unitID");
			return;
		}

		if (unitID.equals(""))
		{
			callbackContext.error("Unit ID not specified");
			return;
		}

		AdRequest request = createAdRequest(debug);

		videoAD = new VideoAdvert(this, unitID, request, callbackContext);
	}

	private void ShowVideoAdvert(CallbackContext callbackContext)
	{
		videoAD.show(callbackContext);
	}

	private void Configure(JSONArray args, CallbackContext callbackContext)
	{
		String id;
		String pubID;
		Boolean displayFree;
		String showConsent;
		String privacyURL;
		Boolean debug;
		String debugLocation;

		Activity activity = this.cordova.getActivity();

		try
		{
			id = args.getString(0);
			pubID = args.getString(1);
			privacyURL = args.getString(2);
			displayFree = args.getString(3).equals("true");
			showConsent = args.getString(4);
			debug = args.getString(5).equals("true");
			debugLocation = args.getString(6);
		}
		catch (JSONException e)
		{
			callbackContext.error("Invalid configuration options");
			return;
		}

		if (id.equals(""))
		{
			callbackContext.error("Application ID not specified");
			return;
		}

		if (pubID.equals(""))
		{
			callbackContext.error("Publisher ID not specified");
			return;
		}

		if (privacyURL.equals(""))
		{
			callbackContext.error("Privacy URL not specified");
			return;
		}

		String deviceID = debug ? getDeviceID() : null;

		MobileAds.initialize(activity, id);

		userConsent.UpdateUserConsent(pubID, this.cordova.getContext(), displayFree, showConsent, privacyURL, deviceID, debugLocation, callbackContext);
	}
}
