package com.mussonindustrial.testcontainers.ignition;

/**
 * Identifies a logical Ignition module.
 *
 * <p>Each Ignition profile maps these modules to image-specific identifiers.
 */
public enum IgnitionModule {

    /** Alarm Notification module. */
    ALARM_NOTIFICATION("Alarm Notification"),

    /** Allen-Bradley legacy driver module. */
    ALLEN_BRADLEY_LEGACY_DRIVER("Allen-Bradley Legacy Drivers"),

    /** BACnet driver module. */
    BACNET_DRIVER("BACnet Driver"),

    /** DNP3 driver module. */
    DNP3_DRIVER("DNP3 Driver"),

    /** DNP3 version 2 driver module. */
    DNP3_DRIVER_V2("DNP3 Driver v2"),

    /** Enterprise Administration module. */
    ENTERPRISE_ADMINISTRATION("Enterprise Administration"),

    /** Event Streams module. */
    EVENT_STREAMS("Event Streams"),

    /** Historian Core module. */
    HISTORIAN_CORE("Historian Core"),

    /** SQL Historian module. */
    SQL_HISTORIAN("SQL Historian"),

    /** IEC 61850 driver module. */
    IEC_61850_DRIVER("IEC 61850 Driver"),

    /** Kafka Connector module. */
    KAFKA_CONNECTOR("Kafka Connector"),

    /** Logix driver module. */
    LOGIX_DRIVER("Logix Driver"),

    /** Micro800 driver module. */
    MICRO800_DRIVER("Micro800 Driver"),

    /** Mitsubishi driver module. */
    MITSUBISHI_DRIVER("Mitsubishi Driver"),

    /** Modbus version 2 driver module. */
    MODBUS_DRIVER_V2("Modbus Driver v2"),

    /** MongoDB Connector module. */
    MONGODB_CONNECTOR("MongoDB Connector"),

    /** Omron driver module. */
    OMRON_DRIVER("Omron Driver"),

    /** OPC UA module. */
    OPC_UA("OPC UA"),

    /** Perspective module. */
    PERSPECTIVE("Perspective"),

    /** Reporting module. */
    REPORTING("Reporting"),

    /** Client-side serial support module. */
    SERIAL_SUPPORT_CLIENT("Serial Support Client"),

    /** Gateway-side serial support module. */
    SERIAL_SUPPORT_GATEWAY("Serial Support Gateway"),

    /** Sequential Function Charts module. */
    SFC("Sequential Function Charts"),

    /** Siemens driver module. */
    SIEMENS_DRIVER("Siemens Driver"),

    /** Siemens symbolic driver module. */
    SIEMENS_SYMBOLIC_DRIVER("Siemens Symbolic Driver"),

    /** SMS Notification module. */
    SMS_NOTIFICATION("SMS Notification"),

    /** SQL Bridge module. */
    SQL_BRIDGE("SQL Bridge"),

    /** Symbol Factory module. */
    SYMBOL_FACTORY("Symbol Factory"),

    /** UDP and TCP driver modules. */
    UDP_TCP_DRIVERS("UDP and TCP Drivers"),

    /** Vision module. */
    VISION("Vision"),

    /** Voice Notification module. */
    VOICE_NOTIFICATION("Voice Notification"),

    /** Web Browser module. */
    WEB_BROWSER("Web Browser"),

    /** Web Dev module. */
    WEB_DEV("Web Dev"),

    /** PostgreSQL JDBC driver module. */
    POSTGRESQL_JDBC_DRIVER("PostgreSQL JDBC Driver"),

    /** MariaDB JDBC driver module. */
    MARIADB_JDBC_DRIVER("MariaDB JDBC Driver"),

    /** Microsoft SQL Server JDBC driver module. */
    MSSQL_JDBC_DRIVER("Microsoft SQL Server JDBC Driver");

    /** Human-readable module name. */
    private final String displayName;

    /**
     * Creates a logical module.
     *
     * @param displayName human-readable module name
     */
    IgnitionModule(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the human-readable module name.
     *
     * @return module display name
     */
    public String displayName() {
        return displayName;
    }
}
