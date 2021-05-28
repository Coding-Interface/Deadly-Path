cordova.define("construct-mobile-rate.RateApp", function(require, exports, module) {
const exec = require('cordova/exec');
const CLASS_NAME = "RateApp";

function RequireString (str) {
    if (typeof str !== "string")
        throw new Error("Expected String");
}

exports.Rate = function Rate (dialogText, confirmButtonText, cancelButtonText, appIdentifier)
{
    RequireString(dialogText);
    RequireString(confirmButtonText);
    RequireString(cancelButtonText);
    RequireString(appIdentifier);

    const METHOD_NAME = "Rate";
    const args = [dialogText, confirmButtonText, cancelButtonText, appIdentifier];
    
	return new Promise ((resolve, reject) => {
        exec(resolve, reject, CLASS_NAME, METHOD_NAME, args);
    });
}

exports.Store = function Store (appIdentifier)
{
    RequireString(appIdentifier);

	const METHOD_NAME = "Store";
	const args = [appIdentifier];
	
    return new Promise ((resolve, reject) => {
        exec(resolve, reject, CLASS_NAME, METHOD_NAME, args);
    });
}
});
