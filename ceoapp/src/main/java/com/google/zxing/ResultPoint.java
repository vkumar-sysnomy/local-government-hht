/*
 * Copyright 2007 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing;


/**
 * <p>Encapsulates a point of interest in an image containing a barcode. Typically, this
 * would be the location of a finder pattern or the corner of the barcode, for example.</p>
 *
 * @author Sean Owen
 */
public class ResultPoint {

  private final float x;
  private final float y;

  public ResultPoint(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public final float getX() {
    return x;
  }

  public final float getY() {
    return y;
  }

  @Override
  public final boolean equals(Object other) {
    if (other instanceof ResultPoint) {
      ResultPoint otherPoint = (ResultPoint) other;
      return x == otherPoint.x && y == otherPoint.y;
    }
    return false;
  }

  @Override
  public final int hashCode() {
    return 31 * Float.floatToIntBits(x) + Float.floatToIntBits(y);
  }

  @Override
  public final String toString() {
    StringBuilder result = new StringBuilder(25);
    result.append('(');
    result.append(x);
    result.append(',');
    result.append(y);
    result.append(')');
    return result.toString();
  }




}
