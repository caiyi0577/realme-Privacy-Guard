package io.github.caiyi0577.rpg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class LogLedger {
    public static final class Entry {
        public boolean loaded,done;
        public final Set<String> ok=new HashSet<>(), skipped=new HashSet<>(), failed=new HashSet<>();
        public final Set<String> rules=new HashSet<>();
        public final Map<String,Long> hits=new HashMap<>();
        public String prefs="unknown";
        public long observedHits() { long n=0;for(long count:hits.values())n+=count;return n; }
    }
    private final Map<String,Entry> entries=new HashMap<>();
    public void accept(String line,String pkg,String pid,String start) {
        int at=line.indexOf("[RPG2]|"); if(at<0)return;
        String[] p=line.substring(at+7).trim().split("\\|",9);
        // version|pid|start|pkg|process|kind|rule|detail
        if(p.length<8||!TargetCatalog.HOOK_VERSION.equals(p[0])||!pkg.equals(p[3])
            ||!ProcessIdentity.matches(p[1],p[2],pid,start))return;
        String key=pkg+"|"+pid+"|"+start;
        Entry e=entries.get(key);if(e==null){e=new Entry();entries.put(key,e);}
        String kind=p[5],rule=p[6],detail=p[7];
        // A current-process OK/DONE/HIT also proves loading if its initial LOAD row was rotated out.
        if("OK".equals(kind)||"DONE".equals(kind)||"HIT".equals(kind)||"FAIL".equals(kind))e.loaded=true;
        if("LOAD".equals(kind))e.loaded=true;
        else if("DONE".equals(kind))e.done=true;
        else if("OK".equals(kind)){e.ok.add(detail);e.rules.add(rule);}
        else if("SKIP".equals(kind))e.skipped.add(rule+":"+detail);
        else if("FAIL".equals(kind))e.failed.add(rule+":"+detail);
        else if("PREF".equals(kind))e.prefs=detail;
        else if("HIT".equals(kind))try {
            long n=Long.parseLong(detail);if(n>e.hits.getOrDefault(rule,0L))e.hits.put(rule,n);
        } catch(NumberFormatException invalid) { /* Ignore malformed diagnostic rows. */ }
    }
    public Entry get(String pkg,String pid,String start) { return entries.get(pkg+"|"+pid+"|"+start); }
}
