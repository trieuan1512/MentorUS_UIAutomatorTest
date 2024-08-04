package com.example.testingmentorus;

import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import org.junit.runners.Parameterized;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import android.util.Log;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


@RunWith(Parameterized.class)
public class TestingChangeProfile {
    private UiDevice mDevice;
    private Context context;
    private List<String> rowData;

    public TestingChangeProfile(List<String> rowData) {
        this.rowData = rowData;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws IOException {
        List<List<String>> csvData = readCSVFromAssets(InstrumentationRegistry.getInstrumentation().getContext(), "input/profile1.csv");
        List<Object[]> data = new ArrayList<>();
        for (List<String> row : csvData) {
            data.add(new Object[]{row});
        }
        return data;
    }

    @Before
    public void setUp() {
        mDevice = UiDevice.getInstance(getInstrumentation());
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void testProfile() throws UiObjectNotFoundException {
//        int x_setting_dp_profile = 349;
//        int y_setting_dp_profile = 831;
//        int x_setting_px_profile = DpToPxConverter.convertDpToPixel(x_setting_dp_profile, context);
//        int y_setting_px_profile = DpToPxConverter.convertDpToPixel(y_setting_dp_profile, context);
        int x_setting_dp_tick = 378;
        int y_setting_dp_tick = 51;
        int x_setting_px_tick = DpToPxConverter.convertDpToPixel(x_setting_dp_tick, context);
        int y_setting_px_tick = DpToPxConverter.convertDpToPixel(y_setting_dp_tick, context);

        UiObject channelText = mDevice.findObject(new UiSelector().text("Cập nhật"));
        channelText.clickAndWaitForNewWindow();

        UiObject fullName = mDevice.findObject(new UiSelector().className("android.widget.EditText").instance(0));
        fullName.setText(rowData.get(0));
        assertNotEquals("Họ tên không được để trống", rowData.get(0),"");

        UiObject phone = mDevice.findObject(new UiSelector().className("android.widget.EditText").instance(2));
        phone.setText(rowData.get(1));
        assertEquals("Nhập số điện thoại không hợp lệ", rowData.get(1), phone.getText());

        String[] birthday = rowData.get(2).trim().split("/");
        int day = Integer.parseInt(birthday[0]);
        int month = Integer.parseInt(birthday[1]);
        int year = Integer.parseInt(birthday[2]);

        UiObject birthDay = mDevice.findObject(new UiSelector().className("android.widget.EditText").instance(1));
        birthDay.clickAndWaitForNewWindow();

        setMonth(mDevice, mDevice.findObject(new UiSelector().className("android.widget.NumberPicker").instance(1)), month);
        setDate(mDevice, mDevice.findObject(new UiSelector().className("android.widget.NumberPicker").instance(0)), day);
        setYear(mDevice, mDevice.findObject(new UiSelector().className("android.widget.NumberPicker").instance(2)), year);

        UiObject okButton = mDevice.findObject(new UiSelector().text("Xác nhận"));
        okButton.click();

        assertEquals("Nhập ngày sinh không hợp lệ", rowData.get(2).trim(), birthDay.getText().trim());
        mDevice.click(x_setting_px_tick, y_setting_px_tick);
        mDevice.waitForWindowUpdate(null, 500);

        UiObject existedChannel = mDevice.findObject(new UiSelector().className("android.widget.TextView").text("Số điện thoại không hợp lệ"));
        assertFalse("Số điện thoại không hợp lệ", existedChannel.exists());

        // Nhấn cập nhật lần 2
        channelText.clickAndWaitForNewWindow();
        // Nhấn tick lần 2
        mDevice.click(x_setting_px_tick, y_setting_px_tick);
        mDevice.waitForWindowUpdate(null, 500);
    }

    @Rule
    public TestWatcher testWatcher = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            mDevice.pressBack();
            Log.e("TestError", "Error during test: " + e.getMessage());
        }
    };

    private static List<List<String>> readCSVFromAssets(Context context, String fileName) throws IOException {
        List<List<String>> data = new ArrayList<>();
        InputStream inputStream = context.getAssets().open(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line = reader.readLine();

        while ((line = reader.readLine()) != null) {
            String[] values = line.split(",");
            data.add(Arrays.asList(values));
        }

        reader.close();
        return data;
    }

    private void setMonth(UiDevice device, UiObject selector, int targetValue) throws UiObjectNotFoundException {
        targetValue = targetValue % 12;
        if (targetValue == 0){
            targetValue = 12;
        }
        int x = 158;
        int y = 356;
        int width = 71;
        int height = 179;
        int height_conv = DpToPxConverter.convertDpToPixel(height, context);
        int startX = DpToPxConverter.convertDpToPixel((int)(x+width/2), context);
        int startY = DpToPxConverter.convertDpToPixel((int)(y+height/2), context);
        boolean check = false;
        int count = 0;

        String arr[] = selector.getChild(new UiSelector().className("android.widget.EditText")).getText().split(" ");
        String currentValue = arr[1].trim();
        int currentValueInt = Integer.parseInt(currentValue);
        while (currentValueInt != targetValue){
            if ((currentValueInt < targetValue && currentValueInt < targetValue - 6) || (currentValueInt > targetValue && currentValueInt < targetValue + 6)) {
                swipeOnObject(SwipeDirection.DOWN, height_conv, startX, startY);
            }
            else {
                swipeOnObject(SwipeDirection.UP, height_conv, startX, startY);
            }
            mDevice.waitForWindowUpdate(null, 1000);
            String tmp[] = selector.getChild(new UiSelector().className("android.widget.EditText")).getText().split(" ");
            currentValue = currentValue = tmp[1].trim();
            currentValueInt = Integer.parseInt(currentValue);
        }
        mDevice.waitForWindowUpdate(null, 1000);
    }

    private void setDate(UiDevice device, UiObject selector, int targetValue) throws UiObjectNotFoundException {
        int x = 108;
        int y = 356;
        int width = 40;
        int height = 179;
        int height_conv = DpToPxConverter.convertDpToPixel(height, context);
        int startX = DpToPxConverter.convertDpToPixel((int)(x+width/2), context);
        int startY = DpToPxConverter.convertDpToPixel((int)(y+height/2), context);

        UiObject child = selector.getChild(new UiSelector().className("android.widget.EditText"));
        String currentValue = selector.getChild(new UiSelector().className("android.widget.EditText")).getText().trim();
        int currentValueInt = Integer.parseInt(currentValue);
        while (currentValueInt != targetValue){
            if ((currentValueInt < targetValue && currentValueInt < targetValue - 15) || (currentValueInt > targetValue && currentValueInt < targetValue + 15)){
                swipeOnObject(SwipeDirection.DOWN, height_conv, startX, startY);
            }
            else {
                swipeOnObject(SwipeDirection.UP, height_conv, startX, startY);
            }
            mDevice.waitForWindowUpdate(null, 1000);
            int previousValue = currentValueInt;
            currentValue = selector.getChild(new UiSelector().className("android.widget.EditText")).getText().trim();
            currentValueInt = Integer.parseInt(currentValue);
            if (currentValueInt == previousValue){
                break;
            }
        }
        mDevice.waitForWindowUpdate(null, 1000); // Chờ thêm 2 giây
    }

    private void setYear(UiDevice device, UiObject selector, int targetValue) throws UiObjectNotFoundException {
        int x = 239;
        int y = 356;
        int width = 64;
        int height = 179;
        int height_conv = DpToPxConverter.convertDpToPixel(height, context);
        int startX = DpToPxConverter.convertDpToPixel((int) (x + width / 2), context);
        int startY = DpToPxConverter.convertDpToPixel((int) (y + height / 2), context);
        String currentValue = selector.getChild(new UiSelector().className("android.widget.EditText")).getText().trim();
        int currentValueInt = Integer.parseInt(currentValue);
        while (currentValueInt != targetValue) {
            if (currentValueInt < targetValue) {
                swipeOnObject(SwipeDirection.UP, height_conv, startX, startY);
            } else {
                swipeOnObject(SwipeDirection.DOWN, height_conv, startX, startY);
            }
            mDevice.waitForWindowUpdate(null, 1000);
            currentValue = selector.getChild(new UiSelector().className("android.widget.EditText")).getText().trim();
            currentValueInt = Integer.parseInt(currentValue);
        }
    }

    private void swipeOnObject(SwipeDirection direction, int height, int startX, int startY) throws UiObjectNotFoundException {
        int endX = startX;
        int endY = startY;

        switch (direction) {
            case UP:
                endY = startY - 200;
                break;
            case DOWN:
                endY = startY + 200;
                break;
        }
        boolean success = mDevice.swipe(startX, startY, endX, endY, 25);
    }

    private enum SwipeDirection {
        UP,
        DOWN
    }
}