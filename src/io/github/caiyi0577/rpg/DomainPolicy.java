package io.github.caiyi0577.rpg;
import java.net.URI;
import java.util.Locale;
public final class DomainPolicy {
    private static final String[] LEGACY={"dm.wo.com.cn","typdm.tydevice.com","zzhc.vnet.cn",
        "diagate.dc.oppomobile.com","moa-upload-online.coloros.com","data.sms.heytapmobi.com",
        "sms.ads.heytapmobi.com","muc.heytap.com","open.oppomobile.com"};
    public static boolean hostMatches(String host,String rule) {
        if(host==null)return false;
        String h=host.toLowerCase(Locale.ROOT);
        while(h.endsWith("."))h=h.substring(0,h.length()-1);
        return h.equals(rule)||h.endsWith("."+rule);
    }
    public static boolean blockedHost(String pkg,String host,Prefs p) {
        if(TargetCatalog.UMS.equals(pkg))return p.ums&&hostMatches(host,"nearby.dc.oppomobile.com");
        if(TargetCatalog.ADS.equals(pkg))return p.ads&&(hostMatches(host,"adx.ads.heytapmobi.com")||hostMatches(host,"stg-data.ads.heytapmobi.com"));
        if(TargetCatalog.OLC.equals(pkg))return false;
        if(!p.net)return false;
        for(String rule:LEGACY)if(hostMatches(host,rule))return true;
        if("com.coloros.regservice".equals(pkg)&&(hostMatches(host,"a.fxltsbl.com")||hostMatches(host,"b.fxltsbl.com")))return true;
        return p.carrier&&"com.mobiletools.systemhelper".equals(pkg)&&hostMatches(host,"map.baidu.com");
    }
    public static boolean blockedUrl(String pkg,String url,Prefs p) {
        try {
            URI u=new URI(url);
            if(blockedHost(pkg,u.getHost(),p))return true;
            if(TargetCatalog.UMS.equals(pkg)&&p.ums&&hostMatches(u.getHost(),"breeno.apps.coloros.com"))
                return "/sms-model/v1/upload".equals(u.getPath());
            if(TargetCatalog.OLC.equals(pkg)&&p.olc) {
                String path=u.getPath();
                return path!=null&&(path.startsWith("/eap/logupload/")||path.startsWith("/log/upload/"));
            }
        } catch(Exception invalid) { return false; }
        return false;
    }
    private DomainPolicy() { }
}
