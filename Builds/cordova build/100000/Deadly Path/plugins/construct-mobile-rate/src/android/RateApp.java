package com.scirra.rateapp;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;

public class RateApp extends CordovaPlugin {
	@Override
	public boolean execute(final String action, final JSONArray arguments, final CallbackContext callbackContext) throws JSONException
	{
		switch (action) {
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

	    try {
            appIdentifier = arguments.getString(0);
        }
	    catch (JSONException e) {
	        ctx.error("Unable to read arguments");
	        return;
        }

        cordova.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appIdentifier)));
	}
	
	void showRateDialog (JSONArray arguments, CallbackContext ctx)
	{
        String dialogText;

        String confirmButtonText;
        String cancelButtonText;

        String appIdentifier;

        try {
            dialogText = arguments.getString(0);

            confirmButtonText = arguments.getString(1);
            cancelButtonText = arguments.getString(2);

            appIdentifier = arguments.getString(3);
        }
        catch (JSONException e) {
            ctx.error("Unable to read arguments");
            return;
        }

        Activity activity = cordova.getActivity();
        final RateApp self = this;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(dialogText)
                .setPositiveButton(confirmButtonText, (dialog, id) -> {
                    JSONArray newArgs = new JSONArray();
                    newArgs.put(appIdentifier);
                    self.openStorePage(newArgs, ctx);
                })
                // no-op dialog self dismisses from any button press
                .setNegativeButton(cancelButtonText, (dialog, id) -> {});
        Dialog dlg = builder.create();
        dlg.show();
	}
}