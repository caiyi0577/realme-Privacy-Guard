package com.caiyi.rpg.hooks;

import android.content.pm.ApplicationInfo;
import java.io.File;
import com.caiyi.rpg.Prefs;
import com.caiyi.rpg.RuntimeLog;
import com.caiyi.rpg.TargetCatalog;
import de.robv.android.xposed.XC_MethodHook;

public final class BusinessHooks {
    public static void install(ClassLoader cl,String pkg,ApplicationInfo info,Prefs p) {
        if(TargetCatalog.UMS.equals(pkg)&&p.ums) {
            HookUtil.exact(cl,"com.oplus.advice.framework.datacollect.phonestatus.PhoneStateInfoUploader","c","void",
                new String[]{"long","android.location.Location","com.oplus.advice.framework.datacollect.util.WifiUtils$WifiInfoStruct","java.util.ArrayList"},
                "ums-location-upload",HookUtil.returning("ums-location-upload",null));
            HookUtil.exact(cl,"com.oplus.advice.component.analyzer.upload.b","a","void",
                new String[]{"com.oplus.sceneservice.components.semanticanalysis.engine.hybrid.upload.e"},
                "ums-sms-upload",HookUtil.returning("ums-sms-upload",null));
            HookUtil.exact(cl,"com.oplus.advice.common.core.statistics.a","a","void",
                new String[]{"java.lang.String","java.util.Map"},"ums-location-dcs",new XC_MethodHook() {
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if("201560302".equals(param.args[0])) {
                            RuntimeLog.hit("ums-location-dcs");param.setResult(null);
                        }
                    }
                });
        }
        if(TargetCatalog.ADS.equals(pkg)&&p.ads) {
            if(knownApk(info,4880757L)) {
                // This adapter already specifies null as a normal network-unavailable result.
                HookUtil.exact(cl,"e6.b","b","e6.d",new String[]{"android.content.Context","e6.c"},
                    "ads-network",HookUtil.returning("ads-network",null));
            } else RuntimeLog.event("SKIP","ads-network","apk-profile-mismatch");
        }
        if(TargetCatalog.OLC.equals(pkg)&&p.olc) {
            for(String cls:new String[]{"com.oplus.olc.dcs.service.UploadJobService",
                    "com.oplus.olc.dcs.upload.RetryLogUploadJobService","com.oplus.olc.uploader.upload.RetryUploadJobService"})
                job(cl,cls,"olc-upload-job");
        }
        if("com.oplus.statistics.rom".equals(pkg)&&p.net)job(cl,"com.oplus.statistics.rom.service.UploadJobService","dcs-upload-job");
        if("com.mobiletools.systemhelper".equals(pkg)&&p.carrier) {
            job(cl,"com.mobiletools.systemhelper.service.RegisterJobService","carrier-job");
            HookUtil.exact(cl,"com.mobiletools.systemhelper.service.RegisterIntentService","onHandleIntent","void",
                new String[]{"android.content.Intent"},"carrier-intent",HookUtil.returning("carrier-intent",null));
            if(knownApk(info,3880533L)) {
                for(String cls:new String[]{"e1.g","e1.e"})
                    HookUtil.exact(cl,cls,"run","void",new String[0],"carrier-register-upload",HookUtil.returning("carrier-register-upload",null));
                if(p.appList)HookUtil.exact(cl,"e1.f","run","void",new String[0],"carrier-app-collection",HookUtil.returning("carrier-app-collection",null));
            } else RuntimeLog.event("SKIP","carrier-register-upload","apk-profile-mismatch");
        }
    }
    private static void job(ClassLoader cl,String cls,String rule) {
        HookUtil.exact(cl,cls,"onStartJob","boolean",new String[]{"android.app.job.JobParameters"},rule,HookUtil.returning(rule,Boolean.FALSE));
    }
    private static boolean knownApk(ApplicationInfo info,long bytes) {
        // Compatibility guard for audited obfuscated names, not a trust/security check.
        return info!=null&&info.sourceDir!=null&&new File(info.sourceDir).length()==bytes;
    }
    private BusinessHooks() { }
}
