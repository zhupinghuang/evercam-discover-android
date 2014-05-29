package io.evercam.connect.db;

import io.evercam.connect.DiscoverMainActivity;
import io.evercam.connect.helper.Constants;
import io.evercam.connect.helper.PropertyReader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper
{

	private static final String dbName = "evercamdb";
	private static final int dbVersion = 8;// version 8: update demo camera details
	private Context ctxt;

	public DatabaseHelper(Context context)
	{
		super(context, dbName, null, dbVersion);
		this.ctxt = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE IF NOT EXISTS cameralist("
				+ "cameraid INTEGER PRIMARY KEY AUTOINCREMENT	NOT NULL, " + "ip	 TEXT	 NOT NULL,"
				+ "mac	TEXT," + "vendor		CHAR(50)," + "model	TEXT," + "upnp		INT," + "onvif	INT,"
				+ "bonjour	INT," + "http	INT," + "https INT," + "rtsp INT," + "ftp INT,"
				+ "ssh INT," + "portforwarded	INT," + "evercam INT," + "exthttp	INT,"
				+ "exthttps	INT," + "extftp	INT," + "extrtsp	INT," + "extssh	INT," + "flag INT,"
				+ "firstseen TEXT," + "lastseen TEXT," + "username TEXT," + "password TEXT,"
				+ "jpg TEXT," + "h264 TEXT," + "ssid TEXT NOT NULL);");

		// add sample camera
		PropertyReader propertyReader = new PropertyReader(ctxt);
		String sampleIP = propertyReader.getPropertyStr(PropertyReader.KEY_SAMPLE_IP);
		String sampleMAC = propertyReader.getPropertyStr(PropertyReader.KEY_SAMPLE_MAC);
		String sampleVendor = propertyReader.getPropertyStr(PropertyReader.KEY_SAMPLE_VENDOR);
		String sampleModel = propertyReader.getPropertyStr(PropertyReader.KEY_SAMPLE_MODEL);
		String sampleUsername = propertyReader.getPropertyStr(PropertyReader.KEY_SAMPLE_USERNAME);
		String samplePassword = propertyReader.getPropertyStr(PropertyReader.KEY_SAMPLE_PASSWORD);
		db.execSQL(
				"insert into cameralist(ip,mac,vendor,model,upnp,onvif,bonjour,portforwarded,flag,http,rtsp,exthttp,extrtsp, firstseen,lastseen,ssid,jpg,h264,username,password)values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
				new Object[] { sampleIP, sampleMAC, sampleVendor, sampleModel, 1, 1, 0, 1, 1, 8101,
						9101, 8101, 8300, DiscoverMainActivity.getSystemTime(),
						DiscoverMainActivity.getSystemTime(), "sample",
						"/Streaming/Channels/1/picture", "/h264/ch1/main/av_stream", sampleUsername, samplePassword });
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("DROP TABLE IF EXISTS cameralist");
		onCreate(db);
	}

}
