
---

### 所需中间件清单

#### 1. 消息队列 (Message Queue)

*   **中间件**: **Kafka & ZooKeeper**
*   **作用**:
    *   作为整个平台的数据总线和“蓄水池”。
    *   **解耦**：`tsp-gateway`只管把数据扔进Kafka，`tsp-application`和`tsp-processor`只管从Kafka里取水喝，三者互不直接依赖。
    *   **削峰填谷**：应对车辆数据上报的瞬时高峰，保护后端应用不被冲垮。
*   **必须性**: **核心必备**。

#### 2. 数据库 (Databases)


*   **A. 关系型数据库**: **MySQL** (版本 8.0+)
    *   **作用**: 存储结构化的、低频变的“主数据”，例如：车辆基本信息、用户信息、设备与用户的绑定关系等。
    *   **必须性**: **核心必备**。

*   **B. 时序数据库 (Time-Series Database)**: **InfluxDB**
    *   **作用**: 高效地存储和查询带有时间戳的“时序数据”，主要是**车辆的历史行驶轨迹**。它在这方面的性能远超MySQL。
    *   **必须性**: **强烈推荐**。这是展现您架构选型能力的亮点。

*   **C. 内存数据库/缓存**: **Redis**
    *   **作用**:
        *   **缓存**: 缓存热点数据，如车辆的最新实时状态，减少对主数据库的访问压力。
        *   **分布式Session**: 在`tsp-gateway`集群化部署时，可以用Redis来存储所有车辆的连接会话信息。
    *   **必须性**: **强烈推荐**。

#### 3. 实时计算引擎 (Stream Processing Engine)

*   **中间件**: **Flink**
*   **作用**: 支撑`tsp-processor`模块，对实时数据流进行窗口计算、状态管理、事件检测等。
*   **必须性**: 在项目初期可以**选装**。您可以先把`gateway -> kafka -> application`这条主链路跑通，再来搭建Flink环境，实现更高级的实时分析功能。

#### 4. MQTT Broker (用于模拟器连接)

*   **中间件**: **EMQX**
*   **作用**: 在您开发`tsp-gateway`的初期，或者您暂时不想自己实现Broker时，`tsp-vehicle-simulator`需要一个稳定可靠的MQTT服务器来连接和发送数据。EMQX是业界最流行的开源MQTT Broker。
*   **必须性**: **强烈推荐**在开发初期使用，便于测试您的模拟器和后端消费者。当您的`tsp-gateway`开发完成后，就可以用它来替换EMQX。

---

### “一键部署”方案：`docker-compose.yml`

**操作步骤：**

1.  **安装Docker Desktop**: 确保您的电脑上已安装并运行。
2.  **创建文件**: 在`neutrino-tsp`项目的根目录下，创建一个名为`docker-compose.yml`的文件。
3.  **粘贴内容**: 将以下内容完整地复制到`docker-compose.yml`中。

```yaml
version: '3.8'

services:
  # Kafka (KRaft 单节点模式)
  kafka:
    image: crpi-5kio3kylkw6w7p01-vpc.cn-hangzhou.personal.cr.aliyuncs.com/neutrino-tsp/cp-kafka:7.5.3
    container_name: kafka
    ports:
      - "9092:9092"
    mem_limit: 1g
    environment:
      KAFKA_HEAP_OPTS: "-Xms512m -Xmx512m"
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: 'broker,controller'
      KAFKA_CONTROLLER_QUORUM_VOTERS: '1@kafka:9093'
      KAFKA_CONTROLLER_LISTENER_NAMES: 'CONTROLLER'
      KAFKA_LISTENERS: 'PLAINTEXT://kafka:29092,CONTROLLER://kafka:9093,PLAINTEXT_HOST://0.0.0.0:9092'
      KAFKA_ADVERTISED_LISTENERS: 'PLAINTEXT://kafka:29092,PLAINTEXT_HOST://47.99.70.238:9092'
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: 'CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT'
      KAFKA_INTER_BROKER_LISTENER_NAME: 'PLAINTEXT'
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      CLUSTER_ID: 'MkU3OEVBNTcwNTJENDM2Qk'
      KAFKA_LOG_DIRS: '/tmp/kafka-logs'

  # MySQL (内存优化版)
  mysql:
    image: crpi-5kio3kylkw6w7p01-vpc.cn-hangzhou.personal.cr.aliyuncs.com/neutrino-tsp/mysql:8.0
    container_name: mysql
    mem_limit: 1g
    ports:
      - "3306:3306"
    command: --default-authentication-plugin=mysql_native_password --innodb_buffer_pool_size=256M --performance_schema=0
    environment:
      MYSQL_ROOT_PASSWORD: LZ5UM0NiAODXGiY0R6K4 
      MYSQL_DATABASE: neutrino_tsp
    volumes:
      - mysql-data:/var/lib/mysql

  # Redis (内存优化版)
  redis:
    image: crpi-5kio3kylkw6w7p01-vpc.cn-hangzhou.personal.cr.aliyuncs.com/neutrino-tsp/redis:6.2-alpine
    container_name: redis
    command: redis-server --requirepass LZ5UM0NiAODXGiY0R6K4
    mem_limit: 256m
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data

  # InfluxDB (内存优化版)
  influxdb:
    image: crpi-5kio3kylkw6w7p01-vpc.cn-hangzhou.personal.cr.aliyuncs.com/neutrino-tsp/influxdb:2.7.7-alpine
    container_name: influxdb
    mem_limit: 512m
    ports:
      - "8086:8086"
    volumes:
      - influxdb-data:/var/lib/influxdb2
    environment:
      - DOCKER_INFLUXDB_INIT_MODE=setup
      - DOCKER_INFLUXDB_INIT_USERNAME=admin
      - DOCKER_INFLUXDB_INIT_PASSWORD=admin_password
      - DOCKER_INFLUXDB_INIT_ORG=neutrino
      - DOCKER_INFLUXDB_INIT_BUCKET=tsp_data
      - DOCKER_INFLUXDB_INIT_ADMIN_TOKEN=my_super_secret_token

  # EMQX (内存优化版)
  emqx:
    image: crpi-5kio3kylkw6w7p01-vpc.cn-hangzhou.personal.cr.aliyuncs.com/neutrino-tsp/emqx:5.8.2
    container_name: emqx
    mem_limit: 512m
    ports:
      - "1883:1883"
      - "18083:18083"
    environment:
      # EMQX 5.8.2 使用的是 5.x 系列的配置方式
      EMQX_AUTH__MNESIA__PASSWORD_HASH: salted_sha256

volumes:
  mysql-data:
  redis-data:
  influxdb-data:

```

4.  **启动环境**:
    *   打开命令行/终端，`cd`到`neutrino-tsp`项目根目录。
    *   执行命令：
        ```bash
        docker-compose up -d
        ```
    *   Docker会开始自动下载镜像并按顺序启动所有容器。`-d`参数表示在后台运行。

5.  **检查状态**:
    *   稍等片刻，执行`docker-compose ps`，您应该能看到所有服务（zookeeper, kafka, mysql, redis, influxdb, emqx）都处于`Up`或`running`状态。

**现在，您本地就拥有了支撑整个项目运行的全套、隔离的中间件环境！** 您的Java应用可以直接通过`localhost`和对应的端口（如`localhost:9092` for Kafka, `localhost:3306` for MySQL）来连接它们。