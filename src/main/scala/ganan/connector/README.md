# Connector - 连接器

在这里提供对外通讯的接口，包括以下几种：

1. TcpConnector：基于 TCP/IP 的连接器
2. WsConnector：基于 WebSocket 协议的连接器

它们的通讯功能都通过 IConnector 这个 Trait 来定义