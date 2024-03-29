package edu.alibaba.mpc4j.common.tool.polynomial.zp;

import edu.alibaba.mpc4j.common.tool.galoisfield.zp.ZpManager;
import edu.alibaba.mpc4j.common.tool.utils.BigIntegerUtils;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Zp多项式差值抽象类。
 *
 * @author Weiran Liu
 * @date 2022/8/7
 */
abstract class AbstractZpPoly implements ZpPoly {
    /**
     * 随机状态
     */
    protected final SecureRandom secureRandom;
    /**
     * 有限域模数p
     */
    protected final BigInteger p;
    /**
     * 有限域比特长度
     */
    protected final int l;

    AbstractZpPoly(int l) {
        p = ZpManager.getPrime(l);
        this.l = l;
        secureRandom = new SecureRandom();
    }

    @Override
    public int getL() {
        return l;
    }

    @Override
    public BigInteger getPrime() {
        return p;
    }


    protected boolean validPoint(BigInteger point) {
        return BigIntegerUtils.greaterOrEqual(point, BigInteger.ZERO) && BigIntegerUtils.less(point, p);
    }
}
