import GoogleMobileAds

class InterstitialAdvert: NSObject, GADInterstitialDelegate
{
	var interstitial: GADInterstitial
	var promise: CommandPromise?

    init(request: GADRequest, prom: CommandPromise, id: String)
	{
		promise = prom
		interstitial = GADInterstitial(adUnitID: id)
        super.init()
		interstitial.delegate = self
    	interstitial.load(request)
	}

	func show(viewController: UIViewController, prom: CommandPromise)
	{
		if promise != nil
		{
			prom.reject(msg: "interstitial is busy");
			return;
		}

		promise = prom
		interstitial.present(fromRootViewController: viewController)
	}

	// EVENTS

	// ad has loaded
	func interstitialDidReceiveAd(_ ad: GADInterstitial)
	{
		if let mPromise = promise
		{
			mPromise.resolve(msg: "Interstitial did load")
			promise = nil
		}
	}

	// ad failed to load
	func interstitial(_ ad: GADInterstitial, didFailToReceiveAdWithError error: GADRequestError)
	{
		if let mPromise = promise
		{
			mPromise.reject(msg: error.localizedDescription)
			promise = nil
		}
	}

	// ad has been hidden
	func interstitialDidDismissScreen(_ ad: GADInterstitial)
	{
		if let mPromise = promise
		{
			mPromise.resolve(msg: "Interstitial did hide")
			promise = nil
		}
	}
}
