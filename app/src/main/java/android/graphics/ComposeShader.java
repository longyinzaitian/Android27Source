/*
 * Copyright (C) 2007 The Android Open Source Project
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

package android.graphics;


import android.annotation.NonNull;

/** A subclass of shader that returns the composition of two other shaders, combined by
    an {@link Xfermode} subclass.
*/
public class ComposeShader extends Shader {

    Shader mShaderA;
    private long mNativeInstanceShaderA;
    Shader mShaderB;
    private long mNativeInstanceShaderB;
    private int mPorterDuffMode;

    /**
     * Create a new compose shader, given shaders A, B, and a combining mode.
     * When the mode is applied, it will be given the result from shader A as its
     * "dst", and the result from shader B as its "src".
     *
     * @param shaderA  The colors from this shader are seen as the "dst" by the mode
     * @param shaderB  The colors from this shader are seen as the "src" by the mode
     * @param mode     The mode that combines the colors from the two shaders. If mode
     *                 is null, then SRC_OVER is assumed.
    */
    public ComposeShader(@NonNull Shader shaderA, @NonNull Shader shaderB, @NonNull Xfermode mode) {
        this(shaderA, shaderB, mode.porterDuffMode);
    }

    /**
     * Create a new compose shader, given shaders A, B, and a combining PorterDuff mode.
     * When the mode is applied, it will be given the result from shader A as its
     * "dst", and the result from shader B as its "src".
     *
     * @param shaderA  The colors from this shader are seen as the "dst" by the mode
     * @param shaderB  The colors from this shader are seen as the "src" by the mode
     * @param mode     The PorterDuff mode that combines the colors from the two shaders.
    */
    public ComposeShader(@NonNull Shader shaderA, @NonNull Shader shaderB,
            @NonNull PorterDuff.Mode mode) {
        this(shaderA, shaderB, mode.nativeInt);
    }

    private ComposeShader(Shader shaderA, Shader shaderB, int nativeMode) {
        if (shaderA == null || shaderB == null) {
            throw new IllegalArgumentException("Shader parameters must not be null");
        }

        mShaderA = shaderA;
        mShaderB = shaderB;
        mPorterDuffMode = nativeMode;
    }

    @Override
    long createNativeInstance(long nativeMatrix) {
        mNativeInstanceShaderA = mShaderA.getNativeInstance();
        mNativeInstanceShaderB = mShaderB.getNativeInstance();
        return nativeCreate(nativeMatrix,
                mShaderA.getNativeInstance(), mShaderB.getNativeInstance(), mPorterDuffMode);
    }

    /** @hide */
    @Override
    protected void verifyNativeInstance() {
        if (mShaderA.getNativeInstance() != mNativeInstanceShaderA
                || mShaderB.getNativeInstance() != mNativeInstanceShaderB) {
            // Child shader native instance has been updated,
            // so our cached native instance is no longer valid - discard it
            discardNativeInstance();
        }
    }

    /**
     * @hide
     */
    @Override
    protected Shader copy() {
        final ComposeShader copy = new ComposeShader(
                mShaderA.copy(), mShaderB.copy(), mPorterDuffMode);
        copyLocalMatrix(copy);
        return copy;
    }

    private static native long nativeCreate(long nativeMatrix,
            long nativeShaderA, long nativeShaderB, int porterDuffMode);
}
