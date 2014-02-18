package io.evercam.connect.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;

/**
 * AwsS3Uploader
 * 
 * Upload user data to AWS s3
 */

public class AwsS3Uploader
{
	private AmazonS3Client s3Client;
	private PutObjectRequest putObjectRequest;
	private final static String SUFFIX_TXT = ".txt";

	public AwsS3Uploader(String title, String content, Context ctxt)
	{
		String accessKey = new PropertyReader(ctxt)
				.getPropertyStr(Constants.PROPERTY_KEY_ACCESS_KEY);
		String secretKey = new PropertyReader(ctxt)
				.getPropertyStr(Constants.PROPERTY_KEY_SECRET_KEY);
		s3Client = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey));

		s3Client.setRegion(Region.getRegion(Regions.EU_WEST_1));

		S3PutObjectTask task = new S3PutObjectTask();
		task.title = title;
		task.content = content;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		else
		{
			task.execute();
		}
	}

	private class S3PutObjectTask extends AsyncTask<Void, Void, Void>
	{

		String title = null;
		String content = null;

		@Override
		protected Void doInBackground(Void... params)
		{
			File file = new File(Environment.getExternalStorageDirectory() + File.separator + title
					+ SUFFIX_TXT);
			try
			{
				file.createNewFile();
				if (file.exists())
				{
					OutputStream outputStream = new FileOutputStream(file);
					outputStream.write(content.getBytes());
					outputStream.close();
					try
					{
						putObjectRequest = new PutObjectRequest("evercamconnect-userdata", title
								+ SUFFIX_TXT, file);
						s3Client.putObject(putObjectRequest);
					}
					catch (Exception e)
					{
						Log.e("Error", e.toString());
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			file.delete();
			return null;
		}

	}
}
