# Sui 区块链技术要点

**日期：** 2026-02-04  
**主题：** Sui 链的密码学、账户、共识等核心技术

---

## 一、密码学

### 1.1 支持的椭圆曲线

Sui 支持 **多种签名方案**（Multi-Scheme）：

| 曲线 | 签名算法 | Flag 前缀 | 说明 |
|------|---------|-----------|------|
| **Ed25519** | EdDSA | 0x00 | 默认，最常用 |
| **secp256k1** | ECDSA | 0x01 | 兼容比特币/以太坊 |
| **secp256r1** | ECDSA | 0x02 | 兼容 WebAuthn/Passkey |

### 1.2 为什么支持多种？

- **Ed25519**：性能最好，Sui 默认选择
- **secp256k1**：方便从以太坊/比特币迁移
- **secp256r1（P-256）**：支持硬件安全模块（HSM）、Apple/Google Passkey

### 1.3 对比

| | Ed25519 | secp256k1 | secp256r1 |
|---|---------|-----------|-----------|
| 签名大小 | 64 bytes | 64 bytes | 64 bytes |
| 公钥大小 | 32 bytes | 33 bytes | 33 bytes |
| 性能 | 最快 | 中等 | 中等 |
| 谁在用 | Solana, Sui | Bitcoin, Ethereum | 政府, WebAuthn |

---

## 二、账户类型

### 2.1 地址格式

Sui 地址是 **32 bytes**，显示为 64 个十六进制字符（带 0x 前缀）：

```
0x + Blake2b(flag(1 byte) + public_key)
```

例：`0x5a6e...3b2c`

### 2.2 账户类型

| 类型 | 说明 |
|------|------|
| **普通账户** | 单签名，由私钥控制 |
| **多签账户 (MultiSig)** | M-of-N 签名，支持混合不同曲线 |
| **zkLogin 账户** | 用 Google/Facebook/Apple 登录，零知识证明 |

### 2.3 zkLogin（重点创新）

用户用 OAuth 登录（Google/Apple/Facebook），无需管理私钥：

```
用户 Google 登录
      │
      ▼
 JWT Token + ZK Proof
      │
      ▼
  Sui 验证通过
      │
      ▼
  执行交易
```

私钥对用户"隐藏"在 ZK 证明里，降低入门门槛。

---

## 三、共识算法

### 3.1 架构：Narwhal + Bullshark

Sui 把共识拆成两层：

```
┌─────────────────────────────────────┐
│  Bullshark (排序层)                  │
│  - 对交易排序                        │
│  - DAG-based BFT                    │
└─────────────────────────────────────┘
              ▲
              │
┌─────────────────────────────────────┐
│  Narwhal (内存池/数据可用性层)        │
│  - 高吞吐量广播                      │
│  - 数据可用性保证                    │
└─────────────────────────────────────┘
```

### 3.2 Narwhal

- 类似"高速公路"，负责把交易数据广播给所有验证者
- 使用 **DAG（有向无环图）** 结构
- 保证数据可用性，但不决定顺序

### 3.3 Bullshark

- 类似"裁判"，决定交易的最终顺序
- 基于 DAG 的 BFT 共识
- 比传统 PBFT 更高效（减少通信轮次）

### 3.4 关键创新：简单交易走快速路径

```
简单交易（单 Owner）:
  ─────────────────────────────────
  不需要完整共识！
  只需要 2/3 验证者签名确认
  延迟: ~400ms
  
共享对象交易:
  ─────────────────────────────────
  需要完整 Narwhal + Bullshark 共识
  延迟: ~2-3s
```

这是 Sui 高性能的秘密：**大部分交易不走共识！**

---

## 四、对象模型（Object Model）

### 4.1 与以太坊的区别

| | 以太坊 | Sui |
|---|--------|-----|
| 数据模型 | 账户模型 | 对象模型 |
| 状态 | 全局状态树 | 独立对象 |
| 并行性 | 差（状态冲突） | 好（对象隔离） |

### 4.2 对象类型

```
Owned Object     - 属于某个地址，只有 owner 能操作
Shared Object    - 多人可操作，需要共识
Immutable Object - 不可变，任何人可读
```

### 4.3 为什么对象模型快？

```
交易 A: 操作 Object 1
交易 B: 操作 Object 2
         │
         ▼
   互不冲突，可以并行执行！
```

以太坊需要串行执行（因为共享全局状态），Sui 可以大规模并行。

### 4.4 Sui 有没有"全局状态"？

**有，但组织方式不同。**

| | 以太坊 | Sui |
|---|--------|-----|
| 状态结构 | 一棵大的 Merkle Patricia Trie | 对象的扁平集合 |
| 访问方式 | 通过账户地址 → 存储槽 | 通过对象 ID 直接访问 |
| 状态根 | 单一状态根哈希 | 每个对象有独立版本号 |

```
以太坊：
┌─────────────────────────────┐
│       Global State Root      │
│             │                │
│    ┌────────┼────────┐       │
│    ▼        ▼        ▼       │
│  Account  Account  Account   │
│    │        │        │       │
│  Storage  Storage  Storage   │
└─────────────────────────────┘

Sui：
┌─────────────────────────────┐
│  Object 1 (v5)               │
│  Object 2 (v3)               │
│  Object 3 (v12)              │
│  Object 4 (v1)               │
│  ...                         │
│  （每个对象独立，有版本号）    │
└─────────────────────────────┘
```

### 4.5 竞争问题怎么解决？

**关键区分：Owned vs Shared**

```
Owned Object（独占对象）
─────────────────────────
- 只有 owner 能操作
- 不存在竞争！
- 不需要共识，直接执行

Shared Object（共享对象）
─────────────────────────
- 多人可同时操作
- 存在竞争
- 必须通过共识排序
```

**Owned Object：无竞争**

```
Object A (owner: Alice)

交易 1 (from Alice): 修改 Object A  ✅ 直接执行
交易 2 (from Bob):   修改 Object A  ❌ 无权操作

不可能有两个人同时操作同一个 Owned Object！
```

这就是 Sui 快的原因：**大部分对象是 Owned，天然无竞争。**

**Shared Object：用共识解决**

当多个交易要操作同一个 Shared Object：

```
Shared Object X（比如一个 DEX 流动性池）

交易 1: Alice 想 swap
交易 2: Bob 想 swap
交易 3: Carol 想 swap
         │
         ▼
┌─────────────────────────────┐
│  Narwhal + Bullshark 共识    │
│  决定执行顺序：2 → 1 → 3     │
└─────────────────────────────┘
         │
         ▼
按顺序串行执行，保证一致性
```

### 4.6 版本号机制（乐观并发控制）

每个对象有版本号，类似数据库的乐观锁：

```
Object X: { data: ..., version: 5 }

交易提交时声明：我要操作 version=5 的 Object X

情况 1：成功
─────────────
执行时 Object X 还是 version 5
→ 执行成功，version 变成 6

情况 2：冲突
─────────────
执行时 Object X 已经是 version 6（被别人改了）
→ 交易失败，需要重试
```

### 4.7 并行执行的依赖分析

Sui 执行引擎会分析交易依赖：

```
交易 A: 操作 Object 1, Object 2
交易 B: 操作 Object 3
交易 C: 操作 Object 1, Object 4
交易 D: 操作 Object 5

依赖分析：
- A 和 C 有冲突（都用 Object 1）→ 串行
- B 和 D 无冲突 → 可以和任何交易并行

执行计划：
时间 ──────────────────────────────►
     │ A ├──────┤ C ├
     │ B ├──┤
     │ D ├────┤
```

### 4.8 DEX Swap 对比示例

**以太坊方式**

```solidity
// 所有人操作同一个合约的 storage
function swap(uint amount) {
    // 读全局状态
    uint reserve = reserves[tokenA];
    // 写全局状态
    reserves[tokenA] = reserve - amount;
}
```

问题：所有 swap 必须串行，因为都在改 `reserves`

**Sui 方式**

```move
// Pool 是一个 Shared Object
public fun swap(pool: &mut Pool, coin_in: Coin<A>): Coin<B> {
    // 操作 pool 对象
    let amount_out = calculate_output(pool, coin_in);
    // 修改 pool 状态
    pool.reserve_a = pool.reserve_a + coin_in.value;
    pool.reserve_b = pool.reserve_b - amount_out;
    // 返回新 Coin 对象
    mint<B>(amount_out)
}
```

竞争处理：
1. 多个 swap 交易进入 Narwhal
2. Bullshark 决定顺序
3. 按顺序执行，每次执行后 pool 版本号 +1

### 4.9 对象模型竞争问题总结

| 问题 | Sui 的解决方案 |
|------|---------------|
| Owned Object 竞争 | 不存在竞争（只有 owner 能操作） |
| Shared Object 竞争 | Narwhal + Bullshark 共识排序 |
| 并发检测 | 对象版本号（乐观锁） |
| 执行效率 | 分析依赖，无冲突的并行执行 |

**核心思想**：
- 把大部分操作设计成 Owned Object → 天然并行
- 只有真正需要共享的才用 Shared Object → 局部串行
- 比以太坊"全局串行"高效得多

---

## 五、Move 语言

### 5.1 特点

- **资源类型**：资产不能被复制或丢弃（除非显式销毁）
- **能力系统**：copy、drop、store、key 四种能力
- **形式验证友好**：编译时检查很多安全问题

### 5.2 与 Solidity 对比

| | Solidity | Move |
|---|----------|------|
| 资产安全 | 容易出错（整数溢出、重入） | 类型系统保证 |
| 学习曲线 | 较低 | 较高 |
| 并行友好 | 差 | 好 |

---

## 六、总结

| 方面 | Sui 的选择 |
|------|-----------|
| 签名曲线 | Ed25519（默认）+ secp256k1 + secp256r1 |
| 签名算法 | EdDSA / ECDSA |
| 地址长度 | 32 bytes |
| 账户类型 | 普通 / MultiSig / zkLogin |
| 共识 | Narwhal（数据层）+ Bullshark（排序层） |
| 数据模型 | Object Model |
| 智能合约 | Move 语言 |

---

## 七、与其他链对比

| | Bitcoin | Ethereum | Solana | Sui |
|---|---------|----------|--------|-----|
| 曲线 | secp256k1 | secp256k1 | Ed25519 | 多种 |
| 签名 | ECDSA | ECDSA | EdDSA | 多种 |
| 共识 | PoW→PoS | PoS (Gasper) | PoH + PoS | Narwhal+Bullshark |
| 数据模型 | UTXO | 账户 | 账户 | 对象 |
| 智能合约 | Script | Solidity | Rust | Move |
| TPS | ~7 | ~15-30 | ~65,000 | ~100,000+ |

---

## 参考资料

- [Sui 官方文档](https://docs.sui.io/)
- [Narwhal and Tusk 论文](https://arxiv.org/abs/2105.11827)
- [Bullshark 论文](https://arxiv.org/abs/2201.05677)
- [Move 语言文档](https://move-language.github.io/move/)
