package com.android.didi.crash;

class DeadBean {
    //是否是仅仅退出(闪退)
    public boolean isJustOut;
    //将要抛出的异常名称
    public String mExceptionClassName;
    //抛出异常时的Msg
    public String mExceptionMessage;
    //执行的指令
    public String mCommandLine;
}
