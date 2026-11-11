package io.github.caiyi0577.rpg.hooks;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.os.CancellationSignal;
import io.github.caiyi0577.rpg.Prefs;
import io.github.caiyi0577.rpg.RuntimeLog;
import io.github.caiyi0577.rpg.TargetCatalog;
import de.robv.android.xposed.XC_MethodHook;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public final class FrameworkHooks {
    private static boolean named(Method m,String... names) { return Arrays.asList(names).contains(m.getName()); }
    public static void install(ClassLoader cl,String pkg,Prefs p) {
        if(p.carrier) { identifiers(cl); locations(cl); }
        if(p.appList) appLists(cl,TargetCatalog.isCollector(pkg));
        if(p.ai) { usage(cl); audio(cl); accessibility(cl); }
        if(p.sample) metadata(cl);
        if(p.telemetry) properties(cl);
    }
    private static void identifiers(ClassLoader cl) {
        HookUtil.install(cl,"android.telephony.TelephonyManager","identity",m ->
            named(m,"getDeviceId","getImei","getMeid","getSubscriberId","getSimSerialNumber","getLine1Number",
                "getSimOperatorName","getNetworkOperatorName") && m.getReturnType()==String.class
                && (m.getParameterCount()==0 || (m.getParameterCount()==1&&m.getParameterTypes()[0]==int.class)),
            new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam p) {
                    String n=p.method.getName(), value="";
                    if(n.equals("getImei")||n.equals("getDeviceId")||n.equals("getSubscriberId"))value="000000000000000";
                    if(n.equals("getMeid"))value="00000000000000";
                    if(n.equals("getSimSerialNumber"))value="00000000000000000000";
                    RuntimeLog.hit("identity"); p.setResult(value);
                }
            });
        HookUtil.install(cl,"android.telephony.TelephonyManager","cells",m ->
            named(m,"getAllCellInfo","getNeighboringCellInfo","getCellSignalStrengths")&&m.getReturnType()==List.class,
            HookUtil.returning("cells",Collections.emptyList()));
        HookUtil.install(cl,"android.telephony.TelephonyManager","cell-location",m ->
            m.getName().equals("getCellLocation")&&!m.getReturnType().isPrimitive(),HookUtil.returning("cell-location",null));
        HookUtil.install(cl,"android.telephony.SubscriptionManager","sim-list",m ->
            named(m,"getActiveSubscriptionInfoList","getCompleteActiveSubscriptionInfoList","getAccessibleSubscriptionInfoList")
            &&m.getReturnType()==List.class,HookUtil.returning("sim-list",Collections.emptyList()));
        HookUtil.install(cl,"android.telephony.SubscriptionManager","sim-info",m ->
            named(m,"getActiveSubscriptionInfoForSimSlotIndex","getActiveSubscriptionInfo")&&!m.getReturnType().isPrimitive(),
            HookUtil.returning("sim-info",null));
        HookUtil.install(cl,"android.telephony.OplusOSTelephonyManager","vendor-identity",m ->
            named(m,"getSubscriberIdGemini","getSimSerialNumberGemini")&&m.getReturnType()==String.class,
            HookUtil.returning("vendor-identity",""));
    }
    private static boolean hasCallback(Method m) {
        for(Class<?> t:m.getParameterTypes()) {
            String n=t.getName();
            if(n.equals("android.location.LocationListener")||n.equals("android.app.PendingIntent")||n.equals("java.util.function.Consumer"))return true;
        }
        return false;
    }
    private static void locations(ClassLoader cl) {
        HookUtil.install(cl,"android.location.LocationManager","location-cache",m ->
            named(m,"getLastKnownLocation","getLastLocation")&&m.getReturnType()==Location.class,
            HookUtil.returning("location-cache",null));
        HookUtil.install(cl,"android.location.LocationManager","location-updates",m ->
            named(m,"requestLocationUpdates","requestSingleUpdate")&&m.getReturnType()==void.class&&hasCallback(m),
            HookUtil.returning("location-updates",null));
        HookUtil.install(cl,"android.location.LocationManager","location-current",m ->
            m.getName().equals("getCurrentLocation")&&m.getReturnType()==void.class&&hasCallback(m),new XC_MethodHook() {
                @SuppressWarnings("unchecked")
                protected void beforeHookedMethod(MethodHookParam p) {
                    Executor executor=null; Consumer<Location> consumer=null; boolean cancelled=false;
                    for(Object a:p.args) {
                        if(a instanceof Executor)executor=(Executor)a;
                        if(a instanceof Consumer)consumer=(Consumer<Location>)a;
                        if(a instanceof CancellationSignal)cancelled=((CancellationSignal)a).isCanceled();
                    }
                    p.setResult(null); RuntimeLog.hit("location-current");
                    if(!cancelled&&consumer!=null&&executor!=null) {
                        final Consumer<Location> target=consumer;
                        try { executor.execute(() -> target.accept(null)); }
                        catch(RuntimeException e) { RuntimeLog.event("CALLBACK","location-current",e.getClass().getSimpleName()); }
                    }
                }
            });
    }
    private static void appLists(ClassLoader cl,boolean collector) {
        HookUtil.install(cl,"android.app.ApplicationPackageManager","app-list",m ->
            named(m,"getInstalledPackages","getInstalledApplications","queryIntentActivities","queryIntentActivityOptions")
            &&m.getReturnType()==List.class,new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam p) {
                    if(collector){p.setResult(Collections.emptyList());RuntimeLog.hit("app-list");}
                }
                protected void afterHookedMethod(MethodHookParam p) {
                    if(collector||p.hasThrowable()||!(p.getResult() instanceof List))return;
                    List<Object> result=new ArrayList<>();
                    for(Object item:(List<?>)p.getResult())if(isSystem(item))result.add(item);
                    p.setResult(result); RuntimeLog.hit("app-list");
                }
            });
    }
    private static boolean isSystem(Object obj) {
        ApplicationInfo a=null;
        if(obj instanceof PackageInfo)a=((PackageInfo)obj).applicationInfo;
        else if(obj instanceof ApplicationInfo)a=(ApplicationInfo)obj;
        else if(obj instanceof ResolveInfo&&((ResolveInfo)obj).activityInfo!=null)a=((ResolveInfo)obj).activityInfo.applicationInfo;
        return a!=null&&(a.flags&ApplicationInfo.FLAG_SYSTEM)!=0;
    }
    private static void usage(ClassLoader cl) {
        HookUtil.install(cl,"android.app.usage.UsageStatsManager","usage-lists",m ->
            named(m,"queryUsageStats","queryEventStats","queryConfigurations")&&m.getReturnType()==List.class,
            HookUtil.returning("usage-lists",Collections.emptyList()));
        HookUtil.install(cl,"android.app.usage.UsageStatsManager","usage-map",m ->
            m.getName().equals("queryAndAggregateUsageStats")&&m.getReturnType()==Map.class,
            HookUtil.returning("usage-map",Collections.emptyMap()));
        try {
            Class<?> events=Class.forName("android.app.usage.UsageEvents",false,cl);
            final Constructor<?> ctor=events.getDeclaredConstructor(); ctor.setAccessible(true);
            // Verify construction before installing a callback that must return this exact type.
            ctor.newInstance();
            HookUtil.install(cl,"android.app.usage.UsageStatsManager","usage-events",m ->
                named(m,"queryEvents","queryEventsForSelf")&&m.getReturnType()==events,new XC_MethodHook() {
                    protected void beforeHookedMethod(MethodHookParam p) throws Throwable {
                        p.setResult(ctor.newInstance());RuntimeLog.hit("usage-events");
                    }
                });
        } catch(Throwable e) { RuntimeLog.event("SKIP","usage-events",e.getClass().getSimpleName()); }
    }
    private static void audio(ClassLoader cl) {
        HookUtil.install(cl,"android.media.AudioRecord","audio-start",m ->
            m.getName().equals("startRecording")&&m.getReturnType()==void.class,HookUtil.returning("audio-start",null));
        HookUtil.install(cl,"android.media.MediaRecorder","recorder-start",m ->
            named(m,"prepare","start")&&m.getReturnType()==void.class,HookUtil.returning("recorder-start",null));
    }
    private static void accessibility(ClassLoader cl) {
        HookUtil.install(cl,"android.view.accessibility.AccessibilityManager","accessibility-list",m ->
            named(m,"getEnabledAccessibilityServiceList","getAccessibilityServiceList")&&m.getReturnType()==List.class,
            HookUtil.returning("accessibility-list",Collections.emptyList()));
    }
    private static void metadata(ClassLoader cl) {
        HookUtil.install(cl,"android.content.pm.PackageManager","archive-metadata",m ->
            m.getName().equals("getPackageArchiveInfo")&&m.getReturnType()==PackageInfo.class,
            HookUtil.returning("archive-metadata",null));
        HookUtil.install(cl,"android.app.ApplicationPackageManager","install-source",m ->
            m.getName().equals("getInstallSourceInfo"),new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam p) {
                    p.setThrowable(new PackageManager.NameNotFoundException("Restricted by privacy policy"));
                    RuntimeLog.hit("install-source");
                }
            });
    }
    private static void properties(ClassLoader cl) {
        final Map<String,String> values=new HashMap<>();
        values.put("ro.boot.verifiedbootstate","green");values.put("ro.boot.flash.locked","1");
        values.put("ro.boot.vbmeta.device_state","locked");values.put("ro.debuggable","0");
        values.put("ro.secure","1");values.put("ro.build.type","user");values.put("ro.build.tags","release-keys");
        values.put("ro.kernel.qemu","0");values.put("ro.boot.warranty_bit","0");values.put("ro.boot.hwc","CN");
        HookUtil.install(cl,"android.os.SystemProperties","properties",m ->
            m.getName().equals("get")&&m.getReturnType()==String.class&&m.getParameterCount()>0
                &&m.getParameterTypes()[0]==String.class,new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam p) {
                    String value=values.get(p.args[0]);
                    if(value!=null){p.setResult(value);RuntimeLog.hit("properties");}
                }
            });
    }
    private FrameworkHooks() { }
}
