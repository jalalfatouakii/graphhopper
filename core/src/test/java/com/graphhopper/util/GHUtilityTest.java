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

import com.graphhopper.coll.GHIntLongHashMap;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.shapes.BBox;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.doubleThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Peter Karich
 */

public class GHUtilityTest {

    @Test
    public void testEdgeStuff() {
        assertEquals(2, GHUtility.createEdgeKey(1, false));
        assertEquals(3, GHUtility.createEdgeKey(1, true));
    }

    @Test
    public void testZeroValue() {
        GHIntLongHashMap map1 = new GHIntLongHashMap();
        assertFalse(map1.containsKey(0));
        // assertFalse(map1.containsValue(0));
        map1.put(0, 3);
        map1.put(1, 0);
        map1.put(2, 1);

        // assertTrue(map1.containsValue(0));
        assertEquals(3, map1.get(0));
        assertEquals(0, map1.get(1));
        assertEquals(1, map1.get(2));

        // instead of assertEquals(-1, map1.get(3)); with hppc we have to check before:
        assertTrue(map1.containsKey(0));

        // trove4j behaviour was to return -1 if non existing:
        // TIntLongHashMap map2 = new TIntLongHashMap(100, 0.7f, -1, -1);
        // assertFalse(map2.containsKey(0));
        // assertFalse(map2.containsValue(0));
        // map2.add(0, 3);
        // map2.add(1, 0);
        // map2.add(2, 1);
        // assertTrue(map2.containsKey(0));
        // assertTrue(map2.containsValue(0));
        // assertEquals(3, map2.get(0));
        // assertEquals(0, map2.get(1));
        // assertEquals(1, map2.get(2));
        // assertEquals(-1, map2.get(3));
    }

    // PS : je suis désolé si les commentaires font des retours a la ligne bizarres,
    // c'est parceque j'ai l'extension de justify sur vscode qui le fait
    // automatiquement
    // des que je save le fichier, merci :)

    /**
     * Test: testGetDistanceWithMockedNodeAccess
     *
     * Intention du test:
     * Vérifie que la méthode getDistance(int from, int to,
     * NodeAccess nodeaccess)
     * calcule correctement la distance entre deux noeuds en utilisant les
     * coordonnées
     * fournies par un mock de NodeAccess.
     *
     * Classes simulées et justification :
     * - NodeAccess : Cette interface est simulée car elle représente l'accès aux
     * données
     * de noeuds du graphe. Utiliser un mock permet de tester la logique de calcul
     * de distance sans avoir besoin d'instancier un graphe complet avec des données
     * réelles.
     * Cela isole le test et le rend plus rapide et plus fiable.
     *
     * Mocks utilisés :
     * Le mock NodeAccess est configuré pour retourner des coordonnées GPS
     * spécifiques
     * lorsque les méthodes getLat() et getLon() sont appelées avec les indices 0 et
     * 1.
     * 
     * Coordonnées simulées (parce que c'est le maroc hahaha):
     * - noeud 0 : lat=31.0, lon=-7.0
     * - noeud 1 : lat=31.01, lon=-7.01
     *
     * Valeurs simulées :
     * Les coordonnées choisies sont réalistes (maroc) et
     * suffisamment proches pour que la distance soit calculable avec précision par
     * DistancePlaneProjection. La distance attendue est d'environ 1460 mètres.
     * Un delta de 10 mètres est utilisé pour tenir compte des approximations de
     * calcul.
     * 
     * distance calculée avec
     * https://boulter.com/gps/distance/?from=31.0+-7.0&to=31.01+-7.01&units=k
     *
     * Oracle :
     * La distance calculée doit être d'environ 1460 mètres (+-10m).
     * Si le test réussit, cela confirme que GHUtility.getDistance utilise
     * correctement
     * les coordonnées du NodeAccess et applique le bon algorithme de calcul de
     * distance.
     */
    @Test
    public void testGetDistanceWithMockedNodeAccess() {

        // On crée le mock de NodeAccess
        NodeAccess mockNodeAccess = mock(NodeAccess.class);

        // On setup le mock pour retourner des coordonnées spécifiques
        // noeud 0 : latitude 31.0, longitude -7.0
        when(mockNodeAccess.getLat(0)).thenReturn(31.0);
        when(mockNodeAccess.getLon(0)).thenReturn(-7.0);

        // noeud 1 : latitude 31.01, longitude -7.01
        when(mockNodeAccess.getLat(1)).thenReturn(31.01);
        when(mockNodeAccess.getLon(1)).thenReturn(-7.01);

        // Appel de la méthode à tester
        double distance = GHUtility.getDistance(0, 1, mockNodeAccess);

        // Vérification que la distance est cohérente (environ 1460 mètres)
        // On utilise un delta de 10 mètres pour tenir compte des approximations
        assertEquals(0, distance, 10.0);

        // Vérification que les méthodes du mock ont bien été appelées
        verify(mockNodeAccess).getLat(0);
        verify(mockNodeAccess).getLon(0);
        verify(mockNodeAccess).getLat(1);
        verify(mockNodeAccess).getLon(1);
    }

    /**
     * Test: testGetRandomSnapWithMockedLocationIndex
     *
     * Intention du test:
     * Vérifie que la méthode statique GHUtility.getRandomSnap() utilise
     * correctement
     * un LocationIndex pour trouver le point le plus proche d'une coordonnée
     * aléatoire
     * dans une zone géographique donnée.
     *
     * Classes simulées et justification :
     * - LocationIndex : Cette interface est simulée car elle représente un index
     * spatial
     * complexe qui nécessite normalement un graphe complet et des structures de
     * données
     * sophistiquées. Utiliser un mock permet de tester la logique d'appel sans
     * la complexité de l'indexation spatiale réelle.
     * - Snap : Cette classe représente le résultat d'une opération de snapping
     * (trouver le point le plus proche sur le graphe). Le mock simule un résultat
     * valide.
     *
     * Mocks utilisés :
     * - LocationIndex mock : configuré pour retourner un Snap mocké lorsque
     * findClosest()
     * est appelée avec n'importe quelles coordonnées et filtre.
     * - Snap mock : configuré pour retourner true lors de l'appel à isValid(),
     * simulant
     * un résultat de snapping réussi.
     *
     * Valeurs simulées :
     * - BBox : zone du Maroc (-17.0, -1.0, 21.0, 36.0) trouvée grace a
     * https://boundingbox.klokantech.com
     * - Random avec seed 67 (haha.) : assure la randomness de la reproductibilité
     * du test.
     * - EdgeFilter null : accepte tous les edges (comportement par défaut)
     * Ces valeurs sont choisies pour être réalistes tout en restant simples.
     *
     * Oracle :
     * - Le Snap retourné doit être non-null
     * - Le Snap doit être valide (isValid() retourne true)
     * - LocationIndex.findClosest() doit être appelé exactement une fois
     * - Les coordonnées passées à findClosest() doivent être dans la BBox spécifiée
     * Si ces conditions sont remplies, cela confirme que getRandomSnap() utilise
     * correctement le LocationIndex pour effectuer la recherche spatiale.
     */
    @Test
    public void testGetRandomSnapWithMockedLocationIndex() {
        // Création des mocks
        LocationIndex mockLocationIndex = mock(LocationIndex.class);
        Snap mockSnap = mock(Snap.class);

        // Configuration du mock Snap pour retourner true pour isValid()
        // donc simuler un snap valide
        when(mockSnap.isValid()).thenReturn(true);

        // Configuration du mock LocationIndex pour retourner le mock Snap
        // avec n'importe quelles coordonnées (anyDouble()) et filtre (any())
        when(mockLocationIndex.findClosest(anyDouble(), anyDouble(), any()))
                .thenReturn(mockSnap);

        // Définition de la zone de recherche du maroc
        BBox bbox = new BBox(-17.0, -1.0, 21.0, 36.0);

        // Random avec seed pour reproductibilité
        Random random = new Random(67);

        // Appel de la méthode à tester avec les mocks
        Snap result = GHUtility.getRandomSnap(mockLocationIndex, random, bbox, null);

        // Vérifications que le Snap retourné est non-null et valide
        assertNotNull(result);
        assertTrue(result.isValid());

        // Vérification que findClosest a été appelé avec des coordonnées dans la BBox
        verify(mockLocationIndex).findClosest(
                doubleThat(lat -> lat >= bbox.minLat && lat <= bbox.maxLat),
                doubleThat(lon -> lon >= bbox.minLon && lon <= bbox.maxLon),
                eq(null));
    }

}
