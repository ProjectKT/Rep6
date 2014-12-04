package utils;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.NoSuchElementException;

public class CharArrayTokenizer implements Enumeration<String> {
	private char[] array;
	private char[] delimiters;
	private boolean reverse = false;
	private int currentPosition;
	private int newPosition;
	private int maxPosition;
	
	public CharArrayTokenizer(char[] array, char[] delimiters, boolean reverse) {
		this.array = array;
		this.delimiters = delimiters;
		this.reverse = reverse;
		currentPosition = 0;
		newPosition = -1;
		maxPosition = array.length;
	}
	
	public CharArrayTokenizer from(int position) {
		this.currentPosition = position;
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasMoreElements() {
		if (newPosition == -1) {
			newPosition = skipDelimiters(currentPosition);
		}
		
		return reverse ? (0 <= newPosition) : (newPosition < maxPosition);
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
		currentPosition = (0 <= newPosition) ? newPosition : skipDelimiters(currentPosition);
		newPosition = -1;
		
		if ((reverse && currentPosition <= -1) || (!reverse && maxPosition <= currentPosition)) {
			throw new NoSuchElementException();
		}
		
		int start = currentPosition;
		int end = scanToken(currentPosition);
		currentPosition = end;
		return new String(reverse ? Arrays.copyOfRange(array, end+1, start+1) : Arrays.copyOfRange(array, start, end));
	}
	
	private int skipDelimiters(int startPos) {
		int position = startPos;
		if (!reverse) {
			while (position < maxPosition) {
				if (!isDelimiter(array[position])) {
					break;
				}
				position++;
			}
		} else {
			while (-1 < position) {
				if (!isDelimiter(array[position])) {
					break;
				}
				position--;
			}
		}
		return position;
	}
	
	private int scanToken(int startPos) {
		int position = startPos;
		if (!reverse) {
			while (position < maxPosition) {
				if (isDelimiter(array[position])) {
					break;
				}
				position++;
			}
		} else {
			while (-1 < position) {
				if (isDelimiter(array[position])) {
					break;
				}
				position--;
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
		char[] array = "hello world, java,hoo".toCharArray();
		CharArrayTokenizer at = new CharArrayTokenizer(array, new char[]{' ', ','}, true).from(array.length-4);
		while (at.hasMoreElements()) {
			System.out.println(at.nextToken());
		}
	}
}
