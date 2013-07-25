import android.Dependencies.apklib
import android.Keys.Android

name := "advanced-keyguard-manager"

android.Plugin.androidBuild

libraryDependencies ++= Seq(
    "com.google.code.gson" % "gson" % "2.2.4",
    "com.android.support" % "appcompat-v7" % "18.0.0",
    "com.android.support" % "support-v4" % "18.0.0",
    "com.google.guava" % "guava" % "14.0.1",
    "org.scalatest" %% "scalatest" % "1.9.1" % "test"
)

javacOptions in Compile += "-Xlint:deprecation"

run <<= run in Android
