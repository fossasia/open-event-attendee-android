
# Open Event Android

An events App to discover events happening around the world using the Open Event Platform. It allows user to view different events happening nearby and get detailed information of any event.

Master [![Build Status](https://travis-ci.org/fossasia/open-event-android.svg?branch=master)](https://travis-ci.org/fossasia/open-event-android)
Development [![Build Status](https://travis-ci.org/fossasia/open-event-android.svg?branch=development)](https://travis-ci.org/fossasia/open-event-android)
[![Gitter Room](https://img.shields.io/badge/gitter-join%20chat%20%E2%86%92-blue.svg)](https://gitter.im/fossasia/open-event-android)

---

### APK Branch

The following will help you understand file structure of apk-branch

 * **debug / release** These postfix keywords denote build type for the APKs, Debug apks are signed with default debug signing keys whereas for release keys we explicitly specify the keys to sign with and the debug flag will be turned off so that it cannot be debugged.
 * **fdroid / playstore** There are two variants for every generated APK, fdroid and playstore depending on what store they are targeting
 * **output.json** An output.json is generated for every build, this contains metadata the corresponding apk
 	- Please Note that :-
		> Each push to master branch automatically generates the APKs for different releases with open-event-master prefix similarly every push to development branch generates APKs with open-event-dev prefix
		


## For Testers: Testing the App
If you are a tester and want to test the app, you have two ways to do that:
1. **Installing APK on your device:** You can get debug APK as well as Release APK in apk branch of the repository. After each PR merge, both the APKs are automatically updated. So, just download the APK you want and install it on your device. The APKs will always be the latest one.

## Open Event Android Suggestions

- Suggestion form link: [Form](https://docs.google.com/forms/d/e/1FAIpQLSd7Y1T1xoXeYaAG_b6Tu1YYK-jZssoC5ltmQbkUX0kmDZaKYw/viewform)
- Suggestion responses link: [Sheet](https://docs.google.com/spreadsheets/d/1SzR75MBEVrTY1sDM3KAMm9wltiulDAp0QT5hv9eJkKM/edit#gid=1676755229)

## License

This project is currently licensed under the Apache License Version 2.0. A copy of [LICENSE](LICENSE) should be present along with the source code. To obtain the software under a different license, please contact FOSSASIA.
