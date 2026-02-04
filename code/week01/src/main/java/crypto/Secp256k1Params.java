package crypto;

import java.math.BigInteger;

/**
 * secp256k1 参数验证
 * 
 * Week 1 Day 3: 验证 secp256k1 曲线参数
 * 
 * 曲线方程: y² = x³ + 7 (mod p)
 */
public class Secp256k1Params {
    
    // 素数 p = 2^256 - 2^32 - 977
    public static final BigInteger P = new BigInteger(
        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);
    
    // 曲线参数 a = 0, b = 7
    public static final BigInteger A = BigInteger.ZERO;
    public static final BigInteger B = BigInteger.valueOf(7);
    
    // 生成元 G 的坐标
    public static final BigInteger Gx = new BigInteger(
        "79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16);
    public static final BigInteger Gy = new BigInteger(
        "483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16);
    
    // 阶 n (G 的循环周期，nG = O)
    public static final BigInteger N = new BigInteger(
        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);
    
    // 余因子 h = 1
    public static final BigInteger H = BigInteger.ONE;
    
    public static void main(String[] args) {
        System.out.println("=== secp256k1 参数验证 ===\n");
        
        // 1. 验证 p = 2^256 - 2^32 - 977
        verifyPrimeP();
        
        // 2. 验证生成元 G 在曲线上
        verifyGeneratorOnCurve();
        
        // 3. 验证 n × G = O (使用我们之前的 EllipticCurve 类)
        verifyOrder();
        
        // 4. 打印参数信息
        printParamsInfo();
    }
    
    /**
     * 验证 p = 2^256 - 2^32 - 977
     */
    private static void verifyPrimeP() {
        System.out.println("【验证 1】p = 2^256 - 2^32 - 977");
        
        BigInteger expected = BigInteger.TWO.pow(256)
            .subtract(BigInteger.TWO.pow(32))
            .subtract(BigInteger.valueOf(977));
        
        boolean match = P.equals(expected);
        System.out.println("  计算值: " + expected.toString(16).toUpperCase());
        System.out.println("  参数值: " + P.toString(16).toUpperCase());
        System.out.println("  匹配: " + (match ? "✓" : "✗"));
        
        // 验证是素数 (BigInteger.isProbablePrime)
        boolean isPrime = P.isProbablePrime(100);
        System.out.println("  是素数: " + (isPrime ? "✓ (概率检验)" : "✗"));
        System.out.println();
    }
    
    /**
     * 验证生成元 G 在曲线 y² = x³ + 7 (mod p) 上
     */
    private static void verifyGeneratorOnCurve() {
        System.out.println("【验证 2】生成元 G 在曲线上");
        System.out.println("  曲线方程: y² = x³ + 7 (mod p)");
        System.out.println();
        System.out.println("  Gx = " + Gx.toString(16).toUpperCase());
        System.out.println("  Gy = " + Gy.toString(16).toUpperCase());
        System.out.println();
        
        // 计算左边: Gy²
        BigInteger left = Gy.modPow(BigInteger.TWO, P);
        
        // 计算右边: Gx³ + 7
        BigInteger right = Gx.modPow(BigInteger.valueOf(3), P)
            .add(B)
            .mod(P);
        
        System.out.println("  左边 (Gy² mod p):");
        System.out.println("    " + left.toString(16).toUpperCase());
        System.out.println();
        System.out.println("  右边 (Gx³ + 7 mod p):");
        System.out.println("    " + right.toString(16).toUpperCase());
        System.out.println();
        
        boolean onCurve = left.equals(right);
        System.out.println("  G 在曲线上: " + (onCurve ? "✓ 验证通过！" : "✗ 验证失败"));
        System.out.println();
    }
    
    /**
     * 验证 n × G = O (无穷远点)
     * 使用之前实现的椭圆曲线类
     */
    private static void verifyOrder() {
        System.out.println("【验证 3】n × G = O (无穷远点)");
        System.out.println("  n = " + N.toString(16).toUpperCase());
        System.out.println();
        
        // 创建 secp256k1 曲线
        EllipticCurve curve = new EllipticCurve(A, B, P);
        
        // 创建生成元 G
        Point G = new Point(Gx, Gy);
        
        // 计算 nG
        System.out.println("  正在计算 n × G (可能需要几秒钟)...");
        Point nG = curve.multiply(N, G);
        
        boolean isInfinity = (nG == null || nG.isInfinity);
        System.out.println("  n × G = " + (isInfinity ? "O (无穷远点)" : nG));
        System.out.println("  验证: " + (isInfinity ? "✓ 通过！" : "✗ 失败"));
        System.out.println();
        
        // 额外验证: (n+1) × G = G
        System.out.println("  额外验证: (n+1) × G = G ?");
        Point nPlus1G = curve.multiply(N.add(BigInteger.ONE), G);
        boolean isG = nPlus1G != null && nPlus1G.x.equals(Gx) && nPlus1G.y.equals(Gy);
        System.out.println("  (n+1) × G = " + nPlus1G);
        System.out.println("  等于 G: " + (isG ? "✓" : "✗"));
        System.out.println();
    }
    
    /**
     * 打印参数信息
     */
    private static void printParamsInfo() {
        System.out.println("=== secp256k1 参数信息 ===\n");
        
        System.out.println("曲线方程: y² = x³ + " + A + "x + " + B);
        System.out.println();
        
        System.out.println("p (有限域大小):");
        System.out.println("  十六进制: " + P.toString(16).toUpperCase());
        System.out.println("  位数: " + P.bitLength() + " bits");
        System.out.println("  特殊形式: 2^256 - 2^32 - 977");
        System.out.println();
        
        System.out.println("n (阶，G的循环周期):");
        System.out.println("  十六进制: " + N.toString(16).toUpperCase());
        System.out.println("  位数: " + N.bitLength() + " bits");
        System.out.println();
        
        System.out.println("h (余因子): " + H);
        System.out.println();
        
        System.out.println("安全性:");
        System.out.println("  私钥空间: 约 2^256 ≈ 10^77 种可能");
        System.out.println("  暴力破解: 宇宙毁灭也算不完 ✓");
    }
}
