import GoogleMobileAds

func EscapeString(_ str: String) -> String
{
	return str.replacingOccurrences(of: "\"", with: "\\\"")
}

class VideoAdvert: NSObject, GADRewardBasedVideoAdDelegate
{
	var promise: CommandPromise?
	var reward: GADAdReward?

    init(request: GADRequest, prom: CommandPromise, id: String)
	{
		promise = prom
        super.init()
		GADRewardBasedVideoAd.sharedInstance().delegate = self
		GADRewardBasedVideoAd.sharedInstance().load(GADRequest(), withAdUnitID: id)
	}

	func show(viewController: UIViewController, prom: CommandPromise)
	{
		if promise != nil
		{
			prom.reject(msg: "video is busy");
			return;
		}

		promise = prom
		GADRewardBasedVideoAd.sharedInstance().present(fromRootViewController: viewController)
	}

	// EVENTS
	func rewardBasedVideoAdDidReceive(_ rewardBasedVideoAd:GADRewardBasedVideoAd)
	{
		if let mPromise = promise
		{
			mPromise.resolve(msg: "Video advert did load")
			promise = nil
		}
	}

	func rewardBasedVideoAd(_ rewardBasedVideoAd: GADRewardBasedVideoAd, didFailToLoadWithError error: Error)
	{
		if let mPromise = promise
		{
			mPromise.reject(msg: "Video ad failed to load")
			promise = nil
		}
	}

	func rewardBasedVideoAd(_ rewardBasedVideoAd: GADRewardBasedVideoAd, didRewardUserWith reward: GADAdReward)
	{
		self.reward = reward
	}

	func rewardBasedVideoAdDidClose(_ rewardBasedVideoAd: GADRewardBasedVideoAd) {
		if let mPromise = promise
		{
			if let mReward = reward
			{
				mPromise.resolve(msg: "[\"\(EscapeString(mReward.type))\",\"\(mReward.amount)\"]")
			}
			else
			{
				mPromise.reject(msg: "close with no reward")
			}

			promise = nil
			reward = nil
		}
	}
}
