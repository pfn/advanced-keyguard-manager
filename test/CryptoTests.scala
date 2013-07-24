package com.hanhuy.android.bluetooth.keyguard

import org.scalatest.FunSuite

class CryptoTests extends FunSuite {

  test("Simple crypto test") {
    val value = "abcdefghijklmnopqrstuvwxyz"
    val encrypted = CryptoUtils.encrypt(value)
    info(encrypted)
    expectResult(value)(CryptoUtils.decrypt(encrypted))
  }

  test("Simple hmac test") {
    val value = "abcdefghijklmnopqrstuvwxyz"
    val hmac1 = CryptoUtils.hmac(value);
    val hmac2 = CryptoUtils.hmac(value);
    info(hmac1)
    expectResult(hmac1)(hmac2)
    expectResult("32d10c7b8cf96570ca04ce37f2a19d84240d3a89")(hmac1.toLowerCase)
  }
}
