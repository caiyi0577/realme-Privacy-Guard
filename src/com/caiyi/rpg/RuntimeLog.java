package com.caiyi.rpg;
import android.os.Process;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import de.robv.android.xposed.XposedBridge;
public final class RuntimeLog {
    private static String pkg="unknown", process="unknown", start="unknown";
    private static final ConcurrentHashMap<String, AtomicLong> hits = new ConcurrentHashMap<>();
    private static boolean initialized;
    public static synchronized boolean init(String p, String proc) {
        if(initialized)return false;
        initialized=true;
        pkg=p; process=proc;
        try (BufferedReader r=new BufferedReader(new FileReader("/proc/self/stat"))) {
            start=ProcessIdentity.startTicks(r.readLine());
        } catch (Exception error) { start="unknown"; }
        event("LOAD","module","active");
        return true;
    }
    public static void event(String kind, String rule, String detail) {
        XposedBridge.log("[RPG2]|"+TargetCatalog.HOOK_VERSION+"|"+Process.myPid()+"|"+start
            +"|"+pkg+"|"+process+"|"+kind+"|"+clean(rule)+"|"+clean(detail));
    }
    public static void hit(String rule) {
        AtomicLong counter=hits.get(rule);
        if(counter==null) {
            AtomicLong fresh=new AtomicLong(), old=hits.putIfAbsent(rule,fresh);
            counter=old==null?fresh:old;
        }
        long count=counter.incrementAndGet();
        // Bound log volume. Counts do not contain arguments or personal data.
        if(count==1 || (count&63)==0) event("HIT",rule,Long.toString(count));
    }
    private static String clean(String s) { return s==null?"":s.replace('|','/').replace('\n',' '); }
    private RuntimeLog() { }
}
