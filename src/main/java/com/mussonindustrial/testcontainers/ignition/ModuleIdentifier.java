package com.mussonindustrial.testcontainers.ignition;

public enum ModuleIdentifier {
    ALARM_NOTIFICATION("alarm-notification"),
    ALLEN_BRADLEY_DRIVERS("allen-bradley-drivers"),
    BACNET_DRIVER("bacnet-driver"),
    DNP3_DRIVER("dnp3-driver"),
    DNP3_DRIVER_V2("dnp3-driver-v2"),
    ENTERPRISE_ADMINISTRATION("enterprise-administration"),
    IEC_61850_DRIVER("iec-61850-driver"),
    LOGIX_DRIVER("logix-driver"),
    MICRO800_DRIVER("micro800-driver"),
    MITSUBISHI_DRIVER("mitsubishi-driver"),
    MODBUS_DRIVER_V2("modbus-driver-v2"),
    OMRON_DRIVER("omron-driver"),
    OPC_UA("opc-ua"),
    PERSPECTIVE("perspective"),
    REPORTING("reporting"),
    SERIAL_SUPPORT_CLIENT("serial-support-client"),
    SERIAL_SUPPORT_GATEWAY("serial-support-gateway"),
    SFC("sfc"),
    SIEMENS_DRIVERS("siemens-drivers"),
    SMS_NOTIFICATION("sms-notification"),
    SQL_BRIDGE("sql-bridge"),
    SYMBOL_FACTORY("symbol-factory"),
    TAG_HISTORIAN("tag-historian"),
    UDP_TCP_DRIVERS("udp-tcp-drivers"),
    VISION("vision"),
    VOICE_NOTIFICATION("voice-notification"),
    WEB_BROWSER("web-browser"),
    WEB_DEVELOPER("web-developer");

    private final String value;

    ModuleIdentifier(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}

