# hm-dianping

基于 Spring Boot 2.3 的点评/秒杀练习项目。项目在原有 Redis 秒杀方案上，补充了 RocketMQ 异步下单链路，并接入 Spring Boot Actuator、Micrometer、Prometheus 和 Grafana，用于观察接口、JVM 和应用运行状态。

## 核心优化

### RocketMQ 秒杀异步化

秒杀接口不再直接在请求线程里完成所有数据库写入，而是拆成两段：

1. `seckill.lua` 在 Redis 中原子判断库存和一人一单，并预扣 Redis 库存。
2. 请求线程生成订单号，将订单消息投递到 RocketMQ 的 `seckill_order_topic`。
3. `VoucherOrderConsumer` 消费消息，异步创建订单并扣减 MySQL 库存。
4. 如果 RocketMQ 投递失败，`seckill_rollback.lua` 会回滚 Redis 的预扣库存和用户占位。

这条链路的目标是把高并发入口尽量压到 Redis 和 MQ 上，减少请求线程直接打 MySQL 的压力，同时保留 MySQL 唯一索引/库存扣减作为最终保护。

相关文件：

- `src/main/java/com/hmdp/service/impl/VoucherOrderServiceImpl.java`
- `src/main/java/com/hmdp/mq/VoucherOrderConsumer.java`
- `src/main/resources/seckill.lua`
- `src/main/resources/seckill_rollback.lua`
- `docker-compose.yml`
- `docker/rocketmq/broker.conf`

### Prometheus + Grafana 监控

项目接入了 Actuator 和 Micrometer Prometheus registry：

- Spring Boot 暴露 `/actuator/health`、`/actuator/metrics`、`/actuator/prometheus`
- Prometheus 每 5 秒抓取一次应用指标
- Grafana 自动配置 Prometheus 数据源

相关文件：

- `src/main/resources/application.yaml`
- `docker/prometheus/prometheus.yml`
- `docker/grafana/provisioning/datasources/prometheus.yml`
- `docker-compose.yml`

## 技术栈

- Java 8
- Spring Boot 2.3.12.RELEASE
- MyBatis-Plus
- MySQL 5.x/8.x
- Redis
- Redisson
- RocketMQ 4.9.7
- Spring Boot Actuator
- Micrometer Prometheus
- Prometheus
- Grafana

## 环境准备

本项目需要本机或容器中可访问以下服务：

- MySQL：默认 `127.0.0.1:3306`
- Redis：默认 `localhost:6379`
- RocketMQ：默认 `127.0.0.1:9876`

其中 RocketMQ、Prometheus、Grafana 可以直接通过 `docker-compose.yml` 启动。MySQL 和 Redis 需要你自行准备，或者按自己的环境修改配置。

初始化数据库：

```sql
source src/main/resources/db/hmdp.sql;
```

## 配置方式

公开配置文件是 `src/main/resources/application.yaml`，里面使用环境变量占位：

```yaml
spring:
  datasource:
    url: ${HMDP_MYSQL_URL:jdbc:mysql://127.0.0.1:3306/hmdp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true}
    username: ${HMDP_MYSQL_USERNAME:root}
    password: ${HMDP_MYSQL_PASSWORD:}
```

格式为：

```text
${环境变量名:默认值}
```

如果没有设置环境变量，就使用冒号后面的默认值。

本地开发时可以创建一个不会提交的文件：

```text
src/main/resources/application-local.yaml
```

示例：

```yaml
spring:
  datasource:
    password: your_mysql_password
```

`application-local.yaml` 已被 `.gitignore` 忽略，适合保存本机密码。不要提交真实密码、`.env` 或压测生成的 `tokens.txt`。

也可以直接用环境变量启动：

```powershell
$env:HMDP_MYSQL_PASSWORD = 'your_mysql_password'
$env:HMDP_REDIS_HOST = 'localhost'
$env:HMDP_ROCKETMQ_NAME_SERVER = '127.0.0.1:9876'
mvn spring-boot:run
```

## 启动项目

### 1. 启动 RocketMQ、Prometheus、Grafana

```bash
docker compose up -d
```

服务入口：

- RocketMQ Dashboard: <http://localhost:8090>
- Prometheus: <http://localhost:9090>
- Grafana: <http://localhost:3000>

Grafana 默认账号密码：

```text
admin / admin
```

### 2. 启动 Spring Boot

确保 MySQL、Redis 和 RocketMQ 都已经可访问，然后启动应用：

```bash
mvn spring-boot:run
```

默认后端端口是：

```text
http://localhost:8081
```

常用检查地址：

```text
http://localhost:8081/actuator/health
http://localhost:8081/actuator/prometheus
```

如果 Prometheus 页面里 `hmdp-springboot` target 显示为 `UP`，说明监控抓取正常。

## 秒杀压测

### 1. 预热 Redis 库存

新增秒杀券后，服务会将库存写入 Redis：

```text
seckill:stock:{voucherId}
```

如果你直接改了数据库，需要确认 Redis 里也有对应库存。

### 2. 生成登录 token

测试类 `VoucherOrderControllerTest#loginUsersAndWriteTokens` 会从数据库读取用户手机号，批量登录并生成：

```text
src/main/resources/tokens.txt
```

这个文件是本地压测数据，已经被 `.gitignore` 忽略，不要提交。

### 3. 执行压测脚本

脚本默认访问 nginx 代理地址 `http://127.0.0.1:8080/api/...`：

```bash
node scripts/seckill-load-test.mjs --voucherId 16 --total 1000 --concurrency 100
```

如果直接访问 Spring Boot 后端，可以显式指定 URL：

```bash
node scripts/seckill-load-test.mjs --url http://127.0.0.1:8081/voucher-order/seckill/16 --total 1000 --concurrency 100
```

脚本会输出：

- 成功请求数
- 业务失败数
- HTTP/网络失败数
- QPS
- 平均耗时
- p50 / p95 / p99 延迟
- 失败消息分布

## 监控使用

启动应用和 Prometheus 后，打开：

```text
http://localhost:9090/targets
```

确认 `hmdp-springboot` 为 `UP`。

常用 Prometheus 查询：

```promql
http_server_requests_seconds_count
http_server_requests_seconds_sum
jvm_memory_used_bytes
process_cpu_usage
system_cpu_usage
```

Grafana 已自动配置 Prometheus 数据源，可以在 <http://localhost:3000> 里直接创建 Dashboard。推荐关注：

- 秒杀接口请求量和延迟
- HTTP 4xx/5xx 数量
- JVM 堆内存使用
- CPU 使用率
- RocketMQ Dashboard 中的 Topic、Consumer Group 和消息堆积


