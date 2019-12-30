# springcloudl-seata
基于阿里seata实现分布式事务
# 整合Eureka环境搭建
## server 端

**步骤一：下载启动包**


**步骤二：建表**

全局事务会话信息由3块内容构成，全局事务-->分支事务-->全局锁，对应表global_table、branch_table、lock_table

**步骤三：修改配置文件**

seata-->conf-->file.conf，修改store.mode="db" 

源码: 根目录-->seata-server-->resources-->file.conf，修改store.mode="db"

动包: seata-->conf-->file.conf，修改store.db相关属性。

源码: 根目录-->seata-server-->resources-->file.conf，修改store.db相关属性。

register.conf

![517e3f799c5b0cb01cbd2a88a6089ad8.png](en-resource://database/40015:1)

file.conf

事务分组配置:

![08496338fce5da7fab353bfb27d204f6.png](en-resource://database/40021:1)

db 作为数据源:

![4802504d07e1f2f3a6f0e75c6e22552c.png](en-resource://database/40017:1)

运行: bin目录下面的 seata-server.bat 文件 观察Eureka中有没有注册过来的服务。

![e4532b3b73082d4038c8153526b06e97.png](en-resource://database/40019:1)

# Client 端

**步骤一：添加seata依赖**

```java
<!--seata-->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
</dependency>
```

**步骤二：undo_log建表、配置参数**

```sql
CREATE TABLE `undo_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) NOT NULL,
  `xid` varchar(100) NOT NULL,
  `context` varchar(128) NOT NULL,
  `rollback_info` longblob NOT NULL,
  `log_status` int(11) NOT NULL,
  `log_created` datetime NOT NULL,
  `log_modified` datetime NOT NULL,
  `ext` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

**步骤三：数据源代理**

手动配置:

```java
@Configuration
public class DataSourceProxyAutoConfiguration {

    private DataSourceProperties dataSourceProperties;

    public DataSourceProxyAutoConfiguration(DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    @Primary
    @Bean("dataSource")
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(dataSourceProperties.getUrl());
        dataSource.setUsername(dataSourceProperties.getUsername());
        dataSource.setPassword(dataSourceProperties.getPassword());
        dataSource.setDriverClassName(dataSourceProperties.getDriverClassName());
        return new DataSourceProxy(dataSource);
    }

}
```
**步骤四: yml 配置**
```yml
spring:
  application:
    name: spring-cloud-stock-8083
  cloud:
    alibaba:
      seata:
        tx-service-group: minbox-seata  # 自定义事务分组名，需要与配置文件中的对应
```

**步骤五: 业务上注解 @GlobalTransactional**
```java
@GlobalTransactional(name = "create-order",rollbackFor = RuntimeException.class)
@Transactional(rollbackFor = RuntimeException.class)
public void update() {

    Stock stock = stockMapper.selectByPrimaryKey(1);

    stock.setStock( stock.getStock() - 1 );

    stockMapper.updateByPrimaryKey(stock);

    }
}
```

**步骤六：初始化GlobalTransactionScanner**

引入了 spring-cloud-alibaba-seata 会自动初始化。

**步骤七：配置注册中心和配置中心**

把 **file.conf  register.conf** 分别放在 每个项目的 resources 资源目录下面。

FileConfiguration类会自动加载配置参数

register.conf

```conf
registry {

  type = "file"

  file {
    name = "file.conf"
  }

}

config {
  type = "file"

  file {
    name = "file.conf"
  }
}
```

file.conf 

```conf
transport {
  # tcp udt unix-domain-socket
  type = "TCP"
  #NIO NATIVE
  server = "NIO"
  #enable heartbeat
  heartbeat = true
  #thread factory for netty
  thread-factory {
    boss-thread-prefix = "NettyBoss"
    worker-thread-prefix = "NettyServerNIOWorker"
    server-executor-thread-prefix = "NettyServerBizHandler"
    share-boss-worker = false
    client-selector-thread-prefix = "NettyClientSelector"
    client-selector-thread-size = 1
    client-worker-thread-prefix = "NettyClientWorkerThread"
    # netty boss thread size,will not be used for UDT
    boss-thread-size = 1
    #auto default pin or 8
    worker-thread-size = 8
  }
  shutdown {
    # when destroy server, wait seconds
    wait = 3
  }
  serialization = "seata"
  compressor = "none"
}
service {

  #transaction service group mapping
  # 自定义事务分组  要与 配置文件中的保持一致  分组名: minbox-seata
  vgroup_mapping.minbox-seata = "default"

  #only support when registry.type=file, please don't set multiple addresses
  
  # 通信的端口  
  default.grouplist = "127.0.0.1:8091"

}
```
