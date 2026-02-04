package crypto;

import java.math.BigInteger;

/**
 * 椭圆曲线: y² = x³ + ax + b (mod p)
 * 
 * Week 1 Day 1-2: 实现点加法和标量乘法
 * 
 * 核心概念:
 * - 点加法: 过两点画线，与曲线交点关于x轴对称
 * - 点倍乘: 过一点画切线，与曲线交点关于x轴对称
 * - 标量乘法: kP = P + P + ... + P (k次)，使用 double-and-add 优化
 * - 离散对数难题 (ECDLP): 已知 P 和 Q=kP，求 k 是计算困难的
 */
public class EllipticCurve {
    
    public final BigInteger a;
    public final BigInteger b;
    public final BigInteger p;
    
    public EllipticCurve(BigInteger a, BigInteger b, BigInteger p) {
        this.a = a;
        this.b = b;
        this.p = p;
    }
    
    /**
     * 验证点是否在曲线上
     * y² = x³ + ax + b (mod p)
     */
    public boolean isOnCurve(Point point) {
        if (point.isInfinity) return true;
        
        // 左边: y²
        BigInteger left = point.y.modPow(BigInteger.TWO, p);
        
        // 右边: x³ + ax + b
        BigInteger right = point.x.modPow(BigInteger.valueOf(3), p)
                .add(a.multiply(point.x))
                .add(b)
                .mod(p);
        
        return left.equals(right);
    }
    
    /**
     * 点加法: P + Q
     * 
     * 分三种情况:
     * 1. P 或 Q 是无穷远点
     * 2. P = -Q (互为逆元)
     * 3. P = Q (点倍乘)
     * 4. P ≠ Q (一般情况)
     */
    public Point add(Point P, Point Q) {
        // 情况 1: 处理无穷远点
        if (P.isInfinity) return Q;
        if (Q.isInfinity) return P;
        
        // 情况 2: P = -Q，返回无穷远点
        // -Q 的 y 坐标是 p - Q.y
        if (P.x.equals(Q.x) && P.y.add(Q.y).mod(p).equals(BigInteger.ZERO)) {
            return Point.INFINITY;
        }
        
        BigInteger lambda;
        
        if (P.x.equals(Q.x) && P.y.equals(Q.y)) {
            // 情况 3: 点倍乘 P + P
            // λ = (3x² + a) / (2y) mod p
            BigInteger numerator = P.x.pow(2)
                    .multiply(BigInteger.valueOf(3))
                    .add(a)
                    .mod(p);
            BigInteger denominator = P.y.multiply(BigInteger.TWO).mod(p);
            lambda = numerator.multiply(modInverse(denominator)).mod(p);
        } else {
            // 情况 4: 一般点加法 P + Q
            // λ = (y₂ - y₁) / (x₂ - x₁) mod p
            BigInteger numerator = Q.y.subtract(P.y).mod(p);
            BigInteger denominator = Q.x.subtract(P.x).mod(p);
            lambda = numerator.multiply(modInverse(denominator)).mod(p);
        }
        
        // x₃ = λ² - x₁ - x₂ mod p
        BigInteger x3 = lambda.pow(2)
                .subtract(P.x)
                .subtract(Q.x)
                .mod(p);
        
        // y₃ = λ(x₁ - x₃) - y₁ mod p
        BigInteger y3 = lambda.multiply(P.x.subtract(x3))
                .subtract(P.y)
                .mod(p);
        
        return new Point(x3, y3);
    }
    
    /**
     * 标量乘法: k × P
     * 
     * 使用 Double-and-Add 算法
     * 时间复杂度: O(log k)
     * 
     * 例: 13 × P = (1101)₂ × P = 8P + 4P + P
     */
    public Point multiply(BigInteger k, Point P) {
        // 处理边界情况
        if (k.equals(BigInteger.ZERO) || P.isInfinity) {
            return Point.INFINITY;
        }
        
        // 处理负数
        if (k.compareTo(BigInteger.ZERO) < 0) {
            k = k.negate();
            P = negate(P);
        }
        
        Point result = Point.INFINITY;
        Point addend = P;
        
        while (k.compareTo(BigInteger.ZERO) > 0) {
            // 如果最低位是 1，加上当前的 addend
            if (k.testBit(0)) {
                result = add(result, addend);
            }
            // addend 翻倍
            addend = add(addend, addend);
            // k 右移一位
            k = k.shiftRight(1);
        }
        
        return result;
    }
    
    /**
     * 点的逆元: -P = (x, -y) = (x, p-y)
     */
    public Point negate(Point P) {
        if (P.isInfinity) return P;
        return new Point(P.x, p.subtract(P.y).mod(p));
    }
    
    /**
     * 模逆元: a^(-1) mod p
     * 费马小定理: a^(-1) = a^(p-2) mod p
     */
    private BigInteger modInverse(BigInteger a) {
        return a.modPow(p.subtract(BigInteger.TWO), p);
    }
    
    @Override
    public String toString() {
        return String.format("y² = x³ + %sx + %s (mod p)", a, b);
    }
    
    // ==================== 演示 ====================
    
    public static void main(String[] args) {
        System.out.println("=== 椭圆曲线点加法与标量乘法演示 ===\n");
        
        // 曲线: y² = x³ + 7 (mod 17) - secp256k1 简化版
        BigInteger a = BigInteger.ZERO;
        BigInteger b = BigInteger.valueOf(7);
        BigInteger p = BigInteger.valueOf(17);
        
        EllipticCurve curve = new EllipticCurve(a, b, p);
        System.out.println("曲线: " + curve + "\n");
        
        // 生成元 G = (15, 13)
        // 验证: 15³ + 7 = 3382, 3382 mod 17 = 16
        //       13² = 169, 169 mod 17 = 16  ✓
        Point G = new Point(BigInteger.valueOf(15), BigInteger.valueOf(13));
        
        System.out.println("生成元 G = " + G);
        System.out.println("G 在曲线上: " + curve.isOnCurve(G) + "\n");
        
        // 计算 2G (点倍乘)
        Point G2 = curve.add(G, G);
        System.out.println("2G = " + G2);
        System.out.println("2G 在曲线上: " + curve.isOnCurve(G2) + "\n");
        
        // 计算 3G = 2G + G
        Point G3 = curve.add(G2, G);
        System.out.println("3G = " + G3);
        
        // 使用标量乘法计算 5G
        Point G5 = curve.multiply(BigInteger.valueOf(5), G);
        System.out.println("5G (标量乘法) = " + G5);
        
        // 验证: 5G = 4G + G
        Point G4 = curve.add(G2, G2);
        Point G5_verify = curve.add(G4, G);
        System.out.println("5G (手动验证) = " + G5_verify);
        System.out.println("两种方法结果一致: " + G5.equals(G5_verify) + "\n");
        
        // 计算 11G，演示 double-and-add
        System.out.println("=== Double-and-Add 演示: 11G ===");
        System.out.println("11 = 1011₂ = 8 + 2 + 1");
        Point G11 = curve.multiply(BigInteger.valueOf(11), G);
        System.out.println("11G = " + G11 + "\n");
        
        // 计算点的阶（最小的 n 使得 nG = O）
        System.out.println("=== 计算 G 的阶 ===");
        Point current = G;
        int order = 1;
        while (!current.isInfinity && order < 100) {
            order++;
            current = curve.add(current, G);
        }
        System.out.println("G 的阶 = " + order);
        Point nG = curve.multiply(BigInteger.valueOf(order), G);
        System.out.println(order + "G = " + (nG.isInfinity ? "O (无穷远点)" : nG));
        
        // 验证 (order+1)G = G
        Point next = curve.multiply(BigInteger.valueOf(order + 1), G);
        System.out.println((order + 1) + "G = " + next + " (应等于 G)");
    }
}
