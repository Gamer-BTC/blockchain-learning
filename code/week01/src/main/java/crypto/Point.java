package crypto;

import java.math.BigInteger;

/**
 * 椭圆曲线上的点
 * 
 * Week 1 Day 1: 椭圆曲线基础
 */
public class Point {
    
    public final BigInteger x;
    public final BigInteger y;
    public final boolean isInfinity;
    
    /**
     * 无穷远点（单位元）
     * 在椭圆曲线群中，无穷远点类似于加法中的 0
     * P + ∞ = P
     */
    public static final Point INFINITY = new Point();
    
    private Point() {
        this.x = null;
        this.y = null;
        this.isInfinity = true;
    }
    
    public Point(BigInteger x, BigInteger y) {
        this.x = x;
        this.y = y;
        this.isInfinity = false;
    }
    
    public Point(long x, long y) {
        this(BigInteger.valueOf(x), BigInteger.valueOf(y));
    }
    
    public Point(String xHex, String yHex) {
        this(new BigInteger(xHex, 16), new BigInteger(yHex, 16));
    }
    
    @Override
    public String toString() {
        if (isInfinity) return "∞ (无穷远点)";
        return String.format("(%s, %s)", 
            x.toString(16).toUpperCase(), 
            y.toString(16).toUpperCase());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;
        Point point = (Point) o;
        if (isInfinity && point.isInfinity) return true;
        if (isInfinity || point.isInfinity) return false;
        return x.equals(point.x) && y.equals(point.y);
    }
    
    @Override
    public int hashCode() {
        if (isInfinity) return 0;
        return x.hashCode() * 31 + y.hashCode();
    }
}
