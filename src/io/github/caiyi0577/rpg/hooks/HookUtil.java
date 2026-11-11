package io.github.caiyi0577.rpg.hooks;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import io.github.caiyi0577.rpg.RuntimeLog;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
public final class HookUtil {
    public interface Match { boolean accepts(Method m); }
    public static int install(ClassLoader cl,String cls,String rule,Match match,XC_MethodHook callback) {
        int count=0;
        try {
            Class<?> c=Class.forName(cls,false,cl);
            for(Method m:c.getDeclaredMethods()) {
                if(!match.accepts(m)||Modifier.isAbstract(m.getModifiers()))continue;
                try {
                    XposedBridge.hookMethod(m,callback); count++;
                    RuntimeLog.event("OK",rule,describe(m));
                } catch(Throwable e) { RuntimeLog.event("FAIL",rule,describe(m)+":"+e.getClass().getSimpleName()); }
            }
            if(count==0)RuntimeLog.event("SKIP",rule,"no-compatible-method:"+cls);
        } catch(ClassNotFoundException absent) { RuntimeLog.event("SKIP",rule,"class-unavailable:"+cls); }
        catch(Throwable e) { RuntimeLog.event("FAIL",rule,e.getClass().getSimpleName()); }
        return count;
    }
    public static int exact(ClassLoader cl,String cls,String method,String result,String[] params,String rule,XC_MethodHook callback) {
        return install(cl,cls,rule,m -> {
            if(!m.getName().equals(method)||!m.getReturnType().getName().equals(result))return false;
            Class<?>[] types=m.getParameterTypes(); if(types.length!=params.length)return false;
            for(int i=0;i<types.length;i++)if(!types[i].getName().equals(params[i]))return false;
            return true;
        },callback);
    }
    public static XC_MethodHook returning(final String rule,final Object value) {
        return new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) {
                RuntimeLog.hit(rule); param.setResult(value);
            }
        };
    }
    public static String describe(Method m) {
        StringBuilder b=new StringBuilder(m.getDeclaringClass().getName()).append('#').append(m.getName()).append('(');
        for(Class<?> p:m.getParameterTypes())b.append(p.getName()).append(',');
        return b.append("):").append(m.getReturnType().getName()).toString();
    }
    private HookUtil() { }
}
