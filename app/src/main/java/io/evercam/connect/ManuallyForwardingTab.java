package io.evercam.connect;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import io.evercam.connect.db.Camera;
import io.evercam.connect.db.CameraOperation;

public class ManuallyForwardingTab extends Fragment
{

	private Camera camera;
	private CameraOperation cameraOperation;
	private String cameraIP;
	private String ssid;

	private LinearLayout titleLayout;
	private LinearLayout warningLayout;
	private RelativeLayout httpLayout;
	private RelativeLayout rtspLayout;
	private RelativeLayout httpsLayout;
	private RelativeLayout ftpLayout;
	private RelativeLayout sshLayout;
	private TextView internalHttpText;
	private TextView internalRtspText;
	private TextView internalHttpsText;
	private TextView internalFtpText;
	private TextView internalSshText;
	private EditText externalHttpEdit;
	private EditText externalRtspEdit;
	private EditText externalHttpsEdit;
	private EditText externalFtpEdit;
	private EditText externalSshEdit;
	private Button saveBtn;
	private Button addBtn;

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		cameraIP = getActivity().getIntent().getExtras().get("IP").toString();
		ssid = getActivity().getIntent().getExtras().get("SSID").toString();
		cameraOperation = new CameraOperation(getActivity().getApplicationContext());

		View view = inflater.inflate(R.layout.tab_two, container, false);

		warningLayout = (LinearLayout) view.findViewById(R.id.warningLayout);
		titleLayout = (LinearLayout) view.findViewById(R.id.titleLayout);
		httpLayout = (RelativeLayout) view.findViewById(R.id.httpLayout);
		rtspLayout = (RelativeLayout) view.findViewById(R.id.rtspLayout);
		httpsLayout = (RelativeLayout) view.findViewById(R.id.httpsLayout);
		ftpLayout = (RelativeLayout) view.findViewById(R.id.ftpLayout);
		sshLayout = (RelativeLayout) view.findViewById(R.id.sshLayout);
		internalHttpText = (TextView) view.findViewById(R.id.httpInternal);
		internalRtspText = (TextView) view.findViewById(R.id.rtspInternal);
		internalHttpsText = (TextView) view.findViewById(R.id.httpsInternal);
		internalFtpText = (TextView) view.findViewById(R.id.ftpInternal);
		internalSshText = (TextView) view.findViewById(R.id.sshInternal);
		externalHttpEdit = (EditText) view.findViewById(R.id.httpExternal);
		externalRtspEdit = (EditText) view.findViewById(R.id.rtspExternal);
		externalHttpsEdit = (EditText) view.findViewById(R.id.httpsExternal);
		externalFtpEdit = (EditText) view.findViewById(R.id.ftpExternal);
		externalSshEdit = (EditText) view.findViewById(R.id.sshExternal);

		saveBtn = (Button) view.findViewById(R.id.buttonSave);
		addBtn = (Button) view.findViewById(R.id.buttonAdd);

		setUpPage();

		saveBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				saveDetails();
			}

		});

		addBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				showAddBtnDialog();
			}

		});

		return view;
	}

	public void setUpPage()
	{
		camera = cameraOperation.getCamera(cameraIP, ssid);

		// If no internal port exists
		if (!camera.hasInternalPorts())
		{
			saveBtn.setVisibility(View.GONE);
			titleLayout.setVisibility(View.GONE);
			warningLayout.setVisibility(View.VISIBLE);
		}
		else
		{
			saveBtn.setVisibility(View.VISIBLE);
			titleLayout.setVisibility(View.VISIBLE);
			warningLayout.setVisibility(View.GONE);

			// HTTP
			if (camera.hasHTTP())
			{
				httpLayout.setVisibility(View.VISIBLE);
				internalHttpText.setText(String.valueOf(camera.getHttp()));
				if (camera.hasExternalHttp())
				{
					externalHttpEdit.setText(String.valueOf(camera.getExthttp()));
				}
				else
				{
					externalHttpEdit.setText(null);
				}
			}
			else
			{
				httpLayout.setVisibility(View.GONE);
			}

			// RTSP
			if (camera.hasRTSP())
			{
				rtspLayout.setVisibility(View.VISIBLE);
				internalRtspText.setText(String.valueOf(camera.getRtsp()));
				if (camera.hasExternalRtsp())
				{
					externalRtspEdit.setText(String.valueOf(camera.getExtrtsp()));
				}
				else
				{
					externalRtspEdit.setText(null);
				}
			}
			else
			{
				rtspLayout.setVisibility(View.GONE);
			}

			// HTTPS
			if (camera.hasHTTPS())
			{
				httpsLayout.setVisibility(View.VISIBLE);
				internalHttpsText.setText(String.valueOf(camera.getHttps()));
				if (camera.hasExternalHttps())
				{
					externalHttpsEdit.setText(String.valueOf(camera.getExthttps()));
				}
				else
				{
					externalHttpsEdit.setText(null);
				}
			}
			else
			{
				httpsLayout.setVisibility(View.GONE);
			}

			// FTP
			if (camera.hasFTP())
			{
				ftpLayout.setVisibility(View.VISIBLE);
				internalFtpText.setText(String.valueOf(camera.getFtp()));
				if (camera.hasExternalFtp())
				{
					externalFtpEdit.setText(String.valueOf(camera.getExtftp()));
				}
				else
				{
					externalFtpEdit.setText(null);
				}

			}
			else
			{
				ftpLayout.setVisibility(View.GONE);
			}

			// SSH
			if (camera.hasSSH())
			{
				sshLayout.setVisibility(View.VISIBLE);
				internalSshText.setText(String.valueOf(camera.getSsh()));
				if (camera.hasExternalSsh())
				{
					externalSshEdit.setText(String.valueOf(camera.getExtssh()));
				}
				else
				{
					externalSshEdit.setText(null);
				}

			}
			else
			{
				sshLayout.setVisibility(View.GONE);
			}
		}
	}

	private void saveDetails()
	{
		String httpStr = externalHttpEdit.getText().toString();
		String rtspStr = externalRtspEdit.getText().toString();
		String httpsStr = externalHttpsEdit.getText().toString();
		String ftpStr = externalFtpEdit.getText().toString();
		String sshStr = externalSshEdit.getText().toString();

		if (httpStr.length() == 0)
		{
			httpStr = "0";
		}
		if (rtspStr.length() == 0)
		{
			rtspStr = "0";
		}
		if (httpsStr.length() == 0)
		{
			httpsStr = "0";
		}
		if (ftpStr.length() == 0)
		{
			ftpStr = "0";
		}
		if (sshStr.length() == 0)
		{
			sshStr = "0";
		}

		try
		{
			int newHttp = Integer.parseInt(httpStr);
			int newRtsp = Integer.parseInt(rtspStr);
			int newHttps = Integer.parseInt(httpsStr);
			int newFtp = Integer.parseInt(ftpStr);
			int newSsh = Integer.parseInt(sshStr);

			// If ports all in 0-65535
			if (isInPortRange(newHttp) && isInPortRange(newRtsp) && isInPortRange(newHttps)
					&& isInPortRange(newFtp) && isInPortRange(newSsh))
			{
				cameraOperation.updateAttributeInt(camera.getIP(), camera.getSsid(), "exthttp",
						newHttp);
				cameraOperation.updateAttributeInt(camera.getIP(), camera.getSsid(), "extrtsp",
						newRtsp);
				cameraOperation.updateAttributeInt(camera.getIP(), camera.getSsid(), "exthttps",
						newHttps);
				cameraOperation.updateAttributeInt(camera.getIP(), camera.getSsid(), "extftp",
						newFtp);
				cameraOperation.updateAttributeInt(camera.getIP(), camera.getSsid(), "extssh",
						newSsh);
				Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Success!",
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();

				getActivity().finish();
			}
			else
			{
				showPortNotInRange();
			}
			setUpPage();
		}
		catch (NumberFormatException e)
		{
			showPortNotInRange();
		}

	}

	private void showPortNotInRange()
	{
		Toast toast = Toast.makeText(getActivity().getApplicationContext(), R.string.portRangeMsg1,
				Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	private void showInternalCannotNull()
	{
		Toast toast = Toast.makeText(getActivity().getApplicationContext(),
				"Please fill in internal port!", Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	private void showAddBtnDialog()
	{
		LayoutInflater mInflater = LayoutInflater.from(getActivity().getApplicationContext());
		final View addPortView = mInflater.inflate(R.layout.manual_forward_add, null);
		final AlertDialog.Builder editBuilder = new AlertDialog.Builder(this.getActivity());
		editBuilder.setView(addPortView);

		final Spinner spinnerChoosePortType = (Spinner) addPortView
				.findViewById(R.id.portType_spinner);

		ArrayAdapter<String> portTypeAdapter = new ArrayAdapter<String>(getActivity()
				.getApplicationContext(), R.layout.spinner_item, getPortTypesToAdd());
		spinnerChoosePortType.setAdapter(portTypeAdapter);

		final EditText addInternalEdit = (EditText) addPortView
				.findViewById(R.id.internalPort_edit);
		final EditText addExternalEdit = (EditText) addPortView
				.findViewById(R.id.externalPort_edit);

		editBuilder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which)
			{

				String choosedPort = spinnerChoosePortType.getSelectedItem().toString();
				String internalStr = addInternalEdit.getText().toString();
				String externalStr = addExternalEdit.getText().toString();

				if (internalStr.length() == 0)
				{
					internalStr = "0";
				}
				if (externalStr.length() == 0)
				{
					externalStr = "0";
				}

				try
				{
					int internalInt = Integer.parseInt(internalStr);
					int externalInt = Integer.parseInt(externalStr);

					if (isInPortRange(internalInt) && isInPortRange(externalInt))
					{
						if (internalInt == 0 && externalInt != 0)
						{
							showInternalCannotNull();
							CameraDetailActivity.keepDialog(dialog);
						}
						else
						{
							if (choosedPort.equals("HTTP"))
							{
								cameraOperation.updateAttributeInt(camera.getIP(),
										camera.getSsid(), "http", internalInt);
								cameraOperation.updateAttributeInt(camera.getIP(),
										camera.getSsid(), "exthttp", externalInt);
							}
							else if (choosedPort.equals("RTSP"))
							{
								cameraOperation.updateAttributeInt(camera.getIP(),
										camera.getSsid(), "rtsp", internalInt);
								cameraOperation.updateAttributeInt(camera.getIP(),
										camera.getSsid(), "extrtsp", externalInt);
							}
							if (choosedPort.equals("HTTPS"))
							{
								cameraOperation.updateAttributeInt(camera.getIP(),
										camera.getSsid(), "https", internalInt);
								cameraOperation.updateAttributeInt(camera.getIP(),
										camera.getSsid(), "exthttps", externalInt);
							}
							else if (choosedPort.equals("FTP"))
							{
								cameraOperation.updateAttributeInt(camera.getIP(),
										camera.getSsid(), "ftp", internalInt);
								cameraOperation.updateAttributeInt(camera.getIP(),
										camera.getSsid(), "extftp", externalInt);
							}
							else if (choosedPort.equals("SSH"))
							{
								cameraOperation.updateAttributeInt(camera.getIP(),
										camera.getSsid(), "ssh", internalInt);
								cameraOperation.updateAttributeInt(camera.getIP(),
										camera.getSsid(), "extssh", externalInt);
							}

							CameraDetailActivity.closeDialog(dialog);
							setUpPage();
						}
					}
					else
					{
						showPortNotInRange();
						CameraDetailActivity.keepDialog(dialog);
					}
				}
				catch (NumberFormatException e)
				{
					showPortNotInRange();
					CameraDetailActivity.keepDialog(dialog);
				}
			}

		});
		editBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				CameraDetailActivity.closeDialog(dialog);
			}
		});
		editBuilder.setTitle("Add Forwarded Port (" + camera.getIP() + ")");
		editBuilder.setCancelable(false);
		editBuilder.show();
	}

	private boolean isInPortRange(int port)
	{
		if (port >= 0 && port <= 65535)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private ArrayList<String> getPortTypesToAdd()
	{
		ArrayList<String> portTypes = new ArrayList<String>();
		if (!camera.hasHTTP())
		{
			portTypes.add("HTTP");
		}
		if (!camera.hasRTSP())
		{
			portTypes.add("RTSP");
		}
		if (!camera.hasHTTPS())
		{
			portTypes.add("HTTPS");
		}

		if (!camera.hasFTP())
		{
			portTypes.add("FTP");
		}
		if (!camera.hasSSH())
		{
			portTypes.add("SSH");
		}

		return portTypes;
	}
}