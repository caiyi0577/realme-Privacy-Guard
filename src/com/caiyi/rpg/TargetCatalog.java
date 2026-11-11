package com.caiyi.rpg;
import java.util.Arrays;
public final class TargetCatalog {
    public static final String VERSION = "1.1.5";
    // Display-only releases share the same hook revision; keep validating PID and start identity.
    public static final String HOOK_VERSION = "1.1.2";
    public static final String UMS = "com.oplus.pantanal.ums", ADS = "com.opos.ads", OLC = "com.oplus.olc";
    public static final String[] PACKAGES = {
        "com.mobiletools.systemhelper", "com.coloros.regservice", "com.oppo.ctautoregist",
        "com.coloros.weather.service", "com.nearme.instant.platform", "com.oplus.safecenter",
        "com.oplus.securitypermission", "com.coloros.securityguard", "com.oplus.statistics.rom",
        "com.oplus.deepthinker", "com.oplus.metis", "com.oplus.obrain", "com.oplus.onetrace",
        "com.coloros.remoteguardservice", UMS, ADS, OLC
    };
    public static boolean contains(String p) { return Arrays.asList(PACKAGES).contains(p); }
    public static boolean isNarrow(String p) { return UMS.equals(p) || ADS.equals(p) || OLC.equals(p); }
    public static boolean isCollector(String p) {
        return "com.mobiletools.systemhelper".equals(p) || "com.coloros.regservice".equals(p) || "com.oplus.statistics.rom".equals(p);
    }
    public static String label(String p) {
        if (UMS.equals(p)) return "智慧建议";
        if (ADS.equals(p)) return "智慧推荐";
        if (OLC.equals(p)) return "日志服务";
        if ("com.mobiletools.systemhelper".equals(p)) return "SystemHelper";
        if ("com.coloros.regservice".equals(p)) return "RegService";
        if ("com.oplus.statistics.rom".equals(p)) return "基础数据服务 DCS";
        return p.substring(p.lastIndexOf('.') + 1);
    }
    private TargetCatalog() { }
}
