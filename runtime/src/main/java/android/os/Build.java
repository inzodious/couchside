package android.os;

/** What the simulated TV says it is. The fields aren't compile-time constants, so the desktop host can change them before the app starts. */
public class Build {
    public static String MANUFACTURER = String.valueOf("Couchside"), BRAND = String.valueOf("Couchside"), MODEL = String.valueOf("Desktop"),
        DEVICE = String.valueOf("desktop"), PRODUCT = String.valueOf("couchside_desktop"), HARDWARE = String.valueOf("desktop"), BOARD = String.valueOf("desktop"),
        FINGERPRINT = String.valueOf("couchside/desktop/desktop:14/SIM/1:user/release-keys"), DISPLAY = String.valueOf("SIM"), ID = String.valueOf("SIM"),
        TYPE = String.valueOf("user"), TAGS = String.valueOf("release-keys"), HOST = String.valueOf("desktop"), USER = String.valueOf("couchside"),
        SOC_MANUFACTURER = String.valueOf("desktop"), SOC_MODEL = String.valueOf("desktop"), BOOTLOADER = String.valueOf("unknown"), SERIAL = String.valueOf("unknown");
    public static String CPU_ABI = String.valueOf("x86_64"), CPU_ABI2 = String.valueOf(""), RADIO = String.valueOf("unknown");
    public static String getRadioVersion() { return null; }
    public static String getSerial() { return "unknown"; }
    public static String[] SUPPORTED_ABIS = {"x86_64"}, SUPPORTED_64_BIT_ABIS = {"x86_64"}, SUPPORTED_32_BIT_ABIS = {};
    public static long TIME = System.currentTimeMillis();

    public static class VERSION {
        public static int SDK_INT = Integer.parseInt("34");
        public static String RELEASE = String.valueOf("14"), CODENAME = String.valueOf("REL"), INCREMENTAL = String.valueOf("1"), SECURITY_PATCH = String.valueOf("2026-01-01");
        public static int PREVIEW_SDK_INT = Integer.parseInt("0");
    }

    public static class VERSION_CODES {
        public static final int BASE = 1, LOLLIPOP = 21, LOLLIPOP_MR1 = 22, M = 23, N = 24, N_MR1 = 25, O = 26, O_MR1 = 27, P = 28, Q = 29, R = 30, S = 31, S_V2 = 32,
            TIRAMISU = 33, UPSIDE_DOWN_CAKE = 34, VANILLA_ICE_CREAM = 35, BAKLAVA = 36, CUR_DEVELOPMENT = 10000;
    }
}
