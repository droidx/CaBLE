// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.blebus;

import java.util.Locale;

public class CRC16 {
  public static int get(final byte[] buffer) {
    int crc = 0xFFFF;          // initial value
    int polynomial = 0x1021;   // 0001 0000 0010 0001  (0, 5, 12)

    for (byte b : buffer) {
      for (int i = 0; i < 8; i++) {
        boolean bit = ((b   >> (7-i) & 1) == 1);
        boolean c15 = ((crc >> 15    & 1) == 1);
        crc <<= 1;
        if (c15 ^ bit) {
          crc ^= polynomial;
        }
      }
    }

    crc &= 0xffff;
    return crc;
  }

  private static final int MAC_LEN = 6;
  public static String getIdfromMAC(String MAC) {
    if (MAC == null) {
      return null;
    }

    String[] addressHex = MAC.split(":");
    if (addressHex.length != MAC_LEN) {
      return null;
    }

    byte[] addressBytes;

    try {
      addressBytes = new byte[addressHex.length];
      for (int i = 0; i < addressHex.length; i++) {
        addressBytes[i] = (byte) Integer.parseInt(addressHex[i], 16);
      }
    } catch (NumberFormatException e) {
      e.printStackTrace();
      return null;
    }

    String hexID = Integer.toHexString(CRC16.get(addressBytes)).toUpperCase(Locale.US);
    while (hexID.length() < 4) {
      hexID = "0" + hexID;
    }

    return hexID;
  }
}
