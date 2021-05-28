const child_process = require("child_process");
const path = require("path");
const fs = require("fs");

function podUpdate (ctx) {
    if (!ctx.opts.platforms.includes('ios'))
        return;

    return new Promise((resolve, reject) => {
        console.log("Running manual pod update");
        child_process.exec("pod update", { cwd: path.join(ctx.opts.projectRoot, "platforms/ios/") }, (err, stdout, stderr) => {
            if (err)
            {
                console.log("Pod update failed");
                if (stdout)
                    console.log(stdout);
                if (stderr)
                    console.log(stderr);
                reject();
            }
            else {
                resolve();
            }
        });
    });
}

function androidXUpgrade (ctx) {
    if (!ctx.opts.platforms.includes('android'))
        return;

    const enableAndroidX = "android.useAndroidX=true";
    const enableJetifier = "android.enableJetifier=true";
    const gradlePropertiesPath = "./platforms/android/gradle.properties";

    let gradleProperties = fs.readFileSync(gradlePropertiesPath, "utf8");

    if (gradleProperties)
    {
        const isAndroidXEnabled = gradleProperties.includes(enableAndroidX);
        const isJetifierEnabled = gradleProperties.includes(enableJetifier);

        if (isAndroidXEnabled && isJetifierEnabled)
            return;

        if (isAndroidXEnabled === false)
            gradleProperties += "\n" + enableAndroidX;

        if (isJetifierEnabled === false)
            gradleProperties += "\n" + enableJetifier;

        fs.writeFileSync(gradlePropertiesPath, gradleProperties);
    }
}

module.exports = function (ctx) {
    androidXUpgrade(ctx);
    return podUpdate(ctx);
};
