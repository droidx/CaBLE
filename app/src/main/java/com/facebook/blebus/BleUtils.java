// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.blebus;

import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by priteshsankhe on 12/08/16.
 */
public class BleUtils {

    private static final String DEV_NAME = "FBTag";
    private static final String TAG = BleUtils.class.getSimpleName();

    private static final byte MANUF_TYPE = -1; // 0xFF
    private static final byte NAME_TYPE = 9;  // 0x09

    /**
     * Checks name and that ID matches CRC16 of MAC
     */
    public static boolean isValidDevice(String deviceAddress, byte[] advPacket) {
        boolean isNameValid = false;
        boolean isManufValid = false;
        int devID = 0;

        try {
            devID = getCrc16Integer(deviceAddress);
        } catch (ParseException e) {
            return false;
        }


        int index = 0;
        while (index < advPacket.length) {
            int length = advPacket[index++];
            //Done once we run out of records
            if (length == 0) break;

            byte type = advPacket[index];
            //Done if our record isn't a valid type
            if (type == 0) break;

            byte[] data = Arrays.copyOfRange(advPacket, index + 1, index + length);

            if (type == NAME_TYPE) {
                // Check Name
                try {
                    isNameValid = DEV_NAME.equals(new String(data, "US-ASCII"));
                } catch (UnsupportedEncodingException e) {
                    isNameValid = false;
                    break;
                }
            } else if (type == MANUF_TYPE && data.length > 3) {
                // Check Manufacturer
                if (data[0] != -85 || data[1] != 01) { // 0xAB01
                    break;
                }
                int value = ((data[3] & 0xFF) << 8) | (data[2] & 0xFF);

                isManufValid = (value == devID);
            }

            //Advance
            index += length;
        }

        if (isNameValid && isManufValid) {
            Log.d(TAG, "Good Device: " + deviceAddress);
        }

        return isNameValid && isManufValid;
    }

    public static String getCrc16(String macId) {

        if (TextUtils.isEmpty(macId)) {
            return null;
        }

        String[] addressHex = macId.split(":");
        byte[] addressBytes = new byte[addressHex.length];
        for (int i = 0; i < addressHex.length; i++)
            addressBytes[i] = (byte) Integer.parseInt(addressHex[i], 16);
        String hexID = Integer.toHexString(CRC16.get(addressBytes)).toUpperCase(Locale.US);
        while (hexID.length() < 4)
            hexID = "0" + hexID;
        return hexID;
    }

    public static int getCrc16Integer(String macId) throws ParseException {
        return Integer.parseInt(getCrc16(macId), 16);
    }

    public static String getHexId(int id) {
        String hexID = Integer.toHexString(id).toUpperCase(Locale.US);
        while (hexID.length() < 4) {
            hexID = "0" + hexID;
        }
        return hexID;
    }

    public static String getCrc16FromId(int id) {
        String value = getCrc16(getHexId(id));
        return value;
    }
}
