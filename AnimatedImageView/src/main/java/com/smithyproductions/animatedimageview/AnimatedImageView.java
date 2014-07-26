/*
 * Copyright (c) 2014 8tracks Inc. All rights reserved.
 */

package com.e8tracks.ui.views.gif;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.e8tracks.util.Logger;

import java.io.IOException;
import java.net.URL;

/**
 * Created by rory on 21/07/2014.
 */
public class AnimatedImageView extends ImageView {
	private boolean mIsPlayingGif = false;
	private GifDecoder mGifDecoder;
	private Bitmap mTmpBitmap;
	final Handler mHandler = new Handler();
	private GifAnimationDrawable drawable;
	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			if (mTmpBitmap != null && !mTmpBitmap.isRecycled()) {
				AnimatedImageView.this.setImageBitmap(mTmpBitmap);
			}
		}
	};
	public AnimatedImageView(final Context context) {
		super(context);
		init();
	}
	public AnimatedImageView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public AnimatedImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	private void init() {

	}
	public void setGifUrl(final String address, final boolean quickLoad) {
		setGifUrl(address, quickLoad, null);
	}
	public void setGifUrl(final String address, final boolean quickLoad, final GifAnimationDrawable.OnGifAnimationDrawableLoadingListener listener) {
		post(new Runnable() {
			@Override
			public void run() {

		drawable = new GifAnimationDrawable(quickLoad, listener);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					drawable.loadData(new URL(address).openStream());
					post(new Runnable() {
						@Override
						public void run() {
							setImageDrawable(drawable);
						}
					});
					drawable.decode();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();

			}
		});
	}
	public void stopRendering() {
		mIsPlayingGif = true;
	}
}