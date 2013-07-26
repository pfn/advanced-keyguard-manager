Advanced Keyguard Manager
=========================

https://play.google.com/store/apps/details?id=com.hanhuy.android.bluetooth.keyguard

Prerequisites for Building
==========================

* SBT
  * Install from http://scala-sbt.org
  * On a Mac with macports or brew: `brew install sbt` or `port install sbt`
* The `android support repository` must be installed (Android SDK manager)
* Android SDK setup, you can do one of:
  * Run the command `android update project -p . -t android-17`
  * Set the environment variable `ANDROID_HOME` to point to the path of the
    Android SDK.

Building
========

* `sbt ~compile` - compile continuously
* `sbt ~android:package` - generate a debug apk continuously
* `sbt ~run` - generate a debug apk, install and run it continuously
* Commands can be run without `~` to run once only. Additionally, `sbt` can
  be run in interactive mode.
* `sbt gen-idea` - generate IDE configuration to easily load into IntelliJ
