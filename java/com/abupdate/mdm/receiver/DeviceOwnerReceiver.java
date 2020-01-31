package com.abupdate.mdm.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.abupdate.mdm.R;
import com.abupdate.mdm.activity.MainActivity;

/*
 * @date   : 2019/09/26
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class DeviceOwnerReceiver extends android.app.admin.DeviceAdminReceiver {
    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
    }

    /**
     * Called on the new profile when device owner provisioning has completed. Device owner
     * provisioning is the process of setting up the device so that its main profile is managed by
     * the mobile device management (MDM) application set up as the device owner.
     */
    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        // Enable the profile
        DevicePolicyManager manager =
                (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = getComponentName(context);
        manager.setProfileName(componentName, context.getString(R.string.app_name));
        // Open the main screen
        Intent launch = new Intent(context, MainActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launch);
    }

    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        super.onEnabled(context, intent);
    }

    /**
     * @return A newly instantiated {@link android.content.ComponentName} for this
     * DeviceOwnerReceiver.
     */
    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context.getApplicationContext(), DeviceOwnerReceiver.class);
    }

}
