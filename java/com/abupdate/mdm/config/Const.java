package com.abupdate.mdm.config;

/*
 * @date   : 2019/09/20
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class Const {

    public static final boolean DEBUG_MODE = false;

    public static final String UNKNOW_STRING = "unknow";

    /**
     * alarm计时器标记
     */
    public static final String ACTION_ALARM_RECEIVER = "com.abupdate.action.ALARM_SERVICE";

    public static final String ALARM_ID = "alarm_id";

    public static final String EMM_NAME = "name";
    public static final String EMM_SN = "sn";

    public static final String POLICY_TASK_ID = "taskId";
    public static final String POLICY_INTF_TYPE = "intfType";
    public static final String POLICY_POLICY_ID = "policyId";
    public static final String POLICY_VERSION = "version";
    public static final String POLICY_STATUS = "status";

    public static final String POLICY_DATA_VERSION = "version";
    public static final String POLICY_DATA_NAME = "name";
    public static final String POLICY_DATA_VALUE = "value";
    public static final String POLICY_DATA_EXTEND1 = "extend1";
    public static final String POLICY_DATA_EXTEND2 = "extend2";
    public static final String POLICY_DATA_EXTEND3 = "extend3";
    public static final String POLICY_DATA_RESULT = "result";

    public static final int EMM_REGISTER_TYPE_SYNC = 1;
    public static final int EMM_REGISTER_TYPE_QR = 2;
    public static final int EMM_REGISTER_TYPE_INPUT = 3;

    public static final int SLOT_ID_1 = 0;
    public static final int SLOT_ID_2 = 1;

    //device info
    public static final String RO_PRODUCT_BUILD_FINGERPRINT = "ro.product.build.fingerprint";
    public static final String RO_BUILD_FLAVOR = "ro.build.flavor";
    public static final String RO_BUILD_PRODUCT = "ro.build.product";
    public static final String RO_BUILD_TYPE = "ro.build.type";
    public static final String RO_HARDWARE = "ro.hardware";
    public static final String RO_PRODUCT_HARDWARE = "ro.product.hardware";
    public static final String RO_PRODUCT_CPU_ABI = "ro.product.cpu.abi";
    public static final String RO_PRODUCT_LOCALE = "ro.product.locale";
    public static final String RO_PRODUCT_MANUFACTURER = "ro.product.manufacturer";
    public static final String RO_RAMSIZE = "ro.ramsize";



    public static final int NETWORK_CODE_JSON_EXCEPTION = -2;
    public static final int NETWORK_CODE_NO_NETWORK = -1;
    public static final int NETWORK_CODE_UNKNOWN = 0;
    public static final int NETWORK_CODE_OK = 1;
    public static final int NETWORK_CODE_PARAM_EXCEPTION = 2;

    //LOCK_TYPE
    public static final int LOCK_TYPE_UNKNOWN = 0;
    public static final int LOCK_TYPE_LBS = 1;
    public static final int LOCK_TYPE_USE_LIFE = 2;
    public static final int LOCK_TYPE_POLICY_LOCK = 3;


    public static final String RESPONSE_CODE = "result";
    public static final String RESPONSE_VALUE = "response";
    public static final String RESPONSE_MSG = "msg";

    //策略
    public static final String POLICY_INSTALL_APP = "installApp";
    public static final String POLICY_UNINSTALL_APP = "uninstallApp";
    public static final String POLICY_UNKNOWN_SOURCES = "installUnknownSources";
    public static final String POLICY_KIOSK = "kiosk";
    public static final String POLICY_ENABLE_GPS = "setUpGPS";
    public static final String POLICY_GPS_STATUS = "turnOnGPS";
    public static final String POLICY_BLUETOOTH_FILE = "transferFilesByBluetooth";
    public static final String POLICY_CHARGE_ONLY = "onlyChargeUSB";
    public static final String POLICY_ENABLE_FACTORY_RESET = "enableFactoryReset";
    public static final String POLICY_EXTEND_STATUS_BAR = "dropDownStatusBar";
    public static final String POLICY_LOCKSCREEN_PASSWORD = "passwordLockScreen";
    public static final String POLICY_CAMERA_STATUS = "useCamera";
    public static final String POLICY_STORAGE_STATUS = "externalStorage";



    //指令
    public static final String POLICY_LBS_STATUS = "lbs";
    public static final String POLICY_USE_LIFE = "timeQuantum";
    public static final String POLICY_LOCK = "lock";
    public static final String POLICY_UNLOCK = "unlock";
    public static final String POLICY_CAPTURE = "screenShot";
    public static final String POLICY_FACTORY_RESET = "factoryReset";
    public static final String POLICY_REBOOT = "reboot";
    public static final String POLICY_SHUTDOWN = "powerOff";
    public static final String POLICY_CLEAR_PASSWORD = "clearScreenPassword";
    public static final String POLICY_CHANGE_WALLPAPER = "changeWallpaper";
    public static final String POLICY_CHANGE_PASSWORD = "changePassword";
    public static final String POLICY_UPDATE_APP = "updateApp";
    public static final String POLICY_CLEAR_APPDATA = "clearAppData";

    public static final String POLICY_OCCUPY_SCREEN = "occupyScreen";
    public static final String POLICY_INITIAL_CHECK = "initialCheck";



    //应用
    public static final String POLICY_BLOCK_NOTICE = "blockAppNotify";
    public static final String POLICY_INSTALL_TYPE = "installType";
    public static final String POLICY_USE_APP = "useApp";
    public static final String POLICY_SHOW_ICON = "icon";
    public static final String POLICY_APP_PERMISSION= "android.permission";

}
