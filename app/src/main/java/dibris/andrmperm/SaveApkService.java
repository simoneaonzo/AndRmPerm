package dibris.andrmperm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;

import java.io.File;

import it.unige.dibris.rmperm.IOutput;
import it.unige.dibris.rmperm.Main;


public class SaveApkService  extends IntentService {
    public static final String MSG_SAVEAPKSERVICE = "MSG_SAVEAPKSERVICE";
    private final int notifyID = 0;
    private String apkPath;
    private String apkName;
    private String perms;



    public SaveApkService() {
        super("SaveApkService");
    }

    public SaveApkService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle b = intent.getExtras();
        String[] apkAndPerms = b.getStringArray(PermissionsMangeActivity.MSG_PERMISSIONSMANAGE);
        apkPath = apkAndPerms[0];
        apkName = getApkFileName(apkPath);
        perms = apkAndPerms[1];

        NotificationCompat.Builder startNotification =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.ic_notification_overlay)
                        .setContentTitle("RmPerm")
                        .setContentText("Removing permissions from " + apkName)
                        .setAutoCancel(true)
                        .setNumber(1);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notifyID, startNotification.build());

        File sdcard = Environment.getExternalStorageDirectory();
        File customClasses = new File(sdcard, "custom.apk");
        File newApk = new File(sdcard, apkName + "_rmperm.apk");
        JarOutput jo = new JarOutput(IOutput.Level.VERBOSE);
        String[] args = {
                "--input" , apkPath,
                "--remove",
                "--custom-methods", customClasses.toString(),
                "--permissions",  perms,
                "--output", newApk.toString()
        };
        Main.androidMain(jo, args);
        startNotification.setContentText("Finished removing permission from " + apkName).setNumber(2);
        mNotificationManager.notify(notifyID, startNotification.build());

        Intent saveApkResultIntent = new Intent(this, SaveApkResult.class);
        Bundle bundle = new Bundle();
        bundle.putStringArray(MSG_SAVEAPKSERVICE, new String[]{newApk.toString(), jo.getErrors(), jo.getMessages()});
        saveApkResultIntent.putExtras(bundle);
        saveApkResultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(saveApkResultIntent);
    }

    private String getApkFileName(String path) {
        if (path.substring(path.length() - 4, path.length()).equals(".apk")) {
            String out = path.substring(0, path.length() - 4);
            out = path.substring(out.lastIndexOf("/")+1, out.length());
            return out;
        }
        return null;
    }

}
