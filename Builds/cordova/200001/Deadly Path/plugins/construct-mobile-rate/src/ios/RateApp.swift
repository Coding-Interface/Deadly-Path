import StoreKit


@objc(RateApp)
class RateApp : CDVPlugin
{
    
    override init() {
    }
    
	@objc(Rate:)
	func Rate(command: CDVInvokedUrlCommand)
	{
		// ignore all params here, we don't need them on iOS
		if #available(iOS 10.3, *) {
			SKStoreReviewController.requestReview()
		}
	}

    @objc(Store:)
    func Store(command: CDVInvokedUrlCommand)
    {
        let appIdentifier = command.arguments[0] as? String ?? "";
		let urlString = "https://itunes.apple.com/app/id" + appIdentifier;

	    guard let writeReviewURL = URL(string: urlString)
        else { 
			let pluginResult = CDVPluginResult(
				status: CDVCommandStatus_ERROR,
				messageAs: "Invalid URL"
			)
			self.commandDelegate.send(pluginResult, callbackId: command.callbackId);
			return;
		}
    	UIApplication.shared.open(writeReviewURL, options: [:], completionHandler: nil);
    }
}
