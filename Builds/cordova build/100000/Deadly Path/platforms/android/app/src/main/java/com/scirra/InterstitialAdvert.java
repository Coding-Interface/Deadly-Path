package com.scirra;

import android.os.Bundle;

import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

public class InterstitialAdvert extends AdListener
{
	private InterstitialAd interstitial;
	private CallbackContext promise;

	InterstitialAdvert(CordovaPlugin ctx, String unitID, AdRequest request, CallbackContext callbackContext)
	{
		promise = callbackContext;
		interstitial = new InterstitialAd(ctx.cordova.getActivity());
		interstitial.setAdUnitId(unitID);
		interstitial.setAdListener(this);
		interstitial.loadAd(request);
	}

	void show(CallbackContext callbackContext)
	{
		if (promise != null)
		{
			callbackContext.error("interstitial is busy");
			return;
		}

		if (interstitial.isLoaded())
		{
			promise = callbackContext;
			interstitial.show();
		}
		else
		{
			callbackContext.error("interstitial not loaded");
		}
	}

	@Override
	public void onAdLoaded()
	{
		if (promise == null)
			return;

		promise.success("interstitial did load");
		promise = null;
	}

	@Override
	public void onAdFailedToLoad(int errorCode)
	{
		if (promise == null)
			return;

		promise.error("interstitial failed to load");
		promise = null;
	}

	@Override
	public void onAdClosed()
	{
		if (promise == null)
			return;

		promise.success("interstitial did hide");
		promise = null;
	}
}
