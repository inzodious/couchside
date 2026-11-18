package android.util;

public class Xml {
    public static String FEATURE_RELAXED = "http://xmlpull.org/v1/doc/features.html#relaxed";
    public static org.xmlpull.v1.XmlPullParser newPullParser() {
        try {
            org.kxml2.io.KXmlParser parser = new org.kxml2.io.KXmlParser();
            parser.setFeature(org.xmlpull.v1.XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            return parser;
        } catch (org.xmlpull.v1.XmlPullParserException e) { throw new AssertionError(e); }
    }
    public static org.xmlpull.v1.XmlSerializer newSerializer() { return new org.kxml2.io.KXmlSerializer(); }
}
