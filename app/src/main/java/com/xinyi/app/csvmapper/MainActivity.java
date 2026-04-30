package com.xinyi.app.csvmapper;

import android.os.Bundle;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.gyf.immersionbar.ImmersionBar;
import com.xinyi.androidbasic.base.activity.BaseViewBindingActivity;
import com.xinyi.androidbasic.extension.ResourcesExtension;
import com.xinyi.app.csvmapper.databinding.ActivityMainBinding;
import com.xinyi.app.csvmapper.model.UserModel;
import com.xinyi.app.csvmapper.touch.TouchCompat;
import com.xinyi.device.app.AppManager;
import com.xinyi.file.io.FileSizeUtil;
import com.xinyi.csvmapper.CsvMapper;
import com.xinyi.csvmapper.annotation.CsvColumn;
import com.xinyi.csvmapper.bind.CsvTypeToken;
import com.xinyi.csvmapper.buffered.reader.CsvReader;
import com.xinyi.csvmapper.model.CsvRow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 框架 Demo 主界面，主要演示框架的核心能力。
 *
 * @author 新一
 * @date 2026/4/24 18:50
 */
public class MainActivity extends BaseViewBindingActivity<ActivityMainBinding> {

    /**
     * 虚假用户数据
     */
    private static List<UserModel> mockUsers() {
        List<UserModel> users = new ArrayList<>();
        users.add(new UserModel(1, "希尔科", 38, "每个人的心里都有一头怪兽，只不过有些人学会了如何与它共处。"));
        users.add(new UserModel(2, "卡米尔", 26, "世界既不黑，也不白，而是一道精致的灰。"));
        users.add(new UserModel(3, "时光", 80, "人生最痛苦的事莫过于明知要失去，却还没有发生。"));
        users.add(new UserModel(4, "婕拉", 32, "世间万物，表里如一者，又有几何？"));
        users.add(new UserModel(5, "无极剑圣", 35, "真正的大师永远都怀着一颗学徒的心。"));
        users.add(new UserModel(6, "艾克", 18, "时间不在乎你拥有多少，而在于你如何使用。"));
        users.add(new UserModel(7, "深海泰坦", 42, "倘若你迷失在黑暗之中，除了前行别无他法。"));
        users.add(new UserModel(8, "塔里克", 33, "我曾踏足山巅，也曾进入低谷，二者都让我受益良多。"));
        users.add(new UserModel(9, "佐伊", 14, "时光啊就像潮水，它送来了一切，也会带走一切。"));
        return users;
    }

    /**
     * 演示用 CSV 文件，存放在应用缓存目录
     */
    private File mDemoFile;

    /** 信息提示颜色 */
    private int mColorInfo;

    /** 成功提示颜色 */
    private int mColorOk;

    /** 错误提示颜色 */
    private int mColorError;

    /** 次要信息颜色 */
    private int mColorDim;

    @Override
    protected int initLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initParams(@Nullable Bundle savedInstanceState) {
        ImmersionBar.with(this)
                // 自动适配暗色模式
                .autoDarkModeEnable(true, 0.2f)
                .fitsSystemWindows(false).init();
        ImmersionBar.setTitleBar(this, getBinding().llToolbar);

        mDemoFile = new File(getExternalCacheDir(), "demo_users.csv");

        mColorInfo = ContextCompat.getColor(this, R.color.flux_log_info);
        mColorOk = ContextCompat.getColor(this, R.color.flux_log_text);
        mColorError = ContextCompat.getColor(this, R.color.flux_log_error);
        mColorDim = ContextCompat.getColor(this, R.color.flux_log_dim);

        // 显示版本名
        getBinding().tvAppVersion.setText(AppManager.getAppVersionName());

        // 解决与父容器（ScrollView/NestedScrollView）的滑动冲突
        TouchCompat.fixTextViewScroll(getBinding().tvLogInfo);

        // 初始化日志区，打印就绪信息
        initLogInfo();
    }

    /**
     * 打印就绪的日志信息
     */
    private void initLogInfo() {
        getBinding().tvLogInfo.setText("");
        appendLog(mColorDim, ResourcesExtension.getString(R.string.app_name) + " 已就绪");
        appendLog(mColorDim, "文件路径：" + mDemoFile.getAbsolutePath());
    }

    @Override
    protected void initListeners() {
        getBinding().btnWrite.setOnClickListener(view -> runWrite());
        getBinding().btnReadByClass.setOnClickListener(view -> runReadByClass());
        getBinding().btnReadByCsvType.setOnClickListener(view -> runReadByCsvType());
        getBinding().btnReadRow.setOnClickListener(view -> runReadRow());
        getBinding().tvReset.setOnClickListener(view -> initLogInfo());
    }

    /**
     * 将 UserModel 列表序列化写入 CSV 文件
     */
    private void runWrite() {
        appendLog(mColorInfo, "▶ 执行写入演示");
        try {
            List<UserModel> users = mockUsers();

            // 使用泛型类型令牌写入，框架自动生成表头
            CsvMapper.serialize(mDemoFile, users, new CsvTypeToken<List<UserModel>>() {});
            for (UserModel user : users) {
                appendLog(mColorDim, user.toString());
            }
            appendLog(mColorOk, "✓ 写入成功，共 " + users.size() + " 条记录");
            appendLog(mColorOk, fileSizeLog());
        } catch (Exception exception) {
            appendLog(mColorError, "✗ 写入失败: " + exception.getMessage());
        }
    }

    /**
     * 文件大小日志
     */
    private String fileSizeLog() {
        return "文件名：" + mDemoFile.getName() + "，大小：" + FileSizeUtil.formatFileSize(mDemoFile.length());
    }

    /**
     * 使用 Class 方式解析 CSV 为对象列表
     *
     * <p> 框架会根据 {@link CsvColumn} 注解将 CSV 行自动映射为 User 对象 </p>
     */
    private void runReadByClass() {
        appendLog(mColorInfo, "▶ 读取演示（Class 方式）");
        if (!mDemoFile.exists()) {
            appendLog(mColorError, "✗ 文件不存在，请先执行写入");
            return;
        }
        try {
            List<UserModel> users = CsvMapper.parse(mDemoFile, UserModel.class);
            for (UserModel user : users) {
                appendLog(mColorDim, user.toString());
            }
            appendLog(mColorOk, "✓ 解析成功，共 " + users.size() + " 条记录");
        } catch (Exception exception) {
            appendLog(mColorError, "✗ 读取失败: " + exception.getMessage());
        }
    }

    /**
     * 使用 CsvType 泛型令牌解析 CSV 为对象列表
     *
     * <p> 与 {@link #runWrite()} 的写入方式完全对称，适合多态场景或需要明确指定类型的情况 </p>
     */
    private void runReadByCsvType() {
        appendLog(mColorInfo, "▶ 读取演示（CsvType 泛型令牌方式）");
        if (!mDemoFile.exists()) {
            appendLog(mColorError, "✗ 文件不存在，请先执行写入");
            return;
        }
        try {
            List<UserModel> users = CsvMapper.parse(mDemoFile, new CsvTypeToken<List<UserModel>>() { });
            for (UserModel user : users) {
                appendLog(mColorDim, user.toString());
            }
            appendLog(mColorOk, "✓ 解析成功，共 " + users.size() + " 条记录");
        } catch (Exception e) {
            appendLog(mColorError, "✗ 读取失败: " + e.getMessage());
        }
    }

    /**
     * 使用 CsvReader 迭代器逐行读取
     *
     * <p> 通过迭代器逐行处理，适合大文件场景，避免一次性加载导致 OOM </p>
     */
    private void runReadRow() {
        appendLog(mColorInfo, "▶ 逐行读取演示（CsvReader 迭代器）");
        if (!mDemoFile.exists()) {
            appendLog(mColorError, "✗ 文件不存在，请先执行写入");
            return;
        }
        try (CsvReader reader = CsvMapper.reader(mDemoFile)) {
            int rowCount = 0;

            // 手动读取表头行并打印
            CsvRow header = reader.readNextRow();
            if (header != null) {
                appendLog(mColorInfo, "表头: " + header.getFields());
            }

            // 逐行读取数据行
            CsvRow row;
            while ((row = reader.readNextRow()) != null) {
                rowCount++;
                appendLog(mColorDim, "行 " + row.getLineNumber() + ": " + row.getFields());
            }
            appendLog(mColorOk, "✓ 逐行读取完成，共 " + rowCount + " 行数据");

        } catch (Exception exception) {
            appendLog(mColorError, "✗ 读取失败: " + exception.getMessage());
        }
    }

    /**
     * 向日志区追加一行带颜色的文本，并自动滚动到底部
     *
     * <p> 使用 {@link SpannableStringBuilder} 实现多色文本拼接 </p>
     *
     * @param color 文字颜色（ARGB 格式）
     * @param text 日志内容
     */
    private void appendLog(int color, String text) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(getBinding().tvLogInfo.getText());
        int start = ssb.length();
        ssb.append(text).append("\n");
        ssb.setSpan(new ForegroundColorSpan(color), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView textView = getBinding().tvLogInfo;
        textView.setText(ssb);
        // 自动滚动到底部，显示最新日志
        textView.post(()-> {
            Layout layout = textView.getLayout();
            int scrollAmount = layout.getLineTop(textView.getLineCount()) - textView.getHeight();
            textView.scrollTo(0, Math.max(scrollAmount, 0));
        });
    }
}