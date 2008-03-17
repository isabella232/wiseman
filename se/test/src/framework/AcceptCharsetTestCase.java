package framework;

import com.sun.ws.management.transport.AcceptCharset;

import util.WsManTestBaseSupport;

public class AcceptCharsetTestCase extends WsManTestBaseSupport {

	public AcceptCharsetTestCase(final String testName) {
		super(testName);
	}
	
	public void testGetPreferredCharset() {
		String charset = null;
		
		charset = AcceptCharset.getPrefferedCharset(null);
		assertEquals("utf-8", charset);
		charset = AcceptCharset.getPrefferedCharset("UTF-8");
		assertEquals("utf-8", charset);
		charset = AcceptCharset.getPrefferedCharset("UTF-8;q=1");
		assertEquals("utf-8", charset);
		charset = AcceptCharset.getPrefferedCharset("UTF-8;q=1.0");
		assertEquals("utf-8", charset);
		charset = AcceptCharset.getPrefferedCharset("UTF-16");
		assertEquals("utf-16", charset);
		charset = AcceptCharset.getPrefferedCharset("ISO-8859-1");
		assertEquals("iso-8859-1", charset);
		charset = AcceptCharset.getPrefferedCharset("utf-8;q=0.9");
		assertEquals("iso-8859-1", charset);
		charset = AcceptCharset.getPrefferedCharset("utf-8;q=0.9, *");
		assertEquals("utf-16", charset);
		charset = AcceptCharset.getPrefferedCharset("utf-8;q=0.9, *;q=0.7");
		assertEquals("utf-8", charset);
		charset = AcceptCharset.getPrefferedCharset("UTF-8, UTF-16, ISO-8859-1");
		assertEquals("utf-8", charset);
		charset = AcceptCharset.getPrefferedCharset("utf-8, utf-16, iso-8859-1");
		assertEquals("utf-8", charset);
		charset = AcceptCharset.getPrefferedCharset("utf-16, utf-8, iso-8859-1");
		assertEquals("utf-16", charset);
		charset = AcceptCharset.getPrefferedCharset("utf-32, utf-8, iso-8859-1");
		assertEquals("utf-8", charset);
		charset = AcceptCharset.getPrefferedCharset("utf-16;q=0.8, utf-8, iso-8859-1");
		assertEquals("utf-8", charset);
		charset = AcceptCharset.getPrefferedCharset("utf-16;q=1.0, utf-8, iso-8859-1");
		assertEquals("utf-16", charset);
		charset = AcceptCharset.getPrefferedCharset(" utf-16; q=1.0, utf-8, iso-8859-1");
		assertEquals("utf-16", charset);
	}
}
