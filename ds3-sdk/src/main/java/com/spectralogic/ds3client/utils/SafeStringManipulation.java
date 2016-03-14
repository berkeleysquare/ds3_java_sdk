/*
 * ******************************************************************************
 *   Copyright 2014-2015 Spectra Logic Corporation. All Rights Reserved.
 *   Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 *   this file except in compliance with the License. A copy of the License is located at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file.
 *   This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *   CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations under the License.
 * ****************************************************************************
 */

package com.spectralogic.ds3client.utils;

import com.google.common.net.UrlEscapers;

import java.util.Date;

/**
 * Safely converts a type to a string. This is used when adding
 * objects to query params.
 */
public final class SafeStringManipulation {

    private SafeStringManipulation() {
        //pass
    }

    public static <T> String safeUrlEscape(final T obj) {
        if (obj == null) {
            return null;
        }
        return UrlEscapers.urlFragmentEscaper().escape(safeToString(obj)).replace("+", "%2B");
    }

    public static <T> String safeToString(final T obj) {
        if (obj == null) {
            return null;
        }
        if(obj.getClass().isPrimitive()) {
            return String.valueOf(obj);
        }
        if (obj instanceof Date) {
            return Long.toString(((Date) obj).getTime());
        }
        return obj.toString();
    }
}
