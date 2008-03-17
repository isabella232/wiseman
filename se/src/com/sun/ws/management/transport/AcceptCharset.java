/*
 * Copyright 2005-2008 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ** Copyright (C) 2006, 2007, 2008 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Denis Rachal (denis.rachal@hp.com)
 **
 *
 */

package com.sun.ws.management.transport;


/**
 * This class parses the HTTP Accept-Charset header to
 * determine what character set to use for the response.
 *
 */
public final class AcceptCharset {

	public static final String UTF_8_CHARSET = "utf-8";
	public static final String UTF_16_CHARSET = "utf-16";
	public static final String ISO_8859_1_CHARSET = "iso-8859-1";
	public static final String PREFERRED_CHARSET = UTF_8_CHARSET;
	public static final String ACCEPTABLE_CHARSETS =
		UTF_8_CHARSET + ", " +
		UTF_16_CHARSET + ";q=0.9, " +
		ISO_8859_1_CHARSET + ";q=0.8";

	public static final String[] SUPPORTED_CHARSETS = { UTF_8_CHARSET,
			UTF_16_CHARSET, ISO_8859_1_CHARSET };

	/**
	 * Given an Accept-Charset header this method returns the
	 * most preferred character set supported by the server. If
	 * the server does not support any of the character sets
	 * requested by the client, an empty string is returned.
	 * 
	 * @param header Accept-Charset header value
	 * @return string holding the preferred character set.
	 */
	public static String getPrefferedCharset(final String header) {
		final String param;
		if ((header == null) || (header.trim().length() == 0))
			param = "*";
		else
			param = header.trim();
		return parse(param);
	}

	private static String parse(final String header) {
		String retCharset = "";
		Double retQvalue = -1.0;
		Double utf8Qvalue = -1.0;
		Double utf16Qvalue = -1.0;
		Double iso88591Qvalue = -1.0;
		Double globalQvalue = 0.0;
		boolean globalIsSet = false;
		boolean iso88591IsSet = false;
		final String[] charsets = header.split(",");

		for (int i = 0; i < charsets.length; i++) {
			double qvalue = 1.0;
			final String[] parsed = charsets[i].split(";", 2);
			if (parsed.length == 2) {
				// Get the quality
				final String quality = parsed[1].trim().toLowerCase();
				if ((quality.startsWith("q=")) && (quality.length() > 2)) {
					try {
						qvalue = Double.valueOf(quality.substring(2));
					} catch (NumberFormatException e) {
						// Ignore it and use the default value
					}
				} // else ignore it
			}
			final String charset = parsed[0].trim().toLowerCase();
			if (charset.equals("*")) {
				globalIsSet = true;
				globalQvalue = qvalue;
			} else {
				if (charset.equals(UTF_8_CHARSET)) {
					utf8Qvalue = qvalue;
					if (qvalue > retQvalue) {
						retCharset = charset;
						retQvalue = qvalue;
					}
				} else if (charset.equals(UTF_16_CHARSET)) {
					utf16Qvalue = qvalue;
					if (qvalue > retQvalue) {
						retCharset = charset;
						retQvalue = qvalue;
					}
				} else if (charset.equals(ISO_8859_1_CHARSET)) {
					iso88591Qvalue = qvalue;
					iso88591IsSet = true;
					if (qvalue > retQvalue) {
						retCharset = charset;
						retQvalue = qvalue;
					}
				}
			}
		}
		if ((globalIsSet == false) && (iso88591IsSet == false)) {
			if (retQvalue < 1.0)
				retCharset = ISO_8859_1_CHARSET;

		}
		if ((globalIsSet) && (globalQvalue > retQvalue)) {
			if (utf8Qvalue == -1.0) {
				retCharset = UTF_8_CHARSET;
				retQvalue = globalQvalue;
			} else if (utf16Qvalue == -1.0) {
				retCharset = UTF_16_CHARSET;
				retQvalue = globalQvalue;
			} else if (iso88591Qvalue == -1.0) {
				retCharset = ISO_8859_1_CHARSET;
				retQvalue = globalQvalue;
			}
		}
		if (retQvalue > 0.0)
			return retCharset;
		else
			return "";
	}
}
