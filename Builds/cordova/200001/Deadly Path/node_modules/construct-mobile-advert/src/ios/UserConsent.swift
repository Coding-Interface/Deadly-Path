class UserConsent: NSObject {
    var status = PACConsentStatus.unknown
    var privacyURL: String?
    var allowFreeOption = false
    
    func UpdateUserConsent(deviceID: String, debugLocation: String, pubID: String, displayFreeOption: Bool, privacyURLString: String, promise: CommandPromise, showConsent: String, plugin: CDVPlugin)
    {
        privacyURL = privacyURLString 
        allowFreeOption = displayFreeOption

        let showEverywhere = showConsent == "always"
        SetDebugLocation(deviceID: deviceID, debugLocation: debugLocation);

        PACConsentInformation.sharedInstance.requestConsentInfoUpdate(
            forPublisherIdentifiers: [ pubID ])
            {(_ error: Error?) -> Void in

            if let error = error
            {
                
                self.status = PACConsentStatus.unknown
                promise.reject(msg: error.localizedDescription)
            }
            else
            {
                let inEEA = PACConsentInformation.sharedInstance.isRequestLocationInEEAOrUnknown;

                self.status = PACConsentInformation.sharedInstance.consentStatus

                // if the user is outside of the EEA then consent is not required
                if showEverywhere == false && inEEA == false
                {
                    self.status = PACConsentStatus.personalized
                }
                
                if self.status == PACConsentStatus.personalized
                {
                    self.SendStatus(promise: promise, status: "PERSONALIZED", inEEA: inEEA);
                }
                else if self.status == PACConsentStatus.nonPersonalized
                {
                    self.SendStatus(promise: promise, status: "NON_PERSONALIZED", inEEA: inEEA);
                }
                else if (showConsent == "never")
                {
                    self.SendStatus(promise: promise, status: "UNKNOWN", inEEA: inEEA);
                }
                else
                {
                    self.ShowUserConsentForm(promise: promise, plugin: plugin, inEEA: inEEA)
                }
            }
        }
    }

    func SendStatus (promise: CommandPromise, status: String, inEEA: Bool)
    {
        promise.resolve(msg: status + "_" + (inEEA ? "true" : "false"));
    }

    func SetUserStatus (status: String, promise: CommandPromise)
    {
        if (status == "PERSONALIZED")
        {
            self.status = PACConsentStatus.personalized;
        }
        else if (status == "NON_PERSONALIZED")
        {
            self.status = PACConsentStatus.nonPersonalized;
        }
        else if (status == "AD_FREE" || status == "UNKNOWN")
        {
            self.status = PACConsentStatus.unknown;
            promise.resolve(msg: "UNKNOWN");
            return;
        }
        else
        {
            self.status = PACConsentStatus.unknown;
            promise.reject(msg: "invalid status type");
            return;
        }
        
        promise.resolve(msg: status);
    }

    func SetDebugLocation (deviceID: String, debugLocation: String)
    {
        if (deviceID != "")
        {
            PACConsentInformation.sharedInstance.debugIdentifiers = [ deviceID ]
        
            if ( debugLocation == "EEA")
            {
                PACConsentInformation.sharedInstance.debugGeography = PACDebugGeography.EEA
            }
            else if ( debugLocation == "NOT_EEA")
            {
                PACConsentInformation.sharedInstance.debugGeography = PACDebugGeography.notEEA
            }
        }
    }
    
    func ShowUserConsentForm(promise: CommandPromise, plugin: CDVPlugin, inEEA: Bool)
    {
        let url = URL(string: privacyURL!)

        guard let form = PACConsentForm(applicationPrivacyPolicyURL: url!) else {
            promise.reject(msg: "Invalid privacy URL")
            return
        }
        form.shouldOfferPersonalizedAds = true
        form.shouldOfferNonPersonalizedAds = true
        form.shouldOfferAdFree = allowFreeOption
        form.load { (_ error: Error?) -> Void in

            if let error = error {
                promise.reject(msg: error.localizedDescription)
            } else {
                form.present(from: plugin.viewController) { (error, userPrefersAdFree) in

                    if let error = error {
                        promise.reject(msg: error.localizedDescription)
                    } else if userPrefersAdFree {
                        self.SendStatus(promise: promise, status: "UNKNOWN", inEEA: inEEA);
                    } else {
                        // Check the user's consent choice.
                        self.status = PACConsentInformation.sharedInstance.consentStatus

                        if (self.status == PACConsentStatus.personalized)
                        {
                            self.SendStatus(promise: promise, status: "PERSONALIZED", inEEA: inEEA);
                        }
                        else if (self.status == PACConsentStatus.nonPersonalized)
                        {
                            self.SendStatus(promise: promise, status: "NON_PERSONALIZED", inEEA: inEEA);
                        }
                        else
                        {
                            self.SendStatus(promise: promise, status: "UNKNOWN", inEEA: inEEA);
                        }
                    }
                }
            }
        }

    }
}
