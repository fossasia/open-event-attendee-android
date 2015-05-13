# re:publica 2015 app based on FOSSASIA companion-android

Advanced native Android schedule browser application for the [re:publica](https://re-publica.de/) conference in Berlin.
This is build upon the [FOSDEM Companion For android](https://github.com/cbeyls/fosdem-companion-android). It uses loaders and fragments extensively and is backward compatible up to Android 2.1 thanks to the support library.

To get more information and install the app, look at the [Google Play Store](https://play.google.com/store/apps/details?id=org.re.publica) page.

[![Join the chat at https://gitter.im/fossasia/rp15](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/fossasia/rp15?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
rp15 Companion App

## How to build

All dependencies are defined in ```app/build.gradle```. Import the project in Android Studio or use Gradle in command line:

```
./gradlew assembleRelease
```

The result apk file will be placed in ```app/build/outputs/apk/```.   

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Used libraries

* [Android Support Library](http://developer.android.com/tools/support-library/) by The Android Open Source Project
* [ViewPagerIndicator](http://viewpagerindicator.com/) by Jake Wharton
* [PhotoView](https://github.com/chrisbanes/PhotoView) by Chris Banes
* [Volley Library](https://android.googlesource.com/platform/frameworks/volley)

## Contributors

* Christophe Beyls
* Abhishek Batra
* Manan Wason
* Pratik Todi
* Mario Behling
* Tymon Radzik
* Arnav Gupta
* Rafal Kowalski
* Duke Leto
