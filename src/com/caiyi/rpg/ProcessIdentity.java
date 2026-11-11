package com.caiyi.rpg;
public final class ProcessIdentity {
    public static String startTicks(String stat) {
        int end = stat.lastIndexOf(')');
        if (end < 0) return "unknown";
        String[] fields = stat.substring(end + 1).trim().split("\\s+");
        return fields.length > 19 ? fields[19] : "unknown";
    }
    public static boolean matches(String pid, String start, String expectedPid, String expectedStart) {
        return !"unknown".equals(start) && !"unknown".equals(expectedStart)
            && pid.equals(expectedPid) && start.equals(expectedStart);
    }
    private ProcessIdentity() { }
}
