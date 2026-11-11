package io.github.caiyi0577.rpg;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Process;

public final class ConfigProvider extends ContentProvider {
    public boolean onCreate(){return true;}
    public Bundle call(String method,String argument,Bundle extras) {
        int uid=Binder.getCallingUid();
        if(!ConfigAccess.allowed(uid,Process.myUid(),getContext().getPackageManager().getPackagesForUid(uid)))
            throw new SecurityException("Configuration is restricted to module targets");
        if(!"read".equals(method)||argument!=null||extras!=null)throw new IllegalArgumentException("Read-only settings endpoint");
        return Prefs.snapshot(getContext());
    }
    public Cursor query(Uri uri,String[] projection,String selection,String[] args,String sort){throw unsupported();}
    public String getType(Uri uri){return null;}
    public Uri insert(Uri uri,ContentValues values){throw unsupported();}
    public int update(Uri uri,ContentValues values,String selection,String[] args){throw unsupported();}
    public int delete(Uri uri,String selection,String[] args){throw unsupported();}
    private UnsupportedOperationException unsupported(){return new UnsupportedOperationException("Read-only settings endpoint");}
}
