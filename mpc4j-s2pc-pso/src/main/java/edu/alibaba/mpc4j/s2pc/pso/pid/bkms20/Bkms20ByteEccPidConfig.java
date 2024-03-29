package edu.alibaba.mpc4j.s2pc.pso.pid.bkms20;

import edu.alibaba.mpc4j.common.rpc.desc.SecurityModel;
import edu.alibaba.mpc4j.common.tool.EnvType;
import edu.alibaba.mpc4j.s2pc.pso.pid.PidConfig;
import edu.alibaba.mpc4j.s2pc.pso.pid.PidFactory;

/**
 * Facebook的字节椭圆曲线PID协议配置项。
 *
 * @author Weiran Liu
 * @date 2022/9/13
 */
public class Bkms20ByteEccPidConfig implements PidConfig {
    /**
     * 环境类型
     */
    private final EnvType envType;

    private Bkms20ByteEccPidConfig(Builder builder) {
        envType = builder.envType;
    }

    @Override
    public PidFactory.PidType getPtoType() {
        return PidFactory.PidType.BKMS20_BYTE_ECC;
    }

    @Override
    public EnvType getEnvType() {
        return envType;
    }

    @Override
    public SecurityModel getSecurityModel() {
        return SecurityModel.SEMI_HONEST;
    }

    public static class Builder implements org.apache.commons.lang3.builder.Builder<Bkms20ByteEccPidConfig> {
        /**
         * 环境类型
         */
        private EnvType envType;

        public Builder() {
            super();
            this.envType = EnvType.STANDARD;
        }

        public Builder setEnvType(EnvType envType) {
            this.envType = envType;
            return this;
        }

        @Override
        public Bkms20ByteEccPidConfig build() {
            return new Bkms20ByteEccPidConfig(this);
        }
    }
}
