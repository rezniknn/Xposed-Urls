# Xposed module that intercepts outgoing HTTP(S) requests made with OkHttp3  SDK by the application and saves URLs to urls.txt file in the private app directory.

## Prerequisites:

1. Rooted Android 6.0 or 6.1 device
2. Xposed <b>v.76</b> framework installed on the device. Link: https://forum.xda-developers.com/showthread.php?t=3034811
3. XposedInstaller.apk installed on the device. Link: https://forum.xda-developers.com/attachment.php?attachmentid=4393082&d=1516301692

## Installation:

1. Build this project with gradle and run as an Android application (Launch: Nothing in Run Configuration)
2. Open XposedInstaller app and enable <b>Xposed Urls</b> module
3. Connect to rooted device via adb and create a file named <b>DT_log_urls</b> in the private directory (`/data/data/<package name>/`) of the application that you want to monitor.
4. Reboot the device, run the application you want to monitor

Example:  
`$adb shell`  
`$su`  
`$touch /data/data/org.wikipedia/DT_log_urls`

## Results

List of the unique URLs that the application has connected to via OkHttp3 SDK can be found in application's private directory (`/data/data/<package name>/`) in a file named <b>urls.txt</b>

Example:  
`$adb shell`  
`$su`  
`$cat /data/data/org.wikipedia/urls.txt`