package utils;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class CharArrayTokenizer implements Enumeration<String> {
	private char[] array;
	private char[] delimiters;
	private boolean reverse = false;
	private int currentPosition;
	private int newPosition;
	private int maxPosition;
	private int offset;
	
	public CharArrayTokenizer(char[] array, char[] delimiters) {
		this.array = array;
		this.delimiters = delimiters;
		currentPosition = 0;
		newPosition = -1;
		maxPosition = array.length;
		offset = 0;
	}
	
	public CharArrayTokenizer offset(int offset) {
		currentPosition -= this.offset;
		this.offset = Math.max(0, offset);
		currentPosition += this.offset;
		newPosition = -1;
		return this;
	}
	
	public CharArrayTokenizer from(int position) {
		this.currentPosition = Math.min(Math.max(offset, offset+position), maxPosition);
		return this;
	}
	
	public CharArrayTokenizer limit(int length) {
		this.maxPosition = Math.min(offset+length, array.length);
		return this;
	}
	
	public CharArrayTokenizer reverse() {
		reverse = !reverse;
		newPosition = -1;
		final int newCurrentPosition = reverse ? currentPosition-1 : currentPosition+1;
		currentPosition = Math.min(Math.max(offset, newCurrentPosition), maxPosition);
		return this;
	}
	
	public int getCurrentPosition() {
		return currentPosition - offset;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasMoreElements() {
		return hasMoreTokens();
	}
	
	public boolean hasMoreTokens() {
		if (newPosition == -1) {
			newPosition = skipDelimiters(currentPosition);
		}
		
		return (offset <= newPosition && newPosition < maxPosition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String nextElement() {
		return nextToken();
	}
	
	/**
	 * 次のトークンを返す
	 * @return
	 * @throws NoSuchElementException
	 */
	public String nextToken() throws NoSuchElementException {
		currentPosition = (offset <= newPosition && newPosition < maxPosition) ? newPosition : skipDelimiters(currentPosition);
		newPosition = -1;
		
		if ((reverse && currentPosition < offset) || (!reverse && maxPosition <= currentPosition)) {
			throw new NoSuchElementException();
		}
		
		int start = currentPosition;
		int end = scanToken(currentPosition);
//		System.out.println("nextToken(): start="+start+", end="+end+", offset="+offset+", max="+maxPosition);
		currentPosition = end;
		return new String(reverse ? Arrays.copyOfRange(array, end+1, start+1) : Arrays.copyOfRange(array, start, end));
	}
	
	private int skipDelimiters(int startPos) {
		int position = startPos;
		while (offset <= position && position < maxPosition) {
			if (!isDelimiter(array[position])) {
				break;
			}
			position = reverse ? position-1 : position+1;
		}
		return position;
	}
	
	private int scanToken(int startPos) {
		int position = startPos;
		while (offset <= position && position < maxPosition) {
			if (isDelimiter(array[position])) {
				break;
			}
			position = reverse ? position-1 : position+1;
		}
		if (startPos == position) {
			if (isDelimiter(array[position])) {
				position = reverse ? position-1 : position+1;
			}
		}
		return position;
	}
	
	private boolean isDelimiter(char t) {
		for (char delimiter : delimiters) {
			if (t == delimiter) {
				return true;
			}
		}
		return false;
	}
	
	public static void main(String[] args) {
		char[] array = "a rule".toCharArray();
		CharArrayTokenizer at = new CharArrayTokenizer(array, new char[]{' '}).offset(3).from(0).reverse();
		if (at.hasMoreElements()) { System.out.println(at.nextElement()); }
		at = at.reverse();
		while (at.hasMoreElements()) {
			int pos = at.getCurrentPosition();
			System.out.println(at.nextToken()+" ("+pos+" --> "+at.getCurrentPosition()+")");
		}
		
		String s = "";
		StringTokenizer st = new StringTokenizer(s);
		while (st.hasMoreElements()) {
			System.out.println(st.nextToken());
		}
	}
}
