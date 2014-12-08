package utils;

import java.io.CharArrayReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.lang.reflect.Field;

public class CharArrayStreamTokenizer extends StreamTokenizer {

	TraceableCharArrayReader reader;

	public CharArrayStreamTokenizer(char[] buf) {
		this(buf, 0, buf.length);
	}
	public CharArrayStreamTokenizer(char[] buf, int offset, int length) {
		super(new TraceableCharArrayReader(buf, offset, length));
		
		try {
			Field f = getClass().getSuperclass().getDeclaredField("reader");
			f.setAccessible(true);
			this.reader = (TraceableCharArrayReader) f.get(this);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public int getCurrentPosition() {
		return reader.getPos();
	}
	
	public void close() {
		if (reader != null) {
			reader.close();
		}
	}

	private static class TraceableCharArrayReader extends CharArrayReader {

		public TraceableCharArrayReader(char[] buf, int offset, int length) {
			super(buf, offset, length);
		}
		
		public int getPos() {
			return pos;
		}
	}
}
