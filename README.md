
---
### **`neutrino-tsp` 模块作用**

| 模块 | 职责定位               | 扮演角色                                        |
| :--- |:-------------------|:--------------------------------------------|
| **`tsp-common`** | **通用工具箱**          | 提供螺丝、螺母、标准化的接口和数据蓝图（POJO），所有其他模块都会用到它。      |
| **`tsp-vehicle-simulator`** | **车辆模拟器 (数据源)**    | 模拟成千上万台车，是整个车联网平台的“陪练”和“数据投喂机”。             |
| **`tsp-gateway`** | **云端接入网关 (平台大门)**  | 负责管理所有车辆的网络连接，像一个高效的“门卫”，接收车辆的上报数据，并向下传达指令。 |
| **`tsp-processor`** | **实时数据处理器 (智慧大脑)** | 对海量的车辆数据流进行实时分析，提取有价值的信息，做出判断（如告警）。         |
| **`tsp-application`** | **业务应用核心 (指挥中心)**  | 实现所有面向用户和后台的业务逻辑，连接数据库，并对外提供API服务。          |

---

### **编码路线图：从数据源到指挥中心**

遵循一个最符合逻辑、由点及面的开发顺序。

**总原则：先有数据 -> 再有通道 -> 最后有处理和应用。**

#### **第一阶段：编写“通用工具箱” (`tsp-common`)**
*   **编码顺序**:
    1.  **定义核心数据模型**: 创建`VehicleData.java`这个POJO。
    2.  **提供工具类**: 创建一个`JsonUtil.java`。

*   **编码功能逻辑**:
    1.  **`VehicleData.java`**:
        *   使用`@Data`和`@Builder` (来自Lombok)注解，简化代码。
        *   定义核心字段：`private String vin;` (车辆唯一标识), `private Long timestamp;` (数据上报时间戳), `private Double longitude;` (经度), `private Double latitude;` (纬度), `private Double speed;` (速度), `private Double batteryLevel;` (电池电量) 等。

    2.  **`JsonUtil.java`**:
        *   内部封装一个`ObjectMapper`实例 (来自Jackson库)。
        *   提供两个静态方法：
            *   `public static String toJson(Object obj)`: 将任意对象转换为JSON字符串。
            *   `public static <T> T fromJson(String json, Class<T> clazz)`: 将JSON字符串转换为指定类型的对象。

#### **第二阶段：编写“车辆模拟器” (`tsp-vehicle-simulator`)**

*   **编码顺序**:
    1.  创建`VehicleSimulator.java`类。
    2.  创建`SimulatorApp.java`主启动类。

*   **编码功能逻辑**:
    1.  **`VehicleSimulator.java`**:
        *   **初始化**: 在构造函数中，创建一个`MqttClient`实例 (来自Paho库)，并配置好服务器地址（例如`tcp://localhost:1883`）、客户端ID（可以用VIN码）。
        *   **连接**: 调用`mqttClient.connect()`连接到MQTT服务器（此时可以先假设有一个服务器存在，比如用Docker起一个EMQX）。
        *   **模拟数据上报**: 在`run()`方法中，写一个`while(true)`循环：
            *   创建一个`VehicleData`对象，并用`Random`类给经纬度、速度等字段填充随机的、看起来比较真实的数据。
            *   调用`JsonUtil.toJson()`将`VehicleData`对象转换为JSON字符串。
            *   创建一个`MqttMessage`对象，将JSON字符串作为其Payload。
            *   调用`mqttClient.publish("vehicle/data/raw", message)`将消息发布到指定的主题。
            *   `Thread.sleep(5000)`，比如每5秒上报一次。
    2.  **`SimulatorApp.java`**:
        *   在`main`方法中，可以创建一个`for`循环，启动10个`VehicleSimulator`线程，模拟10台车同时上报数据。

#### **第三阶段：编写“云端接入网关” (`tsp-gateway`)**

*   **编码顺序**:
    1.  创建`SessionManager.java`。
    2.  创建`MqttBrokerHandler.java`。
    3.  创建`MqttChannelInitializer.java`。
    4.  创建`GatewayServer.java`主启动类。

*   **编码功能逻辑**: (完全按照我们之前的设计)
    1.  **`GatewayServer.java`**: 用Netty的`ServerBootstrap`启动一个TCP服务器，监听`1883`端口。
    2.  **`MqttChannelInitializer.java`**: 在Pipeline中加入Netty官方的`MqttDecoder`, `MqttEncoder`和我们自己写的`MqttBrokerHandler`。
    3.  **`MqttBrokerHandler.java`**:
        *   处理`CONNECT`消息：做简单的认证，成功后将VIN和Channel的对应关系存入`SessionManager`。
        *   处理`PUBLISH`消息：获取Payload，**在这里，您只需要做一件事——把收到的原始字节数据，直接发送到Kafka**。
        *   处理`PINGREQ`和`DISCONNECT`消息。
    4.  **Kafka生产者**: 在`MqttBrokerHandler`中，注入一个`KafkaTemplate`或创建一个原生的`KafkaProducer`，用于向Kafka发送消息。

#### **第四阶段：编写“业务应用核心” (`tsp-application`)**
*   **编码顺序**:
    1.  创建`KafkaConsumerListener.java`。
    2.  创建对应的`Service`和`Repository`层。
    3.  创建`VehicleController.java`。

*   **编码功能逻辑**:
    1.  **`KafkaConsumerListener.java`**:
        *   写一个方法，并使用`@KafkaListener(topics = "vehicle-data-raw")`注解来监听我们之前在网关中指定的Kafka主题。
        *   **方法体**:
            *   接收到消息（一个JSON字符串）。
            *   调用`JsonUtil.fromJson()`将其转换为`VehicleData`对象。
            *   **现在，只做一件最简单的事：`log.info("接收到车辆数据: {}", vehicleData);`**。我们先确保数据链路是通的。
            *   后续再实现调用`Service`层将其存入数据库（比如Redis或InfluxDB）。
    2.  **Controller, Service, Repository**:
        *   可以先搭建好空的`Controller`, `Service`, `Repository`类和接口，为后续的API开发和数据库操作做好准备。

**`tsp-processor`模块可以放在最后，因为它是一个“增强”模块，不影响主链路的通畅。**