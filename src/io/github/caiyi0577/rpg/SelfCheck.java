package io.github.caiyi0577.rpg;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SelfCheck {
    public static String run(Context context) throws Exception {
        StringBuilder script=new StringBuilder();
        for(String pkg:TargetCatalog.PACKAGES)script.append("s=$(dumpsys package ").append(pkg)
            .append(" 2>/dev/null | grep 'User 0:' | head -1); printf 'STATE|").append(pkg).append("|%s\\n' \"$s\"\n");
        script.append("echo PROCESS_BEGIN\nps -A -o PID,ARGS\n");
        String snapshot=Su.text(script.toString(),250000);
        if(!snapshot.contains("STATE|"))throw new IllegalStateException("未取得 root 状态快照");
        Map<String,String> states=new HashMap<>();Map<String,List<String>> processes=new HashMap<>();
        Map<String,String> primaryProcesses=new HashMap<>();
        for(String pkg:TargetCatalog.PACKAGES)try {
            primaryProcesses.put(pkg,context.getPackageManager().getApplicationInfo(pkg,0).processName);
        }catch(Exception ignored){ }
        boolean procSection=false;int processRows=0;
        for(String line:snapshot.split("\\r?\\n")) {
            if(line.startsWith("STATE|")){String[] p=line.split("\\|",3);if(p.length==3)states.put(p[1],p[2]);}
            else if(line.equals("PROCESS_BEGIN"))procSection=true;
            else if(procSection){String[] p=line.trim().split("\\s+",2);if(p.length!=2||!p[0].matches("[0-9]+"))continue;
                processRows++;
                for(String pkg:TargetCatalog.PACKAGES)if(p[1].equals(pkg)||p[1].startsWith(pkg+":")||p[1].equals(primaryProcesses.get(pkg))){
                    if(!processes.containsKey(pkg))processes.put(pkg,new ArrayList<>());processes.get(pkg).add(p[0]);
                }
            }
        }
        if(processRows==0)throw new IllegalStateException("未取得进程列表，无法判断未运行状态");
        Map<String,String> starts=new HashMap<>();
        StringBuilder statScript=new StringBuilder();
        for(List<String> ids:processes.values())for(String id:ids)
            statScript.append("printf 'STAT|").append(id).append("|'; cat /proc/").append(id).append("/stat 2>/dev/null; echo\n");
        String statRows=statScript.length()==0?"":Su.text(statScript.toString(),150000);
        for(String line:statRows.split("\\r?\\n")) {
            String[] p=line.split("\\|",3);if(p.length==3)starts.put(p[1],ProcessIdentity.startTicks(p[2]));
        }
        Set<String> scope=new HashSet<>();Boolean enabled=null;String configError=null;
        try {
            File dir=new File(context.getCacheDir(),"lsp_snapshot");if(!dir.exists()&&!dir.mkdirs())throw new IllegalStateException("cache");
            String remote="/data/adb/lspd/config/modules_config.db";
            String before=Su.text("stat -c '%n:%s:%Y' "+remote+"*",16000);
            for(String suffix:new String[]{"","-wal","-shm"}) {
                byte[] bytes=Su.read("if [ -f "+remote+suffix+" ]; then cat "+remote+suffix+"; fi",8*1024*1024);
                try(FileOutputStream out=new FileOutputStream(new File(dir,"modules_config.db"+suffix))){out.write(bytes);}
            }
            String after=Su.text("stat -c '%n:%s:%Y' "+remote+"*",16000);
            if(!before.equals(after))throw new IllegalStateException("检查时配置正在改变，请重试");
            try(SQLiteDatabase db=SQLiteDatabase.openDatabase(new File(dir,"modules_config.db").getPath(),null,SQLiteDatabase.OPEN_READONLY)) {
                try(Cursor c=db.rawQuery("SELECT enabled FROM modules_state WHERE module_pkg_name=? AND user_id=0",new String[]{Prefs.PKG})) {
                    if(c.moveToFirst())enabled=c.getInt(0)==1;else enabled=false;
                }
                try(Cursor c=db.rawQuery("SELECT app_pkg_name FROM scope WHERE module_pkg_name=? AND user_id=0",new String[]{Prefs.PKG})) {
                    while(c.moveToNext())scope.add(c.getString(0));
                }
            }
        } catch(Exception e){configError=e.getMessage();}
        StringBuilder patterns=new StringBuilder();
        for(Map.Entry<String,List<String>> process:processes.entrySet())for(String pid:process.getValue()) {
            String start=starts.getOrDefault(pid,"unknown");
            if(!"unknown".equals(start))patterns.append(" -e '[RPG2]|").append(TargetCatalog.HOOK_VERSION)
                .append('|').append(pid).append('|').append(start).append('|').append(process.getKey()).append("|'");
        }
        String logs="";
        if(patterns.length()>0) {
            String scan="for f in /data/adb/lspd/log.old/modules_*.log /data/adb/lspd/log/modules_*.log; do "
                +"[ -f \"$f\" ] && grep -h -F"+patterns+" \"$f\"; done";
            // Retain initialization separately so frequent HIT rows cannot evict evidence that hooks are ready.
            logs=Su.text("("+scan+") | grep -v -F '|HIT|' | tail -n 2500\n("
                +scan+") | grep -F '|HIT|' | tail -n 1000",4*1024*1024);
        }
        try(FileOutputStream debug=new FileOutputStream(new File(context.getCacheDir(),"last_evidence.txt"))) {
            debug.write(("identities="+starts+"\nmatching_log_bytes="+logs.length()+"\n"+statRows).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }catch(Exception ignored){ }
        LogLedger ledger=new LogLedger();String[] lines=logs.split("\\r?\\n");
        StringBuilder body=new StringBuilder();int current=0,waiting=0,blocked=0,failed=0,notRunning=0;
        for(String pkg:TargetCatalog.PACKAGES) {
            String state=states.get(pkg);body.append('\n').append(TargetCatalog.label(pkg)).append("\n").append(pkg).append('\n');
            List<String> ids=processes.get(pkg);
            BlockStatus.Evidence evidence=new BlockStatus.Evidence();
            evidence.installed=state!=null&&state.contains("installed=true");
            evidence.disabled=state!=null&&(state.contains("enabled=3")||state.contains("enabled=2"));
            evidence.moduleEnabled=enabled;evidence.scoped=scope.contains(pkg);
            evidence.running=ids==null?0:ids.size();
            if(ids!=null)for(String pid:ids) {
                String start=starts.getOrDefault(pid,"unknown");
                for(String line:lines)ledger.accept(line,pkg,pid,start);
                LogLedger.Entry e=ledger.get(pkg,pid,start);
                if(e!=null&&e.loaded) {
                    evidence.loaded++;evidence.hits+=e.observedHits();evidence.hooks+=e.ok.size();
                    evidence.failed+=e.failed.size();evidence.skipped+=e.skipped.size();
                    evidence.complete&=e.done;evidence.configRead&="provider".equals(e.prefs);
                    int missing=0;for(String rule:required(pkg,context))if(!e.rules.contains(rule))missing++;
                    if(TargetCatalog.OLC.equals(pkg)&&Prefs.open(context).getBoolean("olc_upload",true))
                        for(String cls:new String[]{"com.oplus.olc.dcs.service.UploadJobService",
                            "com.oplus.olc.dcs.upload.RetryLogUploadJobService","com.oplus.olc.uploader.upload.RetryUploadJobService"}) {
                            boolean found=false;for(String method:e.ok)if(method.startsWith(cls+"#onStartJob("))found=true;
                            if(!found)missing++;
                        }
                    evidence.missingRequired+=missing;
                    if(e.done&&!e.ok.isEmpty()&&e.failed.isEmpty()&&missing==0)evidence.ready++;
                }
            }
            BlockStatus.Result status=BlockStatus.evaluate(evidence);body.append(status.line());
            if(status.blocked)blocked++;
            if(status.state==BlockStatus.State.FAILED)failed++;
            if(status.state==BlockStatus.State.NOT_RUNNING)notRunning++;
            if(state==null||!state.contains("installed=true")){body.append("未安装或状态未知\n");continue;}
            if(state.contains("enabled=3")||state.contains("enabled=2")){body.append("已禁用（无需为此启用）\n");continue;}
            if(Boolean.FALSE.equals(enabled)){body.append("模块未启用\n");continue;}
            if(enabled!=null&&!scope.contains(pkg)){body.append("未勾选作用域\n");waiting++;continue;}
            if(ids==null||ids.isEmpty()){body.append("当前未运行；实际加载状态待验证\n");continue;}
            for(String pid:ids) {
                String start=starts.getOrDefault(pid,"unknown");
                LogLedger.Entry e=ledger.get(pkg,pid,start);
                body.append("进程 ").append(pid).append("：");
                if(e==null||!e.loaded){body.append("未找到当前进程的规则记录；可能待重启或日志已轮换\n");waiting++;continue;}
                current++;body.append("当前规则已载入；拦截点 ").append(e.ok.size()).append("，跳过 ")
                    .append(e.skipped.size()).append("，失败 ").append(e.failed.size()).append('\n');
                body.append("  已记录阻断至少 ").append(e.observedHits()).append(" 次；").append(e.done?"初始化结束":"初始化未结束").append('\n');
                if(!e.failed.isEmpty())body.append("  失败：").append(e.failed).append('\n');
                if(!e.prefs.equals("provider"))body.append("  配置：").append(e.prefs).append("（可能使用默认值）\n");
                for(String rule:required(pkg,context))if(!e.rules.contains(rule))body.append("  待核对规则：").append(rule).append('\n');
            }
        }
        StringBuilder out=new StringBuilder("真我隐私卫士 "+TargetCatalog.VERSION+"\n");
        out.append("模块：").append(enabled==null?"配置读取未知":enabled?"已启用":"未启用")
            .append("；作用域 ").append(enabled==null?"?":scope.size()).append(" 个\n");
        try {
            android.os.Bundle config=context.getContentResolver().call(android.net.Uri.parse("content://"+Prefs.AUTHORITY),"read",null,null);
            out.append("私有设置接口：").append(config!=null&&config.getInt("schema")==1?"可读":"未取得配置").append('\n');
        } catch(Exception error){out.append("私有设置接口：").append(error.getClass().getSimpleName()).append('\n');}
        out.append("当前进程规则载入 ").append(current).append(" 个；待处理/待验证 ").append(waiting).append(" 项\n");
        out.append("项目状态：阻断 ").append(blocked).append(" 项；阻断失败 ").append(failed)
            .append(" 项；未阻断 ").append(TargetCatalog.PACKAGES.length-blocked-failed-notRunning)
            .append(" 项；未运行 ").append(notRunning).append(" 项\n");
        if(configError!=null)out.append("配置快照读取失败：").append(configError).append('\n');
        out.append("\n状态说明：阻断＝组件已禁用，或当前进程的拦截规则已就绪，或已记录实际拦截。是否触发过上报由命中次数单独说明；部分进程未就绪会明确提示。\n")
            .append("阻断失败＝Hook 安装、初始化报错或关键拦截规则缺失，以红字标注；其他规则仍有拦截时会一并说明。\n")
            .append("未阻断＝规则尚未加载、模块未启用、作用域未勾选或安装状态未知。默认配置不等于没有拦截，会单独提示。没有命中记录不再单独判为未阻断。\n")
            .append("未运行＝本次快照未发现该组件的运行进程，待进程启动后再验证。\n")
            .append("“载入”不等于拦截生效。阻断计数按 1、64、128…采样记录。\n")
            .append("仅核对当前 PID 和启动标识的新版日志；日志轮换会导致记录未知。此检查不验证服务器是否收到数据。\n")
            .append(body).append("\n框架接口兼容性（只检查签名，不调用采集接口）\n").append(probe());
        return out.toString();
    }
    private static String[] required(String pkg,Context context) {
        if(TargetCatalog.UMS.equals(pkg)&&Prefs.open(context).getBoolean("ums_upload",true))return new String[]{"ums-location-upload","ums-sms-upload","ums-location-dcs"};
        if(TargetCatalog.ADS.equals(pkg)&&Prefs.open(context).getBoolean("ads",true))return new String[]{"ads-network"};
        return new String[0];
    }
    public static String probe() {
        StringBuilder b=new StringBuilder();
        String[][] check={{"android.location.LocationManager","requestLocationUpdates"},
            {"android.location.LocationManager","getCurrentLocation"},{"android.app.usage.UsageStatsManager","queryEvents"},
            {"android.app.usage.UsageStatsManager","queryAndAggregateUsageStats"}};
        for(String[] item:check)try {
            int n=0;for(Method m:Class.forName(item[0]).getDeclaredMethods())if(m.getName().equals(item[1]))n++;
            b.append(item[1]).append("：").append(n).append(" 个声明\n");
        }catch(Exception e){b.append(item[1]).append("：不可用\n");}
        return b.toString();
    }
    private SelfCheck() { }
}
