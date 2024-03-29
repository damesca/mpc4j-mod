package edu.alibaba.mpc4j.common.tool.lpn.ldpc;

import edu.alibaba.mpc4j.common.tool.lpn.LpnParams;
import edu.alibaba.mpc4j.common.tool.lpn.matrix.DenseMatrix;
import edu.alibaba.mpc4j.common.tool.lpn.matrix.ExtremeSparseMatrix;
import edu.alibaba.mpc4j.common.tool.lpn.matrix.SparseMatrix;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
/**
 * LdpcCreator的抽象类，实现接口LdpcCreator。
 *
 * Ldpc由稀疏矩阵H定义，H由分块矩阵 A, B，C，D, E，F 构成, 上半区 (A,B,C)，下半区 （D,E,F）。
 * 其中各分块矩阵的维度由参数 k 和 参数 gap确定，具体：
 * 矩阵 A, 行 k - gap，列 k - gap；
 * 矩阵 B，行 k - gap, 列 gap；
 * 矩阵 C，行 k - gap, 列 k - gap；
 * 矩阵 D，行 gap, 列 k - gap；
 * 矩阵 E，行 gap, 列 gap；
 * 矩阵 F，行 gap， 列 k - gap；
 * 以上矩阵均为稀疏矩阵 SparseMatrix，其中 D,F 大部分列为空，属于极度稀疏矩阵 ExtremeSparseMatrix。
 *
 * 此外，LdpcCoder 的转置编码计算 需要 矩阵 Ep = (F*C^{-1}*B）+ E)^{-1}。
 * LdpcCreator 需要根据指定输出OT数量和Ldpc 类型，生成上述矩阵及相关参数，用于创建 LdpcCoder。
 * 具体定义见论文: Silver: Silent VOLE and Oblivious Transfer from Hardness of Decoding Structured LDPC Codes
 * http://eprint.iacr.org/2021/1150
 *
 * @author Hanwen Feng
 * @date 2022/03/13
 */
public abstract class AbstractLdpcCreator implements LdpcCreator {
    /**
     * 需要生成的Ldpc 类型
     */
    protected final LdpcCreatorUtils.CodeType codeType;
    /**
     * 分块矩阵A
     */
    protected SparseMatrix matrixA;
    /**
     * 分块矩阵B
     */
    protected SparseMatrix matrixB;
    /**
     * 分块矩阵C
     */
    protected SparseMatrix matrixC;
    /**
     * 分块矩阵D
     */
    protected ExtremeSparseMatrix matrixD;
    /**
     * 分块矩阵F
     */
    protected ExtremeSparseMatrix matrixF;
    /**
     * 矩阵Ep
     */
    protected DenseMatrix matrixEp;
    /**
     * Ldpc Encoder 最终生成的OT数量的对数。
     * 例如ceilLogN = 24, 则该Ldpc 可以产生 2^24的OT。
     */
    protected final int ceilLogN;
    /**
     * Ldpc 参数 gap
     */
    protected int gapValue;
    /**
     * Ldpc 参数 k
     */
    protected int kValue;
    /**
     * 矩阵H左矩阵每列的汉明重量
     */
    protected int weight;

    /**
     * Lpdc 根据预置的种子信息生成，种子定义了矩阵H所有非零点的位置。
     * (A,B,D，E) 根据leftseed生成，（C,F）根据rightSeed生成。
     */
    protected int[][] rightSeed;
    protected double[] leftSeed;
    /**
     * LDPC 对应的LPN参数
     */
    protected LpnParams lpnParams;
    /**
     * 包私有构造函数
     * @param codeType Ldpc类型
     * @param ceilLogN 目标OT数量的对数
     */
    AbstractLdpcCreator(LdpcCreatorUtils.CodeType codeType, int ceilLogN) {
        this.ceilLogN = ceilLogN;
        this.codeType = codeType;
        initParams();
    }
    /**
     * 根据code类型，读取生成Ldpc参数信息。
     */
    private void initParams() {
        rightSeed = LdpcCreatorUtils.getRightSeed(codeType);
        leftSeed = LdpcCreatorUtils.getLeftSeed(codeType);
        gapValue = LdpcCreatorUtils.getGap(codeType);
        weight = LdpcCreatorUtils.getWeight(codeType);
    }

    @Override
    public LdpcCoder createLdpcCoder() {
        return new LdpcCoder(matrixA, matrixB, matrixC, matrixD, matrixF, matrixEp, gapValue, kValue);
    }

    @Override
    public LpnParams getLpnParams() {
        return lpnParams;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractLdpcCreator)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        AbstractLdpcCreator that = (AbstractLdpcCreator) obj;
        /*
        * 主要用于检验多种LdpcCreator创建对象是否相同。
        * 当所有矩阵都相同时，gapValue， kValue， weight， ceiLogN一定相同，所以不参加比较。
         */
        return new EqualsBuilder()
                .append(this.matrixA, that.matrixA)
                .append(this.matrixB, that.matrixB)
                .append(this.matrixC, that.matrixC)
                .append(this.matrixD, that.matrixD)
                .append(this.matrixF, that.matrixF)
                .append(this.matrixEp, that.matrixEp)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.matrixA)
                .append(this.matrixB)
                .append(this.matrixC)
                .append(this.matrixD)
                .append(this.matrixF)
                .append(this.matrixEp)
                .toHashCode();
    }
}
