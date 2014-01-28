package io.evercam.connect.db;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * CameraOperation
 * 
 * Interact with local SQLite database with discovered results.
 */

public class CameraOperation
{

	private DatabaseHelper database;
	private SQLiteDatabase db;

	public CameraOperation(Context ctxt)
	{
		database = new DatabaseHelper(ctxt);
		
	}

	public void insertScanCamera(Camera camera, String ssid)
	{
		db = database.getWritableDatabase();
		db.execSQL(
				"insert into cameralist(ip,mac,vendor,flag,firstseen,lastseen,ssid)values(?,?,?,?,?,?,?)",
				new Object[] { camera.getIP(), camera.getMAC(),
						camera.getVendor(), camera.getFlag(),
						camera.getFirstSeen(), camera.getLastSeen(), ssid });
		db.close();

	}

	public void updateScanCamera(Camera camera, String ssid)
	{
		db = database.getWritableDatabase();
		db.execSQL(
				"update cameralist set mac=?,vendor=?,lastseen=?,flag=?  where ip=? and ssid=?",
				new Object[] { camera.getMAC(), camera.getVendor(),
						camera.getLastSeen(), camera.getFlag(), camera.getIP(),
						ssid });
		db.close();
	}

	public void insertCamera(Camera camera, String ssid)
	{
		db = database.getWritableDatabase();
		db.execSQL(
				"insert into cameralist(ip,mac,vendor,model,upnp, onvif, bonjour,http,https,rtsp,ftp, ssh, portforwarded, evercam,exthttp,exthttps, extftp,extrtsp,extssh, flag,firstseen,lastseen,username, password, ssid)values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
				new Object[] { camera.getIP(), camera.getMAC(),
						camera.getVendor(), camera.getModel(),
						camera.getUpnp(), camera.getOnvif(),
						camera.getBonjour(), camera.getHttp(),
						camera.getHttps(), camera.getRtsp(), camera.getFtp(),
						camera.getSsh(), camera.getPortForwarded(),
						camera.getEvercamConnected(), camera.getExthttp(),
						camera.getExthttps(), camera.getExtftp(),
						camera.getExtrtsp(), camera.getExtssh(),
						camera.getFlag(), camera.getFirstSeen(),
						camera.getLastSeen(), camera.getUsername(),
						camera.getPassword(), ssid });
		db.close();
	}

	public void updateBonjourCamera(Camera camera, String ssid)
	{
		db = database.getWritableDatabase();
		db.execSQL(
				"update cameralist set mac=?,vendor=?,model=?,http=?,bonjour=?,lastseen=?  where ip=? and ssid=?",
				new Object[] { camera.getMAC(), camera.getVendor(),
						camera.getModel(), camera.getHttp(),
						camera.getBonjour(), camera.getLastSeen(),
						camera.getIP(), ssid });
		db.close();
	}

	public void updateUpnpCamera(Camera camera, String ssid)
	{
		db = database.getWritableDatabase();
		db.execSQL(
				"update cameralist set model=?,http=?,upnp=?,lastseen=?  where ip=? and ssid=?",
				new Object[] { camera.getModel(), camera.getHttp(), 1,
						camera.getLastSeen(), camera.getIP(), ssid });
		db.close();
	}

	// check camera(ip,ssid) existing in table or not
	public boolean isExisting(String ip, String ssid)
	{
		db = database.getWritableDatabase();
		Cursor c = db.rawQuery(
				"select * from cameralist where ip=? and ssid=?", new String[] {
						ip, ssid });
		if (c.moveToFirst())
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean isMacExisting(String mac, String ssid)
	{
		db = database.getWritableDatabase();
		Cursor c = db.rawQuery(
				"select * from cameralist where mac=? and ssid=?",
				new String[] { mac, ssid });
		if (c.moveToFirst())
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	// read camera info from local database
	public Camera getCamera(String ip, String ssid)
	{
		db = database.getWritableDatabase();
		Camera camera = new Camera(ip);
		Cursor c = db.rawQuery(
				"select * from cameralist where ip=? and ssid=?", new String[] {
						ip, ssid });
		if (c.moveToFirst())
		{
			String mac = c.getString(c.getColumnIndex("mac"));
			camera.setMAC(mac);
			String model = c.getString(c.getColumnIndex("model"));
			camera.setModel(model);
			String vendor = c.getString(c.getColumnIndex("vendor"));
			camera.setVendor(vendor);
			int http = c.getInt(c.getColumnIndex("http"));
			camera.setHttp(http);
			int https = c.getInt(c.getColumnIndex("https"));
			camera.setHttps(https);
			int rtsp = c.getInt(c.getColumnIndex("rtsp"));
			camera.setRtsp(rtsp);
			int ftp = c.getInt(c.getColumnIndex("ftp"));
			camera.setFtp(ftp);
			int ssh = c.getInt(c.getColumnIndex("ssh"));
			camera.setSsh(ssh);

			int exthttp = c.getInt(c.getColumnIndex("exthttp"));
			camera.setExthttp(exthttp);
			int extrtsp = c.getInt(c.getColumnIndex("extrtsp"));
			camera.setExtrtsp(extrtsp);
			int exthttps = c.getInt(c.getColumnIndex("exthttps"));
			camera.setExthttps(exthttps);
			int extftp = c.getInt(c.getColumnIndex("extftp"));
			camera.setExtftp(extftp);
			int extssh = c.getInt(c.getColumnIndex("extssh"));
			camera.setExtssh(extssh);
			String firstseen = c.getString(c.getColumnIndex("firstseen"));
			camera.setFirstSeen(firstseen);
			String lastseen = c.getString(c.getColumnIndex("lastseen"));
			camera.setLastSeen(lastseen);

			String username = c.getString(c.getColumnIndex("username"));
			camera.setUsername(username);
			String password = c.getString(c.getColumnIndex("password"));
			camera.setPassword(password);

			int flag = c.getInt(c.getColumnIndex("flag"));
			camera.setFlag(flag);
			int bonjour = c.getInt(c.getColumnIndex("bonjour"));
			camera.setBonjour(bonjour);
			int upnp = c.getInt(c.getColumnIndex("upnp"));
			camera.setUpnp(upnp);
			int onvif = c.getInt(c.getColumnIndex("onvif"));
			camera.setOnvif(onvif);
			camera.setSsid(ssid);
		}
		db.close();
		return camera;
	}

	public void updateAttributeInt(String ip, String ssid, String attribute,
			int value)
	{
		db = database.getWritableDatabase();
		db.execSQL("update cameralist set " + attribute
				+ "=?  where ip=? and ssid=?", new Object[] { value, ip, ssid });
		db.close();
	}

	public void updateAttributeString(String ip, String ssid, String attribute,
			String value)
	{
		db = database.getWritableDatabase();
		db.execSQL("update cameralist set " + attribute
				+ "=?  where ip=? and ssid=?", new Object[] { value, ip, ssid });
		db.close();
	}

	public ArrayList<Camera> selectAllIP(String ssid)
	{
		db = database.getWritableDatabase();
		ArrayList<Camera> arraylist = new ArrayList<Camera>();
		Cursor cursor = db.rawQuery(
				"select * from cameralist where ssid=? order by ip",
				new String[] { ssid });
		while (cursor.moveToNext())
		{
			String ip = cursor.getString(1);
			Camera camera = getCamera(ip, ssid);
			arraylist.add(camera);
		}
		db.close();
		return arraylist;
	}

	public ArrayList<Camera> selectCameraOnly(String ssid)
	{
		db = database.getWritableDatabase();
		ArrayList<Camera> arraylist = new ArrayList<Camera>();

		try
		{
			Cursor cursor = db
					.rawQuery(
							"select * from cameralist where ssid=? and flag=? order by ip",
							new String[] { ssid, "1" });
			while (cursor.moveToNext())
			{
				String ip = cursor.getString(1);
				Camera camera = getCamera(ip, ssid);
				arraylist.add(camera);
			}
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		db.close();
		return arraylist;
	}
}
