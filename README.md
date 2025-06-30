# SugarIOT-Media
[![Build Status](https://travis-ci.org/xia-chu/ZLMediaKit.svg?branch=master)](https://travis-ci.org/xia-chu/ZLMediaKit)
[![license](http://img.shields.io/badge/license-MIT-green.svg)](https://github.com/xia-chu/ZLMediaKit/blob/master/LICENSE)
[![JAVA](https://img.shields.io/badge/language-java-red.svg)](https://en.cppreference.com/)
[![platform](https://img.shields.io/badge/platform-linux%20|%20macos%20|%20windows-blue.svg)](https://github.com/xia-chu/ZLMediaKit)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-yellow.svg)](https://github.com/xia-chu/ZLMediaKit/pulls)

**SugarIOT-Media** 是一个基于 Spring Boot 3 开发的流媒体管理平台，专为物联网 (IoT) 应用设计，旨在提供稳定高效的音视频流管理解决方案，支持多设备接入和流数据处理,并且支持多租户

## 功能特性

- **实时流接入**：支持多种协议（如 RTSP、RTMP）的实时音视频流接入。
- **国标28181**：支持国标协议接入。
- **视频转码与处理**：集成流媒体处理功能，支持格式转换、视频分辨率调整等操作。
- **设备管理**：提供多设备接入、管理与控制功能。
- **存储与回放**：支持音视频流的持久化存储和历史回放。
- **用户权限控制**：基于用户和设备的权限管理，确保数据访问安全。
- **可扩展性**：模块化设计，便于扩展第三方视频处理工具和云存储服务。



## 技术栈

- **后端**：Java 17+, Spring Boot 3
- **数据库**：Postgresql 
- **缓存数据库**：Redis
- **前端**：Vue3 
- **前端地址**  https://github.com/XDTech/SugarIOT-Media-UI


## 安装指南

### 先决条件

- [Java 17+](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- [Maven 3.8+](https://maven.apache.org/)
- [Postgresql](https://www.mongodb.com/)

### 安装步骤

1. 克隆仓库：

    ```bash
    git clone git@github.com:XDTech/SugarIOT-Media.git
    cd SugarIOT-Media
    ```



3. 构建项目：

    ```bash
    mvn clean install
    ```

4. 运行项目：

    ```bash
    java -jar target/SugarIOT-Media.jar
    ```



## API 文档

请参阅项目中的 [API 文档](docs/API.md) 获取详细的接口信息，包括设备管理、流接入、视频存储控制等内容。

## 使用方法

- **添加设备**：配置设备信息并接入流媒体源。
- **实时监控与回放**：通过平台的管理页面进行视频流的实时监控、转码和历史数据回放。
- **权限管理**：配置用户与设备的访问权限，确保流媒体数据的安全性。

## 贡献指南

我们欢迎对 SugarIOT-Media 项目提出改进建议或参与开发！

1. Fork 本项目
2. 创建新的 Feature 分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的修改 (`git commit -m 'Add some AmazingFeature'`)
4. Push 到分支 (`git push origin feature/AmazingFeature`)
5. 提交一个 Pull Request

## 许可证

本项目采用 MIT 许可证，详情请参阅 [LICENSE](LICENSE) 文件。

## 致谢

特别感谢以下团队和项目，为 SugarIOT-Media 的开发提供了宝贵的支持和灵感：

- **ZLMediaKit** - 流媒体服务基于 [@夏楚](https://github.com/xia-chu) 开发的 [ZLMediaKit](https://github.com/ZLMediaKit/ZLMediaKit)，提供了强大的流媒体接入和分发功能。
- **Jessibuca** - 播放器使用了 [@dexter](https://github.com/langhuihui) 的 [Jessibuca](https://github.com/langhuihui/jessibuca/tree/v3)，该播放器提供了高性能的视频播放支持。
- **Vben Admin** - 前端使用了[Vben Admin](https://doc.vben.pro/)，企业级管理系统框架。
- **wvp-GB28181-pro** 灵感来自[WVP](https://github.com/648540858/wvp-GB28181-pro) 感谢作者开源了功能强大的网络视频平台



## 联系方式

如有任何问题或建议，欢迎提交 Issue 或通过以下方式联系开发团队：

- Email: 944192161@qq.com
- GitHub: [https://github.com/XDTech/SugarIOT-Media](https://github.com/XDTech/SugarIOT-Media)
