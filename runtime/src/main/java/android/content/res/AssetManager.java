package android.content.res;

import couchside.shim.Device;
import java.io.IOException;
import java.io.InputStream;

public final class AssetManager implements AutoCloseable {
    public static final int ACCESS_UNKNOWN = 0, ACCESS_RANDOM = 1, ACCESS_STREAMING = 2, ACCESS_BUFFER = 3;
    public InputStream open(String fileName) throws IOException { return Device.asset(fileName); }
    public InputStream open(String fileName, int accessMode) throws IOException { return Device.asset(fileName); }
    public String[] list(String path) throws IOException { return Device.assets(path); }
    public String[] getLocales() { return new String[] {"en-US"}; }
    @Override public void close() {}
}
