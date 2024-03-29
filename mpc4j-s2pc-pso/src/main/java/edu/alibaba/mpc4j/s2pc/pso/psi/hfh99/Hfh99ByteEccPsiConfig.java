package edu.alibaba.mpc4j.s2pc.pso.psi.hfh99;

import edu.alibaba.mpc4j.common.rpc.desc.SecurityModel;
import edu.alibaba.mpc4j.common.tool.EnvType;
import edu.alibaba.mpc4j.s2pc.pso.psi.PsiConfig;
import edu.alibaba.mpc4j.s2pc.pso.psi.PsiFactory;

/**
 * HFH99-字节椭圆曲线PSI协议配置项。
 *
 * @author Weiran Liu
 * @date 2022/9/19
 */
public class Hfh99ByteEccPsiConfig implements PsiConfig {
    /**
     * 环境类型
     */
    private final EnvType envType;

    private Hfh99ByteEccPsiConfig(Builder builder) {
        envType = builder.envType;
    }

    @Override
    public PsiFactory.PsiType getPtoType() {
        return PsiFactory.PsiType.HFH99_BYTE_ECC;
    }

    @Override
    public EnvType getEnvType() {
        return envType;
    }

    @Override
    public SecurityModel getSecurityModel() {
        return SecurityModel.SEMI_HONEST;
    }

    public static class Builder implements org.apache.commons.lang3.builder.Builder<Hfh99ByteEccPsiConfig> {
        /**
         * 环境类型
         */
        private EnvType envType;

        public Builder() {
            envType = EnvType.STANDARD;
        }

        public Builder setEnvType(EnvType envType) {
            this.envType = envType;
            return this;
        }

        @Override
        public Hfh99ByteEccPsiConfig build() {
            return new Hfh99ByteEccPsiConfig(this);
        }
    }
}
