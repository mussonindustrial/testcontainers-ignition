package com.mussonindustrial.testcontainers.ignition;

/**
 * Inductive Automation Ignition Modules
 * */
public enum StandardModule implements IgnitionModule {
    /** Alarm Notification Module */
    ALARM_NOTIFICATION("alarm-notification"),

    /** Allen-Bradley Drivers Module */
    ALLEN_BRADLEY_DRIVERS("allen-bradley-drivers"),

    /** BACNet Driver Module */
    BACNET_DRIVER("bacnet-driver"),

    /** DNP3 Driver Module */
    DNP3_DRIVER("dnp3-driver"),

    /** DNP3 Driver V2 Module */
    DNP3_DRIVER_V2("dnp3-driver-v2"),

    /** EAM Module */
    ENTERPRISE_ADMINISTRATION("enterprise-administration"),

    /** IEC-61850 Driver Module */
    IEC_61850_DRIVER("iec-61850-driver"),

    /** Logix Driver Module */
    LOGIX_DRIVER("logix-driver"),

    /** Micro800 Driver Module */
    MICRO800_DRIVER("micro800-driver"),

    /** Mitsubishi Driver Module */
    MITSUBISHI_DRIVER("mitsubishi-driver"),

    /** Modbus Driver V2 Module */
    MODBUS_DRIVER_V2("modbus-driver-v2"),

    /** Omron Driver Module */
    OMRON_DRIVER("omron-driver"),

    /** OPC-UA Module */
    OPC_UA("opc-ua"),

    /** Perspective Module */
    PERSPECTIVE("perspective"),

    /** Reporting Module */
    REPORTING("reporting"),

    /** Client Serial Support Module */
    SERIAL_SUPPORT_CLIENT("serial-support-client"),

    /** Gateway Serial Support Module */
    SERIAL_SUPPORT_GATEWAY("serial-support-gateway"),

    /** Sequential Function Chart Module */
    SFC("sfc"),

    /** Siemens Drivers Module */
    SIEMENS_DRIVERS("siemens-drivers"),

    /** SMS Notification Module */
    SMS_NOTIFICATION("sms-notification"),

    /** SQL Bridge Module */
    SQL_BRIDGE("sql-bridge"),

    /** Symbol Factory Module */
    SYMBOL_FACTORY("symbol-factory"),

    /** Tag Historian Module */
    TAG_HISTORIAN("tag-historian"),

    /** UDP/TCP Drivers Module */
    UDP_TCP_DRIVERS("udp-tcp-drivers"),

    /** Vision Module */
    VISION("vision"),

    /** Voice Notification Module */
    VOICE_NOTIFICATION("voice-notification"),

    /** Web Browser Module */
    WEB_BROWSER("web-browser"),

    /** Web Development (WebDev) Module */
    WEB_DEVELOPER("web-developer");

    private final String value;

    StandardModule(String value) {
        this.value = value;
    }

    @Override
    public String getIdentifier() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
