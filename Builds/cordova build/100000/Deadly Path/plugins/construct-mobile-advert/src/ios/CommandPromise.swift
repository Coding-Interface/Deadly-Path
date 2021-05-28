class CommandPromise: NSObject {
    var callback: String
    var delegate: CDVCommandDelegate
    init (id: String, comDelegate: CDVCommandDelegate)
	{
        delegate = comDelegate
		callback = id
		super.init()
	}

	func resolve (msg: String)
	{
		let pluginResult = CDVPluginResult(
			status: CDVCommandStatus_OK,
			messageAs: msg
		)
		delegate.send(
			pluginResult,
			callbackId: callback
		)
	}

	func reject (msg: String)
	{
		let pluginResult = CDVPluginResult(
			status: CDVCommandStatus_ERROR,
			messageAs: msg
		)
		delegate.send(
			pluginResult,
			callbackId: callback
		)
	}
}
