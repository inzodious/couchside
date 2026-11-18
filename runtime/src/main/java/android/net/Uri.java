package android.net;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Uri implements Comparable<Uri> {
    public static final Uri EMPTY = new Uri("");

    private final String text;
    private final String scheme, authority, path, query, fragment, ssp;

    private Uri(String s) {
        text = s;
        String rest = s;
        int hash = rest.indexOf('#');
        fragment = hash < 0 ? null : rest.substring(hash + 1);
        if (hash >= 0) rest = rest.substring(0, hash);
        int colon = rest.indexOf(':');
        int slash = rest.indexOf('/');
        if (colon > 0 && (slash < 0 || colon < slash) && rest.substring(0, colon).matches("[A-Za-z][A-Za-z0-9+.-]*")) {
            scheme = rest.substring(0, colon);
            rest = rest.substring(colon + 1);
        } else scheme = null;
        ssp = rest;
        if (scheme != null && !rest.startsWith("/")) { authority = null; path = null; query = null; return; }
        int q = rest.indexOf('?');
        query = q < 0 ? null : rest.substring(q + 1);
        if (q >= 0) rest = rest.substring(0, q);
        if (rest.startsWith("//")) {
            int end = rest.indexOf('/', 2);
            authority = end < 0 ? rest.substring(2) : rest.substring(2, end);
            path = end < 0 ? "" : rest.substring(end);
        } else { authority = null; path = rest; }
    }

    public static Uri parse(String uriString) { return new Uri(uriString); }
    public static Uri fromFile(File file) { return new Uri("file://" + encode(file.getAbsolutePath(), "/")); }
    public static Uri fromParts(String scheme, String ssp, String fragment) { return new Uri(scheme + ":" + encode(ssp, null) + (fragment == null ? "" : "#" + encode(fragment))); }
    public static Uri withAppendedPath(Uri baseUri, String pathSegment) { return baseUri.buildUpon().appendEncodedPath(pathSegment).build(); }

    public static String encode(String s) { return encode(s, null); }
    public static String encode(String s, String allow) {
        if (s == null) return null;
        StringBuilder sb = new StringBuilder();
        for (byte b : s.getBytes(StandardCharsets.UTF_8)) {
            char c = (char) (b & 0xFF);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || "_-!.~'()*".indexOf(c) >= 0 || (allow != null && allow.indexOf(c) >= 0)) sb.append(c);
            else sb.append('%').append(Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16))).append(Character.toUpperCase(Character.forDigit(b & 0xF, 16)));
        }
        return sb.toString();
    }
    public static String decode(String s) {
        if (s == null) return null;
        try { return URLDecoder.decode(s.replace("+", "%2B"), StandardCharsets.UTF_8); } catch (IllegalArgumentException e) { return s; }
    }

    public boolean isHierarchical() { return scheme == null || ssp.startsWith("/"); }
    public boolean isOpaque() { return !isHierarchical(); }
    public boolean isRelative() { return scheme == null; }
    public boolean isAbsolute() { return scheme != null; }
    public String getScheme() { return scheme; }
    public String getSchemeSpecificPart() { return decode(ssp); }
    public String getEncodedSchemeSpecificPart() { return ssp; }
    public String getAuthority() { return decode(authority); }
    public String getEncodedAuthority() { return authority; }
    public String getUserInfo() { int at = authority == null ? -1 : authority.lastIndexOf('@'); return at < 0 ? null : decode(authority.substring(0, at)); }
    public String getHost() {
        if (authority == null) return null;
        String h = authority.substring(authority.lastIndexOf('@') + 1);
        int colon = h.lastIndexOf(':');
        if (colon >= 0 && h.indexOf(']') < colon) h = h.substring(0, colon);
        return decode(h);
    }
    public int getPort() {
        if (authority == null) return -1;
        String h = authority.substring(authority.lastIndexOf('@') + 1);
        int colon = h.lastIndexOf(':');
        if (colon < 0 || h.indexOf(']') > colon) return -1;
        try { return Integer.parseInt(h.substring(colon + 1)); } catch (NumberFormatException e) { return -1; }
    }
    public String getPath() { return decode(path); }
    public String getEncodedPath() { return path; }
    public String getQuery() { return decode(query); }
    public String getEncodedQuery() { return query; }
    public String getFragment() { return decode(fragment); }
    public String getEncodedFragment() { return fragment; }
    public List<String> getPathSegments() {
        List<String> out = new ArrayList<>();
        if (path != null) for (String s : path.split("/")) if (!s.isEmpty()) out.add(decode(s));
        return Collections.unmodifiableList(out);
    }
    public String getLastPathSegment() { List<String> s = getPathSegments(); return s.isEmpty() ? null : s.get(s.size() - 1); }

    public Set<String> getQueryParameterNames() {
        Set<String> names = new LinkedHashSet<>();
        if (query != null) for (String part : query.split("&")) { int eq = part.indexOf('='); names.add(decodeQuery(eq < 0 ? part : part.substring(0, eq))); }
        return Collections.unmodifiableSet(names);
    }
    public List<String> getQueryParameters(String key) {
        List<String> out = new ArrayList<>();
        if (query != null) for (String part : query.split("&")) {
            int eq = part.indexOf('=');
            if (decodeQuery(eq < 0 ? part : part.substring(0, eq)).equals(key)) out.add(eq < 0 ? "" : decodeQuery(part.substring(eq + 1)));
        }
        return out;
    }
    public String getQueryParameter(String key) { List<String> v = getQueryParameters(key); return v.isEmpty() ? null : v.get(0); }
    public boolean getBooleanQueryParameter(String key, boolean defaultValue) {
        String v = getQueryParameter(key);
        return v == null ? defaultValue : !("false".equalsIgnoreCase(v) || "0".equals(v));
    }
    private static String decodeQuery(String s) { try { return URLDecoder.decode(s, StandardCharsets.UTF_8); } catch (IllegalArgumentException e) { return s; } }

    public Builder buildUpon() {
        Builder b = new Builder();
        b.scheme = scheme; b.authority = authority; b.path = path; b.query = query; b.fragment = fragment;
        if (isOpaque()) b.opaque = ssp;
        return b;
    }
    public Uri normalizeScheme() { return scheme == null || scheme.equals(scheme.toLowerCase()) ? this : buildUpon().scheme(scheme.toLowerCase()).build(); }

    @Override public String toString() { return text; }
    @Override public boolean equals(Object o) { return o instanceof Uri u && u.text.equals(text); }
    @Override public int hashCode() { return text.hashCode(); }
    @Override public int compareTo(Uri other) { return text.compareTo(other.text); }

    public static final class Builder {
        String scheme, authority, path, query, fragment, opaque;

        public Builder scheme(String scheme) { this.scheme = scheme; return this; }
        public Builder opaquePart(String opaquePart) { opaque = Uri.encode(opaquePart, null); return this; }
        public Builder encodedOpaquePart(String opaquePart) { opaque = opaquePart; return this; }
        public Builder authority(String authority) { this.authority = Uri.encode(authority, "@:[]"); opaque = null; return this; }
        public Builder encodedAuthority(String authority) { this.authority = authority; opaque = null; return this; }
        public Builder path(String path) { return encodedPath(Uri.encode(path, "/")); }
        public Builder encodedPath(String path) { this.path = path == null || path.isEmpty() || path.startsWith("/") ? path : "/" + path; opaque = null; return this; }
        public Builder appendPath(String newSegment) { return appendEncodedPath(Uri.encode(newSegment)); }
        public Builder appendEncodedPath(String newSegment) {
            String base = path == null ? "" : path;
            path = base.endsWith("/") ? base + newSegment : base + "/" + newSegment;
            opaque = null;
            return this;
        }
        public Builder query(String query) { this.query = Uri.encode(query, "=&/?:@"); return this; }
        public Builder encodedQuery(String query) { this.query = query; return this; }
        public Builder clearQuery() { query = null; return this; }
        public Builder appendQueryParameter(String key, String value) {
            String pair = URLEncoder.encode(key, StandardCharsets.UTF_8).replace("+", "%20") + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
            query = query == null || query.isEmpty() ? pair : query + "&" + pair;
            return this;
        }
        public Builder fragment(String fragment) { this.fragment = Uri.encode(fragment); return this; }
        public Builder encodedFragment(String fragment) { this.fragment = fragment; return this; }
        public Uri build() {
            StringBuilder sb = new StringBuilder();
            if (scheme != null) sb.append(scheme).append(':');
            if (opaque != null) sb.append(opaque);
            else {
                if (authority != null) sb.append("//").append(authority);
                if (path != null) sb.append(path);
                if (query != null) sb.append('?').append(query);
            }
            if (fragment != null) sb.append('#').append(fragment);
            return new Uri(sb.toString());
        }
        @Override public String toString() { return build().toString(); }
    }
}
