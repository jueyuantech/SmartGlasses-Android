package com.jueyuantech.glasses.util;

import java.security.InvalidParameterException;
import java.util.Random;
import java.util.UUID;

/**
 * Clone of Android's HexDump class, for use in debugging. Cosmetic changes
 * only.
 */
public class ByteUtils {
    /**
     * Data value format type uint8
     */
    public final static int FORMAT_UINT8 = 0x11;

    /**
     * Data value format type uint16
     */
    public final static int FORMAT_UINT16 = 0x12;

    /**
     * Data value format type uint24
     */
    public final static int FORMAT_UINT24 = 0x13;

    /**
     * Data value format type uint32
     */
    public final static int FORMAT_UINT32 = 0x14;


    private final static char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static String dumpHexString(byte[] array) {
        return dumpHexString(array, 0, array.length);
    }

    public static String dumpHexString(byte[] array, int offset, int length) {
        StringBuilder result = new StringBuilder();

        byte[] line = new byte[8];
        int lineIndex = 0;

        for (int i = offset; i < offset + length; i++) {
            if (lineIndex == line.length) {
                for (int j = 0; j < line.length; j++) {
                    if (line[j] > ' ' && line[j] < '~') {
                        result.append(new String(line, j, 1));
                    } else {
                        result.append(".");
                    }
                }

                result.append("\n");
                lineIndex = 0;
            }

            byte b = array[i];
            result.append(HEX_DIGITS[(b >>> 4) & 0x0F]);
            result.append(HEX_DIGITS[b & 0x0F]);
            result.append(" ");

            line[lineIndex++] = b;
        }

        for (int i = 0; i < (line.length - lineIndex); i++) {
            result.append("   ");
        }
        for (int i = 0; i < lineIndex; i++) {
            if (line[i] > ' ' && line[i] < '~') {
                result.append(new String(line, i, 1));
            } else {
                result.append(".");
            }
        }

        return result.toString();
    }

    public static String toHexString(byte b) {
        return toHexString(toByteArray(b));
    }

    public static String toHexString(byte[] array) {
        return toHexString(array, 0, array.length);
    }

    public static String toHexString(byte[] array, int offset, int length) {
        char[] buf = new char[length * 2];

        int bufIndex = 0;
        for (int i = offset; i < offset + length; i++) {
            byte b = array[i];
            buf[bufIndex++] = HEX_DIGITS[(b >>> 4) & 0x0F];
            buf[bufIndex++] = HEX_DIGITS[b & 0x0F];
        }

        return new String(buf);
    }

    public static String toHexString(int i) {
        return toHexString(toByteArray(i));
    }

    public static String toHexString(short i) {
        return toHexString(toByteArray(i));
    }

    public static byte[] toByteArray(byte b) {
        byte[] array = new byte[1];
        array[0] = b;
        return array;
    }

    public static byte[] toByteArray(int i) {
        byte[] array = new byte[4];

        array[3] = (byte) (i & 0xFF);
        array[2] = (byte) ((i >> 8) & 0xFF);
        array[1] = (byte) ((i >> 16) & 0xFF);
        array[0] = (byte) ((i >> 24) & 0xFF);

        return array;
    }

    public static byte[] shortToByteArray(int i) {
        byte[] array = new byte[2];

        array[1] = (byte) (i & 0xFF);
        array[0] = (byte) ((i >> 8) & 0xFF);

        return array;
    }

    public static byte[] toByteArray(short i) {
        byte[] array = new byte[2];

        array[1] = (byte) (i & 0xFF);
        array[0] = (byte) ((i >> 8) & 0xFF);

        return array;
    }

    private static int toByte(char c) {
        if (c >= '0' && c <= '9')
            return (c - '0');
        if (c >= 'A' && c <= 'F')
            return (c - 'A' + 10);
        if (c >= 'a' && c <= 'f')
            return (c - 'a' + 10);

        throw new InvalidParameterException("Invalid hex char '" + c + "'");
    }

    public static byte[] hexStringToByteArray(String hexString) {
        int length = hexString.length();
        byte[] buffer = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            buffer[i / 2] = (byte) ((toByte(hexString.charAt(i)) << 4) | toByte(hexString
                    .charAt(i + 1)));
        }

        return buffer;
    }

    public static byte[] u16ToByte(int value) {
        byte[] ret = new byte[2];
        ret[1] = (byte) ((value & 0xff00) >> 8);
        ret[0] = (byte) (value & 0xff);
        return ret;
    }

    public static byte[] getTarget(int target) {
        return hexStringToByteArray("A" + String.format("%1x", target));
    }

    public static byte[] byteMergerAll(byte[]... values) {
        int length_byte = 0;
        for (int i = 0; i < values.length; i++) {
            length_byte += values[i].length;
        }
        byte[] all_byte = new byte[length_byte];
        int countLength = 0;
        for (int i = 0; i < values.length; i++) {
            byte[] b = values[i];
            System.arraycopy(b, 0, all_byte, countLength, b.length);
            countLength += b.length;
        }
        return all_byte;
    }

    public static int getCRC16(byte[] data) {
        int crc = 0xFFFF;
        for (int i = 0; i < data.length; i++) {
            crc = (((crc >> 8) & 0xFFFF) | ((crc << 8) & 0xFFFF)) & 0xFFFF;
            crc ^= (data[i] & 0xFF);
            crc ^= ((crc & 0xFF) >> 4) & 0xFFFF;
            crc ^= (((crc << 8) & 0xFFFF) << 4) & 0xFFFF;
            crc ^= ((((crc & 0xFF) << 4) & 0xFFFF) << 1) & 0xFFFF;
        }
        return crc;
    }

    public static Integer getIntValue(int formatType, byte[] mValue, int offset) {
        switch (formatType) {
            case FORMAT_UINT8:
                return unsignedByteToInt(mValue[offset]);

            case FORMAT_UINT16:
                return unsignedBytesToInt(mValue[offset], mValue[offset + 1]);

            case FORMAT_UINT32:
                return unsignedBytesToInt(mValue[offset], mValue[offset + 1],
                        mValue[offset + 2], mValue[offset + 3]);
        }
        return null;
    }


    public static Integer getIntValueRevert(int formatType, byte[] mValue, int offset) {
        switch (formatType) {
            case FORMAT_UINT8:
                return unsignedByteToInt(mValue[offset]);

            case FORMAT_UINT16:
                return unsignedBytesToInt(mValue[offset + 1], mValue[offset]);

            case FORMAT_UINT32:
                return unsignedBytesToInt(mValue[offset + 3], mValue[offset + 2],
                        mValue[offset + 1], mValue[offset]);
        }
        return null;
    }

    /**
     * Convert a signed byte to an unsigned int.
     */
    public static int unsignedByteToInt(final byte b) {
        return b & 0xFF;
    }

    /**
     * Convert signed bytes to a 16-bit unsigned int.
     */
    public static int unsignedBytesToInt(final byte b0, final byte b1) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8));
    }

    /**
     * Convert signed bytes to a 32-bit unsigned int.
     */
    private static int unsignedBytesToInt(final byte b0, final byte b1, final byte b2, final byte b3) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8))
                + (unsignedByteToInt(b2) << 16) + (unsignedByteToInt(b3) << 24);
    }

    public static String[] chars = new String[]{"a", "b", "c", "d", "e", "f", "g", "h",
            "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x",
            "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D",
            "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
            "T", "U", "V", "W", "X", "Y", "Z"};

    public static String[] RANDOM_HEX_CHARS = new String[]{
            "a", "b", "c", "d", "e", "f",
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
    };

    public synchronized static String generateShortUuid() {
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 4; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(chars[x % 0x3E]);
        }
        return shortBuffer.toString();
    }

    private static Random mRandom = new Random();
    public synchronized static byte[] generateRandomBytes(int bytesLen) {
        return shortToByteArray(mRandom.nextInt());
    }

    public static void setBit(byte[] src, int idx, int value) {
        if (0 == value) {
            src[idx / 8] = setSpecifiedBitTo0(src[idx / 8], idx % 8);
        } else {
            src[idx / 8] = setSpecifiedBitTo1(src[idx / 8], idx % 8);
        }
    }

    public static int getBit(byte[] src, int idx) {
        return getSpecifiedBitValue(src[idx / 8], idx % 8);
    }

    /**
     * Set the specified bit to 1
     *
     * @param originByte Raw byte value
     * @param bitIndex   bit index (From 0~7)
     * @return Final byte value
     */
    private static byte setSpecifiedBitTo1(byte originByte, int bitIndex) {
        return originByte |= (1 << bitIndex);
    }

    /**
     * Set the specified bit to 0
     *
     * @param originByte Raw byte value
     * @param bitIndex   bit index (From 0~7)
     * @return Final byte value
     */
    private static byte setSpecifiedBitTo0(byte originByte, int bitIndex) {
        return originByte &= ~(1 << bitIndex);
    }

    /**
     * Invert the specified bit
     *
     * @param originByte Raw byte value
     * @param bitIndex   bit index (From 0~7)
     * @return Final byte value
     */
    private static byte setSpecifiedBitToReverse(byte originByte, int bitIndex) {
        return originByte ^= (1 << bitIndex);
    }

    /**
     * Get the value of the specified bit
     *
     * @param originByte Raw byte value
     * @param bitIndex   bit index (From 0~7)
     * @return Final byte value
     */
    private static byte getSpecifiedBitValue(byte originByte, int bitIndex) {
        return (byte) ((originByte) >> (bitIndex) & 1);
    }

    public static byte[] longToBytes(long values) {
        byte[] buffer = new byte[8];
        for (int i = 0; i < 8; i++) {
            int offset = 64 - (i + 1) * 8;
            buffer[i] = (byte) ((values >> offset) & 0xff);
        }
        return buffer;
    }

    public static long bytesToLong(byte[] buffer) {
        long values = 0;
        for (int i = 0; i < 8; i++) {
            values <<= 8;
            values |= (buffer[i] & 0xff);
        }
        return values;
    }
}
