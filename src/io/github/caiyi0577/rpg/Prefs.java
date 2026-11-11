package io.github.caiyi0577.rpg;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public final class Prefs {
    public static final String PKG="io.github.caiyi0577.rpg", AUTHORITY=PKG+".config";
    public static final String[] KEYS={"carrier","ai","applist","sample","telemetry","net","ums_upload","ads","olc_upload"};
    private static final String MIGRATED="settings_migrated_v3";
    public boolean carrier=true, ai=true, appList=true, sample=true, telemetry=true, net=true;
    public boolean ums=true, ads=true, olc=true;

    public static Prefs load(Context context) {
        Prefs p=new Prefs();
        try {
            Bundle values=context.getContentResolver().call(Uri.parse("content://"+AUTHORITY),"read",null,null);
            if(values==null||values.getInt("schema")!=1)throw new IllegalStateException("configuration unavailable");
            for(String key:KEYS)if(!values.containsKey(key))throw new IllegalStateException("incomplete configuration");
            p.carrier=values.getBoolean("carrier");p.ai=values.getBoolean("ai");
            p.appList=values.getBoolean("applist");p.sample=values.getBoolean("sample");
            p.telemetry=values.getBoolean("telemetry");p.net=values.getBoolean("net");
            p.ums=values.getBoolean("ums_upload");p.ads=values.getBoolean("ads");p.olc=values.getBoolean("olc_upload");
            RuntimeLog.event("PREF","settings","provider");
        } catch(Throwable e) { RuntimeLog.event("PREF","settings","defaults/"+e.getClass().getSimpleName()); }
        return p;
    }

    public static synchronized SharedPreferences open(Context context) {
        Context device=context.createDeviceProtectedStorageContext();
        SharedPreferences prefs=device.getSharedPreferences("rpg_private",Context.MODE_PRIVATE);
        if(!prefs.getBoolean(MIGRATED,false))migrate(context,device,prefs);
        return prefs;
    }

    private static void migrate(Context context,Context device,SharedPreferences prefs) {
        // Only the module's own staged settings are read. IPC clients cannot supply a file or write settings.
        File staged=new File(device.getFilesDir(),"rpg_settings_migration.json");
        if(staged.isFile()) {
            try(FileInputStream input=new FileInputStream(staged);ByteArrayOutputStream output=new ByteArrayOutputStream()) {
                byte[] buffer=new byte[1024];int n;
                while((n=input.read(buffer))!=-1){output.write(buffer,0,n);if(output.size()>16384)throw new IllegalStateException("settings file too large");}
                JSONObject values=new JSONObject(new String(output.toByteArray(),StandardCharsets.UTF_8));
                SharedPreferences.Editor edit=prefs.edit();
                for(String key:KEYS)edit.putBoolean(key,values.getBoolean(key));
                if(!edit.putBoolean(MIGRATED,true).commit())throw new IllegalStateException("settings not saved");
                return;
            } catch(Exception ignored) { return; }
        }
        UserManager users=context.getSystemService(UserManager.class);
        if(users!=null&&!users.isUserUnlocked())return;
        SharedPreferences legacy=context.getSharedPreferences("rpg",Context.MODE_PRIVATE);
        SharedPreferences.Editor edit=prefs.edit();
        for(String key:KEYS)edit.putBoolean(key,legacy.getBoolean(key,prefs.getBoolean(key,true)));
        edit.putBoolean(MIGRATED,true).commit();
    }

    public static Bundle snapshot(Context context) {
        SharedPreferences prefs=open(context);Bundle values=new Bundle();values.putInt("schema",1);
        if(!prefs.getBoolean(MIGRATED,false))throw new IllegalStateException("Settings migration pending");
        for(String key:KEYS)values.putBoolean(key,prefs.getBoolean(key,true));
        return values;
    }
}
