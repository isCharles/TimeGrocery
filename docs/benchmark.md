# 压测记录

本文档用于记录商户详情接口和限时权益秒杀链路的压测结果。当前数据均为占位，提交简历或答辩材料前请替换为真实压测结果。

## 环境信息

| 项目 | 内容 |
| --- | --- |
| 测试日期 | yyyy-MM-dd |
| 机器配置 | CPU / 内存 / 系统 |
| JDK | Java 8 |
| Spring Boot | 2.3.x |
| MySQL | xxx |
| Redis | xxx |
| RocketMQ | xxx |
| 压测工具 | JMeter / Node.js |

## 商户详情接口

测试接口：

```text
GET /shop/{id}
```

压测命令示例：

```bash
node scripts/shop-cache-benchmark.mjs --shopId 1 --total 1000 --concurrency 100
```

| 指标 | 改造前 | 改造后 |
| --- | --- | --- |
| QPS | xxx | xxx |
| 平均响应时间 | xxx ms | xxx ms |
| p95 | xxx ms | xxx ms |
| p99 | xxx ms | xxx ms |
| Caffeine 命中率 | - | xxx% |
| Redis 命中率 | xxx% | xxx% |

结论占位：

```text
商户详情接口改造后 QPS 从 xxx 提升至 xxx，提升约 26.1%。
```

## 秒杀链路

测试接口：

```text
POST /voucher-order/seckill/{voucherId}
```

压测命令示例：

```bash
node scripts/seckill-load-test.mjs --voucherId 16 --total 1000 --concurrency 100
```

| 指标 | 结果 |
| --- | --- |
| 总请求数 | xxx |
| 并发数 | xxx |
| 成功下单数 | xxx |
| 业务失败数 | xxx |
| HTTP/网络失败数 | xxx |
| 平均响应时间 | xxx ms |
| p95 | xxx ms |
| p99 | xxx ms |
| 超卖 | 0 |
| 重复下单 | 0 |

## 记录原则

- 不填写没有实际压测依据的数据。
- 保留原始压测命令、测试时间和关键配置。
- 多次测试时记录平均值和波动范围。
- 如果出现网络失败、连接拒绝或 404，先确认端口、Nginx 代理路径和后端服务状态。
