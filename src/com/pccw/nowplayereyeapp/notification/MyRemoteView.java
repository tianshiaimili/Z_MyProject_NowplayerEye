package com.pccw.nowplayereyeapp.notification;
import java.util.Date;

import android.widget.RemoteViews;

import com.pccw.common.notification.layout.NotificationRemoteView;

public class MyRemoteView extends RemoteViews implements NotificationRemoteView {

	public MyRemoteView(String packageName, int layoutId) {
		super(packageName, layoutId);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getDateTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIcon() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setContent(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDateTime(Date arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setIcon(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTitle(String arg0) {
		// TODO Auto-generated method stub
		
	}

}
