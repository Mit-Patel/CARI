package com.avpti.cari.classes;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.avpti.cari.R;

import androidx.appcompat.app.AlertDialog;

//import android.support.v7.app.AlertDialog;

public class Common {
    public static boolean res;
    public static boolean close;
    private static ProgressDialog progress;


    //to display alert dialog box
    public static void showAlertMessage(Context context, String title, String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    //to display alert dialog box
    public static void showAlertMessageAndFinish(final Context context, String title, String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ((Activity) context).finish();
            }
        });
        builder.show();
    }

    public static void showAlertMessageAndExit(final Context context, String title, String message) {
        promptForResult(context, title, message, "Ok", "", false, new PromptRunnable() {
            @Override
            public void run() {
                //android.os.Process.killProcess(android.os.Process.myPid());
//                System.runFinalization();
//                System.exit(0);
                ((Activity)context).finishAffinity();
            }
        });
        /*final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //context.stopService(new Intent(context, BackgroundServerConnection.class));
//                ((Activity) context).moveTaskToBack(true);
//                ((Activity) context).finish();


            }
        });
        builder.show();*/
    }

    public static boolean showConfirmationMessage(final Context context, String title, String message, String titlePositive, String titleNegative) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(titlePositive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                res = true;
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(titleNegative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                res = false;
                dialog.dismiss();
            }
        });
        builder.show();

        return res;
    }

    public static void startProgressDialog(Context context, String title, String msg) {
        progress = new ProgressDialog(context);
        progress.setTitle(title);
        progress.setMessage(msg);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
    }

    public static void stopProgressDialog() {
        progress.dismiss();
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    Log.i("isMyServiceRunning?", true + "");
                    return true;
                }
            }
        }
        Log.i("isMyServiceRunning?", false + "");
        return false;
    }

    public static void promptForResult(Context context, String title, String message, String titlePos, String titleNeg, boolean isNeg, final PromptRunnable postrun) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        alert.setTitle(title);
        alert.setMessage(message);
        // procedure for when the ok button is clicked.
        alert.setPositiveButton(titlePos, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                // set value from the dialog inside our runnable implementation
                postrun.setResult(true);
                // ** HERE IS WHERE THE MAGIC HAPPENS! **
                // now that we have stored the value, lets run our Runnable
                postrun.run();
                return;
            }
        });
        if (isNeg) {
            alert.setNegativeButton(titleNeg, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    return;
                }
            });
        }
        alert.show();
    }

    public static class PromptRunnable implements Runnable {
        private boolean v = false;

        public boolean getResult() {
            return this.v;
        }

        void setResult(boolean inV) {
            this.v = inV;
        }

        public void run() {
            this.run();
        }
    }

}
