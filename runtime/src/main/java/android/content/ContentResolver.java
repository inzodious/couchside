package android.content;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;

/** No content providers on the simulated TV: queries come back empty. Settings live in [couchside.shim.Device]. */
public class ContentResolver {
    public static final String SCHEME_CONTENT = "content", SCHEME_FILE = "file", SCHEME_ANDROID_RESOURCE = "android.resource";
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) { return null; }
    public Cursor query(Uri uri, String[] projection, android.os.Bundle queryArgs, android.os.CancellationSignal cancellationSignal) { return null; }
    public Uri insert(Uri url, ContentValues values) { return null; }
    public int bulkInsert(Uri url, ContentValues[] values) { return 0; }
    public int update(Uri uri, ContentValues values, String where, String[] selectionArgs) { return 0; }
    public int delete(Uri url, String where, String[] selectionArgs) { return 0; }
    public String getType(Uri url) { return null; }
    public java.io.InputStream openInputStream(Uri uri) throws java.io.FileNotFoundException {
        if ("file".equals(uri.getScheme())) return new java.io.FileInputStream(uri.getPath());
        throw new java.io.FileNotFoundException("No content provider for " + uri);
    }
    public java.io.OutputStream openOutputStream(Uri uri) throws java.io.FileNotFoundException {
        if ("file".equals(uri.getScheme())) return new java.io.FileOutputStream(uri.getPath());
        throw new java.io.FileNotFoundException("No content provider for " + uri);
    }
    public void registerContentObserver(Uri uri, boolean notifyForDescendants, ContentObserver observer) {}
    public void unregisterContentObserver(ContentObserver observer) {}
    public void notifyChange(Uri uri, ContentObserver observer) {}
    public void takePersistableUriPermission(Uri uri, int modeFlags) {}
    public android.os.Bundle call(Uri uri, String method, String arg, android.os.Bundle extras) { return null; }
}
