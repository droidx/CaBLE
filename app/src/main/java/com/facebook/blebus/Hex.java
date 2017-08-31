// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.blebus;

public class Hex {
  public static String byteArrayToHexString(byte[] a) {
    StringBuilder sb = new StringBuilder(a.length * 2);
    for (byte b: a)
      sb.append(String.format("%02x", b & 0xff));
    return sb.toString();
  }
}
