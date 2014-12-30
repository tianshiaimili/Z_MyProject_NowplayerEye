package com.pccw.nowplayereyeapp.notification;
import java.io.StringReader;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.pccw.common.notification.usersetting.BaseUserSetting;
import com.pccw.common.notification.usersetting.UserSettingHelper;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.util.Log;

public class UserSetting extends BaseUserSetting {
	private static UserSetting _userSetting;
	public static UserSetting getInstance(Context context){
		if(_userSetting==null)
			_userSetting=new UserSetting(context);
		return _userSetting;			
	}
	
	public void save(){
		boolean saveSuccess  = UserSettingHelper.save(this.context, this);
		Log.d(UserSetting.class.getSimpleName(), "save user setting:"+saveSuccess);
	}
	
	private Context context;
	//private SharedPreferences preference;
	public UserSetting(Context context){
		super(context);
		this.context=context;		
		read();
	}
	
	public void read(){
		UserSettingHelper.readFromLocal(this.context, this);		
	}
	
	private String loginId;
//	@Override
//	public String getAppId() {
//		return "11";
//	}

	/**
	 * @param language the language to set
	 */
	@Override
	public void setLanguage(String language) {
		super.setLanguage(language.contains("zh")?"zh_TW":"en");
		Locale locale=new Locale(language);
		Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale=locale;        
		context.getResources().updateConfiguration(	config, context.getResources().getDisplayMetrics());
	}
	/**
	 * @return the loginId
	 */
	public String getLoginId() {
		return loginId;
	}
	/**
	 * @param loginId the loginId to set
	 */
	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	/*
	 save user setting 
<UserSetting><DeviceToken>b61241f1 e5cad089 5e40a694 af101987 4d0a2e05 6a0cb4ba c59c5127 8dd9565a</DeviceToken><DeviceType>iPhone2</DeviceType><Platform>iOS</Platform><AppId>5</AppId><AlertHours>9</AlertHours><Language>en</Language><PushAlertOn>Y</PushAlertOn><LoginId>20120113</LoginId></UserSetting>
 
read user setting
<UserSetting><DeviceToken>b61241f1 e5cad089 5e40a694 af101987 4d0a2e05 6a0cb4ba c59c5127 8dd9565a</DeviceToken><DeviceType>iPhone2</DeviceType><Platform>iOS</Platform><AppId>5</AppId></UserSetting> 
 
 
 return xml format
 
<?xml version="1.0" encoding="utf-8"?><UserSetting>

  <DeviceId>93</DeviceId>

  <DeviceToken>b61241f1 e5cad089 5e40a694 af101987 4d0a2e05 6a0cb4ba c59c5127 8dd9565a</DeviceToken>

  <DeviceType>iPhone2</DeviceType>

  <Platform>iOS</Platform>

  <AppId>5</AppId>

  <AlertHours>9</AlertHours>

  <Language>en</Language>

  <PushAlertOn>Y</PushAlertOn>

  <LoginId>20120113</LoginId>

</UserSetting>

	 */
	@Override
	public void setValuesByXML(String xml){
		if(xml==null)return;
        try {
        	DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();        
        	Document doc = db.parse(new InputSource(new StringReader(xml)));
        	this.setAlertHours(Integer.parseInt(doc.getElementsByTagName("AlertHours").item(0).getFirstChild().getNodeValue()));
        	this.setLanguage(doc.getElementsByTagName("Language").item(0).getFirstChild().getNodeValue());
        	this.setLoginId(doc.getElementsByTagName("LoginId").item(0).getFirstChild().getNodeValue());
        	this.setPushAlertOnStr(doc.getElementsByTagName("PushAlertOn").item(0).getFirstChild().getNodeValue());        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.toString());
		}
	}

	@Override
	public String toXML(){
		StringBuilder sb=new StringBuilder();
		sb.append("<UserSetting>");
		sb.append("<DeviceToken>"+this.getDeviceToken()+"</DeviceToken>");
		sb.append("<DeviceType>"+this.getDeviceType()+"</DeviceType>");
		sb.append("<Platform>"+this.getPlatform()+"</Platform>");
//		sb.append("<AppId>"+AppInfo.getAppId()+"</AppId>");
		sb.append("<AppId>"+this.getAppId()+"</AppId>");
		sb.append("<AppVersion>"+this.getAppVersion()+"</AppVersion>");
		sb.append("<AlertHours>"+this.getAlertHours()+"</AlertHours>");
		sb.append("<Language>"+this.getLanguage()+"</Language>");
		sb.append("<PushAlertOn>"+this.getPushAlertOnStr()+"</PushAlertOn>");
		sb.append("<LoginId>"+this.getLoginId()+"</LoginId>");
		sb.append("</UserSetting>");
		return sb.toString();
	}
	
	private String appVersion;
	@Override
	public String getAppVersion() {
		if(appVersion==null){
			try {
				appVersion=context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// TODO Auto-generated method stub
		return appVersion;
	}
	
}
