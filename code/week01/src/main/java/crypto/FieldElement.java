package crypto;

import java.math.BigInteger;

/**
 * 有限域元素 - Week 1 Day 1
 * 
 * 有限域 Fp 是密码学的基础：
 * - 包含 {0, 1, 2, ..., p-1} 共 p 个元素
 * - 所有运算结果都 mod p，保证结果仍在域内
 * - p 必须是质数
 */
public class FieldElement {
    
    private final BigInteger num;
    private final BigInteger prime;
    
    public FieldElement(BigInteger num, BigInteger prime) {
        // 确保 num 在 [0, prime) 范围内
        this.num = num.mod(prime);
        this.prime = prime;
    }
    
    public FieldElement(long num, long prime) {
        this(BigInteger.valueOf(num), BigInteger.valueOf(prime));
    }
    
    /**
     * 加法: (a + b) mod p
     */
    public FieldElement add(FieldElement other) {
        checkSameField(other);
        BigInteger result = this.num.add(other.num).mod(prime);
        return new FieldElement(result, prime);
    }
    
    /**
     * 减法: (a - b) mod p
     * 注意: 结果可能为负，需要 +p 调整
     */
    public FieldElement subtract(FieldElement other) {
        checkSameField(other);
        BigInteger result = this.num.subtract(other.num).mod(prime);
        return new FieldElement(result, prime);
    }
    
    /**
     * 乘法: (a * b) mod p
     */
    public FieldElement multiply(FieldElement other) {
        checkSameField(other);
        BigInteger result = this.num.multiply(other.num).mod(prime);
        return new FieldElement(result, prime);
    }
    
    /**
     * 幂运算: a^exp mod p
     * 使用快速幂算法
     */
    public FieldElement pow(BigInteger exp) {
        // 处理负指数: a^(-n) = (a^(-1))^n
        BigInteger e = exp.mod(prime.subtract(BigInteger.ONE));
        BigInteger result = this.num.modPow(e, prime);
        return new FieldElement(result, prime);
    }
    
    public FieldElement pow(long exp) {
        return pow(BigInteger.valueOf(exp));
    }
    
    /**
     * 求逆元: a^(-1) mod p
     * 
     * 费马小定理: a^(p-1) ≡ 1 (mod p)
     * 所以: a^(-1) ≡ a^(p-2) (mod p)
     */
    public FieldElement inverse() {
        if (this.num.equals(BigInteger.ZERO)) {
            throw new ArithmeticException("0 没有逆元");
        }
        BigInteger result = this.num.modPow(prime.subtract(BigInteger.TWO), prime);
        return new FieldElement(result, prime);
    }
    
    /**
     * 除法: a / b = a * b^(-1)
     */
    public FieldElement divide(FieldElement other) {
        checkSameField(other);
        return this.multiply(other.inverse());
    }
    
    private void checkSameField(FieldElement other) {
        if (!this.prime.equals(other.prime)) {
            throw new IllegalArgumentException("不同有限域的元素不能运算");
        }
    }
    
    public BigInteger getNum() {
        return num;
    }
    
    public BigInteger getPrime() {
        return prime;
    }
    
    @Override
    public String toString() {
        return num.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldElement)) return false;
        FieldElement that = (FieldElement) o;
        return num.equals(that.num) && prime.equals(that.prime);
    }
    
    @Override
    public int hashCode() {
        return num.hashCode() * 31 + prime.hashCode();
    }
    
    // ==================== 测试 ====================
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   有限域 Fp 基础运算 - Week 1 Day 1");
        System.out.println("========================================\n");
        
        long p = 7;
        
        FieldElement a = new FieldElement(3, p);
        FieldElement b = new FieldElement(5, p);
        
        // 测试 1: 基本运算
        System.out.println("【F_7 基本运算】");
        System.out.println("3 + 5 mod 7 = " + a.add(b));        // 8 mod 7 = 1
        System.out.println("3 - 5 mod 7 = " + a.subtract(b));   // -2 mod 7 = 5
        System.out.println("3 × 5 mod 7 = " + a.multiply(b));   // 15 mod 7 = 1
        System.out.println("3 ÷ 5 mod 7 = " + a.divide(b));     // 3 × 5^(-1) = 3 × 3 = 9 mod 7 = 2
        
        // 测试 2: 幂运算与费马小定理
        System.out.println("\n【幂运算】");
        System.out.println("3^6 mod 7 = " + a.pow(6));          // 费马小定理: 3^6 ≡ 1
        System.out.println("3^0 mod 7 = " + a.pow(0));          // 任何数^0 = 1
        
        // 测试 3: 逆元验证
        System.out.println("\n【逆元验证】");
        FieldElement four = new FieldElement(4, p);
        FieldElement fourInv = four.inverse();
        System.out.println("4^(-1) mod 7 = " + fourInv);                    // 2
        System.out.println("验证 4 × 4^(-1) = " + four.multiply(fourInv));  // 1
        
        // 测试 4: 找生成元
        System.out.println("\n【F_7* 生成元】");
        findGenerators(7);
        
        System.out.println("\n【F_11* 生成元】");
        findGenerators(11);
    }
    
    /**
     * 找出 F_p* 的所有生成元
     * 
     * 乘法群 F_p* = {1, 2, ..., p-1}
     * 生成元 g: g^1, g^2, ..., g^(p-1) 能覆盖整个 F_p*
     * 等价于: g 的阶 = p-1 (最小的 k 使得 g^k = 1)
     */
    public static void findGenerators(long p) {
        System.out.print("生成元: ");
        StringBuilder generators = new StringBuilder();
        
        for (long g = 2; g < p; g++) {
            if (isGenerator(g, p)) {
                if (generators.length() > 0) {
                    generators.append(", ");
                }
                generators.append(g);
            }
        }
        System.out.println(generators);
        
        // 展示一个生成元如何生成整个群
        long g = 3;
        if (isGenerator(g, p)) {
            System.out.print(g + " 生成的群: {");
            for (int i = 1; i < p; i++) {
                if (i > 1) System.out.print(", ");
                long val = new FieldElement(g, p).pow(i).getNum().longValue();
                System.out.print(val);
            }
            System.out.println("}");
        }
    }
    
    /**
     * 判断 g 是否是 F_p* 的生成元
     */
    private static boolean isGenerator(long g, long p) {
        FieldElement element = new FieldElement(g, p);
        
        // g 的阶必须是 p-1
        // 等价于: 对于 p-1 的每个质因子 q，g^((p-1)/q) ≠ 1
        long order = p - 1;
        
        // 简单方法: 直接计算阶
        for (long k = 1; k < order; k++) {
            if (element.pow(k).getNum().equals(BigInteger.ONE)) {
                return false; // 找到更小的 k 使 g^k = 1
            }
        }
        return element.pow(order).getNum().equals(BigInteger.ONE);
    }
}
