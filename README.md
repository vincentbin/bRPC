# bRPC
<details>
<summary><strong>README 中文版本</strong></summary>
<div>

## 一个基于netty的RPC框架

1. 基于netty NIO、IO多路复用。
2. client与server端建立心跳包保活机制。发生未知断连时，重连保证可靠长连接。
3. 使用kryo序列化，自定义传输包，及传输格式，避免TCP沾包问题。
4. 支持zookeeper或nacos做服务注册中心。
5. 可在注解中配置server端业务线程池核心线程数及最大线程数，客户端可通过注解自由选择接口对应负载均衡策略。
6. 可轻松整合SpringBoot进行使用。

## Getting Start

### 启动
#### API式启动
1. com.application.test.api.server.ServerTest.java  服务端启动
2. com.application.test.api.client.ClientTest.java  客户端启动 并rpc调用HelloService.hello()

#### 与SpringBoot整合启动（由于未发布到远程仓库，所以需本地maven install。）

1. 客户端
- maven
```xml
<dependency>
    <groupId>com.polyu</groupId>
    <artifactId>netty-client</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
- springboot
```java
@Component
public class Test {

    @BRpcConsumer(version = "1.0", loadBalanceStrategy = RpcLoadBalanceConsistentHash.class, timeOutLength = 1000L)
    private static HelloService helloService;

    public static void test() throws InterruptedException {
        String yyb = helloService.hello("yyb");
        System.out.println("yyb = " + yyb);
    }
}
```
- application.properties
```properties
bRPC.client.registry.type=zookeeper
bRPC.client.registry.address=127.0.0.1:2181
bRPC.client.registry.target.name=springbootApplication
bRPC.client.timeout.checkInterval=500
```
2. 服务端
- maven
```xml
<dependency>
    <groupId>com.polyu</groupId>
    <artifactId>netty-server</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
- springboot
```java
@BRpcProvider(value = HelloService.class, version = "1.0")
public class HelloServiceImpl implements HelloService {
    
    @Override
    public String hello(String name) throws InterruptedException {
        return name;
    }
}
```
- application.properties
```properties
bRPC.server.application.name=springbootApplication
bRPC.server.address=127.0.0.1:12277
bRPC.server.registry.type=zookeeper
bRPC.server.registry.address=127.0.0.1:2181
```
### 注册中心部署
#### Docker Zookeeper部署（例 同样支持nacos）

1. 拉取zk镜像&emsp;&emsp;&emsp;指令：docker pull zookeeper:3.4.14
2. 查看镜像id&emsp;&emsp;&emsp;指令：docker images
3. 拉起容器&emsp;&emsp;&emsp;&emsp;指令：docker run -d -p 2181:2181 --name b-zookeeper --restart always {imageId}
4. 查看容器id&emsp;&emsp;&emsp;指令：docker ps -a
5. 进入容器&emsp;&emsp;&emsp;&emsp;指令：docker exec -it {containerId} /bin/bash
6. 起注册中心&emsp;&emsp;&emsp;指令：./bin/zkCli.sh

</div>
</detail>

## A netty-based RPC framewor

1. Based on netty NIO, IO multiplexing.
2. The client and server establish a heartbeat packet keep-alive mechanism. When an unknown disconnection occurs, reconnection ensures a reliable long connection.
3. Use kryo serialization, customize the transmission package, and transmission format to avoid TCP packet contamination problems.
4. Support zookeeper or nacos as service registration center.
5. The number of core threads and the maximum number of threads in the server-side business thread pool can be configured in the annotations, and the client can freely choose the load balancing strategy corresponding to the interface through the annotations.
6. Can easily integrate SpringBoot for use.

## Getting Start

### Start
#### API startup
1. Start the com.application.test.api.server.ServerTest.java server
2. The com.application.test.api.client.ClientTest.java client starts and rpc calls HelloService.hello()

#### Start integrated with SpringBoot (Because it is not published to a remote warehouse, it needs local maven install.)

1. client
- maven
```xml
<dependency>
    <groupId>com.polyu</groupId>
    <artifactId>netty-client</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
- springboot
```java
@Component
public class Test {

    @BRpcConsumer(version = "1.0", loadBalanceStrategy = RpcLoadBalanceConsistentHash.class, timeOutLength = 1000L)
    private static HelloService helloService;

    public static void test() throws InterruptedException {
        String yyb = helloService.hello("yyb");
        System.out.println("yyb = " + yyb);
    }
}
```
- application.properties
```properties
bRPC.client.registry.type=zookeeper
bRPC.client.registry.address=127.0.0.1:2181
bRPC.client.registry.target.name=springbootApplication
bRPC.client.timeout.checkInterval=500
```
2. server
- maven
```xml
<dependency>
    <groupId>com.polyu</groupId>
    <artifactId>netty-server</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
- springboot
```java
@BRpcProvider(value = HelloService.class, version = "1.0")
public class HelloServiceImpl implements HelloService {
    
    @Override
    public String hello(String name) throws InterruptedException {
        return name;
    }
}
```
- application.properties
```properties
bRPC.server.application.name=springbootApplication
bRPC.server.address=127.0.0.1:12277
bRPC.server.registry.type=zookeeper
bRPC.server.registry.address=127.0.0.1:2181
```
### Registry deployment
#### Docker Zookeeper deployment (example also supports nacos)

1. docker pull zookeeper:3.4.14
2. docker images
3. docker run -d -p 2181:2181 --name b-zookeeper --restart always {imageId}
4. docker ps -a
5. docker exec -it {containerId} /bin/bash
6. ./bin/zkCli.sh
