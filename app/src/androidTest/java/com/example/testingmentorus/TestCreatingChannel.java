package com.example.testingmentorus;

import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import android.util.DisplayMetrics;
import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class TestCreatingChannel {
    private UiDevice mDevice;
    private Context context;
    private List<String> rowData;

    public TestCreatingChannel(List<String> rowData) {
        this.rowData = rowData;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws IOException {
        List<List<String>> csvData = readCSVFromAssets(InstrumentationRegistry.getInstrumentation().getContext(), "input/Channel1.csv");
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
    public void testChannel() throws UiObjectNotFoundException {
        // nút tick ở trên bên phải
        int x_setting_dp = 378;
        int y_setting_dp = 51;
        int x_setting_px = DpToPxConverter.convertDpToPixel(x_setting_dp, context);
        int y_setting_px = DpToPxConverter.convertDpToPixel(y_setting_dp, context);

        // Nhấn vào tạo keh mới
        UiObject createChannel = mDevice.findObject(new UiSelector().text("Tạo kênh mới"));
        createChannel.clickAndWaitForNewWindow();

        // Nhập tên kênh
        UiObject titleField = mDevice.findObject(new UiSelector().className("android.widget.EditText").instance(0));
        titleField.setText(rowData.get(0));
        assertNotEquals("Tên kênh trống", rowData.get(0),"");

        // Nhập mô tả kênh
        UiObject descriptionField = mDevice.findObject(new UiSelector().className("android.widget.EditText").instance(1));
        descriptionField.setText(rowData.get(1));

        // Nhấn chọn thành viên
        UiObject chooseMember = mDevice.findObject(new UiSelector().className("android.widget.TextView").instance(3));
        chooseMember.clickAndWaitForNewWindow();

        // Nhấp bỏ chọn tất cả
        UiObject allMember = mDevice.findObject(new UiSelector().text("Tất cả"));
        allMember.click();

        // Đánh dấu vào các thành viên trong danh sách
        int count = 0;
        String[] memberName = rowData.get(2).split("-");
        for (String name : memberName) {
            UiObject member = mDevice.findObject(new UiSelector().text(name));
            assertTrue("Thành viên không hợp lệ: " + name, member.exists());
            count ++;
            member.click();
        }
        // Nhấn tick
        mDevice.click(x_setting_px, y_setting_px);
        assertTrue("Kênh phải có ít nhất 2 thành viên",count >= 2);
        mDevice.waitForWindowUpdate(null, 200);

        mDevice.click(x_setting_px, y_setting_px);
        mDevice.waitForWindowUpdate(null, 3000);
        UiObject existedChannel = mDevice.findObject(new UiSelector().className("android.widget.TextView").text("Tên kênh đã được sử dụng."));
        assertFalse("Tên kênh đã được sử dụng", existedChannel.exists());
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
}

class DpToPxConverter {
    public static int convertDpToPixel(float dp, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return Math.round(px);
    }
}