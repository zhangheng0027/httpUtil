package com.zh.util;

public class ArrayUtils {


    public static void byte2Byte(byte[] src, int srcPos, Byte[] dest, int destPos, int len) {
        for (int i = 0; i < len; i++) {
            dest[destPos + i] = src[srcPos + i];
        }
    }

    public static void Byte2byte(Byte[] src, int srcPos, byte[] dest, int destPos, int len) {
        for (int i = 0; i < len; i++) {
            dest[destPos + i] = src[srcPos + i];
        }
    }

    public static String array2String(Byte[] bs) {
        byte[] b = new byte[bs.length];
        Byte2byte(bs, 0, b, 0, bs.length);
        return new String(b);
    }

    public static String array2String(byte[] bs) {
        return new String(bs);
    }

}
