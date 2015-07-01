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

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.android.qstwzlauncher.R;
import com.android.qstwzlauncher.DropTarget.DragObject;

import android.util.DisplayMetrics;

public class AllAppsButtonDropTarget extends ButtonDropTarget
	implements Animator.AnimatorListener {//BubbleTextView implements DropTarget, DragController.DragListener {

    private static int DELETE_ANIMATION_DURATION = 250;
    private ColorStateList mOriginalTextColor;
    private int mHoverColor = 0xFFFF0000;
    private Drawable mAllAppsDrawable;
    private Drawable mRemoveDrawable;
    //private TransitionDrawable mCurrentDrawable;
    private CharSequence mAllAppsStr;
    private CharSequence mRemoveStr;


    public AllAppsButtonDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsButtonDropTarget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        // Get the drawable
        mOriginalTextColor = getTextColors();
        
        // Get the hover color
        Resources r = getResources();
        mHoverColor = r.getColor(R.color.delete_target_hover_tint);
        mHoverPaint.setColorFilter(new PorterDuffColorFilter(
                mHoverColor, PorterDuff.Mode.SRC_ATOP));
        mAllAppsDrawable = r.getDrawable(R.drawable.all_apps_button_icon);
        mRemoveDrawable = /*(TransitionDrawable)*/ r.getDrawable(R.drawable.ic_home_delete);
        
        mAllAppsStr = r.getString(R.string.all_apps_button_label);
        mRemoveStr = r.getString(R.string.delete_target_label);
        
        setCompoundDrawablesWithIntrinsicBounds(null, mAllAppsDrawable, null, null);
        
    	setText(mAllAppsStr);
    	
    	init();
    }

    private boolean isAllAppsApplication(DragSource source, Object info) {
        return (source instanceof AppsCustomizePagedView) && (info instanceof ApplicationInfo);
    }
    private boolean isAllAppsWidget(DragSource source, Object info) {
        return (source instanceof AppsCustomizePagedView) && (info instanceof PendingAddWidgetInfo);
    }
    private boolean isDragSourceWorkspaceOrFolder(DragObject d) {
        return (d.dragSource instanceof Workspace) || (d.dragSource instanceof Folder);
    }
    private boolean isWorkspaceOrFolderApplication(DragObject d) {
        return isDragSourceWorkspaceOrFolder(d) && (d.dragInfo instanceof ShortcutInfo);
    }
    private boolean isWorkspaceOrFolderWidget(DragObject d) {
        return isDragSourceWorkspaceOrFolder(d) && (d.dragInfo instanceof LauncherAppWidgetInfo);
    }
    private boolean isWorkspaceFolder(DragObject d) {
        return (d.dragSource instanceof Workspace) && (d.dragInfo instanceof FolderInfo);
    }
    
    
    @Override
    public boolean acceptDrop(DragObject d) {
        // We can remove everything including App shortcuts, folders, widgets, etc.
        return true;
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
    	super.onDragStart(source, info, dragAction);
    	
    	mActive = true;
        
    	setTrashIcon(true);
    }


    @Override
    public void onDragEnd() {
        super.onDragEnd();
        mActive = false;
        
        //super.setCompoundDrawablesWithIntrinsicBounds(null, mAllAppsDrawable, null, null);
    	//super.setText(mAllAppsStr);
    	unsetTrashIcon();
    }
    
    @Override
    public void onDragEnter(DragObject d) {
    	super.onDragEnter(d);

        //super.setPressed(true);
    }
    
    @Override
    public void onDragExit(DragObject d) {
    	super.onDragExit(d);

        //super.setPressed(false);
        
        if (!d.dragComplete) {
            //mCurrentDrawable.resetTransition();
            //setTextColor(mOriginalTextColor);
        }
    }

    private void animateToTrashAndCompleteDrop(final DragObject d) {
//        DragLayer dragLayer = mLauncher.getDragLayer();
//        Rect from = new Rect();
//        Rect to = new Rect();
//        dragLayer.getViewRectRelativeToSelf(d.dragView, from);
//        dragLayer.getViewRectRelativeToSelf(this, to);
//
//        int width = mRemoveDrawable.getIntrinsicWidth();
//        int height = mRemoveDrawable.getIntrinsicHeight();
//        to.set(to.left + getPaddingLeft(), to.top + getPaddingTop(),
//                to.left + getPaddingLeft() + width, to.bottom);
//
//        // Center the destination rect about the trash icon
//        int xOffset = (int) -(d.dragView.getMeasuredWidth() - width) / 2;
//        int yOffset = (int) -(d.dragView.getMeasuredHeight() - height) / 2;
//        to.offset(xOffset, yOffset);

        mDeferDropTargetBar.deferOnDragEnd();
        Runnable onAnimationEndRunnable = new Runnable() {
            @Override
            public void run() {
            	mDeferDropTargetBar.onDragEnd();
                mLauncher.exitSpringLoadedDragMode();
                completeDrop(d);
            }
        };
//        dragLayer.animateView(d.dragView, from, to, 0.1f, 0.1f,
//                DELETE_ANIMATION_DURATION, new DecelerateInterpolator(2),
//                new DecelerateInterpolator(1.5f), onAnimationEndRunnable, false);
        
        mDeferDropTargetBar.notifyDropComplete(d.dragView, onAnimationEndRunnable);
    }

    private void completeDrop(DragObject d) {
        ItemInfo item = (ItemInfo) d.dragInfo;
        if (LauncherLog.DEBUG) {
            LauncherLog.d(DragController.TAG, "DeleteDropTarget completeDrop: item = " + item + ",d = " + d);
        }
        if (isAllAppsApplication(d.dragSource, item)) {
            // Uninstall the application if it is being dragged from AppsCustomize
            //mLauncher.startApplicationUninstallActivity((ApplicationInfo) item);
        } else if (isWorkspaceOrFolderApplication(d)) {
            LauncherModel.deleteItemFromDatabase(mLauncher, item);
        } else if (isWorkspaceFolder(d)) {
            // Remove the folder from the workspace and delete the contents from launcher model
            FolderInfo folderInfo = (FolderInfo) item;
            mLauncher.removeFolder(folderInfo);
            LauncherModel.deleteFolderContentsFromDatabase(mLauncher, folderInfo);
        } else if (isWorkspaceOrFolderWidget(d)) {
            // Remove the widget from the workspace
            mLauncher.removeAppWidget((LauncherAppWidgetInfo) item);
            LauncherModel.deleteItemFromDatabase(mLauncher, item);

            final LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) item;
            final LauncherAppWidgetHost appWidgetHost = mLauncher.getAppWidgetHost();
            if (appWidgetHost != null) {
                // Deleting an app widget ID is a void call but writes to disk before returning
                // to the caller...
                new Thread("deleteAppWidgetId") {
                    public void run() {
                        appWidgetHost.deleteAppWidgetId(launcherAppWidgetInfo.appWidgetId);
                    }
                }.start();
            }
        }
    }
    @Override
    public void onDrop(DragObject d) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(DragController.TAG, "DeleteDropTarget onDrop: d = " + d);
        }
        animateToTrashAndCompleteDrop(d);
        
//        mDeferDropTargetBar.deferOnDragEnd();
//        Runnable onAnimationEndRunnable = new Runnable() {
//            @Override
//            public void run() {
//            	mDeferDropTargetBar.onDragEnd();
//                mLauncher.exitSpringLoadedDragMode();
//                completeDrop(d);
//            }
//        };
        
    }
    
    @Override
    public void getLocationInDragLayer(int[] loc) {
        //mLauncher.getHotseat().getLocationInDragLayer(this, loc);
    	View view = mLauncher.getHotseat();
    	if(view != null){
	    	loc[0] = this.getLeft() + view.getLeft();
	    	loc[1] = this.getTop() + view.getTop();
    	} else {
    		mLauncher.getDragLayer().getLocationInDragLayer(this, loc);
    	}
    	
    }
    
    private final float kLidCenterYFactor = 0.4F;
    private float mAllAppsIconAlpha;
    private boolean mIsAnimating = false;
    private boolean mIsTrashCanShakeMode;
    private float mLidAngle = 0.0F;
    private int mLidOffsetY = 0;
    private Paint mPaint = new Paint();
    private Bitmap mTrashBinOnly;
    private Bitmap mTrashBitmap;
    private Bitmap mTrashBitmapHover;
    private float mTrashCanAlpha;
    private Bitmap mTrashCanFill;
    private float mTrashCanFillAlpha;
    private boolean mTrashIconSet = false;
    private Bitmap mTrashLidOnly;

	private void init() {
		Resources localResources = getResources();
		BitmapFactory.Options localOptions = new BitmapFactory.Options();
		localOptions.inScaled = true;
		mTrashBitmapHover = BitmapFactory.decodeResource(localResources,
				R.drawable.hotseat_icon_delete_focus, localOptions);
		mTrashBitmap = BitmapFactory.decodeResource(localResources,
				R.drawable.hotseat_icon_delete, localOptions);
		mTrashCanFill = BitmapFactory.decodeResource(localResources,
				R.drawable.hotseat_delete_fill, localOptions);
		mTrashBinOnly = BitmapFactory.decodeResource(localResources,
				R.drawable.hotseat_icon_delete_can, localOptions);
		mTrashLidOnly = BitmapFactory.decodeResource(localResources,
				R.drawable.hotseat_icon_delete_lid, localOptions);
	}

	private void draw(Canvas paramCanvas, Bitmap paramBitmap, Rect paramRect,
			float paramFloat) {
		int i = getWidth() - paramBitmap.getWidth();
		mPaint.setAlpha((int) (255.0F * paramFloat));
		paramCanvas.drawBitmap(paramBitmap, paramRect.left + i / 2,
				paramRect.top, this.mPaint);
	}

	public void draw(Canvas paramCanvas) {
		
		if (!isAnimating()){
			super.draw(paramCanvas);
			return;
		}

		Rect localRect1 = paramCanvas.getClipBounds();
		localRect1.top += getPaddingTop();
		paramCanvas.save();
		Rect localRect2 = paramCanvas.getClipBounds();
		localRect2.top = (localRect2.top + localRect1.top + this.mTrashBinOnly
				.getHeight());
		paramCanvas.clipRect(new Rect(localRect2));
		super.draw(paramCanvas);
		paramCanvas.restore();
		
		if (this.mIsTrashCanShakeMode) {
			draw(paramCanvas, this.mTrashBinOnly, localRect1, 1.0F);
			localRect1.top -= this.mLidOffsetY;
			paramCanvas.save();
			int i = getWidth() / 2;
			int j = (int) (0.4F * mTrashLidOnly.getHeight());
			paramCanvas.rotate(mLidAngle, i + localRect1.left, j
					+ localRect1.top);
			draw(paramCanvas, mTrashLidOnly, localRect1, 1.0F);
			paramCanvas.restore();
			return;
		}
		
		if (mTrashCanAlpha > 0.0F){
			draw(paramCanvas, mTrashBitmapHover, localRect1,
					mTrashCanAlpha);
		}
		
		if (mTrashCanFillAlpha > 0.0F){
			draw(paramCanvas, mTrashCanFill, localRect1,
					mTrashCanFillAlpha);
		}
		
		if (mAllAppsIconAlpha <= 0.0F)
			return;
		
		Drawable localDrawable = getCompoundDrawables()[1];
		if (localDrawable == null)
			return;
		
		localDrawable.setAlpha((int) (255.0F * mAllAppsIconAlpha));
		super.draw(paramCanvas);
	}

	public float getTrashCanLidAngle() {
		return mLidAngle;
	}

	public boolean isAnimating() {
		if ((mIsAnimating) || (mIsTrashCanShakeMode))
			return true;
		
		return false;
	}

	public boolean isTrashCanShakeMode() {
		return mIsTrashCanShakeMode;
	}

	public void setAnimating(boolean paramBoolean) {
		mIsAnimating = paramBoolean;
		if (!paramBoolean)
			getCompoundDrawables()[1].setAlpha(255);
	}

	public void setIconAlpha(float paramFloat) {
		mAllAppsIconAlpha = paramFloat;
	}

	public void setTrashCanAlpha(float paramFloat) {
		mTrashCanAlpha = paramFloat;
	}

	public void setTrashCanFillAlpha(float paramFloat) {
		mTrashCanFillAlpha = paramFloat;
	}

	public void setTrashCanLidAngle(float paramFloat) {
		mLidAngle = paramFloat;
	}

	void setTrashCanLidOffset(int paramInt1, int paramInt2) {
		mLidOffsetY = paramInt2;
	}

	public void setTrashCanShakeMode(boolean paramBoolean) {
		mIsTrashCanShakeMode = paramBoolean;
	}

	public void setTrashIcon(boolean paramBoolean) {
		if (mTrashIconSet)
			return;
		
		mTrashIconSet = true;
		
		setIcon(mTrashBitmap);
		setText(mRemoveStr);
	}

	public void unsetTrashIcon()
    {
      //Context localContext = getContext();
      if (!mTrashIconSet)
            return;
            
        mTrashIconSet = false;
        
        super.setCompoundDrawablesWithIntrinsicBounds(null, mAllAppsDrawable, null, null);
    	super.setText(mAllAppsStr);
    }

    
//    private Animator mCurrentAnimator;
    public void onAnimationCancel(Animator paramAnimator)
    {
    }

    public void onAnimationEnd(Animator paramAnimator)
    {
    }

    public void onAnimationRepeat(Animator paramAnimator)
    {
    }

    public void onAnimationStart(Animator paramAnimator)
    {
      //this.mIsAnimating = true;
    }
//    
//    private void startTrashCanFillUnfillAnimation()
//    {
//      ArrayList localArrayList = new ArrayList(2);
//      Animator localAnimator1 = AnimatorInflater.loadAnimator(getContext(), R.animator.trashcan_fill);
//      localAnimator1.setTarget(this);
//      localAnimator1.addListener(this);
//      localAnimator1.setStartDelay(0L);
//      localArrayList.add(localAnimator1);
//      Animator localAnimator2 = AnimatorInflater.loadAnimator(getContext(), R.animator.trashcan_unfill);
//      localAnimator2.setTarget(this);
//      localAnimator2.addListener(this);
//      localAnimator2.setStartDelay(100L);
//      localArrayList.add(localAnimator2);
//      Animator localAnimator3 = AnimatorInflater.loadAnimator(getContext(), R.animator.trashcan_fadeout);
//      localAnimator3.setTarget(this);
//      localAnimator3.addListener(new AnimatorListenerAdapter()
//      {
//        public void onAnimationCancel(Animator paramAnimator)
//        {
//          //DeleteDropTarget.this.setAnimating(false);
//        }
//
//        public void onAnimationEnd(Animator paramAnimator)
//        {
//          //DeleteDropTarget.this.setAnimating(false);
//        }
//
//        public void onAnimationRepeat(Animator paramAnimator)
//        {
//        }
//
//        public void onAnimationStart(Animator paramAnimator)
//        {
//          //DeleteDropTarget.this.setAnimating(true);
//        }
//      });
//      localAnimator3.setStartDelay(0L);
//      localArrayList.add(localAnimator3);
//      AnimatorSet localAnimatorSet = new AnimatorSet();
//      localAnimatorSet.playSequentially(localArrayList);
//      localAnimatorSet.start();
//    }
}
