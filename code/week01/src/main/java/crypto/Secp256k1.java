package crypto;

import java.math.BigInteger;

/**
 * secp256k1 曲线参数
 * 
 * 比特币/以太坊使用的椭圆曲线
 * 曲线方程: y² = x³ + 7 (mod p)
 * 
 * Week 1 Day 1: 椭圆曲线密码学基础
 */
public class Secp256k1 {
    
    // ============== 曲线参数 ==============
    
    /**
     * 有限域模数 p
     * p = 2^256 - 2^32 - 977
     */
    public static final BigInteger p = new BigInteger(
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);
    
    /**
     * 曲线参数 a = 0
     */
    public static final BigInteger a = BigInteger.ZERO;
    
    /**
     * 曲线参数 b = 7
     * 曲线方程: y² = x³ + 7
     */
    public static final BigInteger b = BigInteger.valueOf(7);
    
    /**
     * 生成元 G 的 x 坐标
     */
    public static final BigInteger Gx = new BigInteger(
            "79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16);
    
    /**
     * 生成元 G 的 y 坐标
     */
    public static final BigInteger Gy = new BigInteger(
            "483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16);
    
    /**
     * 生成元 G 的阶 n
     * n × G = ∞ (无穷远点)
     * 私钥必须在 [1, n-1] 范围内
     */
    public static final BigInteger n = new BigInteger(
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);
    
    /**
     * 余因子 h = 1
     */
    public static final BigInteger h = BigInteger.ONE;
    
    // ============== 实例 ==============
    
    /**
     * 生成元 G
     */
    public static final Point G = new Point(Gx, Gy);
    
    /**
     * 曲线实例
     */
    public static final EllipticCurve CURVE = new EllipticCurve(a, b, p);
    
    // ============== 验证方法 ==============
    
    /**
     * 验证 G 在曲线上
     */
    public static boolean verifyGenerator() {
        return CURVE.isOnCurve(G);
    }
    
    /**
     * 从私钥计算公钥
     * P = k × G
     */
    public static Point getPublicKey(BigInteger privateKey) {
        if (privateKey.compareTo(BigInteger.ONE) < 0 || 
            privateKey.compareTo(n) >= 0) {
            throw new IllegalArgumentException("私钥必须在 [1, n-1] 范围内");
        }
        return CURVE.multiply(privateKey, G);
    }
    
    // ============== 测试 ==============
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║           secp256k1 椭圆曲线参数验证                          ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
        
        // 1. 显示参数
        System.out.println("【曲线参数】");
        System.out.println("方程: y² = x³ + 7 (mod p)\n");
        System.out.println("p  = " + p.toString(16).toUpperCase());
        System.out.println("p 位数 = " + p.bitLength() + " bits\n");
        
        System.out.println("【生成元 G】");
        System.out.println("Gx = " + Gx.toString(16).toUpperCase());
        System.out.println("Gy = " + Gy.toString(16).toUpperCase() + "\n");
        
        System.out.println("【阶 n】");
        System.out.println("n  = " + n.toString(16).toUpperCase());
        System.out.println("n 位数 = " + n.bitLength() + " bits");
        System.out.println("可能的私钥数量 ≈ 1.16 × 10^77\n");
        
        // 2. 验证 G 在曲线上
        System.out.println("【验证】");
        System.out.println("G 在曲线上? " + (verifyGenerator() ? "✓ 是" : "✗ 否"));
        
        // 验证: Gy² = Gx³ + 7 (mod p)
        BigInteger left = Gy.modPow(BigInteger.TWO, p);
        BigInteger right = Gx.modPow(BigInteger.valueOf(3), p).add(b).mod(p);
        System.out.println("Gy² mod p = " + left.toString(16).substring(0, 16) + "...");
        System.out.println("Gx³+7 mod p = " + right.toString(16).substring(0, 16) + "...");
        System.out.println("相等? " + (left.equals(right) ? "✓" : "✗") + "\n");
        
        // 3. 计算几个公钥示例
        System.out.println("【公钥计算示例】");
        
        // 私钥 = 1
        BigInteger k1 = BigInteger.ONE;
        Point P1 = getPublicKey(k1);
        System.out.println("私钥 k=1:");
        System.out.println("  公钥 = G (因为 1×G = G)");
        System.out.println("  验证: " + (P1.equals(G) ? "✓" : "✗") + "\n");
        
        // 私钥 = 2
        BigInteger k2 = BigInteger.TWO;
        Point P2 = getPublicKey(k2);
        System.out.println("私钥 k=2:");
        System.out.println("  公钥 x = " + P2.x.toString(16).substring(0, 32) + "...");
        System.out.println("  公钥 y = " + P2.y.toString(16).substring(0, 32) + "...");
        System.out.println("  在曲线上? " + (CURVE.isOnCurve(P2) ? "✓" : "✗") + "\n");
        
        // 随机私钥示例
        BigInteger k3 = new BigInteger(
                "1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF", 16);
        Point P3 = getPublicKey(k3);
        System.out.println("私钥 k=1234...CDEF (256位):");
        System.out.println("  公钥 x = " + P3.x.toString(16).substring(0, 32) + "...");
        System.out.println("  公钥 y = " + P3.y.toString(16).substring(0, 32) + "...");
        System.out.println("  在曲线上? " + (CURVE.isOnCurve(P3) ? "✓" : "✗") + "\n");
        
        // 4. 验证 1:1 关系
        System.out.println("【私钥→公钥 1:1 验证】");
        Point P3_again = getPublicKey(k3);
        System.out.println("同一私钥计算两次，公钥相同? " + (P3.equals(P3_again) ? "✓" : "✗"));
        
        System.out.println("\n✅ Day 1 学习完成！");
    }
}
