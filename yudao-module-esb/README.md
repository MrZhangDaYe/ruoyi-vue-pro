# ESB Module (yudao-module-esb)

## 1. Overview

The ESB (Enterprise Service Bus) module provides capabilities for integrating various services and applications within the RuoYi-Vue-Pro framework. It leverages Apache Camel for powerful routing, transformation, and connectivity. This module allows for dynamic management of integration interfaces, data mapping, request/response logging, and basic interface monitoring.

## 2. Key Features

*   **Interface Management**:
    *   Define and manage interface endpoints (e.g., HTTP, HTTPS).
    *   Configure routing rules, including forwarding addresses, protocols, retry attempts, and timeouts.
    *   Dynamically generate and manage Apache Camel routes based on interface definitions.
    *   Control interface status (enable/disable).
*   **Data Mapping**:
    *   Define request and response data transformation templates for each interface.
    *   (Future: Specify supported template engines like FreeMarker, Velocity, etc.)
*   **Interface Logging**:
    *   Automatically log request and response details (headers, body) for configured interfaces.
    *   Store and view logs through the admin UI.
*   **Interface Monitoring**:
    *   Periodically check the availability of configured interfaces.
    *   View interface health status and response times through the admin UI.
    *   Trigger manual health checks.

## 3. Module Structure

*   **`controller`**: Admin API endpoints for managing ESB configurations and viewing data.
    *   `EsbInterfaceController`: Manages interface definitions.
    *   `EsbMappingController`: Manages data mappings.
    *   `EsbLogController`: Provides access to interface logs.
    *   `EsbMonitoringController`: Provides access to interface monitoring status.
*   **`service`**: Business logic for ESB operations.
*   **`dal`**: Data Access Layer, including MyBatis mappers and data objects (DOs).
*   **`convert`**: MapStruct converters for mapping between DOs and VOs.
*   **`config`**: (Future: May contain Camel specific configurations if needed globally).
*   **`camel`**: (Future: May contain core Camel route builders, processors, or components if centralized logic is developed).

## 4. Setup and Configuration

*   The module is automatically included and configured as part of the `yudao-server` application.
*   Database schema (tables: `esb_interface`, `esb_mapping`, `esb_log`, `esb_monitoring`) is managed by Flyway migrations located in `src/main/resources/db/migration/mysql`.
*   Menu entries and permissions are also managed by Flyway migrations.

## 5. API Documentation

API documentation is available via Swagger UI, typically accessible at `/swagger-ui/index.html` or `/doc.html` in a running application instance. Look for tags related to "ESB接口信息", "ESB接口数据映射", "ESB接口日志", and "ESB接口监控状态".

## 6. Future Enhancements (Examples)

*   Support for more protocols (e.g., JMS, FTP, SOAP).
*   Advanced data transformation capabilities and more template engine options.
*   More sophisticated monitoring and alerting.
*   GUI for Camel route visualization or construction.
*   Distributed tracing integration for Camel routes.
