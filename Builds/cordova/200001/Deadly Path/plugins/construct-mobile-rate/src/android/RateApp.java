package com.scirra.rateapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

public class RateApp extends CordovaPlugin
{
	// A few notes on the In App Review API:
		// 1) To trigger the UI the application needs to be published and installed
		// in a device through the Google Play Store
		
		// 2) A test track is enough, closed or internal testing will work.
		// Publishing a release to internal testing is much faster but in order to work there must be at
		// least one release to a closed or open track (which can take a few days).
		// The application does not need to be in production for testing the review flow.
		
		// 3) Tester emails should be regular gmail accounts.
		// G Suit accounts won't trigger the review UI for some reason. This might change in the future.
		
		// 4) Each account viewing the application can only leave one review.
		
		// 5) The UI is not guaranteed to show up. Ej. There is already a review, to allow for another review:
			// i) delete the application from the device
			// ii) clear the application cache of the Play Store app
	
	// Minimum API level needed to use the In App Review API
	private static int MIN_API_LEVEL = 21;
	
	// The package name of the Google Play Store
	private static String PLAY_STORE_PACKAGE_NAME = "com.android.vending";
	
	private ReviewManager reviewManager = null;
	private Task<ReviewInfo> reviewRequest = null;
	private Task<Void> reviewFlow = null;
	private boolean isInAppReviewFlowInProgress = false;
	
	@Override
	public boolean execute (final String action, final JSONArray arguments, final CallbackContext callbackContext) throws JSONException
	{
		switch (action)
		{
			case "Rate":
				this.showRateDialog(arguments, callbackContext);
				break;
			case "Store":
				this.openStorePage(arguments, callbackContext);
				break;
			default:
				callbackContext.error("invalid method");
				return false;
		}
		return true;
	}
	
	void openStorePage (JSONArray arguments, CallbackContext ctx)
	{
		String appIdentifier;
		
		try
		{
			appIdentifier = arguments.getString(0);
		}
		catch (JSONException e)
		{
			ctx.error("Unable to read arguments");
			return;
		}
		
		cordova.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appIdentifier)));
	}
	
	void showRateDialog (JSONArray arguments, CallbackContext ctx)
	{
		Activity activity = cordova.getActivity();
		
		if (this.hasMinimumSupportedAPILevel() && this.hasGooglePlayStorePackage(activity))
		{
			// In App Review API is available!
			
			// If there is an in app review flow in progress, skip the rest to avoid spamming
			if (isInAppReviewFlowInProgress)
				return;
			
			isInAppReviewFlowInProgress = true;
			
			// NOTE: FakeReviewManager is just meant for local and unit testing.
			// It returns mock objects, but it doesn't trigger the UI.
			// reviewManager = new FakeReviewManager(activity);
			
			// Avoid creating these objects again if they already exist
			if (reviewManager == null)
				reviewManager = ReviewManagerFactory.create(activity);
			
			if (reviewRequest == null)
				reviewRequest = reviewManager.requestReviewFlow();
			
			this.useSupportedReviewFlow(ctx, activity, arguments, reviewRequest);
		}
		else
		{
			// In App Review API is not available, fallback to exiting the application for rating
			this.startFallbackReviewFlow(ctx, activity, arguments);
		}
	}
	
	void useSupportedReviewFlow(CallbackContext ctx, Activity activity, JSONArray arguments, Task<ReviewInfo> reviewRequest)
	{
		if (!reviewRequest.isComplete())
		{
			// The request for the review information has not completed yet, wait for it
			reviewRequest.addOnCompleteListener((request) ->
			{
				// When the request is complete, call this method again so one of the other two paths is executed
				this.useSupportedReviewFlow(ctx, activity, arguments, reviewRequest);
			});
		}
		else if (reviewRequest.isSuccessful())
		{
			// The review request was completed successfully, start the In App Review flow
			this.startInAppReviewFlow(activity, reviewRequest.getResult());
		}
		else
		{
			// The review request was completed but it was not successful, use the fallback review flow
			this.startFallbackReviewFlow(ctx, activity, arguments);
		}
	}
	
	void startInAppReviewFlow(Activity activity, ReviewInfo reviewInfo)
	{
		// After launching the In App Review flow there is nothing else for us to do.
		// When the user is done the UI is hidden and control returns to the application
		reviewFlow = reviewManager.launchReviewFlow(activity, reviewInfo);
		
		if (reviewFlow.isComplete() && reviewFlow.isSuccessful())
		{
			// Handle the case the review flow is completed instantly and is successful
			// Toggle the flag indicating the review flow is over so a new call might be handled
			isInAppReviewFlowInProgress = false;
			reviewFlow = null;
		}
		else if (reviewFlow.isComplete() && !reviewFlow.isSuccessful())
		{
			// Handle the case the review flow is completed instantly and is not successful
			// Toggle the flag indicating the review flow is over so a new call might be handled
			isInAppReviewFlowInProgress = false;
			reviewFlow = null;
		}
		else
		{
			// What ever happens toggle the flag indicating the review flow is over so a new call might be handled
			reviewFlow.addOnCompleteListener((result) ->
			{
				// TODO: Consider having a callback when the action is completed
				// if something similar is ever implemented in iOS
				
				isInAppReviewFlowInProgress = false;
				reviewFlow = null;
			});
		}
	}
	
	void startFallbackReviewFlow(CallbackContext ctx, Activity activity, JSONArray arguments)
	{
		String dialogText;
		String confirmButtonText;
		String cancelButtonText;
		String appIdentifier;
		
		final RateApp self = this;
		
		try
		{
			dialogText = arguments.getString(0);
			
			confirmButtonText = arguments.getString(1);
			cancelButtonText = arguments.getString(2);
			
			appIdentifier = arguments.getString(3);
		}
		catch (JSONException e)
		{
			ctx.error("Unable to read arguments");
			return;
		}
		
		this.showAlert(
			dialogText,
			confirmButtonText,
			cancelButtonText,
			(dialog, id) ->
			{
				JSONArray newArgs = new JSONArray();
				newArgs.put(appIdentifier);
				self.openStorePage(newArgs, ctx);
				
				isInAppReviewFlowInProgress = false;
			},
			(dialog, id) ->
			{
				isInAppReviewFlowInProgress = false;
			}
		);
	}
	
	boolean hasMinimumSupportedAPILevel ()
	{
		return Build.VERSION.SDK_INT >= RateApp.MIN_API_LEVEL;
	}
	
	boolean hasGooglePlayStorePackage (Activity activity)
	{
		try
		{
			ApplicationInfo info = activity.getPackageManager().getApplicationInfo(RateApp.PLAY_STORE_PACKAGE_NAME, 0 );
			
			return !!info.packageName.equals(RateApp.PLAY_STORE_PACKAGE_NAME);
		}
		catch(PackageManager.NameNotFoundException e)
		{
			return false;
		}
	}
	
	void showAlert(String dialogText, String confirmText, String cancelText, DialogInterface.OnClickListener positiveAction, DialogInterface.OnClickListener negativeAction)
	{
		Activity activity = cordova.getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		builder.setMessage(dialogText);
		builder.setPositiveButton(confirmText, positiveAction);
		builder.setNegativeButton(cancelText, negativeAction);
		
		Dialog dlg = builder.create();
		
		dlg.show();
	}
}