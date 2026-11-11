package io.github.caiyi0577.rpg;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class Su {
    public static byte[] read(String command,int maxBytes) throws Exception {
        Process p=new ProcessBuilder("su","-c",command).start();
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        final IOException[] failure=new IOException[1];
        Thread reader=new Thread(() -> {
            try(InputStream in=p.getInputStream()) {
                byte[] buffer=new byte[8192];int n;
                while((n=in.read(buffer))!=-1) {
                    if(out.size()+n>maxBytes)throw new IOException("Diagnostic output limit");
                    out.write(buffer,0,n);
                }
            }catch(IOException e){failure[0]=e;}
        },"RPG diagnostic reader");
        // Drain stderr without showing other apps' errors or private data.
        Thread errors=new Thread(() -> {try(InputStream in=p.getErrorStream()){while(in.read()!=-1){}}catch(IOException ignored){}},"RPG stderr");
        reader.start();errors.start();
        if(!p.waitFor(25,TimeUnit.SECONDS)){p.destroy();throw new IOException("Root operation timed out");}
        reader.join(1500);errors.join(1500);
        if(reader.isAlive()){p.destroy();throw new IOException("Incomplete root output");}
        if(failure[0]!=null)throw failure[0];
        if(p.exitValue()!=0)throw new IOException("Root access unavailable ("+p.exitValue()+")");
        return out.toByteArray();
    }
    public static String text(String command,int limit) throws Exception {
        return new String(read(command,limit),java.nio.charset.StandardCharsets.UTF_8);
    }
    private Su() { }
}
