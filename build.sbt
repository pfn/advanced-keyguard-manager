import android.Dependencies.apklib
import android.Keys.Android

android.Plugin.androidBuild

libraryDependencies ++= Seq(
    "com.actionbarsherlock" % "actionbarsherlock" % "4.3.1" % "provided",
    apklib("com.actionbarsherlock" % "actionbarsherlock" % "4.3.1" % "provided"),
    "com.android.support" % "support-v4" % "13.0.0",
    "com.google.guava" % "guava" % "14.0.1",
    "org.scalatest" %% "scalatest" % "1.9.1" % "test"
)

run <<= run in Android
