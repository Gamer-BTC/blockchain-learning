# 区块链顶级专家完整路线图

**开始日期：** 2026-02-03  
**目标：** 2年内成为区块链顶级技术专家

---

# 第一阶段：夯实底层（Week 1-12）

## 第1个月：密码学

### Week 1: 椭圆曲线基础 (2/3 - 2/9)
- [x] 周一：有限域数学基础（模运算、群论）
- [x] 周二：椭圆曲线点加法、标量乘法
- [x] 周三：secp256k1 参数学习
- [x] 周四：手推 ECDSA 签名流程
- [x] 周五：手推 ECDSA 验签流程
- [x] 周六：Java 实现简化版 ECDSA
- 📖 《Serious Cryptography》Ch 11-12

### Week 2: 多种签名方案 (2/10 - 2/16)
- [ ] 周一：ed25519 vs secp256k1 对比
- [ ] 周二：Schnorr 签名原理
- [ ] 周三：Schnorr 聚合签名特性
- [ ] 周四：BLS 签名数学基础（配对）
- [ ] 周五：BLS 在 ETH2 Beacon Chain 应用
- [ ] 周六：写笔记《三种签名方案对比》
- 📖 BIP-340、ETH2 BLS 规范

### Week 3: MPC/TSS 签名 (2/17 - 2/23)
- [ ] 周一：Shamir 秘密分享原理
- [ ] 周二：用 Python 实现 Shamir 分享
- [ ] 周三：TSS 门限签名概念
- [ ] 周四：GG18/GG20 协议框架
- [ ] 周五：分析 tss-lib 源码结构
- [ ] 周六：研究 Fireblocks/Zengo 白皮书
- 📖 GG20 论文

### Week 4: 零知识证明入门 (2/24 - 3/2)
- [ ] 周一：ZKP 三要素（完备、可靠、零知识）
- [ ] 周二：交互式 vs 非交互式证明
- [ ] 周三：zkSNARK 工作流程概览
- [ ] 周四：安装 circom，跑通 hello world
- [ ] 周五：写一个证明"知道原像"的电路
- [ ] 周六：整理《区块链密码学全景》笔记
- 📖 ZK 白板视频 1-5

**月度产出：**
- [ ] 笔记：《区块链密码学全景》
- [ ] 代码：Python ECDSA + circom 电路

---

## 第2个月：共识机制

### Week 5: PoW 深入 (3/3 - 3/9)
- [ ] 周一：重读比特币白皮书
- [ ] 周二：中本聪共识数学分析
- [ ] 周三：自私挖矿攻击原理
- [ ] 周四：51% 攻击成本计算
- [ ] 周五：难度调整算法（DAA）
- [ ] 周六：写笔记《PoW 安全性分析》
- 📖 selfish mining 论文

### Week 6: PoS 与最终性 (3/10 - 3/16)
- [ ] 周一：PoS 基本概念、质押机制
- [ ] 周二：Nothing-at-stake 问题
- [ ] 周三：Casper FFG 机制
- [ ] 周四：epoch/slot/attestation 概念
- [ ] 周五：Slashing 条件与惩罚
- [ ] 周六：对比 PoW vs PoS 安全假设
- 📖 Casper FFG 论文

### Week 7: BFT 共识家族 (3/17 - 3/23)
- [ ] 周一：拜占庭将军问题
- [ ] 周二：PBFT 三阶段协议
- [ ] 周三：手写 PBFT 消息流程图
- [ ] 周四：Tendermint 共识流程
- [ ] 周五：HotStuff 线性复杂度优化
- [ ] 周六：写笔记《BFT 共识演进》
- 📖 PBFT 论文、HotStuff 论文

### Week 8: 共识实践 (3/24 - 3/30)
- [ ] 周一：设计简化版 PBFT 架构
- [ ] 周二-周四：用 Go/Rust 实现（< 500行）
- [ ] 周五：分析 ETH Merge 案例
- [ ] 周六：对比 Solana PoH、Aptos DiemBFT
- 📖 Solana/Aptos 白皮书

**月度产出：**
- [ ] 代码：简化版 PBFT 实现
- [ ] 笔记：《共识机制演进》

---

## 第3个月：虚拟机与执行层

### Week 9: EVM 字节码 (3/31 - 4/6)
- [ ] 周一：EVM 架构概览（栈、内存、存储）
- [ ] 周二：熟记 15 个核心 opcode（PUSH/POP/ADD...）
- [ ] 周三：熟记 15 个核心 opcode（CALL/SSTORE/SLOAD...）
- [ ] 周四：Gas 计量模型
- [ ] 周五：手动解析一个简单合约字节码
- [ ] 周六：在 evm.codes 交互练习
- 📖 《Mastering Ethereum》Ch 13

### Week 10: EVM 实现源码 (4/7 - 4/13)
- [ ] 周一：revm 项目结构概览
- [ ] 周二：读 interpreter 主循环
- [ ] 周三：读 stack 实现
- [ ] 周四：读 memory/storage 实现
- [ ] 周五：尝试添加自定义 opcode
- [ ] 周六：写笔记《revm 源码分析》
- 📖 revm 源码

### Week 11: 其他 VM 架构 (4/14 - 4/20)
- [ ] 周一：Move VM 资源类型概念
- [ ] 周二：Move 能力系统
- [ ] 周三：Solana SVM Account 模型
- [ ] 周四：Solana 并行执行原理
- [ ] 周五：EVM vs Move vs SVM 对比
- [ ] 周六：写笔记《三大 VM 对比》
- 📖 Move Book、Solana 文档

### Week 12: 执行层前沿 (4/21 - 4/27)
- [ ] 周一：EOF 提案
- [ ] 周二：Verkle Tree 原理
- [ ] 周三：状态过期方案
- [ ] 周四：zkEVM 概念与挑战
- [ ] 周五：对比 Scroll/Polygon/zkSync
- [ ] 周六：阶段一总结
- 📖 EIP-3540、Vitalik zkEVM 文章

**月度产出：**
- [ ] 代码：合约反编译 + opcode 注释
- [ ] 笔记：《三大 VM 对比》

---

# 第二阶段：垂直深耕 MEV（Week 13-36）

### Week 13-14: Rust 速成
- [ ] Week 13：Rustlings 练习 1-50
- [ ] Week 14：Rustlings 51-100 + Rust Book 前 15 章

### Week 15-16: reth 入门
- [ ] Week 15：本地跑 reth 全节点，读项目结构
- [ ] Week 16：深入读 txpool 模块，写对比笔记

### Week 17-18: MEV 基础
- [ ] Week 17：MEV 概念、历史、分类
- [ ] Week 18：Flashbots 生态、MEV-boost 架构

### Week 19-20: PBS 深入
- [ ] Week 19：PBS 完整流程
- [ ] Week 20：本地搭建 MEV-boost 测试环境

### Week 21-22: Searcher 实战
- [ ] Week 21：设计 arbitrage searcher 架构
- [ ] Week 22：实现测试网 searcher

### Week 23-24: MEV 分析
- [ ] Week 23：用 eigenphi 分析真实 MEV 交易
- [ ] Week 24：研究 MEV 漏洞案例，写深度文章

### Week 25-28: 跨链 MEV
- [ ] Week 25：跨链 MEV 概念
- [ ] Week 26：跨链桥机制
- [ ] Week 27：轻客户端、ZK 桥原理
- [ ] Week 28：分析 LayerZero/Wormhole

### Week 29-32: DeFi 协议深入
- [ ] Week 29：AMM 数学
- [ ] Week 30：Uniswap V3 源码
- [ ] Week 31：借贷协议清算机制
- [ ] Week 32：永续合约/衍生品协议

### Week 33-36: 综合项目
- [ ] Week 33-34：开源 MEV 分析工具
- [ ] Week 35-36：写 3 篇深度技术文章

---

# 第三阶段：建立影响力（Week 37-52）

### Week 37-40: 开源贡献
- [ ] Week 37：阅读 reth/foundry contribution guide
- [ ] Week 38：提第一个 PR
- [ ] Week 39：持续贡献
- [ ] Week 40：尝试 feature PR

### Week 41-44: 内容输出
- [ ] Week 41：建立 Twitter 技术账号
- [ ] Week 42：Mirror/Substack 发表长文
- [ ] Week 43：每周 2-3 条技术 Twitter
- [ ] Week 44：在 ETH Research 发帖

### Week 45-48: 社区参与
- [ ] Week 45：参加 Code4rena 审计
- [ ] Week 46：参加 ETH Global 黑客松
- [ ] Week 47：加入技术社群
- [ ] Week 48：建立人脉

### Week 49-52: 分享与演讲
- [ ] Week 49：准备技术分享
- [ ] Week 50：社区分享
- [ ] Week 51：整理一年成果
- [ ] Week 52：规划下一年

---

# 第四阶段：持续精进（Week 53+）

### 月度节奏
- 1 篇深度技术文章
- 2-3 个开源 PR
- 1 次社区分享
- 跟踪 1 个前沿话题

### 里程碑检查

| 时间点 | 检查项 |
|--------|--------|
| 3个月 | 能解释密码学/共识/VM 核心原理 |
| 6个月 | Rust 熟练，能读懂客户端源码 |
| 9个月 | 有自己的开源项目 |
| 12个月 | 有稳定内容输出，初步影响力 |
| 18个月 | 在子领域有声誉 |
| 24个月 | 顶级专家水平 |

---

## 学习资源

**密码学**
- 《Serious Cryptography》
- ZK Whiteboard Sessions (YouTube)

**共识**
- PBFT 论文：pmg.csail.mit.edu/papers/osdi99.pdf
- HotStuff：arxiv.org/abs/1803.05069

**EVM**
- evm.codes
- revm：github.com/bluealloy/revm

---

## 进度记录

| 周 | 日期 | 完成情况 | 笔记 |
|---|------|---------|------|
| Week 1 | 2/3-2/9 | | |
| Week 2 | 2/10-2/16 | | |
| Week 3 | 2/17-2/23 | | |
| Week 4 | 2/24-3/2 | | |
| Week 5 | 3/3-3/9 | | |
| Week 6 | 3/10-3/16 | | |
| Week 7 | 3/17-3/23 | | |
| Week 8 | 3/24-3/30 | | |
| Week 9 | 3/31-4/6 | | |
| Week 10 | 4/7-4/13 | | |
| Week 11 | 4/14-4/20 | | |
| Week 12 | 4/21-4/27 | | |
