package com.caiyi.rpg;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public final class MainActivity extends Activity {
    private SharedPreferences prefs;
    private LinearLayout box;
    private TextView result;
    private Button check;
    protected void onCreate(Bundle state) {
        super.onCreate(state);setContentView(R.layout.activity_main);
        prefs=Prefs.open(this);box=findViewById(R.id.container);
        SharedPreferences.Editor defaults=prefs.edit();
        for(String key:new String[]{"carrier","ai","applist","sample","telemetry","net","ums_upload","ads","olc_upload"})
            if(!prefs.contains(key))defaults.putBoolean(key,true);
        defaults.putInt("ui_version",3).commit();
        text("真我隐私卫士",24,true);
        text(TargetCatalog.VERSION+" · LSPosed 模块",14,false);
        text("设置使用私有存储和受限只读接口，不再使用已弃用的共享配置功能。",13,false);
        text("升级后需让目标进程重新加载。新增作用域：智慧建议、智慧推荐、日志服务。RegService 保持禁用即可。",14,false);
        text("新增保护",19,true);
        toggle("智慧建议外发保护","拦截已确认的位置/Wi‑Fi、短信模型上传和位置统计事件；保留本地识别。云端学习及部分建议可能受影响。","ums_upload");
        toggle("广告与推荐请求保护","阻止当前已审计版本的广告网络适配器及已知端点请求；推荐内容可能不再更新。","ads");
        toggle("日志上传任务保护","阻止已确认的日志上传及重试任务，不关闭日志服务或录屏功能。诊断报告上传可能受影响。","olc_upload");
        text("原有保护",19,true);
        toggle("设备标识与位置","对原有目标过滤设备/SIM/基站信息、定位请求，阻止 SystemHelper 已确认的注册入口。天气定位、运营商配置可能受影响。","carrier");
        toggle("录音启动与使用记录","阻止原有目标的录音启动、使用记录查询和无障碍列表枚举。此项不保证阻止特权截屏。","ai");
        toggle("应用列表过滤","采集型目标得到空列表，功能型目标仅得到系统应用。应用扫描等功能可能受影响。","applist");
        toggle("安装包元信息","限制安装包元信息及安装来源查询；不等同于阻止所有文件读取。","sample");
        toggle("设备状态返回值","保留旧版设备状态伪装；不改变手机真实引导状态，也不代表全面遥测防护。","telemetry");
        toggle("已知上报端点保护","在 Java DNS 和 URL 建连前阻止已知端点，补充 RegService 地址。不能代替全面防火墙或覆盖所有原生网络。","net");
        text("自检",19,true);
        check=new Button(this);check.setText("运行自检");box.addView(check);
        result=text("逐项显示“阻断/阻断失败/未阻断/未运行”和原因；阻断失败以红字标注。需要本模块的 root 授权。",13,false);
        result.setTextIsSelectable(true);result.setTypeface(Typeface.MONOSPACE);
        check.setOnClickListener(v -> runCheck());
        text("作用域",19,true);
        StringBuilder scopes=new StringBuilder();for(String p:TargetCatalog.PACKAGES)scopes.append(TargetCatalog.label(p)).append(" · ").append(p).append('\n');
        text(scopes.toString(),12,false);
        text("开关变化在目标进程下次加载时生效。新规则版本不匹配会显示跳过，不能据此宣称已全面阻断。",13,false);
        if(getIntent().getBooleanExtra("run_selfcheck",false))runCheck();
    }
    private TextView text(String value,int size,boolean bold) {
        TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setLineSpacing(dp(3),1);
        if(bold)t.setTypeface(null,Typeface.BOLD);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(bold?20:10);box.addView(t,lp);return t;
    }
    private void toggle(String title,String description,String key) {
        Switch s=new Switch(this);s.setText(title);s.setTextSize(16);s.setChecked(prefs.getBoolean(key,true));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(18);box.addView(s,lp);
        text(description,13,false);
        s.setOnCheckedChangeListener((button,enabled) -> prefs.edit().putBoolean(key,enabled).apply());
    }
    private void runCheck() {
        check.setEnabled(false);result.setText("正在检查…");
        new Thread(() -> {
            String message;
            try{message=SelfCheck.run(this);}catch(Exception e){message="检查未完成："+e.getMessage()+"\n请确认已给本模块 root 授权，或稍后重试。";}
            final String report=message;
            try(FileOutputStream out=new FileOutputStream(new File(getCacheDir(),"last_selfcheck.txt"))){out.write(report.getBytes(StandardCharsets.UTF_8));}
            catch(Exception ignored){ }
            try(FileOutputStream out=new FileOutputStream(new File(getCacheDir(),"last_style_check.txt"))){out.write(ReportStyle.verify(this).getBytes(StandardCharsets.UTF_8));}
            catch(Exception ignored){ }
            runOnUiThread(() -> {if(!isFinishing()){result.setText(ReportStyle.format(report,this));check.setEnabled(true);}});
        },"RPG self-check").start();
    }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
}
