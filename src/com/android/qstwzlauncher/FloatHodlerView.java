package com.android.qstwzlauncher;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.android.internal.util.XmlUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;



public class FloatHodlerView extends ViewGroup{
	
	private Context mContext;

	private GridView mGridView;
	private View mEditBtn;
	private ImageView mDragBar;
	private View mContainer;
	
	private final WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams(
							WindowManager.LayoutParams.WRAP_CONTENT,
							WindowManager.LayoutParams.WRAP_CONTENT); 
	
	private final WindowManager mWindowManager;// = (WindowManager)getContext().getApplicationContext().getSystemService(getContext().WINDOW_SERVICE);
	private BroadcastReceiver mBroadcastReceiver;
	
	
	public final static int FLOATBAR_UNKOWN = 0;
	public final static int FLOATBAR_HIDE = 1;
	public final static int FLOATBAR_SHOWING = 2;
	public final static int FLOATBAR_SHOW = 3;
	
	public final static String FLOATBAR_SHAREDPREF_NAME = "qs.floatbar.name";
	public final static String FLOATBAR_SHAREDPREF_GRAVITY_KEY = "qs.key.gravity";
	public final static String FLOATBAR_SHAREDPREF_PERCENT_KEY = "qs.key.percent";
	public final static String FLOATBAR_SHAREDPREF_STATE_KEY = "qs.key.state";
	
	
	private int mStatusBarHeight;
	private int mDragBarLeftOrTopPer = 50;
	private int mDragBarGravity;
	private final int mContainerSize;
	private LoadAppsTask mLoadAppsTask;
	private int mCurrentOrientation;
	private boolean mIsAttached = false;
	
	private AppAdapter mAppAdapter;
	static ArrayList<AppItemInfo> mAppItemPackages;
	
	private SharedPreferences mSharedPreferences;
	
	public final static String FLOATBAR_REMOVE_INTENT_ACTION = "com.android.qs.QS_ACTION_REMOVE_FLOATHOLDERVIEW";
	
	public FloatHodlerView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public FloatHodlerView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public FloatHodlerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		super.setClickable(true);
		super.setFocusableInTouchMode(true);
		mContext = context;
		
		mCurrentOrientation = context.getResources().getConfiguration().orientation;
		
		//mDragBarDrawable = context.getResources().getDrawable(R.drawable.zzzz_float_holder_drager);
		mContainerSize = context.getResources().getDimensionPixelSize(R.dimen.zzzz_qs_floatHondler_container_size);
		mWindowManager = (WindowManager)context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		
		mStatusBarHeight = mContext.getResources().getDimensionPixelSize(
                com.android.internal.R.dimen.status_bar_height);
		
		mSharedPreferences = context.getSharedPreferences(FLOATBAR_SHAREDPREF_NAME, Context.MODE_MULTI_PROCESS);
		
		mDragBarLeftOrTopPer = mSharedPreferences.getInt(FLOATBAR_SHAREDPREF_PERCENT_KEY, 50);
		mDragBarGravity = mSharedPreferences.getInt(FLOATBAR_SHAREDPREF_GRAVITY_KEY, Gravity.LEFT);
	}


	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		final boolean isSmallMode = (mContainer.getVisibility() != View.VISIBLE);
		final int width = (r - l);
		final int height = (b - t);
		if(LauncherLog.DEBUG_QS_FLOATBAR){
			Log.i("QsLog", "FloatHodlerView::onLayout(0)=="
					+"==isSmallMode:"+isSmallMode
					+"==l:"+l
					+"==t:"+t
					+"==w:"+width
					+"==h:"+height
					+"==width:"+mWindowParams.width
					+"==height:"+mWindowParams.height
					+"==x:"+mWindowParams.x
					+"==y:"+mWindowParams.y);
		}
		
		if(mContainer.getVisibility() == View.VISIBLE){
			if(mDragBarGravity == Gravity.TOP ||
	        		mDragBarGravity  == Gravity.BOTTOM){
				mContainer.layout(0, 0, r, mContainer.getMeasuredHeight());
				int x = (int)(mDragBarLeftOrTopPer * (r - l) / 100);
				if(mDragBar != null)
					mDragBar.layout(x, mContainer.getMeasuredHeight(), x + mDragBar.getMeasuredWidth(), b);
				
			} else {
				mContainer.layout(0, 0, mContainer.getMeasuredWidth(), b);
				int y = (int)(mDragBarLeftOrTopPer * (b - t) / 100);
				if(mDragBar != null)
					mDragBar.layout(mContainer.getMeasuredWidth(), y, r, y + mDragBar.getMeasuredHeight());
			}
		} else {
			mDragBar.layout(l, t, r, b);
		}

		if(LauncherLog.DEBUG_QS_FLOATBAR){
			Log.i("QsLog", "FloatHodlerView::onLayout(1)=="
					+"==isSmallMode:"+isSmallMode
					+"==l:"+l
					+"==t:"+t
					+"==w:"+width
					+"==h:"+height
					+"==width:"+mWindowParams.width
					+"==height:"+mWindowParams.height
					+"==x:"+mWindowParams.x
					+"==y:"+mWindowParams.y);
		}
	}

	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int specWidth = MeasureSpec.getSize(widthMeasureSpec);
		int specHeight = MeasureSpec.getSize(heightMeasureSpec);
		
		if(LauncherLog.DEBUG_QS_FLOATBAR){
			Log.i("QsLog", "FloatHodlerView::onMeasure(0)=="
					+"==specWidth:"+specWidth
					+"==specHeight:"+specHeight
					+"==x:"+mWindowParams.x
					+"==y:"+mWindowParams.y);
		}
		
		//int count = getChildCount();
		final int nMaxWidth = specWidth - super.getPaddingLeft() - super.getPaddingRight();
        final int nMaxHeight = specHeight - super.getPaddingTop() - super.getPaddingBottom();
        
        if(mDragBar != null){
	        mDragBar.measure(MeasureSpec.makeMeasureSpec(mDragBar.getMeasuredWidth(), MeasureSpec.UNSPECIFIED), 
	        		MeasureSpec.makeMeasureSpec(mDragBar.getMeasuredHeight(), MeasureSpec.UNSPECIFIED));
        }
        
        final boolean isSmallMode = (mContainer.getVisibility() != View.VISIBLE);
        
        if(LauncherLog.DEBUG_QS_FLOATBAR){
			Log.i("QsLog", "FloatHodlerView::onMeasure(1)=="
					+"==x:"+mWindowParams.x
					+"==y:"+mWindowParams.y);
		}
        
        if(isSmallMode){
        	if(mDragBar != null){
        		super.setMeasuredDimension(mDragBar.getMeasuredWidth(), mDragBar.getMeasuredHeight());
        	} else {
        		//super.setMeasuredDimension(mDragBarDrawable.getIntrinsicWidth(), mDragBarDrawable.getIntrinsicHeight());
        	}
        } else {
        	if(mDragBarGravity == Gravity.TOP ||
        		mDragBarGravity  == Gravity.BOTTOM){
        		
        		mContainer.measure(MeasureSpec.makeMeasureSpec(nMaxWidth, MeasureSpec.EXACTLY), 
        				MeasureSpec.makeMeasureSpec(mContainerSize, MeasureSpec.EXACTLY));
        		
        		setMeasuredDimension(specWidth, mContainerSize + mDragBar.getMeasuredHeight());
        		
        	} else {
        		
        		mContainer.measure(MeasureSpec.makeMeasureSpec(mContainerSize, MeasureSpec.EXACTLY), 
        				MeasureSpec.makeMeasureSpec(nMaxHeight - mStatusBarHeight, MeasureSpec.EXACTLY));
        		
        		setMeasuredDimension(mContainerSize + mDragBar.getMeasuredWidth(), specHeight);
        	}
        }
	}
	
	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		if(LauncherLog.DEBUG_QS_FLOATBAR){

			Log.i("QsLog", "FloatHodlerView::onFinishInflate(0)=="
					+"==width:"+mWindowParams.width
					+"==height:"+mWindowParams.height
					+"==x:"+mWindowParams.x
					+"==y:"+mWindowParams.y);
		}
		mContainer = super.findViewWithTag("container");
		if(mContainer != null){
			mGridView = (GridView)mContainer.findViewWithTag("gridview");
			if(mGridView != null){
				mAppAdapter = new AppAdapter();
				mGridView.setAdapter(mAppAdapter);
				mGridView.setClickable(true);
				mGridView.setFocusableInTouchMode(true);
			}
			mEditBtn = mContainer.findViewWithTag("editbtn");
		}
		mDragBar = (ImageView)super.findViewWithTag("dragbar");
		if(mDragBar != null){
			mDragBar.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					showOrHideApplications((mContainer.getVisibility() != View.VISIBLE), true);
				}
			});
			
			mDragBar.setOnLongClickListener(new View.OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					// TODO Auto-generated method stub
					//v.setOnDragListener(l)
					//v.startDrag(null, shadowBuilder, myLocalState, flags)
					return true;
				}
			});
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
	}

	private boolean isSmallMode(){
		return (mContainer.getVisibility() != View.VISIBLE);
	}

	@Override
	protected void onAttachedToWindow() {
		// TODO Auto-generated method stub
		super.onAttachedToWindow();
		if(LauncherLog.DEBUG_QS_FLOATBAR){
			Log.i("QsLog", "FloatHodlerView::onAttachedToWindow(0)=="
					+"==width:"+mWindowParams.width
					+"==height:"+mWindowParams.height
					+"==x:"+mWindowParams.x
					+"==y:"+mWindowParams.y
					+"==mIsAttached:"+mIsAttached);
		}
		
		if(!mIsAttached){
			
			if(super.getHandler() != null)
				super.getHandler().post(mLoadAppsRunnable);

			mWindowParams.copyFrom((WindowManager.LayoutParams)getLayoutParams());
			showOrHideApplications((mContainer.getVisibility() == View.VISIBLE), true);

			registBroadCastReceiver();
			
			mSharedPreferences.edit().putInt(FLOATBAR_SHAREDPREF_STATE_KEY, FloatHodlerView.FLOATBAR_SHOW).commit();
		}
		
		mIsAttached = true;
	}

	@Override
	protected void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
		mIsAttached = false;
		
		if(LauncherLog.DEBUG_QS_FLOATBAR){
			Log.i("QsLog", "FloatHodlerView::onDetachedFromWindow(2)==");
		}
		
		if(mLoadAppsTask != null)
			mLoadAppsTask.cancel(true);
		
		mSharedPreferences.edit().putInt(FLOATBAR_SHAREDPREF_STATE_KEY, FloatHodlerView.FLOATBAR_HIDE).commit();
		
		mContext.unregisterReceiver(mBroadcastReceiver);

	}
	
	private void showOrHideApplications(boolean show, boolean updatelayout){
		final Drawable dr = mDragBar != null ? mDragBar.getDrawable() : null;
		
		final int barWidth = dr != null ? dr.getIntrinsicWidth() : WindowManager.LayoutParams.WRAP_CONTENT;
		final int barHeight = dr != null ? dr.getIntrinsicHeight() : WindowManager.LayoutParams.WRAP_CONTENT;
		if(LauncherLog.DEBUG_QS_FLOATBAR){
			Log.i("QsLog", "FloatHodlerView::showOrHideApplications(1)=="
					+"==width:"+mWindowParams.width
					+"==height:"+mWindowParams.height
					+"==x:"+mWindowParams.x
					+"==y:"+mWindowParams.y
					+"==width:"+barWidth
					+"==height:"+barHeight);
		}
		if(show){
			
			final DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
			if(mDragBarGravity == Gravity.TOP ||
	        		mDragBarGravity  == Gravity.BOTTOM){
				mWindowParams.width = dm.widthPixels;
				mWindowParams.height = barHeight + mContainerSize;
			} else {
				mWindowParams.width = barWidth + mContainerSize;
				mWindowParams.height = dm.heightPixels - mStatusBarHeight;
			}
			
			if(mDragBarGravity == Gravity.RIGHT){
				mWindowParams.x = dm.widthPixels - mWindowParams.width;
			} else {
				mWindowParams.x = 0;
			}
			
			if(mDragBarGravity == Gravity.BOTTOM){				
				mWindowParams.y = dm.heightPixels - mWindowParams.height;
			} else {
				mWindowParams.y = mStatusBarHeight;
			}
			
		} else {

			mWindowParams.width = barWidth;
			mWindowParams.height = barHeight;
		}
		
		mContainer.setVisibility(show ? View.VISIBLE : View.GONE);
		
		if(updatelayout)
			updateScreenLayoutView();
	}
	
	private void updateScreenLayoutView(){

		final DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		if(mContainer.getVisibility() == View.VISIBLE){
			
			if(mDragBarGravity == Gravity.TOP ||
	        		mDragBarGravity  == Gravity.BOTTOM){
				mWindowParams.width = dm.widthPixels;
			} else {
				mWindowParams.height = dm.heightPixels - mStatusBarHeight;
			}

		} else {
			
			if(mDragBarGravity == Gravity.TOP ||
	        		mDragBarGravity  == Gravity.BOTTOM){
				
				mWindowParams.x = (int)(dm.widthPixels * mDragBarLeftOrTopPer / 100);
				
				if(mDragBarGravity == Gravity.TOP)
					mWindowParams.y = mStatusBarHeight;
				else
					mWindowParams.y = dm.heightPixels - mWindowParams.height;
				
			} else {
				
				if(mDragBarGravity == Gravity.LEFT){
					mWindowParams.x = 0;
				} else {
					mWindowParams.x = dm.widthPixels - mWindowParams.width;
				}
				
				mWindowParams.y = mStatusBarHeight + (int)((dm.heightPixels - mStatusBarHeight) * mDragBarLeftOrTopPer / 100);
			}
		}
		
		mWindowManager.updateViewLayout(this, mWindowParams);
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		if(LauncherLog.DEBUG_QS_FLOATBAR){
			Log.i("QsLog", "FloatHodlerView::onConfigurationChanged(2)==");
		}
		
		if(mCurrentOrientation != newConfig.orientation){
			mCurrentOrientation = newConfig.orientation;
			updateScreenLayoutView();
		}
	}
	
	private void registBroadCastReceiver(){
		if(mBroadcastReceiver == null){
			mBroadcastReceiver = new BroadcastReceiver() {
				
				@Override
				public void onReceive(Context context, Intent intent) {
					// TODO Auto-generated method stub
					if(intent.getAction().equals(FLOATBAR_REMOVE_INTENT_ACTION))
					{
						mWindowManager.removeView(FloatHodlerView.this);
					}
				}
			};
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction(FLOATBAR_REMOVE_INTENT_ACTION);
		mContext.registerReceiver(mBroadcastReceiver, filter);
	}
	
	public final static String FLOATBAR_APPLICATION_SAVED_FILE = "qs.floatbar.apps.xml";
	public final static String FLOATBAR_APPLICATION_DIR_NAME = "qs.floatbar";
	private static final String TAG_TOPPACKAGES = "toppackages";
    private static final String TAG_TOPPACKAGE = "TopPackage";
    
	private boolean LoadAllApps(){
		// 
		if (mAppItemPackages == null) {
			mAppItemPackages = new ArrayList<AppItemInfo>();
    	} else {
        	mAppItemPackages.clear();
		}
		if(LauncherLog.DEBUG_QS_FLOATBAR){
			Log.i("QsLog", "FloatHodlerView::LoadAllApps(2)==");
		}
		File dirFile = mContext.getDir(FLOATBAR_APPLICATION_DIR_NAME, Context.MODE_WORLD_READABLE);
		if(!dirFile.exists())
			dirFile.mkdirs();
		
		File pkgFile = new File(dirFile, FLOATBAR_APPLICATION_SAVED_FILE);
		if(!pkgFile.exists()){
			//pkgFile.createNewFile();
			loadAllDefaultApps();
		} else {
		
			FileReader permReader = null;
	        try {
	            permReader = new FileReader(pkgFile);
	        } catch (FileNotFoundException e) {
	            //Slog.w(TAG, "Couldn't find or open permissions file " + permFile);
	            return false;
	        }
	
	        try {
	            XmlPullParser parser = Xml.newPullParser();
	            parser.setInput(permReader);
	
	            XmlUtils.beginDocument(parser, TAG_TOPPACKAGES);
	
	            while (true) {
	                XmlUtils.nextElement(parser);
	                if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
	                    break;
	                }
	
	                String name = parser.getName();
	                if (TAG_TOPPACKAGE.equals(name)) {
	                	
	                    String strPkg = parser.getAttributeValue(null, "packageName");
	                    if (strPkg == null || strPkg.isEmpty()) {
	                    	XmlUtils.skipCurrentTag(parser);
	                        continue;
	                    } 
	                    
	                    String strClass = parser.getAttributeValue(null, "className");
	                    if (strClass == null || strClass.isEmpty()) {
	                    	XmlUtils.skipCurrentTag(parser);
	                        continue;
	                    } 
	                    
	                    mAppItemPackages.add(new AppItemInfo(strPkg, strClass));
	                    
	                    XmlUtils.skipCurrentTag(parser);
	                    continue;
	                    
	                } else {
	                    XmlUtils.skipCurrentTag(parser);
	                    continue;
	                }
	
	            }
	            permReader.close();
	        } catch (XmlPullParserException e) {
	            //Slog.w(TAG, "Got execption parsing permissions.", e);
	        	return false;
	        } catch (IOException e) {
	           // Slog.w(TAG, "Got execption parsing permissions.", e);
	        	return false;
	        }
		}
		
		updateAllAppsInfo();

		return true;
	}
	
	private void loadAllDefaultApps(){
		if(LauncherLog.DEBUG_QS_FLOATBAR){
			Log.i("QsLog", "FloatHodlerView::loadAllDefaultApps(2)==");
		}
		try {
            XmlResourceParser parser = mContext.getResources().getXml(R.xml.default_floatbar_package);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            XmlUtils.beginDocument(parser, TAG_TOPPACKAGES);

            final int depth = parser.getDepth();

            int type;
            while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth) 
            		&& type != XmlPullParser.END_DOCUMENT) {

                if (type != XmlPullParser.START_TAG) {
                    continue;
                }                    

                TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.TopPackage);                    
                
                mAppItemPackages.add(new AppItemInfo(a.getString(R.styleable.TopPackage_topPackageName),
                		a.getString(R.styleable.TopPackage_topClassName),
                		a.getInt(R.styleable.TopPackage_topOrder, 0)));
                if(LauncherLog.DEBUG_QS_FLOATBAR){
        			Log.i("QsLog", "FloatHodlerView::loadAllDefaultApps(2)==pkg:"+a.getString(R.styleable.TopPackage_topPackageName));
        		}
                //Xlog.d(TAG, "loadTopPackage packageName==" + a.getString(R.styleable.TopPackage_topPackageName)); 
               // Xlog.d(TAG, "loadTopPackage className==" + a.getString(R.styleable.TopPackage_topClassName));

                a.recycle();
            }
        } catch (XmlPullParserException e) {
        	//Xlog.w(TAG, "Got exception parsing toppackage.", e);
        } catch (IOException e) {
        	//Xlog.w(TAG, "Got exception parsing toppackage.", e);
        }
	}
	
	private void updateAllAppsInfo(){
		if(mAppItemPackages == null)
			return;
		
		final PackageManager packageManager = mContext.getPackageManager();
		 
		Intent mainIntent = new Intent(Intent.ACTION_MAIN);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> listApps = packageManager.queryIntentActivities(mainIntent, 0);
		if(listApps != null && listApps.size() > 0){
			for(AppItemInfo info : mAppItemPackages){
				
				if(info.mClassName == null)
					continue;
				
				for(ResolveInfo item : listApps){
					if(info.mClassName.equals(item.activityInfo.name)){
						info.mTitle = item.activityInfo.loadLabel(packageManager);
						info.mIcon = item.activityInfo.loadIcon(packageManager);
						break;
					}
				}
			}
		}
	}
	
	private boolean saveAllApps(){
		if (mAppItemPackages == null || mAppItemPackages.size() == 0) {
			return false;
    	}
		
		File dirFile = mContext.getDir(FLOATBAR_APPLICATION_DIR_NAME, Context.MODE_WORLD_READABLE);
		if(!dirFile.exists())
			dirFile.mkdirs();
		
		File pkgFile = new File(dirFile, FLOATBAR_APPLICATION_SAVED_FILE);
		if(pkgFile.exists()){
			pkgFile.delete();//.createNewFile();
		}
		
		boolean ret = true;

		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		
		try{
			
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", TAG_TOPPACKAGES);
			
			for(AppItemInfo info : mAppItemPackages){
				
				serializer.startTag("", TAG_TOPPACKAGE);
				
				serializer.attribute("", "packageName", info.mPackageName);
				serializer.attribute("", "className", info.mClassName);
				
				serializer.endTag("", TAG_TOPPACKAGE);
			}
			serializer.endTag("", TAG_TOPPACKAGES);
			serializer.endDocument();

		} catch(Exception e) {
			//throw new RuntimeException(e);
			ret = false;
		}
		
		try{
			pkgFile.createNewFile();
			
			FileOutputStream os =  new FileOutputStream(pkgFile);
			//FileOutputStreamWriter 
			os.write(writer.toString().getBytes());
			os.flush();
			os.close();
			
		} catch (FileNotFoundException e){
			ret = false;
		} catch (IOException  e){
			ret = false;
		}
		
		if(!ret){
			pkgFile.delete();
		}
		
		return false;
	}
	
	private class LoadAppsTask extends AsyncTask<Boolean, Void, Boolean> { 
        protected Boolean doInBackground(Boolean... types) {
        	Boolean isSave = types[0];
        	
           return (isSave ? saveAllApps() : LoadAllApps());//SaveBitmapToFileEx(bmp);
        } 
    
        protected void onPreExecute() {
            
        }
        
        protected void onPostExecute(Boolean unused) { 
        	if(this == mLoadAppsTask)
        		mLoadAppsTask = null;
        	
        	if(unused && !isCancelled() && mAppAdapter != null){
    			mAppAdapter.notifyDataSetChanged();
    		}
        	
        	//if(QS_ENABLE_DEBUG_INFO){
			//	android.util.Log.e("QsLog", "QsFloatCaptureView::SaveImageTask::onPostExecute()==show result:"+unused);
			//}
        	
        	//if(!isCancelled())
        	//	sHandler.sendEmptyMessage(MSG_ON_STOTED);
        } 
    } 
	
	private final Runnable mLoadAppsRunnable = new Runnable() {
        public void run() {
        	mLoadAppsTask = new LoadAppsTask();//.execute(mSetWallpaperType);
        	mLoadAppsTask.execute(false);
        }
    };

	
	private class AppItemInfo{
		public String mPackageName;
		public String mClassName;
    	public int mOrder;
		public Drawable mIcon;
		public CharSequence mTitle;
		
		public AppItemInfo(String packagename,String classname, int order){
			mPackageName = packagename;
    		mClassName = classname;
    		mOrder = order;
    		mIcon = null;
    		mTitle = "";
		}
		
		public AppItemInfo(String packagename,String classname){
			this(packagename, classname, 0);
		}
	}
	
	public class AppAdapter extends BaseAdapter{

		public int getCount() {
			// TODO Auto-generated method stub
			return mAppItemPackages != null ? mAppItemPackages.size() : 0;
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mAppItemPackages != null ? mAppItemPackages.get(position) : null;
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			if(convertView == null)
			{
				convertView = parent.inflate(mContext, R.layout.floatbar_grid_view_item, null);
			}
			
			convertView.setClickable(false);
			ImageView img = (ImageView) convertView.findViewWithTag("icon");
			TextView text = (TextView)convertView.findViewWithTag("title");
			//LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(50, 50);
			//img.setLayoutParams(params);
			AppItemInfo info = mAppItemPackages.get(position);
			//ResolveInfo info = 
			img.setImageDrawable(info.mIcon);
			
			text.setText(info.mTitle);
			convertView.setTag(info);
			convertView.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(v.getTag() instanceof AppItemInfo)
					{
						AppItemInfo info = (AppItemInfo) v.getTag();
						Intent intent = new Intent(Intent.ACTION_MAIN);
						intent.setComponent(new ComponentName(info.mPackageName, info.mClassName));
						intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS|Intent.FLAG_ACTIVITY_NEW_TASK);
						mContext.startActivity(intent);
					}
				}
			});
			
			return convertView;
		}
		
	}
}
