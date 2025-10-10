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

import com.carrotsearch.hppc.IntArrayList;
import org.junit.jupiter.api.Test;

import com.github.javafaker.Faker;

import java.util.Arrays;
import java.util.Random;

import static com.carrotsearch.hppc.IntArrayList.from;
import static org.junit.jupiter.api.Assertions.*;

class ArrayUtilTest {

    @Test
    public void testConstant() {
        IntArrayList list = ArrayUtil.constant(10, 3);
        assertEquals(10, list.size());
        assertEquals(3, list.get(5));
        assertEquals(3, list.get(9));
        assertEquals(10, list.buffer.length);
    }

    @Test
    public void testIota() {
        IntArrayList list = ArrayUtil.iota(15);
        assertEquals(15, list.buffer.length);
        assertEquals(15, list.elementsCount);
        assertEquals(14 / 2.0 * (14 + 1), Arrays.stream(list.buffer).sum());
    }

    @Test
    public void testRange() {
        assertEquals(from(3, 4, 5, 6), ArrayUtil.range(3, 7));
        assertEquals(from(-3, -2), ArrayUtil.range(-3, -1));
        assertEquals(from(), ArrayUtil.range(5, 5));
    }

    @Test
    public void testRangeClosed() {
        assertEquals(from(3, 4, 5, 6, 7), ArrayUtil.rangeClosed(3, 7));
        assertEquals(from(-3, -2, -1), ArrayUtil.rangeClosed(-3, -1));
        assertEquals(from(5), ArrayUtil.rangeClosed(5, 5));
    }

    @Test
    public void testPermutation() {
        IntArrayList list = ArrayUtil.permutation(15, new Random());
        assertEquals(15, list.buffer.length);
        assertEquals(15, list.elementsCount);
        assertEquals(14 / 2.0 * (14 + 1), Arrays.stream(list.buffer).sum());
        assertTrue(ArrayUtil.isPermutation(list));
    }

    @Test
    public void testIsPermutation() {
        assertTrue(ArrayUtil.isPermutation(IntArrayList.from()));
        assertTrue(ArrayUtil.isPermutation(IntArrayList.from(0)));
        assertTrue(ArrayUtil.isPermutation(IntArrayList.from(0, 1)));
        assertTrue(ArrayUtil.isPermutation(IntArrayList.from(6, 2, 4, 0, 1, 3, 5)));
        assertFalse(ArrayUtil.isPermutation(IntArrayList.from(1, 2)));
        assertFalse(ArrayUtil.isPermutation(IntArrayList.from(-1)));
        assertFalse(ArrayUtil.isPermutation(IntArrayList.from(1)));
        assertFalse(ArrayUtil.isPermutation(IntArrayList.from(3, 4, 0, 1)));
        assertFalse(ArrayUtil.isPermutation(IntArrayList.from(0, 1, 3, 3, 4, 4, 6)));
    }

    @Test
    public void testReverse() {
        assertEquals(from(), ArrayUtil.reverse(from()));
        assertEquals(from(1), ArrayUtil.reverse(from(1)));
        assertEquals(from(9, 5), ArrayUtil.reverse(from(5, 9)));
        assertEquals(from(7, 1, 3), ArrayUtil.reverse(from(3, 1, 7)));
        assertEquals(from(4, 3, 2, 1), ArrayUtil.reverse(from(1, 2, 3, 4)));
        assertEquals(from(5, 4, 3, 2, 1), ArrayUtil.reverse(from(1, 2, 3, 4, 5)));
    }

    @Test
    public void testShuffle() {
        assertEquals(from(4, 1, 3, 2), ArrayUtil.shuffle(from(1, 2, 3, 4), new Random(0)));
        assertEquals(from(4, 3, 2, 1, 5), ArrayUtil.shuffle(from(1, 2, 3, 4, 5), new Random(1)));
    }

    @Test
    public void removeConsecutiveDuplicates() {
        int[] arr = new int[]{3, 3, 4, 2, 1, -3, -3, 9, 3, 6, 6, 7, 7};
        assertEquals(9, ArrayUtil.removeConsecutiveDuplicates(arr, arr.length));
        // note that only the first 9 elements should be considered the 'valid' range
        assertEquals(IntArrayList.from(3, 4, 2, 1, -3, 9, 3, 6, 7, 6, 6, 7, 7), IntArrayList.from(arr));

        int[] brr = new int[]{4, 4, 3, 5, 3};
        assertEquals(2, ArrayUtil.removeConsecutiveDuplicates(brr, 3));
        assertEquals(IntArrayList.from(4, 3, 3, 5, 3), IntArrayList.from(brr));
    }

    @Test
    public void removeConsecutiveDuplicates_empty() {
        int[] arr = new int[]{};
        assertEquals(0, ArrayUtil.removeConsecutiveDuplicates(arr, arr.length));
        arr = new int[]{3};
        assertEquals(1, ArrayUtil.removeConsecutiveDuplicates(arr, arr.length));
        assertEquals(0, ArrayUtil.removeConsecutiveDuplicates(arr, 0));
    }

    @Test
    public void testWithoutConsecutiveDuplicates() {
        assertEquals(from(), ArrayUtil.withoutConsecutiveDuplicates(from()));
        assertEquals(from(1), ArrayUtil.withoutConsecutiveDuplicates(from(1)));
        assertEquals(from(1), ArrayUtil.withoutConsecutiveDuplicates(from(1, 1)));
        assertEquals(from(1), ArrayUtil.withoutConsecutiveDuplicates(from(1, 1, 1)));
        assertEquals(from(1, 2), ArrayUtil.withoutConsecutiveDuplicates(from(1, 1, 2)));
        assertEquals(from(1, 2, 1), ArrayUtil.withoutConsecutiveDuplicates(from(1, 2, 1)));
        assertEquals(
                from(5, 6, 5, 8, 9, 11, 2, -1, 3),
                ArrayUtil.withoutConsecutiveDuplicates(from(5, 5, 5, 6, 6, 5, 5, 8, 9, 11, 11, 2, 2, -1, 3, 3)));
    }

    @Test
    public void testTransform() {
        IntArrayList arr = from(7, 6, 2);
        ArrayUtil.transform(arr, ArrayUtil.constant(8, 4));
        assertEquals(IntArrayList.from(4, 4, 4), arr);

        IntArrayList brr = from(3, 0, 1);
        ArrayUtil.transform(brr, IntArrayList.from(6, 2, 1, 5));
        assertEquals(IntArrayList.from(5, 6, 2), brr);
    }

    @Test
    public void testCalcSortOrder() {
        assertEquals(from(), from(ArrayUtil.calcSortOrder(from(), from())));
        assertEquals(from(0), from(ArrayUtil.calcSortOrder(from(3), from(4))));
        assertEquals(from(0, 2, 3, 1), from(ArrayUtil.calcSortOrder(from(3, 6, 3, 4), from(0, -1, 2, -6))));
        assertEquals(from(2, 3, 1, 0), from(ArrayUtil.calcSortOrder(from(3, 3, 0, 0), from(0, -1, 1, 2))));
        assertEquals(from(), from(ArrayUtil.calcSortOrder(new int[]{3, 3, 0, 0}, new int[]{0, -1, 1, 2}, 0)));
        assertEquals(from(0), from(ArrayUtil.calcSortOrder(new int[]{3, 3, 0, 0}, new int[]{0, -1, 1, 2}, 1)));
        assertEquals(from(1, 0), from(ArrayUtil.calcSortOrder(new int[]{3, 3, 0, 0}, new int[]{0, -1, 1, 2}, 2)));
        assertEquals(from(2, 1, 0), from(ArrayUtil.calcSortOrder(new int[]{3, 3, 0, 0}, new int[]{0, -1, 1, 2}, 3)));
        assertEquals(from(2, 3, 1, 0), from(ArrayUtil.calcSortOrder(new int[]{3, 3, 0, 0}, new int[]{0, -1, 1, 2}, 4)));
    }

    @Test
    public void testApplyOrder() {
        assertEquals(from(0, 6, 3, 1, 4), from(ArrayUtil.applyOrder(new int[]{3, 4, 6, 0, 1}, new int[]{3, 2, 0, 4, 1})));
    }

    @Test
    public void testInvert() {
        assertEquals(from(-1, -1, -1, 3), from(ArrayUtil.invert(new int[]{3, 3, 3, 3})));
        assertEquals(from(3, 2, 0, 1), from(ArrayUtil.invert(new int[]{2, 3, 1, 0})));
        assertEquals(from(2, 3, 1, 0), from(ArrayUtil.invert(new int[]{3, 2, 0, 1})));
    }

    @Test
    public void testMerge() {
        assertArrayEquals(new int[]{}, ArrayUtil.merge(new int[]{}, new int[]{}));
        assertArrayEquals(new int[]{4, 5}, ArrayUtil.merge(new int[]{}, new int[]{4, 5}));
        assertArrayEquals(new int[]{4, 5}, ArrayUtil.merge(new int[]{4, 5}, new int[]{}));
        assertArrayEquals(new int[]{3, 6, 9}, ArrayUtil.merge(new int[]{6, 6, 6, 9}, new int[]{3, 9}));
        int[] a = {2, 6, 8, 12, 15};
        int[] b = {3, 7, 9, 10, 11, 12, 15, 20, 21, 26};
        assertEquals(from(2, 3, 6, 7, 8, 9, 10, 11, 12, 15, 20, 21, 26), from(ArrayUtil.merge(a, b)));
    }

    /**
     * Test: testSubList_NormalRange
     *
     * Intention :
     * Vérifie que subList retourne correctement une sous-liste
     * lorsqu’on lui fournit un intervalle valide (indices dans les bornes).
     *
     * Données de test :
     * La liste d’entrée est [1,2,3,4,5] et les indices sont (1,4).
     * On choisit cette liste car elle est simple et ordonnée,
     * et l’intervalle permet d’extraire une sous-partie centrale.
     *
     * Oracle :
     * Le résultat attendu est [2,3,4].
     * Si la méthode retourne cette sous-liste, le test est réussi ;
     * sinon, cela indique un problème d’indexation (off-by-one).
     */
    @Test
    public void testSubList() {
        IntArrayList list = from(1, 2, 3, 4, 5);
        IntArrayList sub = ArrayUtil.subList(list, 1, 4);
        assertEquals(from(2, 3, 4), sub);
    }

    /** 
     * Test: testCalcSortOrder_InvalidLength
     *
     * Intention :
     * Vérifie que calcSortOrder lance une IllegalArgumentException
     * lorsque la longueur demandée ne correspond pas à la taille des tableaux.
     *
     * Données de test :
     * - Cas 1 : arr1=[1,2,3], arr2=[4,5,6], length=4 → length trop grand.
     * - Cas 2 : arr1=[1,2,3], arr2=[7,8], length=3 → arr2 trop court.
     * On utilise de petits tableaux explicites pour rendre le test lisible.
     *
     * Oracle :
     * Dans les deux cas, une IllegalArgumentException doit être levée,
     * car la précondition sur la taille n’est pas respectée.
     */
    @Test
    public void testCalcSortOrder_InvalidLength() {
        int[] arr1 = {1, 2, 3};
        int[] arr2 = {4, 5, 6};
        // length is greater than arr1 and arr2 length
        assertThrows(IllegalArgumentException.class, () -> {
            ArrayUtil.calcSortOrder(arr1, arr2, 4);
        });
        // arr2 is shorter than length
        int[] arr2Short = {7, 8};
        assertThrows(IllegalArgumentException.class, () -> {
            ArrayUtil.calcSortOrder(arr1, arr2Short, 3);
        });
    }

    /**
     * Test: testApplyOrder_InvalidOrderLength_ThrowsException
     *
     * Intention :
     * Vérifie que applyOrder(int[], int[]) rejette un ordre dont la longueur
     * est supérieure à celle du tableau source.
     *
     * Données de test :
     * arr = [10,20,30] (longueur 3),
     * order = [2,1,0,3] (longueur 4).
     * Le mismatch volontaire de taille provoque une erreur.
     *
     * Oracle :
     * Une IllegalArgumentException doit être levée.
     * Cela valide que la méthode vérifie la cohérence de la longueur de order.
     */
    @Test
    public void testApplyOrder_InvalidOrderLength() {
        int[] arr = {10, 20, 30};
        int[] order = {2, 1, 0, 3}; // order.length > arr.length
        assertThrows(IllegalArgumentException.class, () -> {
            ArrayUtil.applyOrder(arr, order);
        });
    }

    /**
     * Test: testZero
     * 
     * Intention :
     * Vérifie que la méthode zero(int) génère correctement un tableau
     * rempli de zéros de la taille spécifiée.
     * 
     * Données de test :
     * Trois cas sont testés : taille 0, taille 1 et taille 5.
     * Ces cas couvrent les scénarios de base, y compris le cas limite
     * de taille zéro.
     * 
     * Oracle :
     * Pour chaque cas, on compare le tableau généré avec le tableau
     * attendu rempli de zéros. Si les tableaux correspondent,
     * le test est réussi, sinon il échoue.
     */
    @Test
    public void testZero(){

        assertEquals(ArrayUtil.zero(0), from());
        assertEquals(ArrayUtil.zero(1), from(0));
        assertEquals(ArrayUtil.zero(5), from(0,0,0,0,0));
        
    }

    /**
     * Test: testRemoveConsecutiveDuplicatesErrors
     * 
     * Intention :
     * Vérifie que removeConsecutiveDuplicates(int[], int) lance une exception
     * lorsque la longueur fournie est invalide (négative ou supérieure à la taille
     * du tableau).
     * 
     * Données de test :
     * Un tableau d’entiers avec des doublons consécutifs est utilisé.
     * Les longueurs testées sont -1 (négative) et arr.length + 1 (trop grande).
     * 
     * Oracle :
     * Une IllegalArgumentException doit être levée pour la longueur négative,
     * et une ArrayIndexOutOfBoundsException pour la longueur trop grande.
     * Cela confirme que la méthode valide correctement la longueur d’entrée.
     */
    @Test
    public void testRemoveConsecutiveDuplicatesErrors () {
        int[] arr = new int[]{3, 3, 4, 2, 1, -3, -3, 9, 3, 6, 6, 7, 7};
        assertThrows(IllegalArgumentException.class, () -> ArrayUtil.removeConsecutiveDuplicates(arr, -1));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> ArrayUtil.removeConsecutiveDuplicates(arr, arr.length + 1));
    }

    /**
     * Test: testCalcSortOrder_IntArrayList_UnequalSize_ThrowsException_Faker
     *
     * Intention :
     * Vérifie que calcSortOrder(IntArrayList, IntArrayList) rejette deux listes
     * de tailles différentes en lançant une IllegalArgumentException a l'aide de JavaFaker.
     * 
     * L'utilisation de JavaFaker est justifiée ici afin de générer dynamiquement
     * des entiers aléatoires dans des listes d'entrée. Cela permet 
     * d'éviter de hardcoder des valeurs arbitraires et de couvrir un éventail
     * plus large de scénarios possibles en creeant deux listes contenant des entiers aleatoires. 
     *
     * Données de test :
     * Les tailles des deux listes sont générées aléatoirement avec Faker
     * dans une plage 3–10, en s’assurant qu’elles sont inégales.
     * Les contenus sont remplis de valeurs aléatoires (1–100),
     * mais le contenu importe peu, seule la différence de taille compte.
     *
     * Oracle :
     * Une IllegalArgumentException doit être levée car les deux listes
     * n’ont pas la même taille. Le test est déterministe sur le résultat attendu,
     * même si les valeurs varient.
     */
    @Test
    void testCalcSortOrder_IntArrayList_UnequalSize_ThrowsException_Faker() {
        Faker faker = new Faker();

        // Generate random sizes for the lists (ensuring they are unequal)
        int size1 = faker.number().numberBetween(3, 10);
        int size2;
        do {
            size2 = faker.number().numberBetween(1, 10);
        } while (size2 == size1); // ensure sizes are unequal

        // Fill the lists with random integers (1–100)
        IntArrayList arr1 = new IntArrayList();
        IntArrayList arr2 = new IntArrayList();

        for (int i = 0; i < size1; i++) arr1.add(faker.number().numberBetween(1, 100));
        for (int i = 0; i < size2; i++) arr2.add(faker.number().numberBetween(1, 100));

        // The test should still throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            ArrayUtil.calcSortOrder(arr1, arr2);
        });
    }

    

}
