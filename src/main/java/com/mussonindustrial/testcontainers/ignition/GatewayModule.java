package com.mussonindustrial.testcontainers.ignition;

/** Ignition 8.3 built-in module and solution suite identifiers. */
public enum GatewayModule implements IgnitionModule {
    /** Alarm Notification module. */
    ALARM_NOTIFICATION("com.inductiveautomation.alarm-notification"),
    /** Legacy Allen-Bradley drivers module. */
    ALLEN_BRADLEY_DRIVERS("com.inductiveautomation.opcua.drivers.ablegacy"),
    /** BACnet driver module. */
    BACNET_DRIVER("com.inductiveautomation.opcua.drivers.bacnet"),
    /** DNP3 driver module. */
    DNP3_DRIVER("com.inductiveautomation.opcua.drivers.dnp3"),
    /** DNP3 v2 driver module. */
    DNP3_DRIVER_V2("com.inductiveautomation.opcua.drivers.dnp3v2"),
    /** Enterprise Administration module. */
    ENTERPRISE_ADMINISTRATION("com.inductiveautomation.eam"),
    /** Event Streams module. */
    EVENT_STREAM("com.inductiveautomation.eventstream"),
    /** Core Historian module. */
    HISTORIAN("com.inductiveautomation.historian"),
    /** IEC 61850 driver module. */
    IEC_61850_DRIVER("com.inductiveautomation.opcua.drivers.iec61850"),
    /** Kafka Connector module. */
    KAFKA_CONNECTOR("com.inductiveautomation.connectors.kafka"),
    /** Logix driver module. */
    LOGIX_DRIVER("com.inductiveautomation.opcua.drivers.logix"),
    /** Micro800 driver module. */
    MICRO800_DRIVER("com.inductiveautomation.opcua.drivers.micro800"),
    /** Mitsubishi driver module. */
    MITSUBISHI_DRIVER("com.inductiveautomation.opcua.drivers.mitsubishi"),
    /** Modbus v2 driver module. */
    MODBUS_DRIVER_V2("com.inductiveautomation.opcua.drivers.modbus"),
    /** MongoDB Connector module. */
    MONGODB_CONNECTOR("com.inductiveautomation.connectors.mongodb"),
    /** Omron driver module. */
    OMRON_DRIVER("com.inductiveautomation.opcua.drivers.omron"),
    /** OPC UA module. */
    OPC_UA("com.inductiveautomation.opcua"),
    /** Perspective module. */
    PERSPECTIVE("com.inductiveautomation.perspective"),
    /** Reporting module. */
    REPORTING("com.inductiveautomation.reporting"),
    /** Sequential Function Charts module. */
    SFC("com.inductiveautomation.sfc"),
    /** Siemens drivers module. */
    SIEMENS_DRIVERS("com.inductiveautomation.opcua.drivers.siemens"),
    /** Siemens Enhanced driver module. */
    SIEMENS_ENHANCED_DRIVER("com.inductiveautomation.opcua.drivers.siemens-symbolic"),
    /** SMS Notification module. */
    SMS_NOTIFICATION("com.inductiveautomation.sms-notification"),
    /** SQL Bridge module. */
    SQL_BRIDGE("com.inductiveautomation.sqlbridge"),
    /** SQL Historian module. */
    SQL_HISTORIAN("com.inductiveautomation.historian.sql"),
    /** Symbol Factory module. */
    SYMBOL_FACTORY("com.inductiveautomation.symbol-factory"),
    /** UDP and TCP drivers module. */
    UDP_TCP_DRIVERS("com.inductiveautomation.opcua.drivers.tcpudp"),
    /** Vision module. */
    VISION("com.inductiveautomation.vision"),
    /** Voice Notification module. */
    VOICE_NOTIFICATION("com.inductiveautomation.phone-notification"),
    /** Web Development module. */
    WEB_DEVELOPER("com.inductiveautomation.webdev"),
    /** PostgreSQL JDBC driver module. */
    POSTGRESQL_JDBC_DRIVER("com.inductiveautomation.jdbc.postgresql"),
    /** MariaDB JDBC driver module. */
    MARIADB_JDBC_DRIVER("com.inductiveautomation.jdbc.mariadb"),
    /** Microsoft SQL Server JDBC driver module. */
    MSSQL_JDBC_DRIVER("com.inductiveautomation.jdbc.mssql"),

    /** Application Building solution suite. */
    APPLICATION_BUILDING_SUITE("com.inductiveautomation.suite.application"),
    /** Industrial Historian solution suite. */
    INDUSTRIAL_HISTORIAN_SUITE("com.inductiveautomation.suite.historian"),
    /** DataOps solution suite. */
    DATAOPS_SUITE("com.inductiveautomation.suite.dataops"),
    /** Enterprise Integration solution suite. */
    ENTERPRISE_INTEGRATION_SUITE("com.inductiveautomation.suite.enterprise"),
    /** Alarm Management solution suite. */
    ALARM_MANAGEMENT_SUITE("com.inductiveautomation.suite.alarms");

    private final String value;

    GatewayModule(String value) {
        this.value = value;
    }

    @Override
    public String getIdentifier() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
