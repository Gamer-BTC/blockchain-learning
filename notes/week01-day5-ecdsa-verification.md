# Week 1 Day 5: ECDSA 验签流程

## 概述

验签是签名的逆过程，任何人都可以用公钥验证签名的有效性，而无需知道私钥。

## 验签流程

### 输入
- **公钥 Q**：Q = d·G（私钥 d 对应的公钥）
- **消息哈希 z**：z = hash(m)
- **签名 (r, s)**：待验证的签名

### 验签步骤

```
1. 检查 r, s ∈ [1, n-1]，否则签名无效
2. 计算 w = s⁻¹ mod n
3. 计算 u₁ = z · w mod n
4. 计算 u₂ = r · w mod n
5. 计算点 P = u₁·G + u₂·Q
6. 如果 P = O（无穷远点），签名无效
7. 检查 P.x mod n == r
   - 相等：签名有效 ✅
   - 不等：签名无效 ❌
```

### 流程图示

```
输入：公钥 Q, 消息哈希 z, 签名 (r, s)
          │
          ▼
    ┌─────────────┐
    │ r,s ∈[1,n-1]│ ──否──▶ 无效
    └─────┬───────┘
          │是
          ▼
    w = s⁻¹ mod n
          │
    ┌─────┴─────┐
    │           │
    ▼           ▼
u₁ = z·w     u₂ = r·w
    │           │
    └─────┬─────┘
          │
          ▼
    P = u₁·G + u₂·Q
          │
          ▼
    ┌─────────────┐
    │   P = O ?   │ ──是──▶ 无效
    └─────┬───────┘
          │否
          ▼
    ┌─────────────┐
    │P.x mod n == r│
    └─────┬───────┘
          │
    ┌─────┴─────┐
    是          否
    │           │
    ▼           ▼
  有效 ✅     无效 ❌
```

## 数学证明：为什么验签能工作？

这是 ECDSA 的精髓，让我们手推证明：

### 目标
证明：如果签名 (r, s) 是用私钥 d 对消息哈希 z 的有效签名，那么 P.x mod n == r

### 推导

**已知签名公式：**
```
s = k⁻¹ · (z + r·d) mod n
```

**变形得到 k：**
```
s·k = z + r·d (mod n)
k = s⁻¹·(z + r·d) (mod n)
k = s⁻¹·z + s⁻¹·r·d (mod n)
k = w·z + w·r·d (mod n)      // 因为 w = s⁻¹
k = u₁ + u₂·d (mod n)         // 因为 u₁ = z·w, u₂ = r·w
```

**由于 R = k·G：**
```
R = k·G
  = (u₁ + u₂·d)·G
  = u₁·G + u₂·d·G
  = u₁·G + u₂·Q               // 因为 Q = d·G
  = P
```

**因此：**
```
P = R
P.x = R.x = r ✅
```

**证毕！**

### 直观理解

验签本质上是在重构签名时使用的随机点 R：
- 签名者用 k 计算 R = k·G
- 验签者用 u₁, u₂ 计算 P = u₁·G + u₂·Q
- 如果签名有效，P 就等于 R

巧妙之处在于：
- 验签者不知道 k
- 但通过 s（包含了 k 的信息）和公钥 Q，可以重构出 R

## 手推示例

延续 Day 4 的例子：

```
已知：
n = 17
公钥 Q = 7·G
消息哈希 z = 10
签名 (r, s) = (3, 13)

验签步骤：

1. 检查 r=3, s=13 ∈ [1, 16] ✅

2. 计算 w = s⁻¹ = 13⁻¹ mod 17
   13 × 4 = 52 = 3×17 + 1 ≡ 1 (mod 17)
   所以 w = 4

3. 计算 u₁ = z × w mod 17
   u₁ = 10 × 4 mod 17 = 40 mod 17 = 6

4. 计算 u₂ = r × w mod 17
   u₂ = 3 × 4 mod 17 = 12

5. 计算 P = u₁·G + u₂·Q = 6·G + 12·Q

   由于 Q = 7·G：
   P = 6·G + 12·(7·G)
     = 6·G + 84·G
     = 90·G
     = 90 mod 17 · G    // 因为 n = 17
     = 5·G

   这正是签名时 k·G = 5·G = R！

6. P.x = R.x = 3 = r ✅

验签通过！
```

## 代码实现

```java
public boolean verify(ECPoint publicKey, byte[] messageHash, Signature sig) {
    BigInteger r = sig.getR();
    BigInteger s = sig.getS();
    BigInteger z = new BigInteger(1, messageHash);
    
    // 1. 检查 r, s 范围
    if (r.compareTo(BigInteger.ONE) < 0 || r.compareTo(n) >= 0) {
        return false;
    }
    if (s.compareTo(BigInteger.ONE) < 0 || s.compareTo(n) >= 0) {
        return false;
    }
    
    // 2. 计算 w = s⁻¹ mod n
    BigInteger w = s.modInverse(n);
    
    // 3. 计算 u₁ = z·w mod n
    BigInteger u1 = z.multiply(w).mod(n);
    
    // 4. 计算 u₂ = r·w mod n
    BigInteger u2 = r.multiply(w).mod(n);
    
    // 5. 计算 P = u₁·G + u₂·Q
    ECPoint P = G.multiply(u1).add(publicKey.multiply(u2));
    
    // 6. 检查 P 是否为无穷远点
    if (P.isInfinity()) {
        return false;
    }
    
    // 7. 验证 P.x mod n == r
    return P.getX().mod(n).equals(r);
}
```

## 公钥恢复

以太坊的一个特殊功能：可以从签名 (r, s, v) 恢复公钥！

### 恢复原理

已知 r 是点 R 的 x 坐标，可以计算出 R（可能有两个候选点，v 指示选哪个）

```
R.x = r
R.y = ±√(r³ + 7) mod p    // secp256k1: y² = x³ + 7
```

然后计算公钥：
```
Q = r⁻¹ · (s·R - z·G)
```

### 为什么这有用？

- 节省存储：交易不需要包含完整公钥（33 字节）
- 以太坊地址就是从公钥派生的，可以从签名验证发送者

## 签名可塑性问题

### 问题

对于有效签名 (r, s)，(r, n-s) 也是有效签名！

因为：
```
(-s)⁻¹ = -(s⁻¹)
u₁' = z·(-w) = -u₁
u₂' = r·(-w) = -u₂
P' = (-u₁)·G + (-u₂)·Q = -(u₁·G + u₂·Q) = -P
```

-P 和 P 的 x 坐标相同，所以验签也通过！

### 解决方案：Low-S

规定 s 必须 ≤ n/2，如果 s > n/2，用 n-s 替换。

比特币和以太坊都采用了这个规范。

## 验签复杂度

- 主要开销：2 次点乘法 + 1 次点加法
- 点乘法是 O(log n) 次点加法
- 总体是椭圆曲线上的多标量乘法（multi-scalar multiplication）

优化技术：
- **Shamir's Trick**：同时计算 u₁·G + u₂·Q，比分开算快约 1.5 倍
- **预计算表**：对 G 预计算倍点表

## 总结对比

| 操作 | 输入 | 输出 | 复杂度 |
|------|------|------|--------|
| 签名 | 私钥 d, 消息哈希 z | (r, s) | 1次点乘 |
| 验签 | 公钥 Q, 消息哈希 z, (r, s) | true/false | 2次点乘 |

## 参考资料

- [ECDSA Wikipedia](https://en.wikipedia.org/wiki/Elliptic_Curve_Digital_Signature_Algorithm)
- [Ethereum Yellow Paper - Appendix F](https://ethereum.github.io/yellowpaper/paper.pdf)
- [BIP-66: Strict DER Signatures](https://github.com/bitcoin/bips/blob/master/bip-0066.mediawiki)
