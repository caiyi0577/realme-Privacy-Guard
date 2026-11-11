package com.caiyi.rpg;

/** Distinguishes installed blocking protection, observed hits, and stopped processes. */
public final class BlockStatus {
    public enum State {
        BLOCKED("阻断"),FAILED("阻断失败"),UNBLOCKED("未阻断"),NOT_RUNNING("未运行");
        public final String label;
        State(String label){this.label=label;}
    }
    public static final class Evidence {
        public boolean installed,disabled,scoped,complete=true,configRead=true;
        public Boolean moduleEnabled;
        public int running,loaded,ready,failed,skipped,hooks,missingRequired;
        public long hits;
    }
    public static final class Result {
        public final State state;
        public final boolean blocked;
        public final String reason;
        private Result(boolean blocked,String reason){this(blocked?State.BLOCKED:State.UNBLOCKED,reason);}
        private Result(State state,String reason){this.state=state;this.blocked=state==State.BLOCKED;this.reason=reason;}
        public String line(){return "状态："+state.label+"（"+reason+"）\n";}
    }
    public static Result evaluate(Evidence e) {
        if(!e.installed)return new Result(false,"未安装或状态未知");
        if(e.disabled)return new Result(true,"组件已禁用，无需为自检启用");
        if(e.running==0)return new Result(State.NOT_RUNNING,"当前未发现运行进程，待启动后验证");
        if(e.failed>0||e.missingRequired>0) {
            String detail=e.missingRequired>0?"关键拦截规则缺失或安装失败":"Hook 安装或初始化失败";
            if(e.ready>0)detail+="；其余 "+e.ready+" 个进程规则已就绪";
            if(e.hits>0)detail+="；已记录实际拦截至少 "+e.hits+" 次";
            return new Result(State.FAILED,detail);
        }
        if(e.hits>0) {
            String detail="当前进程已记录实际命中，至少 "+e.hits+" 次";
            if(e.loaded<e.running)detail+="；仍有 "+(e.running-e.loaded)+" 个进程待验证";
            if(e.failed>0||e.skipped>0)detail+="；部分规则失败或跳过";
            if(!e.configRead)detail+="；使用默认配置";
            if(!Boolean.TRUE.equals(e.moduleEnabled))detail+="；模块当前未启用或启用状态未知";
            return new Result(true,detail);
        }
        if(e.moduleEnabled==null)return new Result(false,"模块启用状态无法核对");
        if(!e.moduleEnabled)return new Result(false,"模块未启用");
        if(!e.scoped)return new Result(false,"未勾选作用域");
        if(e.ready>0) {
            String detail="拦截规则已就绪，尚无实际命中记录";
            if(e.ready<e.running)detail+="；另有 "+(e.running-e.ready)+" 个进程待验证";
            if(e.skipped>0)detail+="；部分接口已跳过";
            if(!e.configRead)detail+="；使用默认配置";
            return new Result(true,detail);
        }
        if(e.loaded==0)return new Result(false,"未找到当前进程的新版记录，待重启或日志核对");
        if(e.loaded<e.running)return new Result(false,"部分进程待加载或日志核对，尚无实际命中记录");
        if(!e.configRead)return new Result(false,"配置读取未确认，尚无实际命中记录");
        if(!e.complete)return new Result(false,"规则初始化未结束，尚无实际命中记录");
        if(e.skipped>0)return new Result(false,"部分规则跳过，尚无实际命中记录");
        if(e.hooks==0)return new Result(false,"未安装拦截点，检查保护开关及加载记录");
        return new Result(false,"新版规则已加载，尚无实际命中记录");
    }
    private BlockStatus(){ }
}
