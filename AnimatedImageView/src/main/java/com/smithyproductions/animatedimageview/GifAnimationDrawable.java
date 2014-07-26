/**
 * Copyright (C) 2013 Orthogonal Labs, Inc. All rights reserved.
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
package com.e8tracks.ui.views.gif;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import com.e8tracks.util.Logger;

import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * <p>Creates an AnimationDrawable from a GIF image.</p>
 *
 * @author Femi Omojola <femi@hipmob.com>
 */
public class GifAnimationDrawable extends AnimationDrawable {
	private final Handler mHandler;
	private boolean mQuickLoad = false;
	private boolean decoded;
	private GifDecoder mGifDecoder;
	private Bitmap mTmpBitmap;
	private int height, width;
	private OnGifAnimationDrawableLoadingListener mListener;
	public interface OnGifAnimationDrawableLoadingListener {
		public void onFirstFrameLoaded();
		public void onAllFramesLoaded();
	}
	public GifAnimationDrawable(boolean quickLoad, OnGifAnimationDrawableLoadingListener listener) {
		super();

		mQuickLoad = quickLoad;
		mListener = listener;
		mHandler = new Handler();


		decoded = false;
		mGifDecoder = new GifDecoder();

	}

	public void loadData(InputStream is) {

		InputStream bis = is;
		if (!BufferedInputStream.class.isInstance(bis))
			bis = new BufferedInputStream(is, 32768);

		mGifDecoder.read(bis);

		setOneShot(mGifDecoder.getLoopCount() != 0);

		mTmpBitmap = mGifDecoder.getFrame(0);

		if(mTmpBitmap == null){
			Logger.e("mTmpBitmap null!!");
		}

		height = mTmpBitmap.getHeight();
		width = mTmpBitmap.getWidth();

		if (mQuickLoad) {

			addFrame(new BitmapDrawable(mTmpBitmap), mGifDecoder.getDelay(0));
			setVisible(true, true);

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (mListener != null)
						mListener.onFirstFrameLoaded();
				}
			});

		}

	}
	/**
	 * Decode any remaining frames.
	 *
	 * @return Runnable that should be posted to the UI thread
	 */
	public void decode() {
		if (mGifDecoder == null) {
			Logger.e("Cannot decode, mGifDecoder is null!");
			return;
		}

		mGifDecoder.complete();

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				int i, n = mGifDecoder.getFrameCount(), t;

				for (i = mQuickLoad ? 1 : 0; i < n; i++) {
					mTmpBitmap = mGifDecoder.getFrame(i);
					t = mGifDecoder.getDelay(i);
					addFrame(new BitmapDrawable(mTmpBitmap), t);
				}

				//this is the hack that determines whether or not
				//the GIF will actually animate on a lot of devices
				setVisible(true, true);
				//true true
				//false false
				//false true

				decoded = true;
				mGifDecoder = null;

				if (mListener != null)
					mListener.onAllFramesLoaded();
			}
		});
	}
	public boolean isDecoded() {
		return decoded;
	}
	public int getMinimumHeight() {
		return height;
	}
	public int getMinimumWidth() {
		return width;
	}
	public int getIntrinsicHeight() {
		return height;
	}
	public int getIntrinsicWidth() {
		return width;
	}
}