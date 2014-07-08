package io.evercam.connect;

import io.evercam.connect.db.Camera;
import io.evercam.connect.db.CameraOperation;
import io.evercam.connect.helper.Constants;
import io.evercam.connect.net.NetInfo;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;

import net.sbbi.upnp.messages.ActionResponse;
import net.sbbi.upnp.messages.UPNPResponseException;

import io.evercam.connect.R;
import io.evercam.network.upnp.IGDDiscovery;
import io.evercam.network.upnp.UpnpDiscovery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;
import android.view.Gravity;

public class UpnpForwardingTab extends Fragment
{
	private final String TAG = "evercamdiscover-UpnpForwardingTab";
	private Handler handler = new Handler();
	private CheckBox useUpnpCheckbox;
	private TextView isAvaliableTxt;
	private ImageView faceImg;
	private TextView httpTxt;
	private TextView rtspTxt;
	private TextView bottomLabel;
	private TextView httpLabel;
	private TextView rtspLabel;
	private TextView natList;
	private LinearLayout useUpnpLayout;
	private LinearLayout manualUpnpLayout;
	private Button saveBtn;
	private Button addBtn;
	private Button removeBtn;
	private TextView helpMsgTxt;
	private RadioGroup autoOrManuRadioGroup;
	private IGDDiscovery igdDiscovery = null;
	private String cameraIP;
	private String ssid;
	private Camera camera;
	private CameraOperation cameraOperation;
	private NetInfo netInfo;
	private boolean httpMapped;
	private boolean rtspMapped;
	private ArrayList<ActionResponse> forwardedList;
	private ArrayList<String> spinnerList = new ArrayList<String>();
	private Spinner spinnerRemovePort;
	private ArrayAdapter<String> removePortAdapter;
	private TextView cameraip;
	private EditText internalPortEdit;
	private EditText externalPortEdit;
	private EditText descriptionEdit;
	private RadioButton udpRadioButton;
	private UPNPTask upnpTask;
	private ProgressBar processAnimate;
	private String natListString;

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{

		View view = inflater.inflate(R.layout.tab_one, container, false);
		cameraIP = getActivity().getIntent().getExtras().get("IP").toString();
		ssid = getActivity().getIntent().getExtras().get("SSID").toString();
		cameraOperation = new CameraOperation(getActivity().getApplicationContext());
		camera = cameraOperation.getCamera(cameraIP, ssid);

		// set up page
		isAvaliableTxt = (TextView) view.findViewById(R.id.isAvailiable_txt);
		faceImg = (ImageView) view.findViewById(R.id.face_img);
		isAvaliableTxt.setText(R.string.searchingRouter);
		helpMsgTxt = (TextView) view.findViewById(R.id.helpMsg);
		useUpnpCheckbox = (CheckBox) view.findViewById(R.id.use_upnp_checkbox);
		useUpnpLayout = (LinearLayout) view.findViewById(R.id.use_upnp_layout);
		autoOrManuRadioGroup = (RadioGroup) view.findViewById(R.id.radioGroup_autoOrManu);
		final RadioButton autoRadioBtn = (RadioButton) view.findViewById(R.id.radio_auto);
		final RadioButton manuallyRadioBtn = (RadioButton) view.findViewById(R.id.radio_manually);
		httpTxt = (TextView) view.findViewById(R.id.externalHTTP_value);
		rtspTxt = (TextView) view.findViewById(R.id.externalRTSP_value);
		bottomLabel = (TextView) view.findViewById(R.id.buttomLabel);
		saveBtn = (Button) view.findViewById(R.id.saveForwarding_button);
		httpLabel = (TextView) view.findViewById(R.id.externalHTTP_Label);
		rtspLabel = (TextView) view.findViewById(R.id.externalRTSP_Label);
		processAnimate = (ProgressBar) view.findViewById(R.id.processBarUPNP);

		// manually page elements
		manualUpnpLayout = (LinearLayout) view.findViewById(R.id.manual_upnp_layout);
		natList = (TextView) view.findViewById(R.id.nat_table_txt);
		addBtn = (Button) view.findViewById(R.id.add_button);
		removeBtn = (Button) view.findViewById(R.id.remove_button);

		// listener for use upnp or not
		useUpnpCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if (isChecked)
				{
					if (autoRadioBtn.isChecked())
					{
						autoOrManuRadioGroup.setVisibility(View.VISIBLE);
						useUpnpLayout.setVisibility(View.VISIBLE);
					}
					else if (manuallyRadioBtn.isChecked())
					{
						autoOrManuRadioGroup.setVisibility(View.VISIBLE);
						manualUpnpLayout.setVisibility(View.VISIBLE);
					}
				}
				else
				{
					useUpnpLayout.setVisibility(View.GONE);
					autoOrManuRadioGroup.setVisibility(View.GONE);
					manualUpnpLayout.setVisibility(View.GONE);
				}
			}

		});

		// listener for manually or automatically
		autoOrManuRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				if (checkedId == autoRadioBtn.getId())
				{
					manualUpnpLayout.setVisibility(View.GONE);
					useUpnpLayout.setVisibility(View.VISIBLE);
					httpTxt.setText("");
					rtspTxt.setText("");
					bottomLabel.setText("");
					httpLabel.setText("");
					rtspLabel.setText("");
					
					launchUpdateForward();
				}
				else
				{
					manualUpnpLayout.setVisibility(View.VISIBLE);
					useUpnpLayout.setVisibility(View.GONE);
					natList.setText(R.string.loadingMsg);
					handler.postDelayed(new Runnable(){
						@Override
						public void run()
						{
							updateNATListString();
						}
					}, 1000);
				}
			}

		});

		// clicking forward button
		saveBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0)
			{
				saveBtn.setVisibility(View.GONE);
				bottomLabel.setText(R.string.pleaseWait);
				bottomLabel.setVisibility(View.VISIBLE);
				try
				{
					if (camera.getHttp() != 0)
					{
						httpMapped = igdDiscovery.IGD.addPortMapping(
								Constants.UPNP_HTTP_DESCRIPTION, null, camera.getHttp(),
								Integer.parseInt(httpTxt.getText().toString()), cameraIP, 0,
								Constants.PROTOCOL_TCP);
					}

					if (camera.getRtsp() != 0)
					{
						rtspMapped = igdDiscovery.IGD.addPortMapping(
								Constants.UPNP_RTSP_DESCRIPTION, null, camera.getRtsp(),
								Integer.parseInt(rtspTxt.getText().toString()), cameraIP, 0,
								Constants.PROTOCOL_TCP);
					}

					// what if only one of them is successful?
					if (httpMapped || rtspMapped)
					{
						handler.postDelayed(new Runnable(){
							@Override
							public void run()
							{
								try
								{
									igdDiscovery = new IGDDiscovery(netInfo.getGatewayIp());
								}
								catch (Exception e)
								{
									Log.e(TAG, "Save Auto Forward" + e.toString());
								}
								launchUpdateForward();
							}
						}, 1000);
					}
					else
					{
						return;
					}
				}
				catch (NumberFormatException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				catch (UPNPResponseException e)
				{
					e.printStackTrace();
				}

			}

		});

		// clicking add button
		addBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v)
			{
				showAddPortDialog();
			}
		});

		// clicking remove button
		removeBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{

				showRemovePortDialog();
			}
		});

		return view;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		netInfo = new NetInfo(getActivity().getApplicationContext());
		upnpTask = new UPNPTask();

		// Check has internal ports or not.
		if (camera.hasInternalPorts())
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			{
				upnpTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
			else
			{
				upnpTask.execute();
			}
		}
		else
		{
			isAvaliableTxt.setText(R.string.noInternalPort);
			faceImg.setVisibility(View.VISIBLE);
			faceImg.setImageResource(R.drawable.not_smile);
			helpMsgTxt.setVisibility(View.VISIBLE);
			helpMsgTxt.setText(R.string.helpNoInternalPort);
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();
		upnpTask.cancel(true);
	}

	// get a random number in [8000, 65535]
	private int getRandomPortNumber()
	{
		Random random = new Random();
		int randomPort = random.nextInt(65535) % (65535 - 8000 + 1) + 8000;
		return randomPort;
	}

	private void launchUpdateForward()
	{
		new AsyncTask<Void, Void, Void>(){

			@Override
			protected Void doInBackground(Void... params)
			{
				forwardedList = igdDiscovery.getMatchedEntries(cameraIP);
				return null;
			}

			@Override
			protected void onPostExecute(Void result)
			{
				updateForwardPage(forwardedList);
			}

		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void updateForwardPage(ArrayList<ActionResponse> forwardedList)
	{
		// get corresponding forwarded ports
		if (igdDiscovery != null)
		{
			if (forwardedList.iterator().hasNext())
			{
				for (int i = 0; i < forwardedList.size(); i++)
				{
					String internalPort = forwardedList.get(i).getOutActionArgumentValue(
							UpnpDiscovery.UPNP_KEY_INTERNAL_PORT);
					String externalPort = forwardedList.get(i).getOutActionArgumentValue(
							UpnpDiscovery.UPNP_KEY_EXTERNAL_PORT);
					if (camera.getHttp() != 0
							&& internalPort.equals(String.valueOf(camera.getHttp())))
					{
						// HTTP already forwarded, show forwarded port
						httpTxt.setText(externalPort);
						httpLabel.setVisibility(View.VISIBLE);
						httpLabel.setText(R.string.forwardedBracket);
						cameraOperation.updateAttributeInt(cameraIP, ssid, "exthttp",
								Integer.parseInt(externalPort));
					}
					else if (camera.getHttp() == 0)
					{
						httpTxt.setText(this.getResources().getString(R.string.notAvaliable));
						httpLabel.setVisibility(View.GONE);
					}

					if (camera.getRtsp() != 0
							&& internalPort.equals(String.valueOf(camera.getRtsp())))
					{
						rtspTxt.setText(externalPort);
						rtspLabel.setVisibility(View.VISIBLE);
						rtspLabel.setText(R.string.forwardedBracket);
						cameraOperation.updateAttributeInt(cameraIP, ssid, "extrtsp",
								Integer.parseInt(externalPort));
					}
					else if (camera.getRtsp() == 0)
					{
						rtspTxt.setText(this.getResources().getString(R.string.notAvaliable));
						rtspLabel.setVisibility(View.GONE);
					}
				}
			}
			else
			{
				// nothing matches, update database to set to 0.
				cameraOperation.updateAttributeInt(cameraIP, ssid, "exthttp", 0);
				cameraOperation.updateAttributeInt(cameraIP, ssid, "extrtsp", 0);

			}

			// show msg
			if (httpTxt.getText().length() != 0 && rtspTxt.getText().length() != 0)
			{
				bottomLabel.setVisibility(View.VISIBLE);
				bottomLabel.setText(R.string.msg_isForwarded);
			}

			// get random http
			if (httpTxt.getText().length() == 0)
			{
				httpTxt.setText(String.valueOf(getRandomPortNumber()));
				saveBtn.setVisibility(View.VISIBLE);
				httpLabel.setVisibility(View.VISIBLE);
				httpLabel.setText(R.string.readyBracket);
			}

			// get random rtsp
			if (rtspTxt.getText().length() == 0)
			{
				rtspTxt.setText(String.valueOf(getRandomPortNumber()));
				saveBtn.setVisibility(View.VISIBLE);
				rtspLabel.setVisibility(View.VISIBLE);
				rtspLabel.setText(R.string.readyBracket);
			}

		}
	}

	private void updateNATListString()
	{
		new AsyncTask<Void, Void, Void>()
		{

			@Override
			protected Void doInBackground(Void... params)
			{
				if (igdDiscovery != null)
				{
				forwardedList = igdDiscovery.getMatchedEntries(cameraIP);
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result)
			{
				String cameraNATListStr = "Camera: " + cameraIP + " forwarded list:" + "\n";
				if (igdDiscovery != null)
				{
					if (forwardedList.iterator().hasNext())
					{
						removeBtn.setVisibility(View.VISIBLE);
						for (int i = 0; i < forwardedList.size(); i++)
						{
							String number = (i + 1) + "";
							String description = forwardedList.get(i).getOutActionArgumentValue(
									UpnpDiscovery.UPNP_KEY_DESCRIPTION);
							String internalPort = forwardedList.get(i).getOutActionArgumentValue(
									UpnpDiscovery.UPNP_KEY_INTERNAL_PORT);
							String externalPort = forwardedList.get(i).getOutActionArgumentValue(
									UpnpDiscovery.UPNP_KEY_EXTERNAL_PORT);
							String protocol = forwardedList.get(i).getOutActionArgumentValue(
									UpnpDiscovery.UPNP_KEY_PROTOCOL);
							String thisEntry = number + "." + "\n" + "Description: " + description + "\n"
									+ "Internal Port: " + internalPort + "\n" + "External Port: "
									+ externalPort + "\n" + "Protocol: " + protocol + "\n";
							cameraNATListStr += thisEntry;
						}
					}
					else
					{
						cameraNATListStr = "Camera: " + cameraIP + " has no ports mapped.";
						removeBtn.setVisibility(View.GONE);
					}
				}
				// if igd is null
				else
				{
					cameraNATListStr = "Camera: " + cameraIP + " has no ports mapped.";
				}
				natList.setText(cameraNATListStr);
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void showAddPortDialog()
	{
		LayoutInflater mInflater = LayoutInflater.from(getActivity().getApplicationContext());
		final View forwardView = mInflater.inflate(R.layout.forward_dialog, null);
		final AlertDialog.Builder forwardBuilder = new AlertDialog.Builder(this.getActivity());
		forwardBuilder.setView(forwardView);

		// show camera IP
		cameraip = (TextView) forwardView.findViewById(R.id.cameraip_value);
		cameraip.setText(cameraIP);

		// get user's input
		internalPortEdit = (EditText) forwardView.findViewById(R.id.internal_edit);
		externalPortEdit = (EditText) forwardView.findViewById(R.id.external_edit);
		descriptionEdit = (EditText) forwardView.findViewById(R.id.portDiscription_edit);
		udpRadioButton = (RadioButton) forwardView.findViewById(R.id.radio_udp);

		forwardBuilder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				final String internalPortStr = internalPortEdit.getText().toString();
				final String externalPortStr = externalPortEdit.getText().toString();
				final String descriptionStr = descriptionEdit.getText().toString();
				final String protocolStr;
				if (udpRadioButton.isChecked())
				{
					protocolStr = Constants.PROTOCOL_UDP;
				}
				else
				{
					protocolStr = Constants.PROTOCOL_TCP;
				}

				if (!(internalPortStr.length() == 0) && !(externalPortStr.length() == 0)
						&& !(descriptionStr.length() == 0))
				{
					try
					{
						Field field = dialog.getClass().getSuperclass()
								.getDeclaredField("mShowing");
						field.setAccessible(true);
						field.set(dialog, true);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					
					final int internalPortInt;
					final int externalPortInt;
					try
					{
					internalPortInt = Integer.parseInt(internalPortStr);
					externalPortInt = Integer.parseInt(externalPortStr);
					}
					catch (NumberFormatException e)
					{
						Toast toast = Toast.makeText(getActivity().getApplicationContext(),
								R.string.portRangeMsg, Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
						return;
					}
					
					new AsyncTask<Void, Void, Boolean>()
					{

						@Override
						protected Boolean doInBackground(Void... params)
						{
							boolean portAdded = false;
							
							try
							{
								portAdded = igdDiscovery.IGD.addPortMapping(descriptionStr,
										null, internalPortInt,
										externalPortInt, cameraIP, 0, protocolStr);
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
							catch (UPNPResponseException e)
							{
								e.printStackTrace();
							}
							catch (Exception e)
							{
								Log.e(TAG, "Add port forward" + e.toString());
							}
							
							if(portAdded)
							{
								publishProgress();
								try
								{
									igdDiscovery = new IGDDiscovery(netInfo.getGatewayIp());
									return true;
								}
								catch (Exception e)
								{
									Log.e(TAG, "After add port forward" + e.toString());
								}
							}
							return false;
						}

						@Override
						protected void onPostExecute(Boolean success)
						{
							if (success)
							{
										updateNATListString();
							}
							else
							{
								Toast toast = Toast.makeText(getActivity().getApplicationContext(),
										R.string.portForwardFailed, Toast.LENGTH_SHORT);
								toast.setGravity(Gravity.CENTER, 0, 0);
								toast.show();
							}
						}

						@Override
						protected void onProgressUpdate(Void... values)
						{
							natList.setText(R.string.manualForwardSuccessMsg);
						}
						
						
					}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}

				// If form not been complete
				else
				{
					Toast toast = Toast.makeText(getActivity().getApplicationContext(),
							R.string.fillInAllMsg, Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					try
					{
						Field field = dialog.getClass().getSuperclass()
								.getDeclaredField("mShowing");
						field.setAccessible(true);
						field.set(dialog, false);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		});
		forwardBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				try
				{
					Field fieldAdd = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
					fieldAdd.setAccessible(true);
					fieldAdd.set(dialog, true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

		});
		forwardBuilder.setTitle(R.string.addPortForward);
		forwardBuilder.setCancelable(false);
		forwardBuilder.show();
	}

	private void showRemovePortDialog()
	{
		LayoutInflater mInflater = LayoutInflater.from(getActivity().getApplicationContext());
		final View removeView = mInflater.inflate(R.layout.remove_layout, null);
		final AlertDialog.Builder removeBuilder = new AlertDialog.Builder(this.getActivity());
		removeBuilder.setView(removeView);

		// spinner
		spinnerList = getSpinnerList();
		spinnerRemovePort = (Spinner) removeView.findViewById(R.id.removePort_spinner);
		removePortAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),
				R.layout.spinner_item, spinnerList);
		spinnerRemovePort.setAdapter(removePortAdapter);

		removeBuilder.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				new AsyncTask<Void, Void, Boolean>()
				{
					@Override
					protected Boolean doInBackground(Void... params)
					{
						String[] selectedValues = spinnerRemovePort.getSelectedItem()
								.toString().split(" - ");
						String extPort = selectedValues[0];
						String protocol = selectedValues[1];

						boolean unmapped = false;
					
						try
						{
							unmapped = igdDiscovery.IGD
									.deletePortMapping(null, Integer.parseInt(extPort), protocol);
						}
						catch (NumberFormatException e)
						{
							e.printStackTrace();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						catch (UPNPResponseException e)
						{
							e.printStackTrace();
						}
						
						if(unmapped)
						{
							this.publishProgress();
							try
							{
								igdDiscovery = new IGDDiscovery(netInfo.getGatewayIp());
								return true;
							}
							catch (Exception e)
							{
								Log.e(TAG, "After remove port forward" + e.toString());
							}
						}
						return false;
					}

					@Override
					protected void onPostExecute(Boolean success)
					{
						if (success)
						{
							updateNATListString();
						}
						else
						{
							Toast toast = Toast.makeText(getActivity().getApplicationContext(),
									R.string.deleteForwardFailed, Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						}
					}

					@Override
					protected void onProgressUpdate(Void... values)
					{
						natList.setText(R.string.manualDeleteSuccessMsg);
					}			
				}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		});

		removeBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				return;
			}
		});

		removeBuilder.setTitle(R.string.removePortForward);
		removeBuilder.setCancelable(false);
		removeBuilder.show();
	}

	// get list display in spinner for removing
	private ArrayList<String> getSpinnerList()
	{
		spinnerList.clear();
		for (int i = 0; i < forwardedList.size(); i++)
		{
			spinnerList.add(forwardedList.get(i).getOutActionArgumentValue(
					UpnpDiscovery.UPNP_KEY_EXTERNAL_PORT)
					+ " - "
					+ forwardedList.get(i).getOutActionArgumentValue(
							UpnpDiscovery.UPNP_KEY_PROTOCOL)
					+ " - "
					+ forwardedList.get(i).getOutActionArgumentValue(
							UpnpDiscovery.UPNP_KEY_DESCRIPTION));
		}

		return spinnerList;
	}

	private class UPNPTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected void onPreExecute()
		{
			processAnimate.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... params)
		{
			igdDiscovery = null;
			try
			{
				igdDiscovery = new IGDDiscovery(netInfo.getGatewayIp());
			}
			catch (Exception e)
			{
				Log.e(TAG, "UPnPTask" + e.toString());
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result)
		{
			processAnimate.setVisibility(View.GONE);
			if (igdDiscovery.isAvaliable())
			{
				isAvaliableTxt.setText(R.string.routerAvaliable);
				faceImg.setVisibility(View.VISIBLE);
				faceImg.setImageResource(R.drawable.smile);
				useUpnpCheckbox.setVisibility(View.VISIBLE);
			}
			else
			{
				isAvaliableTxt.setText(R.string.routerNotAvaliable);
				faceImg.setVisibility(View.VISIBLE);
				faceImg.setImageResource(R.drawable.not_smile);
				helpMsgTxt.setVisibility(View.VISIBLE);
				helpMsgTxt.setText(R.string.helpNotAvailiable);
			}

			launchUpdateForward();
		}
	}
}