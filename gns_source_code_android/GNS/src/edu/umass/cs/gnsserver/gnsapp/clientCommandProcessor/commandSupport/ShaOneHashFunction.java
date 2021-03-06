/*
 *
 *  Copyright (c) 2015 University of Massachusetts
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you
 *  may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 *  Initial developer(s): Westy
 *
 */
package edu.umass.cs.gnsserver.gnsapp.clientCommandProcessor.commandSupport;

import edu.umass.cs.gnsserver.main.GNSConfig;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author westy
 */
public class ShaOneHashFunction extends AbstractHashFunction {

  private MessageDigest messageDigest;

  private ShaOneHashFunction() {

    try {
      messageDigest = MessageDigest.getInstance("SHA1");
    } catch (NoSuchAlgorithmException e) {
      GNSConfig.getLogger().severe("Problem initializing digest: " + e);
    }
  }

  /**
   * Hash the string.
   *
   * @param key
   * @return a byte array
   */
  @Override
  public synchronized byte[] hash(String key) {
    try {
      messageDigest.update(key.getBytes("UTF-8"));
      return messageDigest.digest();
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * Hash the byte array.
   *
   * @param bytes
   * @return a byte array
   */
  public synchronized byte[] hash(byte[] bytes) {
    messageDigest.update(bytes);
    return messageDigest.digest();
  }

  /**
   * Returns the single instance of the ShaOneHashFunction.
   *
   * @return a ShaOneHashFunction instance
   */
  public static ShaOneHashFunction getInstance() {
    return SHA1HashFunctionHolder.INSTANCE;
  }

  private static class SHA1HashFunctionHolder {

    private static final ShaOneHashFunction INSTANCE = new ShaOneHashFunction();
  }
}
