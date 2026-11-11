package io.github.caiyi0577.rpg;

public final class ConfigAccess {
    public static boolean allowed(int callingUid,int ownUid,String[] packages) {
        if(callingUid==ownUid)return true;
        if(packages!=null)for(String pkg:packages)if(TargetCatalog.contains(pkg))return true;
        return false;
    }
    private ConfigAccess() { }
}
