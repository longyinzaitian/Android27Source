/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.location.provider;

import android.hardware.location.IFusedLocationHardware;
import android.location.IFusedProvider;
import android.os.IBinder;

/**
 * Base class for Fused providers implemented as unbundled services.
 *
 * <p>Fused providers can be implemented as services and return the result of
 * {@link FusedProvider#getBinder()} in its getBinder() method.
 *
 * <p>IMPORTANT: This class is effectively a public API for unbundled applications, and must remain
 * API stable. See README.txt in the root of this package for more information.
 */
public abstract class FusedProvider {
    private IFusedProvider.Stub mProvider = new IFusedProvider.Stub() {
        @Override
        public void onFusedLocationHardwareChange(IFusedLocationHardware instance) {
            setFusedLocationHardware(new FusedLocationHardware(instance));
        }
    };

    /**
     * Gets the Binder associated with the provider.
     * This is intended to be used for the onBind() method of a service that implements a fused
     * service.
     *
     * @return The IBinder instance associated with the provider.
     */
    public IBinder getBinder() {
        return mProvider;
    }

    /**
     * Sets the FusedLocationHardware instance in the provider..
     * @param value     The instance to set. This can be null in cases where the service connection
     *                  is disconnected.
     */
    public abstract void setFusedLocationHardware(FusedLocationHardware value);
}
