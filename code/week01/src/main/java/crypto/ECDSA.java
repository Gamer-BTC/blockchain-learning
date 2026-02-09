package crypto;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * ECDSA 签名算法 - 简化版 Java 实现
 *
 * Week 1 Day 6: 基于 secp256k1 曲线实现 ECDSA 签名与验签
 *
 * 核心流程:
 *   签名: (私钥, 消息哈希) → (r, s)
 *   验签: (公钥, 消息哈希, r, s) → true/false
 *
 * 安全说明:
 *   - 本实现仅用于学习，生产环境请使用成熟的密码学库
 *   - 使用 SecureRandom 生成随机数 k
 *   - 实现了 Low-S 规范化，防止签名可塑性攻击
 */
public class ECDSA {

    // secp256k1 曲线和参数
    private static final EllipticCurve curve = Secp256k1.CURVE;
    private static final Point G = Secp256k1.G;
    private static final BigInteger n = Secp256k1.n;

    private static final SecureRandom random = new SecureRandom();

    // ==================== 签名结果 ====================

    /**
     * ECDSA 签名，包含 (r, s) 两个分量
     */
    public static class Signature {
        public final BigInteger r;
        public final BigInteger s;

        public Signature(BigInteger r, BigInteger s) {
            this.r = r;
            this.s = s;
        }

        @Override
        public String toString() {
            return String.format("Signature {\n  r = %s\n  s = %s\n}",
                    r.toString(16).toUpperCase(),
                    s.toString(16).toUpperCase());
        }
    }

    // ==================== 密钥生成 ====================

    /**
     * 生成随机私钥
     * 私钥范围: [1, n-1]
     */
    public static BigInteger generatePrivateKey() {
        BigInteger privateKey;
        do {
            privateKey = new BigInteger(256, random);
        } while (privateKey.compareTo(BigInteger.ONE) < 0
                || privateKey.compareTo(n) >= 0);
        return privateKey;
    }

    /**
     * 从私钥计算公钥: Q = d × G
     */
    public static Point getPublicKey(BigInteger privateKey) {
        return curve.multiply(privateKey, G);
    }

    // ==================== 哈希 ====================

    /**
     * SHA-256 哈希
     * 实际区块链中用双重 SHA-256 或 Keccak-256，这里简化用 SHA-256
     */
    public static byte[] sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 不可用", e);
        }
    }

    /**
     * 消息字符串 → 哈希值 (BigInteger)
     */
    public static BigInteger hashMessage(String message) {
        byte[] hash = sha256(message.getBytes(StandardCharsets.UTF_8));
        return new BigInteger(1, hash);
    }

    // ==================== 签名 ====================

    /**
     * ECDSA 签名
     *
     * 步骤:
     *   1. 生成随机数 k ∈ [1, n-1]
     *   2. 计算 R = k × G
     *   3. r = R.x mod n（若 r=0 则重新选 k）
     *   4. s = k⁻¹ × (z + r × d) mod n（若 s=0 则重新选 k）
     *   5. Low-S 规范化: 若 s > n/2，令 s = n - s
     *
     * @param privateKey 私钥 d
     * @param z          消息哈希值
     * @return 签名 (r, s)
     */
    public static Signature sign(BigInteger privateKey, BigInteger z) {
        BigInteger r, s;

        do {
            // 1. 生成安全随机数 k
            BigInteger k;
            do {
                k = new BigInteger(256, random);
            } while (k.compareTo(BigInteger.ONE) < 0
                    || k.compareTo(n) >= 0);

            // 2. 计算 R = k × G
            Point R = curve.multiply(k, G);

            // 3. r = R.x mod n
            r = R.x.mod(n);
            if (r.equals(BigInteger.ZERO)) continue;

            // 4. s = k⁻¹ × (z + r × d) mod n
            BigInteger kInv = k.modInverse(n);
            s = kInv.multiply(z.add(r.multiply(privateKey))).mod(n);

            if (s.equals(BigInteger.ZERO)) continue;

            // 5. Low-S 规范化: 防止签名可塑性
            //    对于有效签名 (r, s)，(r, n-s) 也是有效签名
            //    统一取 s ≤ n/2 的那个
            BigInteger halfN = n.shiftRight(1);
            if (s.compareTo(halfN) > 0) {
                s = n.subtract(s);
            }

            return new Signature(r, s);

        } while (true);
    }

    /**
     * 对字符串消息签名（便捷方法）
     */
    public static Signature sign(BigInteger privateKey, String message) {
        return sign(privateKey, hashMessage(message));
    }

    // ==================== 验签 ====================

    /**
     * ECDSA 验签
     *
     * 步骤:
     *   1. 检查 r, s ∈ [1, n-1]
     *   2. w = s⁻¹ mod n
     *   3. u₁ = z × w mod n
     *   4. u₂ = r × w mod n
     *   5. P = u₁ × G + u₂ × Q
     *   6. 若 P = ∞，签名无效
     *   7. 检查 P.x mod n == r
     *
     * 数学原理:
     *   因为 s = k⁻¹(z + rd)，所以 k = s⁻¹(z + rd) = wz + wrd = u₁ + u₂d
     *   所以 P = u₁G + u₂Q = u₁G + u₂dG = (u₁ + u₂d)G = kG = R
     *   因此 P.x = R.x = r ✓
     *
     * @param publicKey 公钥 Q
     * @param z         消息哈希值
     * @param sig       签名 (r, s)
     * @return true = 签名有效
     */
    public static boolean verify(Point publicKey, BigInteger z, Signature sig) {
        BigInteger r = sig.r;
        BigInteger s = sig.s;

        // 1. 范围检查
        if (r.compareTo(BigInteger.ONE) < 0 || r.compareTo(n) >= 0) {
            return false;
        }
        if (s.compareTo(BigInteger.ONE) < 0 || s.compareTo(n) >= 0) {
            return false;
        }

        // 2. w = s⁻¹ mod n
        BigInteger w = s.modInverse(n);

        // 3. u₁ = z × w mod n
        BigInteger u1 = z.multiply(w).mod(n);

        // 4. u₂ = r × w mod n
        BigInteger u2 = r.multiply(w).mod(n);

        // 5. P = u₁ × G + u₂ × Q
        Point p1 = curve.multiply(u1, G);
        Point p2 = curve.multiply(u2, publicKey);
        Point P = curve.add(p1, p2);

        // 6. 无穷远点检查
        if (P.isInfinity) {
            return false;
        }

        // 7. 验证 P.x mod n == r
        return P.x.mod(n).equals(r);
    }

    /**
     * 对字符串消息验签（便捷方法）
     */
    public static boolean verify(Point publicKey, String message, Signature sig) {
        return verify(publicKey, hashMessage(message), sig);
    }

    // ==================== 演示 ====================

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║    ECDSA 签名算法 - secp256k1 Java 实现         ║");
        System.out.println("║    Week 1 Day 6                                 ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        // ====== 1. 密钥生成 ======
        System.out.println("=== 1. 密钥生成 ===\n");

        BigInteger privateKey = generatePrivateKey();
        Point publicKey = getPublicKey(privateKey);

        System.out.println("私钥 d = " + privateKey.toString(16).toUpperCase());
        System.out.println("公钥 Q = (" );
        System.out.println("  x: " + publicKey.x.toString(16).toUpperCase());
        System.out.println("  y: " + publicKey.y.toString(16).toUpperCase());
        System.out.println(")");
        System.out.println("公钥在曲线上: " + (curve.isOnCurve(publicKey) ? "✓" : "✗"));

        // ====== 2. 签名 ======
        System.out.println("\n=== 2. ECDSA 签名 ===\n");

        String message = "Hello, Bitcoin! 你好，区块链！";
        System.out.println("原始消息: \"" + message + "\"");

        BigInteger z = hashMessage(message);
        System.out.println("SHA-256 哈希 z = " + z.toString(16).toUpperCase());

        long signStart = System.currentTimeMillis();
        Signature sig = sign(privateKey, z);
        long signTime = System.currentTimeMillis() - signStart;

        System.out.println("\n签名结果:");
        System.out.println(sig);
        System.out.println("签名耗时: " + signTime + " ms");

        // ====== 3. 验签 ======
        System.out.println("\n=== 3. ECDSA 验签 ===\n");

        long verifyStart = System.currentTimeMillis();
        boolean valid = verify(publicKey, z, sig);
        long verifyTime = System.currentTimeMillis() - verifyStart;

        System.out.println("验签结果: " + (valid ? "✓ 有效签名" : "✗ 无效签名"));
        System.out.println("验签耗时: " + verifyTime + " ms");

        // ====== 4. 篡改测试 ======
        System.out.println("\n=== 4. 篡改检测测试 ===\n");

        // 4a. 篡改消息
        String tamperedMsg = "Hello, Bitcoin! 你好，区块链！（被篡改）";
        BigInteger z2 = hashMessage(tamperedMsg);
        boolean tamperedValid = verify(publicKey, z2, sig);
        System.out.println("篡改消息后验签: " + (tamperedValid ? "✗ 未检测到篡改" : "✓ 检测到篡改！"));

        // 4b. 错误公钥
        BigInteger fakePrivKey = generatePrivateKey();
        Point fakePublicKey = getPublicKey(fakePrivKey);
        boolean wrongKeyValid = verify(fakePublicKey, z, sig);
        System.out.println("用错误公钥验签: " + (wrongKeyValid ? "✗ 未检测到" : "✓ 检测到非法签名者！"));

        // 4c. 篡改签名
        Signature tamperedSig = new Signature(sig.r.add(BigInteger.ONE), sig.s);
        boolean tamperedSigValid = verify(publicKey, z, tamperedSig);
        System.out.println("篡改签名 r 值:   " + (tamperedSigValid ? "✗ 未检测到" : "✓ 检测到签名被篡改！"));

        // ====== 5. 多次签名同一消息 ======
        System.out.println("\n=== 5. 同一消息多次签名（每次 k 不同）===\n");

        Signature sig2 = sign(privateKey, z);
        Signature sig3 = sign(privateKey, z);
        System.out.println("签名 1: r=" + sig.r.toString(16).substring(0, 16) + "...");
        System.out.println("签名 2: r=" + sig2.r.toString(16).substring(0, 16) + "...");
        System.out.println("签名 3: r=" + sig3.r.toString(16).substring(0, 16) + "...");
        System.out.println("三次签名各不相同（因为 k 随机）: " +
                (!sig.r.equals(sig2.r) && !sig2.r.equals(sig3.r) ? "✓" : "✗"));
        System.out.println("但都能验证通过:");
        System.out.println("  签名 1 验证: " + (verify(publicKey, z, sig) ? "✓" : "✗"));
        System.out.println("  签名 2 验证: " + (verify(publicKey, z, sig2) ? "✓" : "✗"));
        System.out.println("  签名 3 验证: " + (verify(publicKey, z, sig3) ? "✓" : "✗"));

        // ====== 6. Low-S 验证 ======
        System.out.println("\n=== 6. Low-S 规范化验证 ===\n");

        BigInteger halfN = n.shiftRight(1);
        System.out.println("n/2 = " + halfN.toString(16).toUpperCase());
        System.out.println("sig1.s ≤ n/2 ? " + (sig.s.compareTo(halfN) <= 0 ? "✓ Low-S" : "✗ High-S"));
        System.out.println("sig2.s ≤ n/2 ? " + (sig2.s.compareTo(halfN) <= 0 ? "✓ Low-S" : "✗ High-S"));

        // ====== 总结 ======
        System.out.println("\n══════════════════════════════════════════════════");
        System.out.println("✅ ECDSA 实现完成！");
        System.out.println();
        System.out.println("核心要点回顾:");
        System.out.println("  • 签名 = k⁻¹ × (z + r×d) mod n");
        System.out.println("  • 验签 = 重构 R 点，检查 R.x == r");
        System.out.println("  • 随机数 k 绝不能重复（PS3 破解教训）");
        System.out.println("  • Low-S 规范化防止签名可塑性攻击");
        System.out.println("══════════════════════════════════════════════════");
    }
}
