package com.sun.ws.management.transport;

public final class ContentType {
    
    private static final String SOAP_MIME_TYPE = "application/soap+xml";
    private static final String CHARSET = "charset";
    private static final String DEFAULT_CHARSET= "utf-8";
    private static final String UTF16_CHARSET= "utf-16";
    
    public static final ContentType DEFAULT_CONTENT_TYPE = new ContentType();
    public static final ContentType UTF16_CONTENT_TYPE = createFromEncoding(UTF16_CHARSET);
    
    // either utf-8 or utf-16 is acceptable
    public static final String ACCEPTABLE_CONTENT_TYPES =
            SOAP_MIME_TYPE + ";" + CHARSET + "=" + DEFAULT_CHARSET + ", " +
            SOAP_MIME_TYPE + ";" + CHARSET + "=" + UTF16_CHARSET;
    
    private boolean acceptable = true;
    private String mimeType = SOAP_MIME_TYPE;
    private String encoding = DEFAULT_CHARSET;
    
    private ContentType() {}
    
    public static ContentType createFromHttpContentType(final String httpContentType) {
        final ContentType contentType = new ContentType();
        boolean foundCharSet = false;
        boolean foundMimeType = false;
        for(final String type : httpContentType.split(";")) {
            final String trimType = type.trim();
            if (SOAP_MIME_TYPE.equals(trimType)) {
                foundMimeType = true;
                contentType.mimeType = trimType;
            } else {
                final String[] charset = trimType.split("=");
                for (int i = 0; i < charset.length; i += 2) {
                    if (i + 1 < charset.length &&
                            CHARSET.equals(charset[i].trim())) {
                        final String value = unquote(charset[i + 1].trim());
                        if (DEFAULT_CHARSET.equalsIgnoreCase(value) ||
                                UTF16_CHARSET.equalsIgnoreCase(value)) {
                            foundCharSet = true;
                            contentType.encoding = value;
                            break;
                        }
                    }
                }
            }
        }
        contentType.acceptable = foundCharSet && foundMimeType;
        return contentType;
    }
    
    public static ContentType createFromEncoding(final String charset) {
        final ContentType contentType = new ContentType();
        contentType.mimeType = SOAP_MIME_TYPE;
        contentType.encoding = charset == null ? DEFAULT_CHARSET : charset;
        contentType.acceptable = true;
        return contentType;
    }
    
    public boolean isAcceptable() {
        return acceptable;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    public boolean equals(final Object ct) {
        if (! (ct instanceof ContentType)) {
            return false;
        }
        
        final ContentType other = (ContentType) ct;
        return encoding.equals(other.encoding) && 
                mimeType.equals(other.mimeType) &&
                acceptable == other.acceptable;
    }
    
    public String toString() {
        return mimeType + ";" + CHARSET + "=" + encoding;
    }
    
    // remove leading and ending quotes from input string
    // TODO: improve the algorithm - possibly using regex
    private static String unquote(final String s) {
        int start = 0;
        if (s.startsWith("\"")) {
            start ++;
        }
        int end = s.length();
        if (s.endsWith("\"")) {
            end --;
        }
        return s.substring(start, end);
    }
}