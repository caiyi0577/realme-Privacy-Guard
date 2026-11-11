package com.caiyi.rpg;

import android.app.Application;
import android.content.Context;
import java.util.concurrent.atomic.AtomicBoolean;
import android.app.AndroidAppHelper;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import com.caiyi.rpg.hooks.BusinessHooks;
import com.caiyi.rpg.hooks.FrameworkHooks;
import com.caiyi.rpg.hooks.NetworkHooks;

public final class HookEntry implements IXposedHookLoadPackage {
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lp) {
        if(!TargetCatalog.contains(lp.packageName)||!RuntimeLog.init(lp.packageName,lp.processName))return;
        final AtomicBoolean installed=new AtomicBoolean();
        try {
            Application app=AndroidAppHelper.currentApplication();
            // Shared processes may have a different main Application package (for example obrAIn/midas).
            if(app!=null) { install(lp,app,installed);return; }
            XposedHelpers.findAndHookMethod(Application.class,"attach",Context.class,new XC_MethodHook(){
                protected void beforeHookedMethod(MethodHookParam param){
                    Context context=(Context)param.args[0];
                    install(lp,context,installed);
                }
            });
        } catch(Throwable error) { RuntimeLog.event("FAIL","context-initialization",error.getClass().getSimpleName()); }
    }
    private void install(XC_LoadPackage.LoadPackageParam lp,Context context,AtomicBoolean installed) {
        if(!installed.compareAndSet(false,true))return;
        try {
            Prefs p=Prefs.load(context);
            // Install before Application.attachBaseContext/onCreate can start the audited collectors.
            if(!TargetCatalog.isNarrow(lp.packageName))FrameworkHooks.install(lp.classLoader,lp.packageName,p);
            BusinessHooks.install(lp.classLoader,lp.packageName,lp.appInfo,p);
            NetworkHooks.install(lp.classLoader,lp.packageName,p);
            RuntimeLog.event("DONE","configuration","ok");
        } catch(Throwable error) { RuntimeLog.event("FAIL","initialization",error.getClass().getSimpleName()); }
    }
}
