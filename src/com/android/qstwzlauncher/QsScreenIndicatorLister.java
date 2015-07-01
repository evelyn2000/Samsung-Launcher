package com.android.qstwzlauncher;

public interface QsScreenIndicatorLister {
	
	public int getCurrentPage();
	public int getPageCount();
	public void setQsScreenIndicatorCallback(QsScreenIndicatorCallback callback);
	
	//protected QsScreenIndicatorCallback mQsWorkspaceCallback;
//	public void setQsScreenIndicatorCallback(QsScreenIndicatorCallback callback){
//		mQsWorkspaceCallback = callback;
//	}
}
