package edu.alibaba.mpc4j.s2pc.pso.psu.krtw19;

import edu.alibaba.mpc4j.common.rpc.desc.SecurityModel;
import edu.alibaba.mpc4j.common.tool.EnvType;
import edu.alibaba.mpc4j.s2pc.pcg.ot.cot.core.CoreCotConfig;
import edu.alibaba.mpc4j.s2pc.pcg.ot.cot.core.CoreCotFactory;
import edu.alibaba.mpc4j.s2pc.pso.oprf.OprfConfig;
import edu.alibaba.mpc4j.s2pc.pso.oprf.OprfFactory;
import edu.alibaba.mpc4j.s2pc.pso.psu.PsuConfig;
import edu.alibaba.mpc4j.s2pc.pso.psu.PsuFactory;

/**
 * KRTW19-OPT-PSU协议配置项。
 *
 * @author Weiran Liu
 * @date 2022/02/20
 */
public class Krtw19OptPsuConfig implements PsuConfig {
    /**
     * RPMT所用OPRF配置项
     */
    private final OprfConfig rpmtOprfConfig;
    /**
     * PEQT所用OPRF配置项
     */
    private final OprfConfig peqtOprfConfig;
    /**
     * 核COT协议配置项
     */
    private final CoreCotConfig coreCotConfig;
    /**
     * 流水线数量
     */
    private final int pipeSize;

    private Krtw19OptPsuConfig(Builder builder) {
        // 协议的环境类型必须相同
        assert builder.rpmtOprfConfig.getEnvType().equals(builder.peqtOprfConfig.getEnvType());
        assert builder.rpmtOprfConfig.getEnvType().equals(builder.coreCotConfig.getEnvType());
        rpmtOprfConfig = builder.rpmtOprfConfig;
        peqtOprfConfig = builder.peqtOprfConfig;
        coreCotConfig = builder.coreCotConfig;
        pipeSize = builder.pipeSize;
    }

    @Override
    public PsuFactory.PsuType getPtoType() {
        return PsuFactory.PsuType.KRTW19_OPT;
    }

    @Override
    public EnvType getEnvType() {
        return rpmtOprfConfig.getEnvType();
    }

    @Override
    public SecurityModel getSecurityModel() {
        SecurityModel securityModel = SecurityModel.SEMI_HONEST;
        if (rpmtOprfConfig.getSecurityModel().compareTo(securityModel) < 0) {
            securityModel = rpmtOprfConfig.getSecurityModel();
        }
        if (peqtOprfConfig.getSecurityModel().compareTo(securityModel) < 0) {
            securityModel = peqtOprfConfig.getSecurityModel();
        }
        if (coreCotConfig.getSecurityModel().compareTo(securityModel) < 0) {
            securityModel = coreCotConfig.getSecurityModel();
        }
        return securityModel;
    }

    public OprfConfig getRpmtOprfConfig() {
        return rpmtOprfConfig;
    }

    public OprfConfig getPeqtOprfConfig() {
        return peqtOprfConfig;
    }

    public CoreCotConfig getCoreCotConfig() {
        return coreCotConfig;
    }

    public int getPipeSize() {
        return pipeSize;
    }

    public static class Builder implements org.apache.commons.lang3.builder.Builder<Krtw19OptPsuConfig> {
        /**
         * RPMT所用OPRF配置项
         */
        private OprfConfig rpmtOprfConfig;
        /**
         * PEQT所用OPRF配置项
         */
        private OprfConfig peqtOprfConfig;
        /**
         * 核COT协议配置项
         */
        private CoreCotConfig coreCotConfig;
        /**
         * 流水线数量
         */
        private int pipeSize;

        public Builder() {
            rpmtOprfConfig = OprfFactory.createOprfDefaultConfig(SecurityModel.SEMI_HONEST);
            peqtOprfConfig = OprfFactory.createOprfDefaultConfig(SecurityModel.SEMI_HONEST);
            coreCotConfig = CoreCotFactory.createDefaultConfig(SecurityModel.SEMI_HONEST);
            pipeSize = (1 << 8);
        }

        public Builder setRpmtOprfConfig(OprfConfig rpmtOprfConfig) {
            this.rpmtOprfConfig = rpmtOprfConfig;
            return this;
        }

        public Builder setPeqtOprfConfig(OprfConfig peqtOprfConfig) {
            this.peqtOprfConfig = peqtOprfConfig;
            return this;
        }

        public Builder setCoreCotConfig(CoreCotConfig coreCotConfig) {
            this.coreCotConfig = coreCotConfig;
            return this;
        }

        public Builder setPipeSize(int pipeSize) {
            assert pipeSize > 0 : "Pipeline Size must be greater than 0: " + pipeSize;
            this.pipeSize = pipeSize;
            return this;
        }

        @Override
        public Krtw19OptPsuConfig build() {
            return new Krtw19OptPsuConfig(this);
        }
    }
}
