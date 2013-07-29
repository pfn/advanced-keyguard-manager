package com.hanhuy.android.bluetooth.keyguard

import org.scalatest.FunSuite

class SettingsTests extends FunSuite {
  test("Determine type") {
    val settings = new Settings()
    val cls = settings.getTypeOf(Settings.BLUETOOTH_CONNECTIONS)
    expectResult(classOf[java.util.List[_]])(cls)
    val cls2 = settings.getTypeOf(Settings.BT_CLEAR_KEYGUARD)
    expectResult(classOf[java.lang.Boolean])(cls2)
  }
}
