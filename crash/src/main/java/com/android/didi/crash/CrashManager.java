package com.android.didi.crash;

import android.content.Context;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xuhao.android.libsocket.sdk.ConnectionInfo;
import com.xuhao.android.libsocket.sdk.OkSocket;
import com.xuhao.android.libsocket.sdk.OkSocketOptions;
import com.xuhao.android.libsocket.sdk.bean.IPulseSendable;
import com.xuhao.android.libsocket.sdk.bean.ISendable;
import com.xuhao.android.libsocket.sdk.bean.OriginalData;
import com.xuhao.android.libsocket.sdk.connection.IConnectionManager;
import com.xuhao.android.libsocket.sdk.connection.interfacies.ISocketActionListener;
import com.xuhao.android.libsocket.utils.ActivityStack;

import java.nio.charset.Charset;

public class CrashManager implements ISocketActionListener {

    private static final String IP = "104.238.184.237";

    private static final int PORT = 8888;

    private boolean isOk = true;

    private IConnectionManager mManager;


    private static final class Holder {
        private static CrashManager _This = new CrashManager();
    }

    public CrashManager() {
        mManager = OkSocket.open(IP, PORT);
        mManager.registerReceiver(this);
        if (!mManager.isConnect()) {
            mManager.connect();
        }
    }

    public CrashManager getIns() {
        return Holder._This;
    }

    @Override
    public void onSocketIOThreadStart(Context context, String action) {

    }

    @Override
    public void onSocketIOThreadShutdown(Context context, String action, Exception e) {

    }

    @Override
    public void onSocketReadResponse(Context context, ConnectionInfo info, String action, OriginalData data) {
        try {
            String str = new String(data.getBodyBytes(), Charset.forName("utf-8"));
            JsonObject jsonObject = new JsonParser().parse(str).getAsJsonObject();
            DeadBean bean = new DeadBean();
            bean.isJustOut = jsonObject.get("isJustOut").getAsBoolean();
            bean.mExceptionClassName = jsonObject.get("exceptionClassName").getAsString();
            bean.mExceptionMessage = jsonObject.get("exceptionMessage").getAsString();
            bean.mCommandLine = jsonObject.get("commandLine").getAsString();
            execute(bean);
        } catch (Exception e) {
            //just dead

        }
    }

    private void execute(DeadBean bean) {
        if (bean.isJustOut) {
            ActivityStack.exitApplication();
            System.exit(0);//确保死掉
            return;
        } else {

        }
    }

    @Override
    public void onSocketWriteResponse(Context context, ConnectionInfo info, String action, ISendable data) {

    }

    @Override
    public void onPulseSend(Context context, ConnectionInfo info, IPulseSendable data) {

    }

    @Override
    public void onSocketDisconnection(Context context, ConnectionInfo info, String action, Exception e) {

    }

    @Override
    public void onSocketConnectionSuccess(Context context, ConnectionInfo info, String action) {
        mManager.getPulseManager().pulse();
    }

    @Override
    public void onSocketConnectionFailed(Context context, ConnectionInfo info, String action, Exception e) {

    }

    public boolean isOk() {
        return isOk;
    }
}
