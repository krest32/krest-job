# 分布式任务调度框架 Krest-Job

## 模块介绍

1. admin 服务端
2. core 核心工具包
2. common 公用工具包
3. demo 示例模块代码
3. starter 客户掉导入依赖

## 已完成功能

1. 服务注册，定时探测服务是否存活，及时更新服务列表
2. 基于Quartz实现分布式任务调度，通过cron表达式操作任务的调度规则
3. 调度策略：随机、轮询、加权轮询
4. 基于http协议的远程调度策略，加入重试机制
5. 调度任务日志记录

## 未来目标

1. 调度任务框架的高可用
2. 异常检测机制
3. 分片任务的完善