# Quran Karim

An Android Quran reader developed and maintained by Abdulkerim Abdulmejid.

For support, contact Abdulkerim Abdulmejid at **+251 911 264 314**.

The selected app language also selects the Quran translation when available (Amharic or English; Arabic UI uses English translation). Quran text and translations download automatically while the app is open on a validated internet connection, including Wi-Fi and mobile data. Yasser Al-Dosari recitations are not downloaded automatically; choose the download action in reader settings to queue them over Wi-Fi. Tafsir can be saved offline from its verse dialog. Audio controls and verse highlighting follow playback, and tapping a verse starts recitation from that verse. Bookmarks save the current verse and reopen its location.

Before publishing, verify that you have distribution rights for all included Quran translations, recitations, images, fonts, and other assets. Retain any notices required by their licenses; do not present third-party material as your own.

## Run locally

1. Open this project in Android Studio and allow Gradle sync to finish.
2. Run the `app` configuration on an emulator or device.

## Supported Android devices

The app does not require a camera, GPS, telephony, or other optional hardware, and its launcher activity is resizable. It is intended for Android phones, tablets, foldables, and Chromebook devices that support Android apps, including portrait, landscape, and resizable-window use. The current minimum Android version is Android 7.0 (API 24); devices below that version cannot install this build.

“All devices” cannot literally be guaranteed: availability also depends on a device's Android version, Play certification, country, account, and Play Console device-catalog exclusions. This is a touch-first Quran reader, not a TV, Wear OS, or Android Auto app. Those form factors need dedicated layouts and launcher/integration work before they can be advertised as supported. Before production, inspect Play Console's **Device catalog** and test representative compact phones, large phones, tablets, foldables, and Chromebook windows in both orientations. Report any exclusions or layout problems and fix them before rollout.

## Release security and signing

The app disables cleartext network traffic and Android backup, and exposes only its launcher activity. These protections reduce risk but cannot make an app immune to every attack. Keep Android Studio, the Android Gradle Plugin, and dependencies up to date; test releases on real devices; and never store passwords, private keys, or privileged service credentials in the app.

## Sharing an installable APK

This project builds one APK containing native libraries for 32-bit and 64-bit ARM and x86 devices; it does not build an ABI-specific APK. To share an installable release, configure the protected release-signing variables below and run `./gradlew :app:assembleRelease`. Share `app/build/outputs/apk/release/app-release.apk`, not the `.aab` file. An App Bundle must be uploaded to Google Play; it cannot be installed directly by tapping the file.

Keep using the same release keystore for every update. Android will reject an update signed with a different key, even if the package name is identical. If a recipient has a build signed with another key, they must uninstall that build before installing this one; uninstalling removes its local app data. This app requires Android 7.0 (API 24) or newer, so devices running older Android versions remain unsupported.

Before publishing, confirm the permanent application ID in `app/build.gradle.kts`. It is currently `com.abdulkerimabdulmejid.qurankarim`; check that it is available in Play Console. Do not change it after creating the Play listing.

Use Google Play App Signing and keep the upload keystore outside the project directory. Back up the keystore and its passwords separately in a secure location. Do not commit the keystore or passwords. Configure the following environment variables in a protected local build environment or your CI provider's encrypted secrets:

| Variable | Value |
| --- | --- |
| `KEYSTORE_PATH` | Absolute path to the upload keystore |
| `STORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Alias in the keystore |
| `KEY_PASSWORD` | Key password |

Create an upload key once, outside the project directory. `keytool` prompts for passwords; do not put passwords directly in the command:

```bash
mkdir -p "$HOME/secure-keys"
chmod 700 "$HOME/secure-keys"
keytool -genkeypair -v \
  -keystore "$HOME/secure-keys/quran-karim-upload.jks" \
  -alias upload \
  -keyalg RSA -keysize 2048 -validity 10000
chmod 600 "$HOME/secure-keys/quran-karim-upload.jks"
```

Keep a secure backup of the keystore and passwords separately. To build locally, set `KEYSTORE_PATH`, `KEY_ALIAS`, `STORE_PASSWORD`, and `KEY_PASSWORD` in a protected environment that launches Gradle. Enter passwords using a secret manager or hidden prompts; do not put secrets into project files, screenshots, shared logs, or shell history. If using CI, store them as encrypted secrets and expose them only to the release job.

Build the Android App Bundle from the project root:

```bash
./gradlew :app:bundleRelease
```

The bundle is produced at `app/build/outputs/bundle/release/app-release.aab`. If the upload key was ever shared or committed, request an upload-key reset in Play Console before publishing.

## Detailed Google Play publishing guide

1. **Finish release preparation.** Decide the final public app name, support email, privacy-policy URL, screenshots, store text, and countries where it will be available. Check the Quran text, translations, tafsir, recitations, image, and font licenses and preserve required attribution. Test fresh install, upgrade, offline reading, audio downloads/playback, all app languages, and both orientations on more than one screen size.
2. **Confirm the permanent package ID.** In `app/build.gradle.kts`, verify `applicationId` is the package you want permanently. It is currently `com.abdulkerimabdulmejid.qurankarim`. Check availability in Play Console before creating the app; an existing Play app cannot be renamed to a different package ID. Also set a suitable `versionName` and a `versionCode` not previously used for this package. Increase the code for every later release.
3. **Register for Play Console.** Create an individual or organization developer account, pay any fee displayed during registration, complete identity/contact verification, and secure the account with strong authentication. For organization accounts, use the correct legal organization details. Follow any closed-testing or production-access requirements shown for your account; requirements can differ by account type and can change.
4. **Create the upload key and protect it.** Follow the command above on a trusted computer, outside the project folder. Choose strong unique passwords, record the alias and passwords in a secure password manager, and make an encrypted backup. Do not send the private upload keystore to Google; Google Play App Signing holds the app-signing key while you use the upload key to authenticate uploads.
5. **Create the app entry.** In Play Console, choose **Create app**, enter the display name, default language, app type, and whether it is free or paid, then accept the displayed declarations. Select the final package ID when Play Console creates the app.
6. **Complete the store listing.** Add the app name, short and full descriptions, app icon, feature graphic, phone/tablet screenshots as requested, category, support email, and website if available. Provide clear, accurate information and use only artwork you own or are licensed to distribute. Follow the current image dimensions and file requirements shown by Play Console.
7. **Publish the privacy policy.** Host a readable policy at a stable public HTTPS URL and enter that URL in Play Console. Explain what the app does with data and how users can contact you. Review every network service used for Quran text, tafsir, or audio and disclose any information those services may receive according to their current terms. Do not claim “no data collected” without checking the app, libraries, and providers.
8. **Complete policy and app-content forms.** In **App content**, complete the privacy-policy, ads, app-access, target-audience, content-rating, Data safety, and other forms shown. Answer each based on the release version and all included SDKs and network providers. State that no ads are shown only if that is true. If no account or restricted area exists, say so in app-access; if any content or feature is gated, provide valid review instructions. Ensure target-audience answers and content rating match the actual app.
9. **Check device availability.** Open **Device catalog** and check for excluded models, required features, and Android version compatibility. This project declares broad screen-size support and no optional hardware requirements, but catalog availability is still controlled by Play Console, device certification, Android version, country, and other policies. Do not advertise TV, Wear OS, or Auto compatibility unless those experiences are built and tested.
10. **Configure protected signing variables.** In the protected environment that launches Gradle, provide `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD`. Keep the key file outside the project and passwords out of source control and build logs. If any value is missing or the keystore path does not exist, the release signing task stops with a direct error before signing.
11. **Build the release bundle.** From the project root, run `./gradlew :app:bundleRelease`. Confirm that `app/build/outputs/bundle/release/app-release.aab` exists and that the build completed successfully. Do not upload a debug APK. Keep a record of the source revision, version code, and bundle you submitted.
12. **Create an internal test release.** In Play Console, open **Testing > Internal testing**, create a release, upload the `.aab`, and enroll in Play App Signing when prompted. Add tester email addresses or a Google Group, save/publish the test, and share the opt-in link. Install through the Play link rather than sideloading so you test the Play-delivered build.
13. **Test across form factors.** Ask testers to cover phones, small screens, tablets, foldables, and Chromebook resizable windows if available, including portrait/landscape, Arabic right-to-left layout, large font/display scaling, offline use, network loss, audio interruptions, and a fresh install plus update. Check automated pre-launch reports, Android vitals, crashes, and warnings. Fix issues and upload a higher `versionCode` for another test build.
14. **Meet production-access requirements.** If Play Console requires a closed test or other qualification, follow the exact tester count, duration, and participation requirements displayed there. Collect feedback, resolve stability/accessibility problems, and submit the production-access application when the console enables it.
15. **Roll out to production.** Create the production release, select the countries/regions, review Play's bundle and policy warnings, and submit it for review. Use a staged rollout where available, monitor crash/ANR rates and reviews, and pause or halt the rollout if a serious issue appears.
16. **Maintain future releases.** Keep the upload key and passwords backed up, protect the Play Console account, respond to policy notices, keep dependencies and Android target requirements current, and increment `versionCode` for every update. Review Play Console's **Device catalog**, **Policy status**, and Android vitals regularly.

Play Console labels, asset requirements, account verification, target API deadlines, and testing rules can change and may differ by account or region. Treat the current Play Console dashboard and official policy pages as authoritative. No client-side app can be guaranteed safe from every form of Android hacking; ongoing updates, minimal permissions, secure key handling, accurate privacy disclosures, and release testing are essential.

# quran-karim
