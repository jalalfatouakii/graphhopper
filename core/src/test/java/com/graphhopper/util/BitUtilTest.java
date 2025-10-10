/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Karich
 */
public class BitUtilTest {
    private static final BitUtil bitUtil = BitUtil.LITTLE;

    @Test
    public void testToBitString() {
        assertEquals("0010101010101010101010101010101010101010101010101010101010101010", bitUtil.toBitString(Long.MAX_VALUE / 3));
        assertEquals("0111111111111111111111111111111111111111111111111111111111111111", bitUtil.toBitString(Long.MAX_VALUE));

        assertEquals("00101010101010101010101010101010", bitUtil.toBitString(bitUtil.fromInt(Integer.MAX_VALUE / 3)));

        assertEquals("10000000000000000000000000000000", bitUtil.toBitString(1L << 63, 32));
        assertEquals("00000000000000000000000000000001", bitUtil.toBitString((1L << 32), 32));
    }

    @Test
    public void testFromBitString() {
        String str = "001110110";
        assertEquals(str + "0000000", bitUtil.toBitString(bitUtil.fromBitString(str)));

        str = "01011110010111000000111111000111";
        assertEquals(str, bitUtil.toBitString(bitUtil.fromBitString(str)));

        str = "0101111001011100000011111100011";
        assertEquals(str + "0", bitUtil.toBitString(bitUtil.fromBitString(str)));
    }

    @Test
    public void testCountBitValue() {
        assertEquals(1, BitUtil.countBitValue(1));
        assertEquals(2, BitUtil.countBitValue(2));
        assertEquals(2, BitUtil.countBitValue(3));
        assertEquals(3, BitUtil.countBitValue(4));
        assertEquals(3, BitUtil.countBitValue(7));
        assertEquals(4, BitUtil.countBitValue(8));
        assertEquals(5, BitUtil.countBitValue(20));
    }

    @Test
    public void testUnsignedConversions() {
        long l = Integer.toUnsignedLong(-1);
        assertEquals(4294967295L, l);
        assertEquals(-1, BitUtil.toSignedInt(l));

        int intVal = Integer.MAX_VALUE;
        long maxInt = intVal;
        assertEquals(intVal, BitUtil.toSignedInt(maxInt));

        intVal++;
        maxInt = Integer.toUnsignedLong(intVal);
        assertEquals(intVal, BitUtil.toSignedInt(maxInt));

        intVal++;
        maxInt = Integer.toUnsignedLong(intVal);
        assertEquals(intVal, BitUtil.toSignedInt(maxInt));

        assertEquals(0xFFFFffffL, (1L << 32) - 1);
        assertTrue(0xFFFFffffL > 0L);
    }

    @Test
    public void testToFloat() {
        byte[] bytes = bitUtil.fromFloat(Float.MAX_VALUE);
        assertEquals(Float.MAX_VALUE, bitUtil.toFloat(bytes), 1e-9);

        bytes = bitUtil.fromFloat(Float.MAX_VALUE / 3);
        assertEquals(Float.MAX_VALUE / 3, bitUtil.toFloat(bytes), 1e-9);
    }

    @Test
    public void testToDouble() {
        byte[] bytes = bitUtil.fromDouble(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, bitUtil.toDouble(bytes), 1e-9);

        bytes = bitUtil.fromDouble(Double.MAX_VALUE / 3);
        assertEquals(Double.MAX_VALUE / 3, bitUtil.toDouble(bytes), 1e-9);
    }

    @Test
    public void testToInt() {
        byte[] bytes = bitUtil.fromInt(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, bitUtil.toInt(bytes));

        bytes = bitUtil.fromInt(Integer.MAX_VALUE / 3);
        assertEquals(Integer.MAX_VALUE / 3, bitUtil.toInt(bytes));
    }

    @Test
    public void testToShort() {
        byte[] bytes = bitUtil.fromShort(Short.MAX_VALUE);
        assertEquals(Short.MAX_VALUE, bitUtil.toShort(bytes));

        bytes = bitUtil.fromShort((short) (Short.MAX_VALUE / 3));
        assertEquals(Short.MAX_VALUE / 3, bitUtil.toShort(bytes));

        bytes = bitUtil.fromShort((short) -123);
        assertEquals(-123, bitUtil.toShort(bytes));

        bytes = bitUtil.fromShort((short) (0xFF | 0xFF));
        assertEquals(0xFF | 0xFF, bitUtil.toShort(bytes));
    }

    @Test
    public void testToLong() {
        byte[] bytes = bitUtil.fromLong(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, bitUtil.toLong(bytes));

        bytes = bitUtil.fromLong(Long.MAX_VALUE / 7);
        assertEquals(Long.MAX_VALUE / 7, bitUtil.toLong(bytes));
    }

    @Test
    public void testIntsToLong() {
        int high = 2565;
        int low = 9421;
        long l = bitUtil.toLong(low, high);
        assertEquals(high, bitUtil.getIntHigh(l));
        assertEquals(low, bitUtil.getIntLow(l));
    }

    @Test
    public void testToLastBitString() {
        assertEquals("1", bitUtil.toLastBitString(1L, 1));
        assertEquals("01", bitUtil.toLastBitString(1L, 2));
        assertEquals("001", bitUtil.toLastBitString(1L, 3));
        assertEquals("010", bitUtil.toLastBitString(2L, 3));
        assertEquals("011", bitUtil.toLastBitString(3L, 3));
    }

    @Test
    public void testUInt3() {
        byte[] bytes = new byte[3];
        bitUtil.fromUInt3(bytes, 12345678, 0);
        assertEquals(12345678, bitUtil.toUInt3(bytes, 0));

        bytes = new byte[3];
        bitUtil.fromUInt3(bytes, -12345678, 0);
        assertEquals(-12345678 & 0x00FF_FFFF, bitUtil.toUInt3(bytes, 0));
    }

    /**
     * Test: testFromFloat_WrapperDelegatesCorrectly
     *
     * Intention :
     * Vérifie que la méthode fromFloat(byte[], float) écrit correctement
     * une valeur flottante dans le tableau d’octets, en utilisant un décalage = 0.
     *
     * Données de test :
     * - Tableau d’octets de taille 4.
     * - Valeur flottante 42.42f.
     *
     * Oracle :
     * Après écriture, la lecture avec toFloat(bytes, 0) doit restituer la
     * même valeur. Cela prouve que le wrapper appelle bien la version
     * principale avec offset = 0.
     */
    @Test
    public void testFromFloat_WrapperDelegatesCorrectly() {
        byte[] bytes = new byte[4]; // float uses 4 bytes
        float value = 42.42f;

        BitUtil.LITTLE.fromFloat(bytes, value); // call wrapper

        // Read back to check if written correctly
        float result = BitUtil.LITTLE.toFloat(bytes, 0);
        assertEquals(value, result, 0.000001f);
    }

    /**
     * Test: testFromDouble_WrapperDelegatesCorrectly
     *
     * Intention :
     * Vérifie que la méthode fromDouble(byte[], double) écrit correctement
     * une valeur double dans le tableau d’octets, en utilisant un décalage = 0.
     *
     * Données de test :
     * - Tableau d’octets de taille 8.
     * - Valeur double 12345.6789.
     *
     * Oracle :
     * Après écriture, la lecture avec toDouble(bytes, 0) doit restituer la
     * même valeur. Cela prouve que le wrapper appelle bien la version
     * principale avec offset = 0.
     */
    @Test
    public void testFromDouble_WrapperDelegatesCorrectly() {
        byte[] bytes = new byte[8]; // double uses 8 bytes
        double value = 123456.789;

        BitUtil.LITTLE.fromDouble(bytes, value); // call wrapper

        // Read back to check if written correctly
        double result = BitUtil.LITTLE.toDouble(bytes, 0);
        assertEquals(value, result, 0.0000001d);
    }
}
