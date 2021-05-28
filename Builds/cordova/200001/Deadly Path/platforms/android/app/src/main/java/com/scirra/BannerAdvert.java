package com.scirra;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;

import android.os.Bundle;
import android.widget.RelativeLayout;


public class BannerAdvert extends AdListener
{
	private AdView banner;
	private CallbackContext promise;
	private RelativeLayout parentLayout;

	BannerAdvert(CordovaPlugin ctx, String unitID, String adSize, AdRequest request, String position, CallbackContext callbackContext)
	{

		AdSize realAdSize;

		switch (adSize) {
			case "portrait":
				realAdSize = AdSize.SMART_BANNER;
				break;
			case "landscape":
				realAdSize = AdSize.SMART_BANNER;
				break;
			case "standard":
				realAdSize = AdSize.BANNER;
				break;
			case "large":
				realAdSize = AdSize.LARGE_BANNER;
				break;
			case "medium":
				realAdSize = AdSize.MEDIUM_RECTANGLE;
				break;
			case "full":
				realAdSize = AdSize.FULL_BANNER;
				break;
			case "leaderboard":
				realAdSize = AdSize.LEADERBOARD;
				break;
			default:
				realAdSize = AdSize.SMART_BANNER;
				break;
		}

		promise = callbackContext;
		banner = new AdView(ctx.cordova.getActivity());
		banner.setAdSize(realAdSize);
		banner.setAdUnitId(unitID);
		RelativeLayout.LayoutParams bannerLayoutStyle = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT
		);
		if (position.equals("top"))
		{
			bannerLayoutStyle.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		}
		else
		{
			bannerLayoutStyle.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		}
		
		bannerLayoutStyle.addRule(RelativeLayout.CENTER_HORIZONTAL);
		banner.setLayoutParams(bannerLayoutStyle);
		banner.loadAd(request);
		banner.setAdListener(this);
	}
	@Override
	public void onAdLoaded()
	{
		if (promise == null)
			return;

		promise.success("banner ad loaded");
		promise = null;
	}

	@Override
	public void onAdFailedToLoad(int errorCode)
	{
		if (promise == null)
			return;

		promise.error("banner ad failed to load");
		promise = null;
	}

	void show (RelativeLayout layout, CallbackContext callbackContext)
	{
		if (promise != null)
		{
			callbackContext.error("banner is busy");
			return;
		}

		if (parentLayout != null)
		{
			callbackContext.success("banner already shown");
			return;
		}
		if (banner.isLoading())
		{
			callbackContext.error("banner still loading");
			return;
		}
		parentLayout = layout;
		parentLayout.addView(banner);
		callbackContext.success("banner shown");
	}
	void hide(CallbackContext callbackContext)
	{
		if (promise != null)
		{
			callbackContext.error("banner is busy");
			return;
		}

		if (parentLayout == null)
		{
			callbackContext.success("banner not being shown");
			return;
		}
		parentLayout.removeView(banner);
		parentLayout = null;
		callbackContext.success("banner hidden");
	}
}
