package io.evercam.connect.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

import io.evercam.connect.R;

public class CustomedDialog
{
    // Alert dialog with single button
    public static AlertDialog getAlertDialog(Context context, String title, String message,
                                             DialogInterface.OnClickListener listener)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(message);
        dialogBuilder.setPositiveButton(R.string.ok, listener);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }

    // Dialog that shows when Internet is not connected.
    public static AlertDialog getNoInternetDialog(final Context context)
    {
        AlertDialog.Builder connectDialogBuilder = new AlertDialog.Builder(context);
        connectDialogBuilder.setMessage(R.string.dialogMsgMustConnect);

        connectDialogBuilder.setPositiveButton(R.string.wifiSettings, new DialogInterface
                .OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                });
        connectDialogBuilder.setNegativeButton(R.string.notNow, new DialogInterface
                .OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        return;
                    }
                });
        connectDialogBuilder.setTitle(R.string.notConnected);
        connectDialogBuilder.setCancelable(false);
        AlertDialog alertDialog = connectDialogBuilder.create();
        return alertDialog;
    }
}
