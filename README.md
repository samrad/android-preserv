android-preserv (InputPeers)
===============

Android application to get the location, create secret, generate Shamir's share, and send the share via SSL socket (Java)

####  InputPeers
InputPeer is a Gradle-based project written purely in Android/Java. The following libraries are required:
 
 - [msgpack-0.6.8] - Binary serialization format
 - [sepia-0.9.1] - Java library for secure multiparty-computation (MPC)
 - [slf4j-android-1.5.8] - SLF4J logging framework on Android
 - [android-maps-utils-0.3] - Google Maps Android API utility library
 - [Google Play Services] - Provides features and functionality through APIs which integrates with Google services.
 
The minimum SDK version is set to 15 which according to [Android Dashboard] will support 87.9% of the Android devices.

##### Description
The main part of the task is done in a service called `PulseService`. It's invoked periodically by `AlarmManager`, waits for three locations from Google Play Service Location API, uses the most accurate one and decides whether the location is inside any of the polygons. If so, it then creates the share and sends it out to PrivacyPeers using `ShamirGPS` class. `ShamirGPS` uses the public key, `"cert.pem"`, to secure the connection.

The service execution interval and PrivacyPeers' IPs and Ports are configurable via Setting:

![Settings][img-setting]

## Screenshots
[img-main-activity]
[img-setting-full]

**TODO**:
 - [ ] There is also another option for POI opt-out in settings to let the user decide which POI (polygon) he/she would like to be opted-out but it lacks the functionality.

[msgpack-0.6.8]:http://repo1.maven.org/maven2/org/msgpack/msgpack/0.6.8/msgpack-0.6.8.jar
[sepia-0.9.1]:http://sepia.ee.ethz.ch/download/v0.9.1/sepia.jar
[slf4j-android-1.5.8]:https://code.google.com/p/osmdroid/source/browse/trunk/OpenStreetMapViewer/lib/slf4j-android-1.5.8.jar?r=225
[android-maps-utils-0.3]:https://github.com/googlemaps/android-maps-utils
[Android Dashboard]:https://developer.android.com/about/dashboards/index.html
[Google Play Services]:https://developer.android.com/google/play-services/index.html
[img-setting]:https://dl.dropboxusercontent.com/u/169649705/Henrik/screenshot-settings.png  "Settings activity"
[img-main-activity]:https://dl.dropboxusercontent.com/u/169649705/Henrik/screenshot-main.png "Main activity"
[img-setting-full]:https://dl.dropboxusercontent.com/u/169649705/Henrik/screenshot-settings-full.png "Settings activity"
