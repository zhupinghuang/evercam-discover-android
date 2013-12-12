# Evercam Connect

Evercam Connect is an Android app to find local camera devices, get all possible details for each camera, and make it easier to add to Evercam.

## Features

* Porform IP scan, port scan, Bonjour and UPnP Discovery
* Support both WiFi and Ethernet scanning on Android device
* Easy approach of camera default username & password, webpage, thumnail, RTSP stream
* UPnP portforwarding on UPnP enabld routers
* Fast access to router/network infomation
* Sign in with Google+ account
* Edit functions to help complete camera details

## Todo

* Sign in with Evercam
* Queries using Evercam API

## Build

    # Checkout from Git
    git clone https://github.com/evercam/android.connect.git

### With Eclipse

Import project into Eclipse and add google service dependency:

1. `Eclipse > Window > Android SDK Manager` or run `android` from the command line. 
2. Scroll to the bottom of the package list and select `Extras > Google Play services`. The package is downloaded to your computer and installed in your SDK environment at <android-sdk-folder>/extras/google/google_play_services.
3. `File > Import > Android > Existing Android Code Into Workspace` and click Next. 
4. Select `Browse....` Enter <android-sdk-folder>/extras/google/google_play_services/libproject.

## Published App
[![Google Play](http://developer.android.com/images/brand/en_generic_rgb_wo_45.png)](https://play.google.com/store/apps/details?id=io.evercam.connect&hl=en)
