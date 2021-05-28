package com.scirra;

import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdRequest;
import android.app.Activity;
import android.os.Bundle;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

public class VideoAdvert implements RewardedVideoAdListener
{
	private RewardedVideoAd videoInstance;
	private CallbackContext promise;
	private RewardItem reward;

	VideoAdvert (CordovaPlugin ctx, String unitID, AdRequest request, CallbackContext callbackContext)
	{
		promise = callbackContext;
		videoInstance = MobileAds.getRewardedVideoAdInstance(ctx.cordova.getActivity());
		videoInstance.setRewardedVideoAdListener(this);
		videoInstance.loadAd(unitID, request);
	}

	public void show (CallbackContext callbackContext)
	{
		if (promise != null)
		{
			callbackContext.error("video instance busy");
			return;
		}

		if (videoInstance.isLoaded())
		{
			promise = callbackContext;
			videoInstance.show();
		}
		else
		{
			callbackContext.error("video not loaded");
		}
	}

	@Override
	public void onRewarded(RewardItem item)
	{
		reward = item;
	}

	@Override
	public void onRewardedVideoAdOpened()
	{

	}

	public void onRewardedVideoCompleted()
	{

	}

	@Override
	public void onRewardedVideoAdLoaded()
	{
		if (promise == null)
			return;

		promise.success();
		promise = null;
	}

	@Override
	public void onRewardedVideoAdFailedToLoad(int errorCode)
	{
		if (promise == null)
			return;

		promise.error("failed to load");
		promise = null;
	}

	@Override
	public void onRewardedVideoStarted()
	{

	}

	@Override
	public void onRewardedVideoAdClosed() {
		if (promise == null)
			return;

		if (reward != null)
		{
			String type = reward.getType().replace("\"", "\\\"");
			int amount = reward.getAmount();

			promise.success(String.format("[\"%s\",\"%d\"]", type, amount));
		}
		else
		{
			promise.error("closed with no reward");
		}

		promise = null;
		reward = null;
	}

	@Override
	public void onRewardedVideoAdLeftApplication() {

	}

	void onResume(Activity ctx)
	{
		if (videoInstance != null)
		{
			videoInstance.resume(ctx);
		}
	}

	void onPause(Activity ctx)
	{
		if (videoInstance != null)
		{
			videoInstance.pause(ctx);
		}
	}

	void onDestroy(Activity ctx)
	{
		if (videoInstance != null)
		{
			videoInstance.destroy(ctx);
		}
	}
}
