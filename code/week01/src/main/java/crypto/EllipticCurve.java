package crypto;

import java.math.BigInteger;

/**
 * 椭圆曲线: y² = x³ + ax + b (mod p)
 * 
 * Week 1 Day 1: 实现点加法和标量乘法
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
}
