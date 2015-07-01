package com.android.qstwzlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import com.android.qstwzlauncher.FloatHodlerView;
import android.util.Log;

public class FloatHolderReceiver extends BroadcastReceiver {
//	private static FloatHodlerView mFloatHodlerView;
//	private static WindowManager wm;
	
	
	
	public FloatHolderReceiver() {
		// TODO Auto-generated constructor stub
		
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
//		if(LauncherLog.DEBUG_QS_FLOATBAR){
//			Log.e("lijia", "receive floatholder start:"+intent.getAction());
//		}
		final String action = intent.getAction();
		
		if(LauncherLog.DEBUG_QS_FLOATBAR){
			android.util.Log.v("QsLog", "FloatHolderReceiver::onReceive(0)=="
					+"==action:"+action);
		}
		
		final SharedPreferences pref = context.getSharedPreferences(FloatHodlerView.FLOATBAR_SHAREDPREF_NAME, Context.MODE_MULTI_PROCESS);
		
		if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
			
			int value = pref.getInt(FloatHodlerView.FLOATBAR_SHAREDPREF_STATE_KEY, FloatHodlerView.FLOATBAR_UNKOWN);
			if(value == FloatHodlerView.FLOATBAR_SHOW){
				addFloatBar(context, pref);
			} else if(value == FloatHodlerView.FLOATBAR_SHOWING){
				pref.edit().putInt(FloatHodlerView.FLOATBAR_SHAREDPREF_STATE_KEY, FloatHodlerView.FLOATBAR_HIDE).commit();
			}
			
		} else if(action.equals("android.intent.qs.ACTION_QS_SHOW_FLOATBAR")){

			int value = pref.getInt(FloatHodlerView.FLOATBAR_SHAREDPREF_STATE_KEY, FloatHodlerView.FLOATBAR_UNKOWN);
			
			if(LauncherLog.DEBUG_QS_FLOATBAR){
				Log.v("QsLog", "FloatHolderReceiver::onReceive(0)=="
						+"==state:"+value);
			}
			
			if(value > FloatHodlerView.FLOATBAR_HIDE){
				//if(value == FloatHodlerView.FLOATBAR_SHOW){
					context.sendBroadcast(new Intent(FloatHodlerView.FLOATBAR_REMOVE_INTENT_ACTION));
					
					pref.edit().putInt(FloatHodlerView.FLOATBAR_SHAREDPREF_STATE_KEY, FloatHodlerView.FLOATBAR_HIDE).commit();
				//}
				return;
			}

			addFloatBar(context, pref);
		}
	}
	
	private void addFloatBar(Context context, SharedPreferences pref){
		
		pref.edit().putInt(FloatHodlerView.FLOATBAR_SHAREDPREF_STATE_KEY, FloatHodlerView.FLOATBAR_SHOWING).commit();
		
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.float_holder_view, null);
		
		WindowManager wm = (WindowManager)context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

		final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
										WindowManager.LayoutParams.WRAP_CONTENT,
										WindowManager.LayoutParams.WRAP_CONTENT);

		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT | WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
		params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL 
        		| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        		| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		params.format = PixelFormat.TRANSLUCENT;  
		params.windowAnimations = 0;
		
//		Drawable dr = context.getResources().getDrawable(R.drawable.zzzz_float_holder_drager);
//		final int width = dr != null ? dr.getIntrinsicWidth() : WindowManager.LayoutParams.WRAP_CONTENT;
//		final int height = dr != null ? dr.getIntrinsicHeight() : WindowManager.LayoutParams.WRAP_CONTENT;
//		params.width = width;
//		params.height = height;
		params.gravity = Gravity.LEFT|Gravity.TOP;
		params.setTitle("qsfoatbar");
		
		//String str = pref.getString(FloatHodlerView.FLOATBAR_SHAREDPREF_POINT_KEY, "");
		//final Point pt = FloatHodlerView.parsePointString(str);
		
		if(LauncherLog.DEBUG_QS_FLOATBAR){
			Log.v("QsLog", "FloatHolderReceiver::onReceive(2)==" 
					+"==w:"+params.width
					+"==h:"+params.height);
		}

//		params.x = pt.x;
//		params.y = 100;//pt.y;
		
	    wm.addView(view, params);
	}

}
