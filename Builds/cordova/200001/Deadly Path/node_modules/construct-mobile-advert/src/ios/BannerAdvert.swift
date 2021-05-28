import GoogleMobileAds

class BannerAdvert: NSObject, GADBannerViewDelegate
{
	var banner: GADBannerView
	var promise: CommandPromise?
	var position: String

    init(request: GADRequest, viewController: UIViewController, prom: CommandPromise, id: String, adSize: String, position: String)
	{
        var realAdSize: GADAdSize

        switch (adSize)
        {
        case "portrait":
            realAdSize = kGADAdSizeSmartBannerPortrait;
        case "landscape":
            realAdSize = kGADAdSizeSmartBannerLandscape;
        case "standard":
            realAdSize = kGADAdSizeBanner;
        case "large":
            realAdSize = kGADAdSizeLargeBanner;
        case "medium":
            realAdSize = kGADAdSizeMediumRectangle;
        case "full":
            realAdSize = kGADAdSizeFullBanner;
        case "leaderboard":
            realAdSize = kGADAdSizeLeaderboard;
        default:
            realAdSize = kGADAdSizeSmartBannerPortrait;
        }

		self.position = position;
        banner = GADBannerView(adSize: realAdSize)
        promise = prom;
		
        super.init();
		
		banner.rootViewController = viewController
        banner.delegate = self
        banner.adUnitID = id
        banner.load(request)
	}

	func show(view: UIView, prom: CommandPromise)
	{
		if promise != nil
		{
			prom.reject(msg: "banner is busy");
			return;
		}
		
		banner.translatesAutoresizingMaskIntoConstraints = false
	  	view.addSubview(banner)
		
	  	if #available(iOS 11.0, *)
		{
	    	// In iOS 11, we need to constrain the view to the safe area.
			if position == "top" {
				positionBannerViewFullWidthAtTopOfSafeArea(view)
			}
			else {
				positionBannerViewFullWidthAtBottomOfSafeArea(view)
			}
	  	}
	  	else
		{
	    	// In lower iOS versions, safe area is not available so we use
	    	// bottom layout guide and view edges.
			if position == "top" {
				positionBannerViewFullWidthAtTopOfView(view)
			}
			else {
				positionBannerViewFullWidthAtBottomOfView(view)
			}
	  	}

		prom.resolve(msg: "Banner advert did show")
	}

	// MARK: - view positioning
	@available (iOS 11, *)
	func positionBannerViewFullWidthAtBottomOfSafeArea(_ view: UIView)
	{
	  	// Position the banner. Stick it to the bottom of the Safe Area.
	  	// Make it constrained to the edges of the safe area.
	  	let guide = view.safeAreaLayoutGuide
		
	  	NSLayoutConstraint.activate([
	    	guide.leftAnchor.constraint(equalTo: banner.leftAnchor),
	    	guide.rightAnchor.constraint(equalTo: banner.rightAnchor),
	    	guide.bottomAnchor.constraint(equalTo: banner.bottomAnchor)
	  	])
	}

	// MARK: - view positioning
	@available (iOS 11, *)
	func positionBannerViewFullWidthAtTopOfSafeArea(_ view: UIView)
	{
	  	// Position the banner. Stick it to the top of the Safe Area.
	  	// Make it constrained to the edges of the safe area.
	  	let guide = view.safeAreaLayoutGuide
		
	  	NSLayoutConstraint.activate([
	    	guide.leftAnchor.constraint(equalTo: banner.leftAnchor),
	    	guide.rightAnchor.constraint(equalTo: banner.rightAnchor),
	    	guide.topAnchor.constraint(equalTo: banner.topAnchor)
	  	])
	}

	func positionBannerViewFullWidthAtBottomOfView(_ view: UIView)
	{
		// NOTE ignore the below depreciation, this code block is particularly for iOS 10 and below
	  	view.addConstraint(NSLayoutConstraint(item: banner,
	                                        attribute: .leading,
	                                        relatedBy: .equal,
	                                        toItem: view,
	                                        attribute: .leading,
	                                        multiplier: 1,
	                                        constant: 0))
	  	view.addConstraint(NSLayoutConstraint(item: banner,
	                                        attribute: .trailing,
	                                        relatedBy: .equal,
	                                        toItem: view,
	                                        attribute: .trailing,
	                                        multiplier: 1,
	                                        constant: 0))
		view.addConstraint(NSLayoutConstraint(item: banner,
	                                        attribute: .bottom,
	                                        relatedBy: .equal,
	                                        toItem: banner.rootViewController!.bottomLayoutGuide,
	                                        attribute: .top,
	                                        multiplier: 1,
	                                        constant: 0))
	}

	func positionBannerViewFullWidthAtTopOfView(_ view: UIView)
	{
		// TODO this code needs some testing on a device...
		// NOTE ignore the below depreciation, this code block is particularly for iOS 10 and below
	  	view.addConstraint(NSLayoutConstraint(item: banner,
	                                        attribute: .leading,
	                                        relatedBy: .equal,
	                                        toItem: view,
	                                        attribute: .leading,
	                                        multiplier: 1,
	                                        constant: 0))
	  	view.addConstraint(NSLayoutConstraint(item: banner,
	                                        attribute: .trailing,
	                                        relatedBy: .equal,
	                                        toItem: view,
	                                        attribute: .trailing,
	                                        multiplier: 1,
	                                        constant: 0))
		view.addConstraint(NSLayoutConstraint(item: banner,
	                                        attribute: .top,
	                                        relatedBy: .equal,
	                                        toItem: banner.rootViewController!.topLayoutGuide ,
	                                        attribute: .top,
	                                        multiplier: 1,
	                                        constant: 0))
	}

	func hide(prom: CommandPromise)
	{
		if promise != nil
		{
			prom.reject(msg: "banner is busy");
			return;
		}

		banner.removeFromSuperview()
		prom.resolve(msg: "Banner Advert hidden")
	}

	// EVENTS

	/// Tells the delegate an ad request loaded an ad.
	func adViewDidReceiveAd(_ bannerView: GADBannerView)
	{
		if let mPromise = promise
		{
			mPromise.resolve(msg: "Banner advert did load")
			promise = nil
		}
	}

	/// Tells the delegate an ad request failed.
	func adView(_ bannerView: GADBannerView, didFailToReceiveAdWithError error: GADRequestError)
	{
        print(error.localizedDescription)
		if let mPromise = promise
		{
			mPromise.reject(msg: "Banner advert failed to load")
			promise = nil
		}
	}
}
