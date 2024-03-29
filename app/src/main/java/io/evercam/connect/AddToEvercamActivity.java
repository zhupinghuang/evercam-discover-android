package io.evercam.connect;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import io.evercam.CameraBuilder;
import io.evercam.CameraDetail;
import io.evercam.EvercamException;
import io.evercam.Model;
import io.evercam.connect.db.Camera;
import io.evercam.connect.db.CameraOperation;
import io.evercam.connect.helper.LocationReader;
import io.evercam.connect.helper.SharedPrefsManager;
import io.evercam.network.discovery.NetworkInfo;
import io.evercam.network.discovery.Port;
import io.evercam.network.discovery.PortScan;

public class AddToEvercamActivity extends Activity
{
    private final String TAG = "evercamdiscover-AddToEvercamActivity";
    private Camera camera;
    private EditText nameEdit;
    private EditText snapshotEdit;
    private EditText usernameEdit;
    private EditText passwordEdit;
    private EditText exthttpEdit;
    private EditText modelEdit;
    private EditText vendorEdit;
    private EditText macEdit;
    private RadioButton publicRadioBtn;
    private Button addBtn;
    private CreateCameraTask createCameraTask;
    private SharedPreferences sharedPrefs;
    private String cameraName;
    private String snapshotPath;
    private boolean isPublic;
    private int exthttp;
    private String cameraUsername;
    private String cameraPassword;
    private String cameraModel;
    private String cameraVendor;
    private String cameraMac;
    private ProgressDialog progressDialog;
    private String externalIp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_evercam);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        camera = (Camera) getIntent().getSerializableExtra("camera");
        initPage();
        fillPage();

        addBtn.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                if(isPassed())
                {
                    if(detailsChecked())
                    {
                        if(createCameraTask != null)
                        {
                            createCameraTask = null;
                        }
                        createCameraTask = new CreateCameraTask();
                        createCameraTask.execute();
                    }
                }
                else
                {
                    showShortToast(R.string.extPortNotOpen);
                }
            }
        });

        exthttpEdit.addTextChangedListener(new TextWatcher()
        {

            @Override
            public void afterTextChanged(Editable arg0)
            {
                exthttpEdit.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });

        exthttpEdit.setOnFocusChangeListener(new OnFocusChangeListener()
        {

            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if(!hasFocus)
                {
                    if(extHttpChecked())
                    {
                        portCheck(exthttp);
                    }
                }
            }
        });
    }

    private void initPage()
    {
        nameEdit = (EditText) findViewById(R.id.addCameraName_edit);
        snapshotEdit = (EditText) findViewById(R.id.addCameraJpg_edit);
        usernameEdit = (EditText) findViewById(R.id.addUsername_edit);
        passwordEdit = (EditText) findViewById(R.id.addPassword_edit);
        exthttpEdit = (EditText) findViewById(R.id.addExtHttp_edit);
        modelEdit = (EditText) findViewById(R.id.addModel_value);
        vendorEdit = (EditText) findViewById(R.id.addVendor_value);
        macEdit = (EditText) findViewById(R.id.addMac_value);
        publicRadioBtn = (RadioButton) findViewById(R.id.publicRadio);
        addBtn = (Button) findViewById(R.id.button_creatCamera);
    }

    private void fillPage()
    {
        if(camera.getExthttp() > 0)
        {
            portCheck(camera.getExthttp());
            exthttpEdit.setText(String.valueOf(camera.getExthttp()));
        }
        if(camera.hasJpgURL())
        {
            snapshotEdit.setText(camera.getJpg());
        }
        usernameEdit.setText(camera.getUsername());
        passwordEdit.setText(camera.getPassword());
        macEdit.setText(camera.getMAC().toLowerCase(Locale.UK));
        macEdit.setEnabled(false);
        macEdit.setTextColor(Color.parseColor("#808080"));
        if(camera.hasModel())
        {
            if(camera.getModel().startsWith(camera.getVendor()))
            {
                camera.setModel(camera.getModel().substring(camera.getVendor().length() + 1).trim
                        ());
            }
            modelEdit.setText(camera.getModel().toLowerCase());
        }
        if(camera.hasVendor())
        {
            vendorEdit.setText(camera.getVendor().toLowerCase());
            vendorEdit.setEnabled(false);
            vendorEdit.setTextColor(Color.parseColor("#808080"));
        }
        nameEdit.setText(R.string.myCamera);
    }

    private void showShortToast(int id)
    {
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
    }

    private void showShortToast(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private boolean detailsChecked()
    {
        String nameStr = nameEdit.getText().toString();
        String snapshotStr = snapshotEdit.getText().toString();
        String exthttpStr = exthttpEdit.getText().toString();
        String usernameStr = usernameEdit.getText().toString();
        String passwordStr = passwordEdit.getText().toString();

        if(extHttpChecked())
        {
            if(nameStr.length() == 0)
            {
                showShortToast(R.string.nameEmpty);
                nameEdit.requestFocus();
            }
            else if(snapshotStr.length() == 0)
            {
                showShortToast(R.string.snapshotEmpty);
                snapshotEdit.requestFocus();
            }
            else if(!snapshotStr.startsWith("/"))
            {
                showShortToast(R.string.snapshotInvalid);
            }
            else if(exthttpStr.length() == 0)
            {
                showShortToast(R.string.exthttpEmpty);
                exthttpEdit.requestFocus();
            }
            else if(usernameStr.length() == 0)
            {
                showShortToast(R.string.usernameEmpty);
                usernameEdit.requestFocus();
            }
            else if(passwordStr.length() == 0)
            {
                showShortToast(R.string.passwordEmpty);
                passwordEdit.requestFocus();
            }
            else
            {
                cameraName = nameStr;
                snapshotPath = snapshotStr;
                isPublic = publicRadioBtn.isChecked();
                cameraUsername = usernameStr;
                cameraPassword = passwordStr;
                cameraModel = modelEdit.getText().toString();
                cameraVendor = vendorEdit.getText().toString();
                cameraMac = macEdit.getText().toString();
                return true;
            }
        }
        return false;
    }

    private boolean extHttpChecked()
    {
        String exthttpStr = exthttpEdit.getText().toString();
        try
        {
            exthttp = Integer.parseInt(exthttpStr);
            if(!(exthttp > 0 && exthttp <= 65535))
            {
                showShortToast(R.string.portRangeMsg);
                return false;
            }
            else
            {
                return true;
            }
        }
        catch(NumberFormatException e)
        {
            showShortToast(R.string.portRangeMsg);
            return false;
        }
    }

    private void showTick()
    {
        Drawable tick = getResources().getDrawable(R.drawable.tick);
        exthttpEdit.setCompoundDrawablesWithIntrinsicBounds(null, null, tick, null);
    }

    private void showCross()
    {
        Drawable cross = getResources().getDrawable(R.drawable.cross);
        exthttpEdit.setCompoundDrawablesWithIntrinsicBounds(null, null, cross, null);
    }

    private boolean isPassed()
    {
        Drawable[] drawables = exthttpEdit.getCompoundDrawables();
        for(Drawable drawable : drawables)
        {
            if(drawable != null)
            {
                if(drawable.equals(getResources().getDrawable(R.drawable.tick))) ;
                return true;
            }
        }
        return false;
    }

    private void portCheck(final int port)
    {
        new AsyncTask<Void, Void, Boolean>()
        {
            @Override
            protected Boolean doInBackground(Void... params)
            {
                if(externalIp == null)
                {
                    externalIp = NetworkInfo.getExternalIP();
                }
                if(externalIp != null)
                {
                    try
                    {
                        if(Port.isReachable(externalIp, port))
                        {
                            return true;
                        }
                        else
                        {
                            return false;
                        }
                    }
                    catch(Exception e)
                    {
                        Log.e(TAG, e.toString());
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean active)
            {
                if(active)
                {
                    showTick();
                }
                else
                {
                    showCross();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class CreateCameraTask extends AsyncTask<Void, Void, Boolean>
    {
        CameraDetail cameraDetail;
        Location currentLocation;
        String errorMsg = "Error, please try again later.";

        @Override
        protected void onPreExecute()
        {
            progressDialog = ProgressDialog.show(AddToEvercamActivity.this, "", getString(R
                    .string.creating_camera), true);
            currentLocation = new LocationReader(AddToEvercamActivity.this).getLocation();
        }

        @Override
        protected void onPostExecute(Boolean success)
        {
            if(progressDialog.isShowing())
            {
                progressDialog.dismiss();
            }
            if(success)
            {
                showShortToast(R.string.Success);
                CameraOperation cameraOperation = new CameraOperation(AddToEvercamActivity.this);
                cameraOperation.updateAttributeInt(camera.getIP(), camera.getSsid(), "evercam", 1);
                AddToEvercamActivity.this.finish();
            }
            else
            {
                showShortToast(errorMsg);
            }
        }

        @Override
        protected Boolean doInBackground(Void... arg0)
        {
            initialDetailObject();
            try
            {
                io.evercam.Camera camera = io.evercam.Camera.create(cameraDetail);

                if(camera != null)
                {
                    return true;
                }
                return false;
            }
            catch(EvercamException e)
            {
                errorMsg = e.getMessage();
                return false;
            }
        }

        private void initialDetailObject()
        {
            while(externalIp == null)
            {
                externalIp = NetworkInfo.getExternalIP();
            }

            try
            {
                ArrayList<Model> modelList = Model.getAll(cameraModel, cameraVendor);
                if(modelList.size() > 0)
                {
                    cameraModel = modelList.get(0).getId();
                }
            }
            catch(EvercamException e1)
            {
                errorMsg = e1.getMessage();
            }

            CameraBuilder cameraBuilder;
            try
            {
                cameraBuilder = new CameraBuilder(cameraName, isPublic).setExternalHost(externalIp).setExternalHttpPort(exthttp).setTimeZone(TimeZone.getDefault().getID()).setCameraUsername(cameraUsername).setCameraPassword(cameraPassword).setJpgUrl(snapshotPath);
                if(camera.hasHTTP())
                {
                    cameraBuilder.setInternalHost(camera.getIP()).setInternalHttpPort(camera.getHttp());
                }
                if(camera.hasRTSP())
                {
                    cameraBuilder.setInternalHost(camera.getIP()).setInternalRtspPort(camera.getRtsp());
                }
                if(camera.hasExternalRtsp())
                {
                    cameraBuilder.setExternalRtspPort(camera.getRtsp());
                }

                if(cameraVendor != null)
                {
                    if(!cameraVendor.equals("Unknown Vendor") && cameraVendor.length() != 0)
                    {
                        cameraBuilder.setVendor(cameraVendor);
                    }
                }
                if(cameraModel != null)
                {
                    if(cameraModel.length() != 0)
                    {
                        cameraBuilder.setModel(cameraModel);
                    }
                }
                if(cameraMac != null)
                {
                    if(cameraMac.length() != 0)
                    {
                        cameraBuilder.setMacAddress(cameraMac);
                    }
                }

                // Add location data if exists.
                if(currentLocation != null)
                {
                    Float lat = (float) currentLocation.getLatitude();
                    Float lng = (float) currentLocation.getLongitude();
                    cameraBuilder.setLocation(lat, lng);
                }

                cameraDetail = cameraBuilder.build();
            }
            catch(EvercamException e)
            {
                e.printStackTrace();
            }
        }
    }

    public int random()
    {
        Random rand = new Random();
        return rand.nextInt(100001);
    }
}
