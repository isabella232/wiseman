/*
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
 ** Copyright (C) 2008 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Denis Rachal (denis.rachal@hp.com),
 **
 */

package com.sun.ws.management.transport;

import java.io.*;
import java.lang.System;

/**
 * Stream used to write base64 encoded data.
 */
public class Base64OutputStream extends FilterOutputStream {
	private int size = 0;
	private static final byte[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
			.getBytes();
	private byte[] buffer = new byte[0];

	/**
	 * Creates a Base64OutputStream with the given OutputStream
	 * 
	 */
	public Base64OutputStream(OutputStream out) {
		super(out);
	}

	public void write(int b) throws IOException {
		checkOut();

		byte[] ba = new byte[1];
		ba[0] = (byte) b;
		write(ba, 0, 1);
	}

	public void write(byte[] b) throws IOException {
		checkOut();

		write(b, 0, b.length);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		checkOut();

		int count = len + buffer.length;
		int length = count - (count % 3);

		if (length < 3) {
			saveBuffer(b, off, len);
			return;
		}

		int b64int;

		// process data in the buffer first
		if (buffer.length > 0) {
			size += 4;
			length -= 3;

			off += processBuffer(b, off, len);
		}

		// Pack 3 ascii characters into an int
		// length is always a multiple of 3
		for (; length > 0;) {
			b64int = (int) b[off++] & 0xff;

			b64int <<= 8;
			b64int |= b[off++] & 0xff;

			b64int <<= 8;
			b64int |= b[off++] & 0xff;

			length -= 3;
			size += 4;

			int c3 = b64int & 0x3f;
			b64int >>>= 6;
			int c2 = b64int & 0x3f;
			b64int >>>= 6;
			int c1 = b64int & 0x3f;
			b64int >>>= 6;
			int c0 = b64int & 0x3f;
			b64int >>>= 6;

			// now unpack the b64int into 4, 6-bit, base 64 characters
			out.write(ALPHABET[c0]);
			out.write(ALPHABET[c1]);
			out.write(ALPHABET[c2]);
			out.write(ALPHABET[c3]);

			if (size >= 72) {
				out.write('\n');
				size = 0;
			}
		}

		// put the rest into the buffer
		writeBuffer(b, off, len, count);
	}

	public void flush() throws IOException {
		checkOut();

		out.flush();
	}

	public void close() throws IOException {
		if (out == null)
			return;

		int b64int;
		int length = buffer.length;

		int b64Len = (4 * ((length / 3)));
		if (length % 3 > 0)
			b64Len += 4;

		b64Len -= length;

		for (; length > 0;) {
			int i = 0;
			byte bAscii = buffer[i++];
			b64int = (int) bAscii & 0xff;

			--length;
			if (length > 0) {
				bAscii = buffer[i++];
				b64int <<= 8;
				b64int |= (byte) bAscii & 0xff;

				--length;
				if (length > 0) {
					bAscii = buffer[i++];
					b64int <<= 8;
					b64int |= (byte) bAscii & 0xff;

					--length;
				} else
					b64int <<= 8;
			} else
				b64int <<= 16;

			int c3 = b64int & 0x3f;
			b64int >>>= 6;
			int c2 = b64int & 0x3f;
			b64int >>>= 6;
			int c1 = b64int & 0x3f;
			b64int >>>= 6;
			int c0 = b64int & 0x3f;
			b64int >>>= 6;

			// now unpack the b64int into 4, 6-bit, base 64 characters
			out.write(ALPHABET[c0]);
			out.write(ALPHABET[c1]);

			if (i > 1) {
				out.write(ALPHABET[c2]);
				if (i == 3)
					out.write(ALPHABET[c3]);
			}
		}

		for (--b64Len; b64Len > 0; --b64Len) {
			out.write('=');
		}

		out.close();
		out = null;
	}

	private void checkOut() throws IOException {
		if (out == null)
			throw new IOException("This base64 output stream has been closed.");
	}

	private void saveBuffer(byte[] b, int off, int len) {
		byte[] buf = new byte[buffer.length + len];
		System.arraycopy(buffer, 0, buf, 0, buffer.length);
		int p = buffer.length;
		for (int i = off; i < len; ++i) {
			buf[p] = b[i];
			++p;
		}
		buffer = new byte[buf.length];
		System.arraycopy(buf, 0, buffer, 0, buf.length);
	}

	private int processBuffer(byte[] b, int off, int len) throws IOException {
		int b64int;
		byte bAscii = buffer[0];
		int n = 0;

		b64int = (int) buffer[0] & 0xff;
		if (buffer.length < 2) {
			bAscii = b[off++];
			++n;
		} else
			bAscii = buffer[1];

		b64int <<= 8;
		b64int |= bAscii & 0xff;

		if (buffer.length < 3) {
			bAscii = b[off++];
			++n;
		} else
			bAscii = buffer[2];

		b64int <<= 8;
		b64int |= bAscii & 0xff;

		int c3 = b64int & 0x3f;
		b64int >>>= 6;
		int c2 = b64int & 0x3f;
		b64int >>>= 6;
		int c1 = b64int & 0x3f;
		b64int >>>= 6;
		int c0 = b64int & 0x3f;
		b64int >>>= 6;

		// now unpack the b64int into 4, 6-bit, base 64 characters
		out.write(ALPHABET[c0]);
		out.write(ALPHABET[c1]);
		out.write(ALPHABET[c2]);
		out.write(ALPHABET[c3]);

		if (size == 72) {
			out.write('\n');
			size = 0;
		}
		buffer = new byte[0];
		return n;
	}

	private void writeBuffer(byte[] b, int off, int len, int count) {
		buffer = new byte[count % 3];
		int i = 0;
		for (; off < len; ++off) {
			buffer[i++] = b[off];
		}
	}
}
