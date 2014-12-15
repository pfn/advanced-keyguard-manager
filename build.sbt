import android.Keys.Android

name := "advanced-keyguard-manager"

android.Plugin.androidBuild

libraryDependencies ++= Seq(
    "com.google.code.gson" % "gson" % "2.2.4",
    "com.android.support" % "appcompat-v7" % "21.0.2",
    "com.android.support" % "support-v4" % "21.0.2",
    "com.google.guava" % "guava" % "14.0.1",
    "ch.acra" % "acra" % "4.5.0",
    "org.scalatest" %% "scalatest" % "1.9.1" % "test"
)

javacOptions in Compile += "-Xlint:deprecation"

run <<= run in Android
