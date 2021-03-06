/*
 * Copyright (c) 2010, Sun Microsystems, Inc. Copyright (c) 2010, The Storage Networking Industry
 * Association.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 * 
 * Neither the name of The Storage Networking Industry Association (SNIA) nor the names of its
 * contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 */

package org.snia.cdmiserver.filter;

/**
 * This class provides encode/decode for RFC 2045 Base64 as defined by RFC 2045, N. Freed and N.
 * Borenstein. RFC 2045: Multipurpose Internet Mail Extensions (MIME) Part One: Format of Internet
 * Message Bodies. Reference 1996 Available at: http://www.ietf.org/rfc/rfc2045.txt This class is
 * used by XML Schema binary format validation
 * 
 * @author Jeffrey Rodriguez
 * @version $Revision: 41 $ $Date: 2009-12-06 13:50:36 -0800 (Sun, 06 Dec 2009) $
 */

public final class Base64 {

  private static final int BASELENGTH = 255;
  private static final int LOOKUPLENGTH = 63;
  private static final int TWENTYFOURBITGROUP = 24;
  private static final int EIGHTBIT = 8;
  private static final int SIXTEENBIT = 16;
  @SuppressWarnings("unused")
  private static final int SIXBIT = 6;
  private static final int FOURBYTE = 4;

  private static final byte PAD = (byte) '=';
  private static byte[] base64Alphabet = new byte[BASELENGTH];
  private static byte[] lookUpBase64Alphabet = new byte[LOOKUPLENGTH];

  static {

    for (int i = 0; i < BASELENGTH; i++) {
      base64Alphabet[i] = -1;
    }
    for (int i = 'Z'; i >= 'A'; i--) {
      base64Alphabet[i] = (byte) (i - 'A');
    }
    for (int i = 'z'; i >= 'a'; i--) {
      base64Alphabet[i] = (byte) (i - 'a' + 26);
    }

    for (int i = '9'; i >= '0'; i--) {
      base64Alphabet[i] = (byte) (i - '0' + 52);
    }

    base64Alphabet['+'] = 62;
    base64Alphabet['/'] = 63;

    for (int i = 0; i <= 25; i++) {
      lookUpBase64Alphabet[i] = (byte) ('A' + i);
    }
    for (int i = 26, j = 0; i <= 51; i++, j++) {
      lookUpBase64Alphabet[i] = (byte) ('a' + j);
    }
    for (int i = 52, j = 0; i <= 61; i++, j++) {
      lookUpBase64Alphabet[i] = (byte) ('0' + j);
    }
  }

  static boolean isBase64(byte octect) {
    // shall we ignore white space? JEFF??
    return (octect == PAD || base64Alphabet[octect] != -1);
  }

  static boolean isArrayByteBase64(byte[] arrayOctect) {
    int length = arrayOctect.length;
    if (length == 0) {
      return false;
    }
    for (int i = 0; i < length; i++) {
      if (Base64.isBase64(arrayOctect[i]) == false) {
        return false;
      }
    }
    return true;
  }

  /**
   * Encodes hex octects into Base64.
   * 
   * @param binaryData Array containing binaryData
   * @return Encoded Base64 array
   */
  public static byte[] encode(byte[] binaryData) {
    int lengthDataBits = binaryData.length * EIGHTBIT;
    int fewerThan24bits = lengthDataBits % TWENTYFOURBITGROUP;
    int numberTriplets = lengthDataBits / TWENTYFOURBITGROUP;
    byte[] encodedData = null;

    if (fewerThan24bits != 0) { // data not divisible by 24 bit
      encodedData = new byte[(numberTriplets + 1) * 4];
    } else {
      // 16 or 8 bit
      encodedData = new byte[numberTriplets * 4];
    }
    byte k0 = 0;
    byte l0 = 0;
    byte b1 = 0;
    byte b2 = 0;
    byte b3 = 0;

    int encodedIndex = 0;
    int dataIndex = 0;
    int index = 0;
    for (index = 0; index < numberTriplets; index++) {

      dataIndex = index * 3;
      b1 = binaryData[dataIndex];
      b2 = binaryData[dataIndex + 1];
      b3 = binaryData[dataIndex + 2];

      l0 = (byte) (b2 & 0x0f);
      k0 = (byte) (b1 & 0x03);

      encodedIndex = index * 4;
      encodedData[encodedIndex] = lookUpBase64Alphabet[b1 >> 2];
      encodedData[encodedIndex + 1] = lookUpBase64Alphabet[(b2 >> 4) | (k0 << 4)];
      encodedData[encodedIndex + 2] = lookUpBase64Alphabet[(l0 << 2) | (b3 >> 6)];
      encodedData[encodedIndex + 3] = lookUpBase64Alphabet[b3 & 0x3f];
    }

    // form integral number of 6-bit groups
    dataIndex = index * 3;
    encodedIndex = index * 4;
    if (fewerThan24bits == EIGHTBIT) {
      b1 = binaryData[dataIndex];
      k0 = (byte) (b1 & 0x03);
      encodedData[encodedIndex] = lookUpBase64Alphabet[b1 >> 2];
      encodedData[encodedIndex + 1] = lookUpBase64Alphabet[k0 << 4];
      encodedData[encodedIndex + 2] = PAD;
      encodedData[encodedIndex + 3] = PAD;
    } else if (fewerThan24bits == SIXTEENBIT) {

      b1 = binaryData[dataIndex];
      b2 = binaryData[dataIndex + 1];
      l0 = (byte) (b2 & 0x0f);
      k0 = (byte) (b1 & 0x03);
      encodedData[encodedIndex] = lookUpBase64Alphabet[b1 >> 2];
      encodedData[encodedIndex + 1] = lookUpBase64Alphabet[(b2 >> 4) | (k0 << 4)];
      encodedData[encodedIndex + 2] = lookUpBase64Alphabet[l0 << 2];
      encodedData[encodedIndex + 3] = PAD;
    }
    return encodedData;
  }

  /**
   * Decodes Base64 data into octects
   * 
   * @param base64Data Byte array containing Base64 data
   * @return Array containind decoded data.
   */
  public byte[] decode(byte[] base64Data) {
    int numberQuadruple = base64Data.length / FOURBYTE;
    byte[] decodedData = null;
    byte b1 = 0;
    byte b2 = 0;
    byte b3 = 0;
    byte b4 = 0;
    byte marker0 = 0;
    byte marker1 = 0;

    // Throw away anything not in base64Data
    // Adjust size

    int encodedIndex = 0;
    int dataIndex = 0;
    decodedData = new byte[numberQuadruple * 3 + 1];

    for (int i = 0; i < numberQuadruple; i++) {
      dataIndex = i * 4;
      marker0 = base64Data[dataIndex + 2];
      marker1 = base64Data[dataIndex + 3];

      b1 = base64Alphabet[base64Data[dataIndex]];
      b2 = base64Alphabet[base64Data[dataIndex + 1]];

      if (marker0 != PAD && marker1 != PAD) { // No PAD e.g 3cQl
        b3 = base64Alphabet[marker0];
        b4 = base64Alphabet[marker1];

        decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
        decodedData[encodedIndex + 1] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
        decodedData[encodedIndex + 2] = (byte) (b3 << 6 | b4);
      } else if (marker0 == PAD) { // Two PAD e.g. 3c[Pad][Pad]
        decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
        decodedData[encodedIndex + 1] = (byte) ((b2 & 0xf) << 4);
        decodedData[encodedIndex + 2] = (byte) 0;
      } else if (marker1 == PAD) { // One PAD e.g. 3cQ[Pad]
        b3 = base64Alphabet[marker0];

        decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
        decodedData[encodedIndex + 1] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
        decodedData[encodedIndex + 2] = (byte) (b3 << 6);
      }
      encodedIndex += 3;
    }
    return decodedData;

  }

  static final int[] base64 = {64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
      64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
      64, 64, 64, 62, 64, 64, 64, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 64, 64, 64, 64, 64,
      64, 64, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
      24, 25, 64, 64, 64, 64, 64, 64, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
      41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
      64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
      64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
      64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
      64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
      64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
      64, 64, 64, 64, 64, 64};

  /**
   * Base64 decode the given string.
   * 
   * @param orig the original {@link String}
   * @return the base64 decoded {@link String}
   */
  public static String base64Decode(String orig) {
    char[] chars = orig.toCharArray();
    StringBuilder sb = new StringBuilder();
    int index = 0;

    int shift = 0; // # of excess bits stored in accum
    int acc = 0;

    for (index = 0; index < chars.length; index++) {
      int value = base64[chars[index] & 0xFF];

      if (value >= 64) {
        // Removed logging at finest level
      } else {
        acc = (acc << 6) | value;
        shift += 6;
        if (shift >= 8) {
          shift -= 8;
          sb.append((char) ((acc >> shift) & 0xff));
        }
      }
    }
    return sb.toString();
  }

}
