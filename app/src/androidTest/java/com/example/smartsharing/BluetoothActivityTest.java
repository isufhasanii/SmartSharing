package com.example.smartsharing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.smartsharing.BluetoothActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BluetoothActivityTest {

    private static final String TARGET_DEVICE_ADDRESS = "00:11:22:33:44:55";
    @Rule
    public ActivityTestRule<BluetoothActivity> activityRule = new ActivityTestRule<>(BluetoothActivity.class);

    @Test
    public void testSendImageViaBluetooth() {
        // Vorbedingung: Eine Bluetooth-Verbindung wurde hergestellt

        Bitmap imageBitmap = BitmapFactory.decodeResource(activityRule.getActivity().getResources(), R.drawable.logo);
        activityRule.getActivity().setSelectedImage(imageBitmap);

        activityRule.getActivity().connectToDevice(TARGET_DEVICE_ADDRESS);
    }
}