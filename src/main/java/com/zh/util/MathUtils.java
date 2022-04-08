package com.zh.util;

public class MathUtils {

	public static int getLengthByte(int a) {
		if (a <= 0)
			return a;
		int count = 0;
		while (a > 0) {
			count++;
			a = a >> 1;
		}
		int offset = ((count & 7) == 0) ? 0 : 1;
		return (count >> 3) + offset;
	}

	public static byte[] int2bytes(int src) {
		int l = getLengthByte(src);
		byte[] bs = new byte[l];
		for (int i = 0; i < l; i++) {
			bs[l - i - 1] = (byte)(src & 0xFF);
			src = src >> 8;
		}
		return bs;
	}

	public static int bytes2int(byte[] bs, int offset, int len) {
		int result = 0;
		for (int i = offset; i < offset + len; i++) {
			result = (result << 8) | ((int)bs[i] & 0xFF);
		}
		return result;
	}

}
