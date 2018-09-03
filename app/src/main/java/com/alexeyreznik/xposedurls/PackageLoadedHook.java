package com.alexeyreznik.xposedurls;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class PackageLoadedHook implements IXposedHookLoadPackage {

    /**
     * Callback method executed by Xposed framework when an application package is loaded.
     * Inserts hooks into the two methods of OkHTTP RealCall.java class: execute() and enqueue(callback)
     *
     * @param lpparam: parameters of the loaded package
     * @see <a href="https://github.com/square/okhttp/blob/master/okhttp/src/main/java/okhttp3/RealCall.java">RealCall.java</a>
     */
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {

        //Check if application contains a flag file indicating that the app should be scanned for URLs
        if (FileUtils.shouldScan(lpparam.packageName)) {

            XposedBridge.log("Scanning " + lpparam.packageName + " for OkHTTP URLs");

            try {

                //Hook into okhttp3.RealCall.execute() method of OkHTTP SDK
                findAndHookMethod(
                        "okhttp3.RealCall",
                        lpparam.classLoader,
                        "execute",
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                            }

                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {

                                String urlString = extractUrl(param);
                                FileUtils.saveStringToResultsFile(lpparam.packageName, urlString);
                                XposedBridge.log("OkHTTP execute() url: " + urlString);
                            }
                        });

                //Hook into okhttp3.RealCall.enqueue(callback) method of OkHTTP SDK
                findAndHookMethod(
                        "okhttp3.RealCall",
                        lpparam.classLoader,
                        "enqueue",
                        "okhttp3.Callback",
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                            }

                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                                String urlString = extractUrl(param);
                                FileUtils.saveStringToResultsFile(lpparam.packageName, urlString);
                                XposedBridge.log("OkHTTP enqueue() url: " + urlString);
                            }
                        });
            } catch (Exception ex) {

                XposedBridge.log("Failed to scan " + lpparam.packageName + " for OkHTTP URLs");
                XposedBridge.log(ex.getMessage());
            }
        }
    }

    /**
     * Extracts url string value from OkHTTP RealCall object
     *
     * @param param: parameters passed to a hooked method
     * @return url string value extracted from OkHTTP RealCall object
     * @see <a href="https://github.com/square/okhttp/blob/master/okhttp/src/main/java/okhttp3/RealCall.java">RealCall.java</a>
     */
    private static String extractUrl(XC_MethodHook.MethodHookParam param) {

        //Get RealCall.this.originalRequest.url object
        Object request = getObjectField(param.thisObject, "originalRequest");
        Object httpUrl = getObjectField(request, "url");

        //Invoke HttpUrl.toString() method
        return (String) callMethod(httpUrl, "toString");
    }
}
