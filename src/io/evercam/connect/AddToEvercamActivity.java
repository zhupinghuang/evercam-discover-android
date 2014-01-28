package io.evercam.connect;

import io.evercam.connect.db.Camera;
import android.os.Bundle;
import android.app.Activity;

public class AddToEvercamActivity extends Activity
{
	private Camera camera;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_to_evercam);
		
		camera = (Camera) getIntent().getSerializableExtra("camera");
		String ip = camera.getIP();
	}
}
