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

package com.graphhopper.routing;

import com.carrotsearch.hppc.IntArrayList;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValueImpl;
import com.graphhopper.routing.ev.SimpleBooleanEncodedValue;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.DistanceCalcEuclidean;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.Helper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PointList;

class HeadingResolverTest {

    @Test
    public void straightEdges() {
        // 0 1 2
        // \|/
        // 7 -- 8 --- 3
        // /|\
        // 6 5 4
        BooleanEncodedValue accessEnc = new SimpleBooleanEncodedValue("access", true);
        DecimalEncodedValue speedEnc = new DecimalEncodedValueImpl("speed", 5, 5, false);
        EncodingManager em = EncodingManager.start().add(accessEnc).add(speedEnc).build();
        BaseGraph graph = new BaseGraph.Builder(em).create();
        NodeAccess na = graph.getNodeAccess();
        na.setNode(0, 49.5073, 1.5545);
        na.setNode(1, 49.5002, 2.3895);
        na.setNode(2, 49.4931, 3.3013);
        na.setNode(3, 48.8574, 3.2025);
        na.setNode(4, 48.2575, 3.0651);
        na.setNode(5, 48.2393, 2.2576);
        na.setNode(6, 48.2246, 1.2249);
        na.setNode(7, 48.8611, 1.2194);
        na.setNode(8, 48.8538, 2.3950);

        GHUtility.setSpeed(60, true, true, accessEnc, speedEnc, graph.edge(8, 0).setDistance(10)); // edge 0
        GHUtility.setSpeed(60, true, true, accessEnc, speedEnc, graph.edge(8, 1).setDistance(10)); // edge 1
        GHUtility.setSpeed(60, true, true, accessEnc, speedEnc, graph.edge(8, 2).setDistance(10)); // edge 2
        GHUtility.setSpeed(60, true, true, accessEnc, speedEnc, graph.edge(8, 3).setDistance(10)); // edge 3
        GHUtility.setSpeed(60, true, true, accessEnc, speedEnc, graph.edge(8, 4).setDistance(10)); // edge 4
        GHUtility.setSpeed(60, true, true, accessEnc, speedEnc, graph.edge(8, 5).setDistance(10)); // edge 5
        GHUtility.setSpeed(60, true, true, accessEnc, speedEnc, graph.edge(8, 6).setDistance(10)); // edge 6
        GHUtility.setSpeed(60, true, true, accessEnc, speedEnc, graph.edge(8, 7).setDistance(10)); // edge 7

        HeadingResolver resolver = new HeadingResolver(graph);
        // using default tolerance
        assertEquals(IntArrayList.from(7, 6, 0), resolver.getEdgesWithDifferentHeading(8, 90));
        assertEquals(IntArrayList.from(7, 6, 0), resolver.setTolerance(100).getEdgesWithDifferentHeading(8, 90));
        assertEquals(IntArrayList.from(7, 6, 5, 4, 2, 1, 0),
                resolver.setTolerance(10).getEdgesWithDifferentHeading(8, 90));
        assertEquals(IntArrayList.from(7, 6, 5, 1, 0), resolver.setTolerance(60).getEdgesWithDifferentHeading(8, 90));

        assertEquals(IntArrayList.from(1), resolver.setTolerance(170).getEdgesWithDifferentHeading(8, 180));
        assertEquals(IntArrayList.from(2, 1, 0), resolver.setTolerance(130).getEdgesWithDifferentHeading(8, 180));

        assertEquals(IntArrayList.from(5, 4, 3), resolver.setTolerance(90).getEdgesWithDifferentHeading(8, 315));
        assertEquals(IntArrayList.from(6, 5, 4, 3, 2), resolver.setTolerance(50).getEdgesWithDifferentHeading(8, 315));
    }

    @Test
    public void curvyEdge() {
        // 1 -|
        // |- 0 -|
        // |- 2
        BooleanEncodedValue accessEnc = new SimpleBooleanEncodedValue("access", true);
        DecimalEncodedValue speedEnc = new DecimalEncodedValueImpl("speed", 5, 5, false);
        EncodingManager em = EncodingManager.start().add(accessEnc).add(speedEnc).build();
        BaseGraph graph = new BaseGraph.Builder(em).create();
        NodeAccess na = graph.getNodeAccess();
        na.setNode(1, 0.01, 0.00);
        na.setNode(0, 0.00, 0.00);
        na.setNode(2, -0.01, 0.00);
        GHUtility.setSpeed(60, true, true, accessEnc, speedEnc, graph.edge(0, 1).setDistance(10))
                .setWayGeometry(Helper.createPointList(0.00, 0.01, 0.01, 0.01));
        GHUtility.setSpeed(60, true, true, accessEnc, speedEnc, graph.edge(0, 2).setDistance(10))
                .setWayGeometry(Helper.createPointList(0.00, -0.01, -0.01, -0.01));
        HeadingResolver resolver = new HeadingResolver(graph);
        resolver.setTolerance(120);
        // asking for the edges not going east returns 0-2
        assertEquals(IntArrayList.from(1), resolver.getEdgesWithDifferentHeading(0, 90));
        // asking for the edges not going west returns 0-1
        assertEquals(IntArrayList.from(0), resolver.getEdgesWithDifferentHeading(0, 270));
    }

    @Test
    public void withQueryGraph() {
        // 2
        // 0 -x- 1
        BooleanEncodedValue accessEnc = new SimpleBooleanEncodedValue("access", true);
        DecimalEncodedValue speedEnc = new DecimalEncodedValueImpl("speed", 5, 5, false);
        EncodingManager em = EncodingManager.start().add(accessEnc).add(speedEnc).build();
        BaseGraph graph = new BaseGraph.Builder(em).create();
        NodeAccess na = graph.getNodeAccess();
        na.setNode(0, 48.8611, 1.2194);
        na.setNode(1, 48.8538, 2.3950);

        EdgeIteratorState edge = GHUtility.setSpeed(60, true, true, accessEnc, speedEnc,
                graph.edge(0, 1).setDistance(10));
        Snap snap = createSnap(edge, 48.859, 2.00, 0);
        QueryGraph queryGraph = QueryGraph.create(graph, snap);
        HeadingResolver resolver = new HeadingResolver(queryGraph);

        // if the heading points East we get the Western edge 0->2
        assertEquals("0->2", queryGraph.getEdgeIteratorState(1, Integer.MIN_VALUE).toString());
        assertEquals(IntArrayList.from(1), resolver.getEdgesWithDifferentHeading(2, 90));

        // if the heading points West we get the Eastern edge 2->1
        assertEquals("2->1", queryGraph.getEdgeIteratorState(2, Integer.MIN_VALUE).toString());
        assertEquals(IntArrayList.from(2), resolver.getEdgesWithDifferentHeading(2, 270));
    }

    private Snap createSnap(EdgeIteratorState closestEdge, double lat, double lon, int wayIndex) {
        Snap snap = new Snap(lat, lon);
        snap.setClosestEdge(closestEdge);
        snap.setSnappedPosition(Snap.Position.EDGE);
        snap.setWayIndex(wayIndex);
        snap.calcSnappedPoint(new DistanceCalcEuclidean());
        return snap;
    }

    // PS : je suis désolé si les commentaires font des retours a la ligne bizarres,
    // c'est parceque j'ai l'extension de justify sur vscode qui le fait
    // automatiquement
    // des que je save le fichier, merci :)

    /**
     * Test: testEdgesMockedSimilarHeading
     *
     * Intention du test:
     * Vérifie que la méthode getEdgesWithDifferentHeading() retourne une liste vide
     * lorsque toutes les arêtes adjacentes à un noeud ont une orientation similaire
     * au heading fourni (dans la tolérance définie).
     *
     * Classes simulées et justification :
     * - EdgeExplorer : Cette interface est simulée car elle représente
     * l'exploration
     * des arêtes d'un graphe. Utiliser un mock permet de tester la logique de
     * filtrage par orientation sans avoir besoin d'instancier un graphe complet.
     * Cela isole le test et le rend plus rapide et déterministe.
     * - EdgeIterator : Cette interface représente l'itération sur les arêtes
     * adjacentes. Le mock simule une seule arête avec une géométrie contrôlée.
     *
     * Mocks utilisés :
     * - EdgeExplorer mock : configuré pour retourner un EdgeIterator mocké lorsque
     * setBaseNode() est appelé.
     * - EdgeIterator mock : configuré pour simuler une arête avec une orientation
     * similaire au heading recherché (90 degrés = Est).
     *
     * Valeurs simulées :
     * - Base node : 0 (noeud de départ)
     * - Heading : 90 degrés (direction Est)
     * - Tolérance : 100 degrés (par défaut)
     * - Géométrie de l'arête : deux points alignés Est (lat=31.0, lon=-7.0 à -6.99)
     * Cette orientation correspond environ à 90 degrés, donc dans la tolérance.
     *
     * Coordonnées utilisées (Maroc) :
     * - Point 1 : lat=31.0, lon=-7.0 (point de départ)
     * - Point 2 : lat=31.0, lon=-6.99 (légèrement à l'Est)
     *
     * Oracle :
     * - La liste retournée doit être vide (aucune arête avec heading différent)
     * - EdgeExplorer.setBaseNode() doit être appelé avec le noeud 0
     * - EdgeIterator.next() doit être appelé (pour itérer)
     * - EdgeIterator.fetchWayGeometry() doit être appelé pour obtenir la géométrie
     * Si le test réussit, cela confirme que HeadingResolver filtre correctement
     * les arêtes selon leur orientation et la tolérance définie.
     */
    @Test
    public void testEdgesMockedSimilarHeading() {
        // Création des mocks de EdgeExplorer et EdgeIterator
        EdgeExplorer mockEdgeExplorer = mock(EdgeExplorer.class);
        EdgeIterator mockEdgeIterator = mock(EdgeIterator.class);

        // Configuration du mock EdgeIterator
        // Simuler une seule arête (next() retourne true une fois, puis false)
        // avec un ID d'arête arbitraire (0)
        when(mockEdgeIterator.next()).thenReturn(true).thenReturn(false);
        when(mockEdgeIterator.getEdge()).thenReturn(0);

        // Créer une géométrie avec deux points alignés vers l'Est
        PointList pointList = new PointList(2, false);
        pointList.add(31.0, -7.0); // Point de départ
        pointList.add(31.0, -6.99); // Point légèrement à l'est

        when(mockEdgeIterator.fetchWayGeometry(FetchMode.ALL)).thenReturn(pointList);

        // Configuration du mock EdgeExplorer pour retourner le mock EdgeIterator
        when(mockEdgeExplorer.setBaseNode(0)).thenReturn(mockEdgeIterator);

        // Créer un HeadingResolver avec un Graph mocké
        HeadingResolver resolver = new HeadingResolver(mock(com.graphhopper.storage.Graph.class));

        // Injecter le mock EdgeExplorer via reflect (car c'est un champ privé)
        try {
            java.lang.reflect.Field field = HeadingResolver.class.getDeclaredField("edgeExplorer");
            field.setAccessible(true);
            field.set(resolver, mockEdgeExplorer);
        } catch (Exception e) {
            throw new RuntimeException("Impossible d'injecter le mock EdgeExplorer", e);
        }

        // Appel de la méthode à tester avec heading = 90 deg (Est)
        IntArrayList result = resolver.getEdgesWithDifferentHeading(0, 90.0);

        // Vérifications
        // La liste doit être vide car l'arête a un heading similaire
        assertEquals(0, result.size());

        // Vérifier que les méthodes du mock ont été appelées
        verify(mockEdgeExplorer).setBaseNode(0);
        verify(mockEdgeIterator, atLeastOnce()).next();
        verify(mockEdgeIterator).fetchWayGeometry(FetchMode.ALL);
    }

    /**
     * Test: testEdgesMockedDifferentHeading
     *
     * Intention du test:
     * Vérifie que la méthode getEdgesWithDifferentHeading() retourne correctement
     * les arêtes dont l'orientation diffère significativement du heading fourni
     * (au-delà de la tolérance définie).
     *
     * Classes simulées et justification :
     * - EdgeExplorer : Cette interface est simulée pour contrôler précisément
     * les arêtes retournées sans créer un graphe complet. Cela permet de tester
     * uniquement la logique de filtrage par orientation.
     *
     * - EdgeIterator : Cette interface simule deux arêtes :
     * 1. Une arête orientée Nord (heading 0 deg environ)
     * 2. Une arête orientée Sud (heading 180 deg environ)
     * Les deux ont des orientations très différentes de l'Est (90 deg).
     *
     * Mocks utilisés :
     * - EdgeExplorer mock : retourne un EdgeIterator qui simule 2 arêtes.
     * - EdgeIterator mock : configuré pour retourner deux arêtes avec des
     * géométries perpendiculaires au heading recherché.
     *
     * Valeurs simulées :
     * - Base node : 1
     * - Heading : 90 degrés
     * - Tolérance : 45 degrés (stricte)
     * - Arête 0 : orientation Nord (0 deg environ), différence de 90 deg avec le
     * heading
     * - Arête 1 : orientation Sud (180 deg environ), différence de 90 deg avec le
     * heading
     *
     * Coordonnées utilisées :
     * - Arête 0 : de (31.0, -7.0) vers (31.01, -7.0) = direction Nord
     * - Arête 1 : de (31.0, -7.0) vers (30.99, -7.0) = direction Sud
     *
     * Oracle :
     * - La liste retournée doit contenir 2 arêtes (IDs 0 et 1)
     * - Les deux arêtes doivent être filtrées car leur orientation diffère
     * de plus de 45 deg par rapport au heading de 90 deg
     * - EdgeExplorer.setBaseNode() doit être appelé avec le noeud 1
     * - EdgeIterator.next() doit être appelé 3 fois (2 true, 1 false)
     * - EdgeIterator.fetchWayGeometry() doit être appelé 2 fois
     *
     * Si le test réussit, cela confirme que HeadingResolver identifie correctement
     * les arêtes avec des orientations différentes du heading cible.
     */
    @Test
    public void testEdgesMockedDifferentHeading() {
        // Création des mocks
        EdgeExplorer mockEdgeExplorer = mock(EdgeExplorer.class);
        EdgeIterator mockEdgeIterator = mock(EdgeIterator.class);

        // Configuration du mock EdgeIterator pour simuler 2 arêtes
        // next() retourne true deux fois (2 arêtes), puis false
        when(mockEdgeIterator.next()).thenReturn(true, true, false);

        // Première arête (ID 0) : orientation Nord
        // Deuxième arête (ID 1) : orientation Sud
        when(mockEdgeIterator.getEdge()).thenReturn(0, 1);

        // Créer des géométries avec orientations différentes du heading (90 deg = Est)

        // Arête 0 : orientation Nord (0 deg environ)
        PointList pointListNorth = new PointList(2, false);
        pointListNorth.add(31.0, -7.0); // Point de départ
        pointListNorth.add(31.01, -7.0); // Point au Nord

        // Arête 1 : orientation Sud (180 deg environ)
        PointList pointListSouth = new PointList(2, false);
        pointListSouth.add(31.0, -7.0); // Point de départ
        pointListSouth.add(30.99, -7.0); // Point au Sud

        // Retourner les géométries alternativement
        when(mockEdgeIterator.fetchWayGeometry(FetchMode.ALL))
                .thenReturn(pointListNorth)
                .thenReturn(pointListSouth);

        // Configuration du mock EdgeExplorer
        when(mockEdgeExplorer.setBaseNode(1)).thenReturn(mockEdgeIterator);

        // Créer un HeadingResolver
        HeadingResolver resolver = new HeadingResolver(mock(com.graphhopper.storage.Graph.class));

        // Injecter le mock EdgeExplorer via reflect
        try {
            java.lang.reflect.Field field = HeadingResolver.class.getDeclaredField("edgeExplorer");
            field.setAccessible(true);
            field.set(resolver, mockEdgeExplorer);
        } catch (Exception e) {
            throw new RuntimeException("Impossible d'injecter le mock EdgeExplorer", e);
        }

        // Définir une tolérance stricte de 45 degrés
        resolver.setTolerance(45);

        // Appel de la méthode à tester avec heading = 90 deg (Est)
        IntArrayList result = resolver.getEdgesWithDifferentHeading(1, 90.0);

        // Vérifications
        // Les deux arêtes doivent être retournées car elles ont des headings
        // très différents (Nord et Sud vs Est)
        assertEquals(2, result.size(), "Deux arêtes devraient être retournées");
        assertEquals(0, result.get(0), "La première arête devrait avoir l'ID 0");
        assertEquals(1, result.get(1), "La deuxième arête devrait avoir l'ID 1");

        // Vérifier que les méthodes du mock ont été appelées
        verify(mockEdgeExplorer).setBaseNode(1);
        verify(mockEdgeIterator, times(3)).next(); // 2 true + 1 false
        verify(mockEdgeIterator, times(2)).fetchWayGeometry(FetchMode.ALL); // une fois par arête
        verify(mockEdgeIterator, times(2)).getEdge(); // une fois par arête
    }

}