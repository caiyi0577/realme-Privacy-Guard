package io.github.caiyi0577.rpg.hooks;

import io.github.caiyi0577.rpg.DomainPolicy;
import io.github.caiyi0577.rpg.Prefs;
import io.github.caiyi0577.rpg.RuntimeLog;
import io.github.caiyi0577.rpg.TargetCatalog;
import de.robv.android.xposed.XC_MethodHook;
import java.io.IOException;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.URLConnection;

public final class NetworkHooks {
    public static void install(ClassLoader cl,String pkg,Prefs p) {
        boolean enabled=TargetCatalog.UMS.equals(pkg)?p.ums:TargetCatalog.ADS.equals(pkg)?p.ads:TargetCatalog.OLC.equals(pkg)?p.olc:p.net;
        if(!enabled) return;
        HookUtil.exact(cl,"java.net.InetAddress","getAllByName","[Ljava.net.InetAddress;",new String[]{"java.lang.String"},"dns",new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) {
                if(param.args[0] instanceof String&&DomainPolicy.blockedHost(pkg,(String)param.args[0],p)) {
                    RuntimeLog.hit("dns");param.setThrowable(new UnknownHostException("Blocked by privacy policy"));
                }
            }
        });
        HookUtil.install(cl,"java.net.URL","url-open",m -> m.getName().equals("openConnection")&&URLConnection.class.isAssignableFrom(m.getReturnType()),new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) {
                if(param.thisObject instanceof URL&&DomainPolicy.blockedUrl(pkg,param.thisObject.toString(),p)) {
                    RuntimeLog.hit("url-open");param.setThrowable(new IOException("Blocked by privacy policy"));
                }
            }
        });
    }
    private NetworkHooks() { }
}
