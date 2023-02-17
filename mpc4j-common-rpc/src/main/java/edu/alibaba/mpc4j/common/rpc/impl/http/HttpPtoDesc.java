package edu.alibaba.mpc4j.common.rpc.impl.http;

import edu.alibaba.mpc4j.common.rpc.desc.PtoDesc;
import edu.alibaba.mpc4j.common.rpc.desc.PtoDescManager;

public class HttpPtoDesc implements PtoDesc {

    private static final int PTO_ID = Math.abs((int)5555555555555555555L);

    private static final String PTO_NAME = "HTTP_CONNECT";

    enum StepEnum {
        CLIENT_SYNCHRONIZE,
        SERVER_SYNCHRONIZE,
    }

    private static final HttpPtoDesc INSTANCE = new HttpPtoDesc();

    private HttpPtoDesc() {
        // empty
    }

    public static PtoDesc getInstance() {
        return INSTANCE;
    }

    static {
        PtoDescManager.registerPtoDesc(INSTANCE);
    }

    @Override
    public int getPtoId() {
        return PTO_ID;
    }

    @Override
    public String getPtoName() {
        return PTO_NAME;
    }
}
