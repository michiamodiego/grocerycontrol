package com.ds.app.pricereading.activity.support;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

public class AlertDialogFactory {

    public static AlertDialog createYesAlertDialog(Context context, String message, DialogInterface.OnClickListener callback) {
        return new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("Sì", callback)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // DO NOTHING
                    }
                })
                .show();
    }

    public static AlertDialog createExitDialog(Context context, DialogInterface.OnClickListener callback) {
        return createYesAlertDialog(context, "Sei sicuro di voler uscire?", callback);
    }

    public static AlertDialog createAbortDialog(Context context, DialogInterface.OnClickListener callback) {
        return createYesAlertDialog(context, "Sei sicuro di voler annullare le modifiche?", callback);
    }

    public static AlertDialog createSaveDialog(Context context, DialogInterface.OnClickListener callback) {
        return createYesAlertDialog(context, "Sei sicuro di voler salvare?", callback);
    }
}
