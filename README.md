# Potassium - 极致的 Minecraft 客户端优化模组

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21-brightgreen.svg)](https://www.minecraft.net/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)

## 简介

Potassium 是一个专注于 **CPU 多线程优化** 和 **内存管理优化** 的 Minecraft 客户端渲染模组。

### 核心特性

- ✅ **并行区块构建** - 基于 ForkJoinPool 的工作窃取调度
- ✅ **无锁任务队列** - 环形缓冲区实现，避免锁竞争
- ✅ **内存池化** - 对象复用，减少 GC 压力
- ✅ **智能调度** - 基于距离的优先级队列

### 设计原则

```
原则 1: 主线程专注渲染提交
原则 2: 计算密集任务交给工作线程
原则 3: 避免锁竞争，使用无锁或单生产者单消费者模式
原则 4: 复用内存，减少对象分配
```

详见 [SPECIFICATION.md](SPECIFICATION.md) - **净室开发法律文档**

## 性能提升

*测试环境：Minecraft 1.21 / Java 21 / 2560x1600 / 32区块渲染距离*

| 指标 | 原版 | Potassium |
|:---|:---|:---|
| **平均 FPS** | 71 | **77** (+8.5%) |
| **CPU 主线程耗时** | 39.8ms | **30.4ms** (-24%) |
| **区块吞吐量** | 7205 | **8281** (+15%) |

## 法律声明

本项目基于以下合法来源独立开发：

1. **用户提供的性能测试报告** - 真实性能数据
2. **Minecraft 原版公开 API** - Fabric API 文档
3. **通用的计算机科学原理**:
   - 《Java Concurrency in Practice》- Brian Goetz
   - 《Disruptor High-Performance Queue》- LMAX 开源文档
   - 《Work-Stealing Scheduling》- Blumofe & Leiserson 论文 (1999)
   - 《Object Pool Pattern》- 游戏引擎设计通用模式

**本项目未参考任何特定渲染优化模组的源代码。**

所有架构设计均基于 SPECIFICATION.md 中的独立设计思路。

## 构建

```bash
./gradlew build
```

## 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。
