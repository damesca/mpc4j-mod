package edu.alibaba.mpc4j.common.tool.galoisfield.Zp64;

import cc.redberry.rings.IntegersZp64;
import edu.alibaba.mpc4j.common.tool.CommonConstants;
import edu.alibaba.mpc4j.common.tool.EnvType;
import edu.alibaba.mpc4j.common.tool.crypto.kdf.Kdf;
import edu.alibaba.mpc4j.common.tool.crypto.kdf.KdfFactory;
import edu.alibaba.mpc4j.common.tool.crypto.prg.Prg;
import edu.alibaba.mpc4j.common.tool.crypto.prg.PrgFactory;
import edu.alibaba.mpc4j.common.tool.utils.CommonUtils;
import edu.alibaba.mpc4j.common.tool.utils.LongUtils;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * 应用Rings实现的Zp64运算。
 *
 * @author Weiran Liu
 * @date 2022/7/7
 */
public class RingsZp64 implements Zp64 {
    /**
     * 素数
     */
    private final long prime;
    /**
     * l比特长度
     */
    private final int l;
    /**
     * l字节长度
     */
    private final int byteL;
    /**
     * 最大有效元素：2^k - 1
     */
    private final long rangeBound;
    /**
     * Zp64素数域
     */
    private final IntegersZp64 finiteField;
    /**
     * KDF
     */
    private final Kdf kdf;
    /**
     * 伪随机数生成器
     */
    private final Prg prg;

    /**
     * 素数为输入的构造函数。
     *
     * @param envType 环境类型。
     * @param prime   素数。
     */
    public RingsZp64(EnvType envType, long prime) {
        assert BigInteger.valueOf(prime).isProbablePrime(CommonConstants.STATS_BIT_LENGTH)
            : "input prime is not a prime: " + prime;
        this.prime = prime;
        finiteField = new IntegersZp64(prime);
        l = LongUtils.ceilLog2(prime) - 1;
        byteL = CommonUtils.getByteLength(l);
        rangeBound = 1L << l;
        kdf = KdfFactory.createInstance(envType);
        prg = PrgFactory.createInstance(envType, Long.BYTES);
    }

    /**
     * 有效比特为输入的构造函数。
     *
     * @param envType 环境类型。
     * @param l       有效比特长度。
     */
    public RingsZp64(EnvType envType, int l) {
        prime = Zp64Manager.getPrime(l);
        this.l = l;
        finiteField = new IntegersZp64(prime);
        byteL = CommonUtils.getByteLength(l);
        rangeBound = 1L << l;
        kdf = KdfFactory.createInstance(envType);
        prg = PrgFactory.createInstance(envType, Long.BYTES);
    }

    @Override
    public Zp64Factory.Zp64Type getZp64Type() {
        return Zp64Factory.Zp64Type.RINGS;
    }

    @Override
    public long getPrime() {
        return prime;
    }

    @Override
    public int getL() {
        return l;
    }

    @Override
    public int getByteL() {
        return byteL;
    }

    @Override
    public long getRangeBound() {
        return rangeBound;
    }

    @Override
    public long module(long a) {
        return finiteField.modulus(a);
    }

    @Override
    public long add(long a, long b) {
        assert validateElement(a) && validateElement(b);
        return finiteField.add(a, b);
    }

    @Override
    public long neg(long a) {
        assert validateElement(a);
        return finiteField.negate(a);
    }

    @Override
    public long sub(long a, long b) {
        assert validateElement(a) && validateElement(b);
        return finiteField.subtract(a, b);
    }

    @Override
    public long mul(long a, long b) {
        assert validateElement(a) && validateElement(b);
        return finiteField.multiply(a, b);
    }

    @Override
    public long div(long a, long b) {
        assert validateElement(a) && validateNonZeroElement(b);
        return finiteField.divide(a, b);
    }

    @Override
    public long inv(long a) {
        assert validateNonZeroElement(a);
        return finiteField.divide(1L, a);
    }

    @Override
    public long mulPow(long a, long b) {
        assert validateElement(a) && validateElement(b);
        return finiteField.powMod(a, b);
    }

    @Override
    public long createRandom(SecureRandom secureRandom) {
        return LongUtils.randomNonNegative(prime, secureRandom);
    }

    @Override
    public long createRandom(byte[] seed) {
        byte[] key = kdf.deriveKey(seed);
        byte[] elementByteArray = prg.extendToBytes(key);
        return Math.abs(LongUtils.byteArrayToLong(elementByteArray) % prime);
    }

    @Override
    public long createNonZeroRandom(SecureRandom secureRandom) {
        long random = 0L;
        while (random == 0L) {
            random = LongUtils.randomPositive(prime, secureRandom);
        }
        return random;
    }

    @Override
    public long createNonZeroRandom(byte[] seed) {
        byte[] key = kdf.deriveKey(seed);
        byte[] elementByteArray = prg.extendToBytes(key);
        long random = Math.abs(LongUtils.byteArrayToLong(elementByteArray) % prime);
        while (random == 0L) {
            // 如果恰巧为0，则迭代种子
            key = kdf.deriveKey(key);
            elementByteArray = prg.extendToBytes(key);
            random = Math.abs(LongUtils.byteArrayToLong(elementByteArray) % prime);
        }
        return random;
    }

    @Override
    public long createRangeRandom(SecureRandom secureRandom) {
        return LongUtils.randomNonNegative(rangeBound, secureRandom);
    }

    @Override
    public long createRangeRandom(byte[] seed) {
        byte[] key = kdf.deriveKey(seed);
        byte[] elementByteArray = prg.extendToBytes(key);
        return Math.abs(LongUtils.byteArrayToLong(elementByteArray) % rangeBound);
    }

    @Override
    public boolean validateElement(long a) {
        return a >= 0 && a < prime;
    }

    @Override
    public boolean validateNonZeroElement(long a) {
        return a > 0 && a < prime;
    }

    @Override
    public boolean validateRangeElement(long a) {
        return a >= 0 && a < rangeBound;
    }
}
