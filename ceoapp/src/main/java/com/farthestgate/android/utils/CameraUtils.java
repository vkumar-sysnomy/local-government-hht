package com.farthestgate.android.utils;

/***
 Copyright (c) 2013 CommonsWare, LLC

 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import android.hardware.Camera;
import android.hardware.Camera.Size;

import java.util.List;

public class CameraUtils {
    // based on ApiDemos

    private static final double ASPECT_TOLERANCE=0.1;

    public static Camera.Size getOptimalPreviewSize(int displayOrientation,
                                                    int width,
                                                    int height,
                                                    Camera.Parameters parameters) {
        double targetRatio=(double)width / height;
        List<Size> sizes=parameters.getSupportedPreviewSizes();
        Camera.Size optimalSize=null;
        double minDiff= Double.MAX_VALUE;
        int targetHeight=height;

        if (displayOrientation == 90 || displayOrientation == 270) {
            targetRatio=(double)height / width;
        }

        // Try to find an size match aspect ratio and size

        for (Camera.Size size : sizes) {
            double ratio=(double)size.width / size.height;

            if (Math.abs(ratio - targetRatio) <= ASPECT_TOLERANCE) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize=size;
                    minDiff= Math.abs(size.height - targetHeight);
                }
            }
        }

        // Cannot find the one match the aspect ratio, ignore
        // the requirement

        if (optimalSize == null) {
            minDiff= Double.MAX_VALUE;

            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize=size;
                    minDiff= Math.abs(size.height - targetHeight);
                }
            }
        }

        return(optimalSize);
    }


    public static String findBestFlashModeMatch(Camera.Parameters params,
                                                String... modes) {
        String match=null;

        List<String> flashModes=params.getSupportedFlashModes();

        if (flashModes != null) {
            for (String mode : modes) {
                if (flashModes.contains(mode)) {
                    match=mode;
                    break;
                }
            }
        }

        return(match);
    }


}
