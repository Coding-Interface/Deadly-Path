import GoogleMobileAds
import AdSupport
import Foundation
import CommonCrypto
#if canImport(AppTrackingTransparency)
import AppTrackingTransparency
#endif
import AdSupport

let GMAD_TEST_APPLICATION_ID = 	"ca-app-pub-3940256099942544~1458002511"

@objc(ConstructAd)
class ConstructAd : CDVPlugin
{
	var bannerAD: BannerAdvert?
	var interstitialAD: InterstitialAdvert?
	var videoAD: VideoAdvert?
    var userConsent: UserConsent?
    
    var deviceID: String = ""
    
    override init() {
    }
    
    func GetDeviceID () -> String
    {
        func MD5(_ string: String) -> String {
            let UTF8 = String.Encoding.utf8

            let context = UnsafeMutablePointer<CC_MD5_CTX>.allocate(capacity: 1)
            let string_length = string.lengthOfBytes(using: UTF8)
            let digest_length = Int(CC_MD5_DIGEST_LENGTH)
            var digest = Array<UInt8>(repeating: 0, count: digest_length)
            
            CC_MD5_Init(context)
            CC_MD5_Update(context, string, CC_LONG(string_length))
            CC_MD5_Final(&digest, context)
            context.deallocate()

            return digest.map { String(format: "%02hhx", $0 ) }.joined()
        }
        
        if (ASIdentifierManager.shared().isAdvertisingTrackingEnabled)
        {
            let adID: String = ASIdentifierManager.shared().advertisingIdentifier.uuidString
            return MD5(adID)
        }
        else
        {
            return ""
        }
    }
    
    func CreateGADRequest(debug: Bool) -> GADRequest
    {
        let request = GADRequest()
     
        if (debug)
        {
            request.testDevices = [ deviceID ]
        }
        
        if (userConsent?.status == PACConsentStatus.nonPersonalized)
        {
            let extras = GADExtras()
            extras.additionalParameters = ["npa": "1"]
            request.register(extras)
        }
        return request
    }

	@objc(CreateBannerAdvert:)
	func CreateBannerAdvert(command: CDVInvokedUrlCommand)
	{
		let promise = CommandPromise(id: command.callbackId, comDelegate: self.commandDelegate)

		let unitID = command.arguments[0] as? String ?? ""
		let adSize = command.arguments[1] as? String ?? ""
        let debug = command.arguments[2] as? String ?? ""
		let position = command.arguments[3] as? String ?? "bottom"

		if unitID == ""
		{
         promise.reject(msg: "Unit ID not specified")
			return
		}

		if adSize == ""
		{
			promise.reject(msg: "Ad size not specified")
			return
		}
        
        let request = CreateGADRequest(debug: debug == "true")
        
        self.bannerAD = BannerAdvert(request: request, viewController: viewController, prom: promise, id: unitID, adSize: adSize, position: position)
	}

	@objc(ShowBannerAdvert:)
	func ShowBannerAdvert(command: CDVInvokedUrlCommand)
	{
		let promise = CommandPromise(id: command.callbackId, comDelegate: self.commandDelegate)

		self.bannerAD?.show(view: viewController.view, prom: promise)
	}

	@objc(HideBannerAdvert:)
	func HideBannerAdvert(command: CDVInvokedUrlCommand)
	{
		let promise = CommandPromise(id: command.callbackId, comDelegate: self.commandDelegate)

		self.bannerAD?.hide(prom: promise)
	}

	@objc(CreateInterstitialAdvert:)
	func CreateInterstitialAdvert(command: CDVInvokedUrlCommand)
	{
		let promise = CommandPromise(id: command.callbackId, comDelegate: self.commandDelegate)
		let unitID = command.arguments[0] as? String ?? ""
        let debug = command.arguments[1] as? String ?? ""

		if unitID == ""
		{
			promise.reject(msg: "Unit ID not specified")
			return
		}
        
        let request = CreateGADRequest(debug: debug == "true")
        
        self.interstitialAD = InterstitialAdvert(request: request, prom: promise, id: unitID)
	}

	@objc(ShowInterstitialAdvert:)
	func ShowInterstitialAdvert(command: CDVInvokedUrlCommand)
	{
		let promise = CommandPromise(id: command.callbackId, comDelegate: self.commandDelegate)

		self.interstitialAD?.show(viewController: viewController, prom: promise)
	}

	@objc(CreateVideoAdvert:)
	func CreateVideoAdvert(command: CDVInvokedUrlCommand)
	{
		let promise = CommandPromise(id: command.callbackId, comDelegate: self.commandDelegate)
		let unitID = command.arguments[0] as? String ?? ""
        let debug = command.arguments[1] as? String ?? ""

		if unitID == ""
		{
			promise.reject(msg: "Unit ID not specified")
			return
		}
        
        let request = CreateGADRequest(debug: debug == "true")

        self.videoAD = VideoAdvert(request: request, prom: promise, id: unitID)
	}

	@objc(ShowVideoAdvert:)
	func ShowVideoAdvert(command: CDVInvokedUrlCommand)
	{
		let promise = CommandPromise(id: command.callbackId, comDelegate: self.commandDelegate)

		self.videoAD?.show(viewController: viewController, prom: promise)
	}

	@objc(SetUserPersonalisation:)
	func SetUserPersonalisation(command: CDVInvokedUrlCommand)
	{
		let promise = CommandPromise(id: command.callbackId, comDelegate: self.commandDelegate)
		let status = command.arguments[0] as? String ?? ""

		userConsent?.SetUserStatus(status: status, promise: promise)
	}

    @objc(SetMaxAdContentRating:)
    func SetMaxAdContentRating(command: CDVInvokedUrlCommand)
    {
        let promise = CommandPromise(id: command.callbackId, comDelegate: self.commandDelegate)
		let label = command.arguments[0] as? String ?? ""

        if label != "" {
            let config = GADMobileAds.sharedInstance().requestConfiguration

            switch label {
                case "G":
                    config.maxAdContentRating = GADMaxAdContentRating.general
                case "MA":
                    config.maxAdContentRating = GADMaxAdContentRating.matureAudience
                case "PG":
                    config.maxAdContentRating = GADMaxAdContentRating.parentalGuidance
                case "T":
                    config.maxAdContentRating = GADMaxAdContentRating.teen
                default:
                    promise.reject(msg: "invalid rating")
            }
        }

        promise.resolve(msg: "")
    }

    @objc(TagForChildDirectedTreatment:)
    func TagForChildDirectedTreatment(command: CDVInvokedUrlCommand)
    {
        let promise = CommandPromise(id: command.callbackId, comDelegate: self.commandDelegate)
		let label = command.arguments[0] as? Int ?? 0

        if label > -1 {
            let config = GADMobileAds.sharedInstance().requestConfiguration
            config.tag(forChildDirectedTreatment: label == 1)
        }

        promise.resolve(msg: "")
    }

    @objc(TagForUnderAgeOfConsent:)
    func TagForUnderAgeOfConsent(command: CDVInvokedUrlCommand)
    {
        let promise = CommandPromise(id: command.callbackId, comDelegate: self.commandDelegate)
		let label = command.arguments[0] as? Int ?? 0

        if label > -1 {
            let config = GADMobileAds.sharedInstance().requestConfiguration
            config.tagForUnderAge(ofConsent: label == 1)
        }
        
        promise.resolve(msg: "")
    }

	@objc(Configure:)
	func Configure(command: CDVInvokedUrlCommand) {
		let promise = CommandPromise(id: command.callbackId, comDelegate: self.commandDelegate)

		var id = command.arguments[0] as? String ?? ""
        let pubID = command.arguments[1] as? String ?? ""
        let privacyURL = command.arguments[2] as? String ?? ""
		let displayFree = (command.arguments[3] as? String ?? "") == "true"
        let showConsent = command.arguments[4] as? String ?? ""
        let debug = (command.arguments[5] as? String ?? "") == "true"
        let debugLocation = command.arguments[6] as? String ?? ""
        
		if id == ""
		{
			promise.reject(msg: "Application ID not specified")
			return
		}
        
        if pubID == ""
        {
            promise.reject(msg: "Publisher ID not specified")
            return
        }
        
        if privacyURL == ""
        {
            promise.reject(msg: "Privacy URL not specified")
            return
        }

        /* 
            WARN we need to keep a local copy here
            Originally completionHanlder directly referred to the class field
            but it turns out that the pointer could be invalidated
            initially this issue was missed because if we modified the value of
            the field in this method the pointer was not invalidated
         */
        let deviceID: String
        
        if debug {
            deviceID = GetDeviceID()
            self.deviceID = deviceID
            print(deviceID)
        }
        else {
            deviceID = ""
        }
        
        func completionHandler (_status: GADInitializationStatus) {
            userConsent = UserConsent()
            
            userConsent?.UpdateUserConsent(deviceID: deviceID, debugLocation: debugLocation, pubID: pubID, displayFreeOption: displayFree, privacyURLString: privacyURL, promise: promise, showConsent: showConsent, plugin: self)
        }
        
        GADMobileAds.sharedInstance().start(completionHandler: completionHandler)
	}
    
    @objc(RequestConsent:)
    func RequestConsent(command: CDVInvokedUrlCommand)
    {
        let promise = CommandPromise(id: command.callbackId, comDelegate: self.commandDelegate)
        self.userConsent?.ShowUserConsentForm(promise: promise, plugin: self, inEEA: true)
    }

    @objc(RequestIDFA:)
    func RequestIDFA(command: CDVInvokedUrlCommand)
    {
        let promise = CommandPromise(id: command.callbackId, comDelegate: self.commandDelegate)

#if canImport(AppTrackingTransparency)
        if #available(iOS 14, *) {
            ATTrackingManager.requestTrackingAuthorization(completionHandler: { status in
                promise.resolve(msg: (status == ATTrackingManager.AuthorizationStatus.authorized ? "authorized" : "denied"))
            })
        }
        else {
            promise.resolve(msg: "authorized")
        }
#else
        promise.resolve(msg: "authorized")
#endif
    }
}
