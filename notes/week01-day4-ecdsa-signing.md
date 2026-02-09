# Week 1 Day 4: ECDSA 签名流程

## 概述

ECDSA（Elliptic Curve Digital Signature Algorithm）是基于椭圆曲线的数字签名算法，广泛应用于比特币、以太坊等区块链系统。

## 签名流程

### 输入
- **私钥 d**：一个大整数，满足 1 < d < n
- **消息哈希 z**：对原始消息 m 进行哈希得到的值，z = hash(m)

### 曲线参数（以 secp256k1 为例）
- **G**：基点（生成元）
- **n**：基点的阶（曲线上点的数量）
- **Q = d·G**：公钥

### 签名步骤

```
1. 选取随机数 k，满足 1 < k < n
2. 计算点 R = k·G
3. 取 r = R.x mod n（R 点的 x 坐标）
   - 如果 r = 0，回到步骤 1 重新选择 k
4. 计算 s = k⁻¹ · (z + r·d) mod n
   - k⁻¹ 是 k 在模 n 下的逆元
   - 如果 s = 0，回到步骤 1 重新选择 k
5. 输出签名 (r, s)
```

### 数学推导图示

```
消息 m
   │
   ▼
z = hash(m)
   │
   ├──────────────────────┐
   │                      │
   ▼                      ▼
随机数 k               私钥 d
   │                      │
   ▼                      │
R = k·G                   │
   │                      │
   ▼                      │
r = R.x mod n             │
   │                      │
   └──────┬───────────────┘
          │
          ▼
s = k⁻¹ · (z + r·d) mod n
          │
          ▼
     签名 (r, s)
```

## 关键概念

### 1. 模逆元 k⁻¹

模逆元满足：k · k⁻¹ ≡ 1 (mod n)

计算方法：**扩展欧几里得算法**

```
扩展欧几里得算法求 a⁻¹ mod n：

设 gcd(a, n) = 1（互素）
则存在 x, y 使得 ax + ny = 1
因此 ax ≡ 1 (mod n)
所以 x 就是 a⁻¹ mod n
```

### 2. 随机数 k 的重要性

**k 必须是真随机且不能重复使用！**

如果攻击者知道两个签名使用了相同的 k：
- 签名1：s₁ = k⁻¹(z₁ + r·d)
- 签名2：s₂ = k⁻¹(z₂ + r·d)

则：
```
s₁ - s₂ = k⁻¹(z₁ - z₂)
k = (z₁ - z₂) / (s₁ - s₂)
d = (s₁·k - z₁) / r
```

**私钥直接泄露！**

#### 真实案例：PlayStation 3 破解（2010）

索尼在 PS3 的签名中使用了固定的 k 值，被黑客发现后私钥被计算出来，导致：
- 任意游戏可以被签名运行
- 盗版泛滥
- 安全系统完全崩溃

### 3. 为什么签名包含 (r, s) 两部分？

- **r**：证明签名者选择了特定的随机点 R
- **s**：将私钥、消息和随机数绑定在一起

验证时可以通过 r, s, z 和公钥 Q 恢复出点 R，验证签名的有效性。

## 手推示例

假设使用简化参数（实际中数字巨大）：

```
曲线参数：
n = 17（曲线阶）
G = (某个基点)

签名过程：
私钥 d = 7
消息哈希 z = 10
随机数 k = 5

步骤1：计算 R = 5·G，假设 R.x = 3
步骤2：r = 3 mod 17 = 3
步骤3：求 k⁻¹ = 5⁻¹ mod 17
       5 × 7 = 35 = 2×17 + 1 ≡ 1 (mod 17)
       所以 k⁻¹ = 7
步骤4：s = 7 × (10 + 3×7) mod 17
         = 7 × (10 + 21) mod 17
         = 7 × 31 mod 17
         = 217 mod 17
         = 13

签名结果：(r, s) = (3, 13)
```

## 签名格式

### DER 编码

比特币和以太坊中，签名通常使用 DER（Distinguished Encoding Rules）格式：

```
30 <总长度>
   02 <r长度> <r值>
   02 <s长度> <s值>
```

### 以太坊的 (v, r, s)

以太坊签名多了一个 **v** 值（recovery id），用于从签名恢复公钥：
- v = 27 或 28（传统）
- v = 0 或 1（EIP-155 之后根据 chainId 计算）

## 代码实现要点

```java
// 伪代码
public Signature sign(BigInteger privateKey, byte[] messageHash) {
    BigInteger z = new BigInteger(1, messageHash);
    BigInteger r, s;
    
    do {
        // 1. 生成安全随机数 k
        BigInteger k = generateSecureRandom(n);
        
        // 2. 计算 R = k·G
        ECPoint R = G.multiply(k);
        
        // 3. r = R.x mod n
        r = R.getX().mod(n);
        
        if (r.equals(BigInteger.ZERO)) continue;
        
        // 4. s = k⁻¹ · (z + r·d) mod n
        BigInteger kInv = k.modInverse(n);
        s = kInv.multiply(z.add(r.multiply(privateKey))).mod(n);
        
    } while (s.equals(BigInteger.ZERO));
    
    return new Signature(r, s);
}
```

## 安全注意事项

1. **使用密码学安全的随机数生成器**（如 `SecureRandom`）
2. **永远不要重复使用 k**
3. **考虑使用 RFC 6979** 确定性 k 生成（基于私钥和消息哈希派生 k）
4. **Low-S 规范化**：确保 s < n/2，防止签名可塑性攻击

## 参考资料

- [SEC 1: Elliptic Curve Cryptography](https://www.secg.org/sec1-v2.pdf)
- [RFC 6979: Deterministic DSA and ECDSA](https://tools.ietf.org/html/rfc6979)
- [Bitcoin Wiki: Elliptic Curve Digital Signature Algorithm](https://en.bitcoin.it/wiki/Elliptic_Curve_Digital_Signature_Algorithm)
