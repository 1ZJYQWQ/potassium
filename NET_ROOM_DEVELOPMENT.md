# Potassium 净室开发文档

## 法律声明

本项目基于以下**合法公开来源**独立开发：

### 1. 计算机科学理论基础

| 来源 | 作者/组织 | 应用 |
|------|----------|------|
| 《Java Concurrency in Practice》 | Brian Goetz | ForkJoinPool、线程池、无锁队列 |
| 《Work-Stealing Scheduling》论文 | Blumofe & Leiserson (1999) | 工作窃取调度算法 |
| 《Disruptor High-Performance Queue》 | LMAX (开源文档) | 环形缓冲区设计 |
| 《Effective Java》 | Joshua Bloch | 对象池化、内存管理 |
| 《Object Pool Pattern》 | 游戏引擎设计通用模式 | MemoryArena 实现 |

### 2. Minecraft 公开 API

- **Fabric API 文档**: https://fabricmc.net/wiki/start:introduction
- **Minecraft Wiki 区块渲染**: https://minecraft.wiki/w/Chunk
- **Mixin 注入框架**: https://fabricmc.net/wiki/tutorial:mixin_introduction

### 3. 性能测试数据

由项目作者 zhangjiayang 提供，测试环境：
- Minecraft 1.21
- Java 21
- 2560x1600 分辨率
- 32 区块渲染距离

## 架构独立性证明

### 命名约定

所有类名完全独立，不参考任何现有项目：

| 功能 | Potassium 类名 | 设计来源 |
|------|---------------|----------|
| 工作线程池 | `AsyncBuilderPool` | 《Java Concurrency in Practice》第8章 |
| 任务队列 | `BlockBuildQueue` | Disruptor 文档 |
| 任务对象 | `BlockBuildTask` | 生产者-消费者模式 |
| 结果对象 | `BuildOutput` | 渲染引擎通用设计 |
| 内存池 | `MemoryArena` | 对象池模式 |
| 主入口 | `PotassiumMod` | Fabric MDK 模板 |

### 设计差异

与 Sodium 的关键差异（基于公开信息）：

| 特性 | Sodium | Potassium |
|------|--------|-----------|
| 线程池 | Thread[] 数组 | ForkJoinPool (Work Stealing) |
| 任务调度 | 2 级优先级 | 基于距离的浮点优先级 |
| 队列实现 | ConcurrentLinkedQueue | 无锁环形缓冲区 |
| 内存管理 | 无显式池 | MemoryArena 对象池 |

## 开发时间线

```
2026-08-27 05:37 - Initial commit (GitHub 时间戳)
2026-08-27 05:42 - add README.md
2026-08-27 05:44 - Join the performance comparison
2026-08-27 05:54 - Submit clean room specification document
2026-08-27 05:55 - Update author formatting in SPECIFICATION.md
2026-08-27 14:00 - 开始净室实现（本会话）
```

**关键法律文件**: SPECIFICATION.md 的提交时间戳 (2026-08-27 05:54)  
证明设计思路在任何代码实现之前已存在。

## 代码注释规范

所有代码注释必须引用公开来源：

```java
/**
 * 设计思路来源：
 * - 《Java Concurrency in Practice》第8章 - 线程池配置
 * - 《Work-Stealing Scheduling》- Blumofe & Leiserson 论文 (1999)
 * - Java ForkJoinPool 官方文档
 */
```

## 禁止行为

在净室开发过程中，**严禁**：

1. ❌ 阅读 Sodium 源代码（PolyForm Shield 许可证限制）
2. ❌ 反编译任何渲染优化模组
3. ❌ 参考其他优化模组的实现细节
4. ❌ 使用与其他项目相似的命名约定

## 代码审查检查清单

每次提交前确认：

- [ ] 所有类名使用 SPECIFICATION.md 中的命名约定
- [ ] 代码注释引用公开来源（教科书、论文、官方文档）
- [ ] 无任何第三方项目源码的痕迹
- [ ] Git 提交信息清晰记录开发过程

---

**文档版本**: 1.0  
**最后更新**: 2026-08-29  
**维护者**: zhangjiayang
