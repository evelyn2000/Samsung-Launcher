/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.qstwzlauncher;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.qstwzlauncher.R;

import com.mediatek.common.featureoption.FeatureOption;

public class Hotseat extends FrameLayout implements DragController.DeferDragListener
	,Animator.AnimatorListener/*, Workspace.StateAnimatorProvider, CellLayoutContainer*/{
    private static final String TAG = "Hotseat";
    public static final int sAllAppsButtonRank = 
    		FeatureOption.Qs_Sub_Project_Name.contains("GNET") ? 2 : 4;
    // 2 In the middle of the dock

    private Launcher mLauncher;
    private CellLayout mContent;

    private int mCellCountX;
    private int mCellCountY;
    private boolean mIsLandscape;
    
    private boolean mDeferOnDragEnd = false;
    //private boolean mIsAllAppsButtonHidden = false;
    private AllAppsButtonDropTarget mAllAppsButton;
    //private TextView mAllAppsButton;
    
    private AnimatorSet mInAnimation;
    private AnimatorSet mOutAnimation;
    private View mAnimationHandle;//layout_bg_animation

    public Hotseat(Context context) {
        this(context, null);
    }

    public Hotseat(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Hotseat(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.Hotseat, defStyle, 0);
        mCellCountX = a.getInt(R.styleable.Hotseat_cellCountX, -1);
        mCellCountY = a.getInt(R.styleable.Hotseat_cellCountY, -1);
        a.recycle();
        mIsLandscape = context.getResources().getConfiguration().orientation ==
            Configuration.ORIENTATION_LANDSCAPE;
    }

    public void setup(Launcher launcher, DragController dragController) {
        mLauncher = launcher;
        setOnKeyListener(new HotseatIconKeyEventListener());
        //android.util.Log.e("QsLog", "hotseat::setup()===="+mAllAppsButton);

        dragController.addDragListener(mAllAppsButton);
        dragController.addDropTarget(mAllAppsButton);
        
        dragController.addDragListener(this);
        
        mAllAppsButton.setLauncher(launcher);
    }
    
    public void onDragStart(DragSource source, Object info, int dragAction) {
    	//showAllAppsButton(false);
    	//mAllAppsButton.setBackgroundResource(R.drawable.bottom_bar);
    	//mAllAppsButton.setText(R.string.delete_target_label);
    	mAnimationHandle.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    	mAnimationHandle.buildLayer();
    	//android.util.Log.w("QsLog", "hotseat::onDragStart()===="+mAllAppsButton);
        if(mInAnimation != null){
	        mInAnimation.cancel();
	        mInAnimation.start();
        }
    }

    public void onDragEnd() {
    	//showAllAppsButton(true);
    	//mAllAppsButton.setText(R.string.all_apps_button_label);
    	if (!mDeferOnDragEnd) {
            // Restore the QSB search bar, and animate out the drop target bar
    		if(mOutAnimation != null){
	    		mOutAnimation.cancel();
	    		mOutAnimation.start();
    		}
    		
    		if (this.mCurrentAnimator != null){
	    		mCurrentAnimator.cancel();
	            mCurrentAnimator = null;
    		}
    		
    		getAllappsIcon().unsetTrashIcon();
            getAllappsIcon().setAnimating(false);
            getAllappsIcon().setTrashCanShakeMode(false);
            getAllappsIcon().invalidate();
            
        } else {
            mDeferOnDragEnd = false;
        }
    }
    
    public void deferOnDragEnd() {
        mDeferOnDragEnd = true;

        
    }
    
    public void notifyDragEnter(){
    	mAnimationHandle.setBackgroundResource(R.drawable.bottom_bar_warn_right);

    	getAllappsIcon().setTrashIcon(true);
        if (!getAllappsIcon().isAnimating())
        {
          getAllappsIcon().setTrashCanShakeMode(true);
          startAnimator(R.animator.trashcan_lid_up, 60L, new TrashCanLidUpAdapter());
        }
    }
    
    public void notifyDragExit(){

		getAllappsIcon().setTrashIcon(false);
		if (getAllappsIcon().isTrashCanShakeMode())
			getAllappsIcon().setTrashCanShakeMode(false);
		
		if (this.mCurrentAnimator != null){
    		mCurrentAnimator.cancel();
            mCurrentAnimator = null;
		}
		
		mAnimationHandle.setBackgroundResource(R.drawable.bottom_bar);
    }
    
    public void notifyDropComplete(final View view, final Runnable onCompleteRunnable){

        if (this.mCurrentAnimator != null)
			this.mCurrentAnimator.cancel();
		
		getAllappsIcon().setTrashCanShakeMode(false);
		getAllappsIcon().setIconAlpha(0.0F);
		getAllappsIcon().setTrashCanAlpha(1.0F);
		getAllappsIcon().setTrashCanFillAlpha(0.0F);
		getAllappsIcon().setAnimating(true);
		startAnimator(R.animator.trashcan_fill, 200L, new TrashCanFillAdapter(onCompleteRunnable));
    }

    CellLayout getLayout() {
        return mContent;
    }

    /* Get the orientation invariant order of the item in the hotseat for persistence. */
    int getOrderInHotseat(int x, int y) {
        return mIsLandscape ? (mContent.getCountY() - y - 1) : x;
    }
    /* Get the orientation specific coordinates given an invariant order in the hotseat. */
    int getCellXFromOrder(int rank) {
        return mIsLandscape ? 0 : rank;
    }
    int getCellYFromOrder(int rank) {
        return mIsLandscape ? (mContent.getCountY() - (rank + 1)) : 0;
    }
    public static boolean isAllAppsButtonRank(int rank) {
        return rank == sAllAppsButtonRank;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mCellCountX < 0) mCellCountX = LauncherModel.getCellCountX();
        if (mCellCountY < 0) mCellCountY = LauncherModel.getCellCountY();
        mContent = (CellLayout) findViewById(R.id.layout);
        mContent.setGridSize(mCellCountX, mCellCountY);
        
        mAnimationHandle = this.findViewById(R.id.layout_bg_animation);
        
        resetLayout();
        
        //android.util.Log.w("QsLog", "hotseat::onFinishInflate()====");
        createAnimations();
    }

    void resetLayout() {
        mContent.removeAllViewsInLayout();
        
        // Add the Apps button
        Context context = getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        
        AllAppsButtonDropTarget allAppsButton = (AllAppsButtonDropTarget)
                inflater.inflate(R.layout.all_apps_button, mContent, false);
        mAllAppsButton = allAppsButton;
        allAppsButton.setSearchDropTargetBar(this);
        //android.util.Log.e("QsLog", "hotseat::resetLayout()===="+mAllAppsButton);
        /*BubbleTextView allAppsButton = (BubbleTextView)
                        inflater.inflate(R.layout.application, mContent, false);
        
        mAllAppsButton = allAppsButton;
        
        allAppsButton.setCompoundDrawablesWithIntrinsicBounds(null,
                context.getResources().getDrawable(R.drawable.all_apps_button_icon), null, null);
        allAppsButton.setText(context.getString(R.string.all_apps_button_label));
        */
        allAppsButton.setContentDescription(context.getString(R.string.all_apps_button_label));
        allAppsButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mLauncher != null &&
                    (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN
                    && !mAllAppsButton.isDropEnabled()) {
                    mLauncher.onTouchDownAllAppsButton(v);
                }
                return false;
            }
        });

        allAppsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                if (LauncherLog.DEBUG) {
                    LauncherLog.d(TAG, "Click on all apps view on hotseat: mLauncher = " + mLauncher);
                }
                if (mLauncher != null && !mAllAppsButton.isDropEnabled()) {
                    mLauncher.onClickAllAppsButton(v);
                }
            }
        });

        // Note: We do this to ensure that the hotseat is always laid out in the orientation of
        // the hotseat in order regardless of which orientation they were added
        int x = getCellXFromOrder(sAllAppsButtonRank);
        int y = getCellYFromOrder(sAllAppsButtonRank);
        mContent.addViewToCellLayout(allAppsButton, -1, 0, new CellLayout.LayoutParams(x,y,1,1),
                true);
    }
    
    void resetLayout(Launcher launcher, DragController dragController){
    	if(mAllAppsButton != null){
    		dragController.removeDragListener(mAllAppsButton);
    		dragController.removeDropTarget(mAllAppsButton);
    	}
    	
    	resetLayout();
    	
    	mAllAppsButton.setLauncher(launcher);
    	dragController.addDragListener(mAllAppsButton);
        dragController.addDropTarget(mAllAppsButton);
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
//        if (LauncherLog.DEBUG_TEMP) {
//            LauncherLog.d(TAG, "Hotseat dispatchDraw: this = " + this);
//        }
        super.dispatchDraw(canvas);
    }
    
    private void createAnimations() {
    	int BarHeight = getResources().getDimensionPixelSize(R.dimen.button_bar_height_plus_padding);
    	// Create the various fade animations
    	mAnimationHandle.setAlpha(0f);
        ObjectAnimator fadeInAlphaAnim = ObjectAnimator.ofFloat(mAnimationHandle, "alpha", 1f);
        fadeInAlphaAnim.setInterpolator(new DecelerateInterpolator());
        mInAnimation = new AnimatorSet();
        AnimatorSet.Builder fadeInAnimators = mInAnimation.play(fadeInAlphaAnim);
        if (true) {
        	mAnimationHandle.setTranslationY(BarHeight);
            fadeInAnimators.with(ObjectAnimator.ofFloat(mAnimationHandle, "translationY", 0f));
        }
        mInAnimation.setDuration(SearchDropTargetBar.sTransitionInDuration);
        mInAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
            	mAnimationHandle.setVisibility(View.VISIBLE);
            	mAnimationHandle.setAlpha(0f);
            }
        });

        ObjectAnimator fadeOutAlphaAnim = ObjectAnimator.ofFloat(mAnimationHandle, "alpha", 0f);
        fadeOutAlphaAnim.setInterpolator(new AccelerateInterpolator());
        mOutAnimation = new AnimatorSet();
        AnimatorSet.Builder fadeOutAnimators = mOutAnimation.play(fadeOutAlphaAnim);
        if (true) {
            fadeOutAnimators.with(ObjectAnimator.ofFloat(mAnimationHandle, "translationY",
                    BarHeight));
        }
        mOutAnimation.setDuration(SearchDropTargetBar.sTransitionOutDuration);
        mOutAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
            	mAnimationHandle.setVisibility(View.INVISIBLE);
            	mAnimationHandle.setLayerType(View.LAYER_TYPE_NONE, null);
                mAnimationHandle.setAlpha(1f);
            }
        });
    }
    
    
	public void setFadeOutTrashCan(float paramFloat) {
		getAllappsIcon().setTrashCanAlpha(1.0F - paramFloat);
		getAllappsIcon().setIconAlpha(paramFloat);
		getAllappsIcon().invalidate();
	}

	public void setTrashCanFill(float paramFloat) {
		getAllappsIcon().setTrashCanAlpha(1.0F - paramFloat);
		getAllappsIcon().setTrashCanFillAlpha(paramFloat);
		getAllappsIcon().invalidate();
	}

	public void setTrashCanLidDown(float paramFloat) {
		getAllappsIcon().setTrashCanLidOffset(0,
				(int) (4.0F * (1.0F - paramFloat)));
		getAllappsIcon().setTrashCanLidAngle(0.0F);
		getAllappsIcon().invalidate();
	}
	private float mAngleDirection = 1.0F;
	private float mPreviousLidShakeValue;
	public void setTrashCanLidShake(float paramFloat) {
		float f1 = paramFloat * 8.0F;
		float f2 = getAllappsIcon().getTrashCanLidAngle();
		float f3 = f1 - this.mPreviousLidShakeValue;
		this.mPreviousLidShakeValue = f1;
		float f4 = f2 + 12.0F * (f3 * this.mAngleDirection);
		
		if (f4 > 12.0F) {
			f4 = 12.0F - (f4 - 12.0F);
			this.mAngleDirection = (-1.0F * this.mAngleDirection);
		} else if (f4 < -12.0F){
			f4 = -12.0F - (f4 + 12.0F);
			this.mAngleDirection = (-1.0F * this.mAngleDirection);
		} 
		
		getAllappsIcon().setTrashCanLidAngle(f4);
		getAllappsIcon().invalidate();
		
//		while (true) {
//			getAllappsIcon().setTrashCanLidAngle(f4);
//			getAllappsIcon().invalidate();
//			return;
//			if (f4 >= -12.0F)
//				continue;
//			f4 = -12.0F - (f4 + 12.0F);
//			this.mAngleDirection = (-1.0F * this.mAngleDirection);
//		}
//		

	}

	public void setTrashCanLidUp(float paramFloat) {
		getAllappsIcon().setTrashCanLidOffset(0, (int) (4.0F * paramFloat));
		getAllappsIcon().invalidate();
	}

	public void setTrashCanUnfill(float paramFloat) {
		getAllappsIcon().setTrashCanAlpha(paramFloat);
		getAllappsIcon().setTrashCanFillAlpha(1.0F - paramFloat);
		getAllappsIcon().invalidate();
	}
    
    public AllAppsButtonDropTarget getAllappsIcon(){
    	return mAllAppsButton;
    }
    
    public void onAnimationCancel(Animator paramAnimator)
    {
    }

    public void onAnimationEnd(Animator paramAnimator)
    {
      this.mAnimationHandle.setLayerType(0, null);
      CellLayoutChildren localCellLayoutChildren = this.mContent.getChildrenLayout();
      int i = localCellLayoutChildren.getChildCount();
      for (int j = 0; j < i; j++)
        localCellLayoutChildren.getChildAt(j).setLayerType(0, null);
    }

    public void onAnimationRepeat(Animator paramAnimator)
    {
    }

    public void onAnimationStart(Animator paramAnimator)
    {
      CellLayoutChildren localCellLayoutChildren = this.mContent.getChildrenLayout();
      int i = localCellLayoutChildren.getChildCount();
      for (int j = 0; j < i; j++)
        localCellLayoutChildren.getChildAt(j).setLayerType(2, null);
      this.mAnimationHandle.setLayerType(2, null);
    }

	private Animator mCurrentAnimator;

	private void startAnimator(int animresid, long delay,
			Animator.AnimatorListener paramAnimatorListener) {
		if ((this.mCurrentAnimator != null)
				&& (this.mCurrentAnimator.isRunning()))
			this.mCurrentAnimator.cancel();
		Animator localAnimator = AnimatorInflater.loadAnimator(getContext(),
				animresid);
		localAnimator.setTarget(this);
		localAnimator.setStartDelay(delay);
		localAnimator.addListener(paramAnimatorListener);
		localAnimator.start();
		this.mCurrentAnimator = localAnimator;
	}
	
	private class QsAnimatorListener extends  AnimatorListenerAdapter{
		protected Runnable mEndRunnable;
		protected boolean mCanceled = false;
		public QsAnimatorListener(){
			mEndRunnable = null;
		}
		
		public QsAnimatorListener(Runnable onEndRunnable){
			mEndRunnable = onEndRunnable;
		}
		
		public QsAnimatorListener(QsAnimatorListener that){
			this.mEndRunnable = that.mEndRunnable;
		}
		
		public void onAnimationCancel(Animator paramAnimator) {
			mCanceled = true;
		}
		
		public void onAnimationEnd(Animator paramAnimator) {
			if(mEndRunnable != null)
				mEndRunnable.run();
		}
	}

	private class TrashCanFadeoutAdapter extends QsAnimatorListener {
		public TrashCanFadeoutAdapter() {
		}
		
		public TrashCanFadeoutAdapter(Runnable onEndRunnable) {
			super(onEndRunnable);
		}
		
		public TrashCanFadeoutAdapter(QsAnimatorListener that) {
			super(that);
		}

		public void onAnimationEnd(Animator paramAnimator) {
			Hotseat.this.getAllappsIcon().unsetTrashIcon();
			Hotseat.this.getAllappsIcon().setAnimating(false);
//			if (Hotseat.this.mShowHotseatTitle)
//				Hotseat.this.mAllappsIcon.setText(R.string.all_apps_button_label);
			Hotseat.this.getAllappsIcon().invalidate();
			
			super.onAnimationEnd(paramAnimator);
		}
	}

	private class TrashCanUnfillAdapter extends QsAnimatorListener {
		public TrashCanUnfillAdapter() {
		}

		public TrashCanUnfillAdapter(Runnable onEndRunnable) {
			super(onEndRunnable);
		}
		
		public TrashCanUnfillAdapter(QsAnimatorListener that) {
			super(that);
		}

		public void onAnimationCancel(Animator paramAnimator) {
			//this.mCanceled = true;
			super.onAnimationCancel(paramAnimator);
			
			getAllappsIcon().unsetTrashIcon();
			getAllappsIcon().setAnimating(false);
			getAllappsIcon().invalidate();
		}

		public void onAnimationEnd(Animator paramAnimator) {
			if (!this.mCanceled) {
				getAllappsIcon().unsetTrashIcon();
				startAnimator(R.animator.trashcan_fadeout, 100L,
						new TrashCanFadeoutAdapter(this));
			} else {
				super.onAnimationEnd(paramAnimator);
			}
		}
	}

	private class TrashCanFillAdapter extends QsAnimatorListener {
		public TrashCanFillAdapter() {
		}

		public TrashCanFillAdapter(Runnable onEndRunnable) {
			super(onEndRunnable);
		}
		
		public TrashCanFillAdapter(QsAnimatorListener that) {
			super(that);
		}

		public void onAnimationCancel(Animator paramAnimator) {
			//this.mCanceled = true;
			super.onAnimationCancel(paramAnimator);
			Hotseat.this.getAllappsIcon().unsetTrashIcon();
			Hotseat.this.getAllappsIcon().setAnimating(false);
			Hotseat.this.getAllappsIcon().invalidate();
		}

		public void onAnimationEnd(Animator paramAnimator) {
			if (!this.mCanceled){
				Hotseat.this.startAnimator(R.animator.trashcan_unfill, 100L,
						new TrashCanUnfillAdapter(this));
			}  else {
				super.onAnimationEnd(paramAnimator);
			}
		}
	}

	private class TrashCanLidShakeAdapter extends QsAnimatorListener {
		public TrashCanLidShakeAdapter() {
		}
		
		public TrashCanLidShakeAdapter(Runnable onEndRunnable) {
			super(onEndRunnable);
		}
		
		public TrashCanLidShakeAdapter(QsAnimatorListener that) {
			super(that);
		}

		public void onAnimationEnd(Animator paramAnimator) {
			if (!this.mCanceled){
				Hotseat.this.startAnimator(R.animator.trashcan_lid_down, 60L,
						new Hotseat.TrashCanLidDownAdapter(this));
			}  else {
				super.onAnimationEnd(paramAnimator);
			}
		}
	}

	private class TrashCanLidDownAdapter extends QsAnimatorListener {
		public TrashCanLidDownAdapter() {
		}

		public TrashCanLidDownAdapter(Runnable onEndRunnable) {
			super(onEndRunnable);
		}
		
		public TrashCanLidDownAdapter(QsAnimatorListener that) {
			super(that);
		}

		public void onAnimationEnd(Animator paramAnimator) {
			if ((!this.mCanceled) && getAllappsIcon().isTrashCanShakeMode()){
				startAnimator(R.animator.trashcan_lid_up, 200L,
						new Hotseat.TrashCanLidUpAdapter(this));
			} else {
				super.onAnimationEnd(paramAnimator);
			}
		}
	}

	private class TrashCanLidUpAdapter extends QsAnimatorListener {
		public TrashCanLidUpAdapter() {
		}

		public TrashCanLidUpAdapter(Runnable onEndRunnable) {
			super(onEndRunnable);
		}
		
		public TrashCanLidUpAdapter(QsAnimatorListener that) {
			super(that);
		}

		public void onAnimationEnd(Animator paramAnimator) {
			if (!this.mCanceled) {
				//Hotseat.access$302(Hotseat.this, 0.0F);
				//Hotseat.access$402(Hotseat.this, 1.0F);
				mPreviousLidShakeValue = 0.0f;
				mAngleDirection = 1.0F;
				startAnimator(R.animator.trashcan_lid_shake, 60L,
								new Hotseat.TrashCanLidShakeAdapter(this));
			} else {
				super.onAnimationEnd(paramAnimator);
			}
		}
	}
}
