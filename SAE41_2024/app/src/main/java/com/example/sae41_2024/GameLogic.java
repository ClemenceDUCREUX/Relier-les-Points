package com.example.sae41_2024;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gère la logique principale du jeu : chargement des niveaux, validation des tracés,
 * gestion des chemins, des points et des règles de victoire.
 */
public class GameLogic {
    private int gridSize;
    private boolean isAchromateMode = false;
    private List<FlowPoint> points;
    private Map<Integer, List<FlowPoint>> pointPairs;
    private Map<Integer, List<int[]>> paths;
    private String levelName = "FILE_NAME";

    // Définition des nuances de gris pour le mode achromate
    private static final int[] grayShades = {
            Color.rgb(0, 0, 0),         // Noir
            Color.rgb(230, 230, 230),   // Très clair, mais pas blanc
            Color.rgb(60, 60, 60),      // Foncé
            Color.rgb(200, 200, 200),   // Très clair
            Color.rgb(30, 30, 30),      // Très foncé
            Color.rgb(170, 170, 170),   // Moyen-clair
            Color.rgb(90, 90, 90),      // Moyen-foncé
            Color.rgb(140, 140, 140),   // Moyen
            Color.rgb(115, 115, 115),   // Moyen équilibré
            Color.rgb(210, 210, 210),   // Encore clair mais visible
            Color.rgb(130, 120, 130)    // Légèrement plus marqué que le moyen
    };

    /**
     * Initialise la logique du jeu avec une taille de grille donnée.
     * @param gridSize La taille de la grille (ex : 5x5, 6x6…)
     */
    public GameLogic(int gridSize) {
        this.gridSize = gridSize;
        this.points = new ArrayList<>();
        this.pointPairs = new HashMap<>();
        this.paths = new HashMap<>();
    }

    /**
     * Active ou désactive le mode achromate (nuances de gris).
     * @param isAchromate true pour activer, false pour désactiver
     */
    public void setAchromateMode(boolean isAchromate) {
        this.isAchromateMode = isAchromate;
    }

    /**
     * Charge un niveau depuis un fichier XML contenu dans les assets.
     * @param context Le contexte Android
     * @param name Nom interne du niveau
     * @param fileName Nom du fichier XML sans l'extension
     */
    public void loadLevelFromAssets(Context context, String name, String fileName) {
        points.clear();
        pointPairs.clear();
        paths.clear();

        try {
            InputStream is = context.getAssets().open("puzzles/" + fileName + ".xml");
            XmlPullParser parser = android.util.Xml.newPullParser();
            parser.setInput(is, "UTF-8");

            int eventType = parser.getEventType();
            int colorIndex = 0;
            List<FlowPoint> currentPair = new ArrayList<>();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();

                    if (tagName.equals("puzzle")) {
                        gridSize = Integer.parseInt(parser.getAttributeValue(null, "size"));
                        levelName = parser.getAttributeValue(null, "nom");

                        // Affiche la taille de la grille
                        System.out.println("Taille de la grille: " + gridSize);
                    } else if (tagName.equals("paire")) {
                        currentPair = new ArrayList<>();
                    } else if (tagName.equals("point")) {
                        int col = Integer.parseInt(parser.getAttributeValue(null, "colonne"));
                        int row = Integer.parseInt(parser.getAttributeValue(null, "ligne"));
                        int color = getColorByIndex(colorIndex, isAchromateMode);

                        FlowPoint point = new FlowPoint(col, row, colorIndex);
                        points.add(point);
                        currentPair.add(point);

                        // Affiche chaque point chargé
                        System.out.println("Point ajouté: (" + col + ", " + row + ") Couleur: " + color);

                        if (currentPair.size() == 2) {
                            pointPairs.put(color, new ArrayList<>(currentPair));
                            colorIndex++;
                        }
                    }
                }
                eventType = parser.next();
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retourne la taille actuelle de la grille.
     * @return La taille (ex: 5 pour une grille 5x5)
     */
    public int getGridSize() {
        return gridSize;
    }

    /**
     * Retourne une couleur en fonction de son index, selon le mode actif.
     * @param index L’index de la couleur
     * @param isAchromateMode Si true, retourne une nuance de gris
     * @return Une couleur (RGB)
     */
    public int getColorByIndex(int index, boolean isAchromateMode) {
        Log.d("DEBUG_FLOW", "Couleur demandée pour index " + index + " | Achromate = " + isAchromateMode);
        if (!isAchromateMode) {
            int[] colors = {
                    Color.rgb(255, 0, 0),   // Rouge
                    Color.rgb(0, 0, 255),   // Bleu
                    Color.rgb(0, 255, 0),   // Vert
                    Color.rgb(255, 255, 0), // Jaune
                    Color.rgb(0, 255, 255), // Cyan
                    Color.rgb(255, 0, 255), // Magenta
                    Color.rgb(255, 165, 0), // Orange
                    Color.rgb(128, 0, 128), // Violet
                    Color.rgb(165, 42, 42), // Marron
                    Color.rgb(0, 130, 127), // Vert sapin
                    Color.rgb(75, 0, 130)   // Indigo
            };
            return colors[index % colors.length];
        } else {
            return getGrayShade(index);
        }
    }

    /**
     * Retourne une nuance de gris en fonction de l’index fourni.
     * @param index L’index de la couleur
     * @return Une couleur en niveaux de gris
     */
    private int getGrayShade(int index) {
        return grayShades[index % grayShades.length];
    }

    /**
     * Retourne la liste de tous les points du niveau.
     * @return Liste de FlowPoint
     */
    public List<FlowPoint> getPoints() {
        return points;
    }

    /**
     * Renvoie un point s’il existe à la position (x, y).
     * @param x Colonne
     * @param y Ligne
     * @return Le point trouvé ou null
     */
    public FlowPoint getPointAt(int x, int y) {
        for (FlowPoint point : points) {
            if (point.getX() == x && point.getY() == y) {
                return point;
            }
        }
        return null;
    }

    /**
     * Enregistre un chemin tracé par le joueur pour une couleur donnée.
     * @param color Index de la couleur
     * @param path Liste des coordonnées du chemin
     */
    public void savePath(int color, List<int[]> path) {
        paths.put(color, new ArrayList<>(path));
    }

    /**
     * Retourne tous les chemins actuellement tracés.
     * @return Map des chemins par couleur
     */
    public Map<Integer, List<int[]>> getPaths() {
        return paths;
    }

    /**
     * Vérifie si tous les chemins sont corrects et si la grille est complétée.
     * @return true si la partie est gagnée, false sinon
     */
    public boolean checkWin() {
        int gridSize = getGridSize();
        boolean[][] usedCells = new boolean[gridSize][gridSize];

        for (Map.Entry<Integer, List<int[]>> entry : paths.entrySet()) {
            List<int[]> path = entry.getValue();
            if (path.size() < 2) return false;

            int[] start = path.get(0);
            int[] end = path.get(path.size() - 1);

            FlowPoint startPoint = getPointAt(start[0], start[1]);
            FlowPoint endPoint = getPointAt(end[0], end[1]);

            if (startPoint == null || endPoint == null || startPoint.getColorIndex() != endPoint.getColorIndex()) {
                return false;
            }

            for (int[] pos : path) {
                usedCells[pos[0]][pos[1]] = true;
            }
        }

        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                if (!usedCells[x][y]) return false;
            }
        }

        return true;
    }

    /**
     * Supprime tous les chemins enregistrés (réinitialise la grille).
     */
    public void resetGrid() {
        paths.clear();
    }

    /**
     * Vérifie si un chemin existe déjà pour une couleur donnée.
     * @param color L’index de la couleur
     * @return true si un chemin existe, false sinon
     */
    public boolean isPathExists(int color) {
        return paths.containsKey(color);
    }

    /**
     * Supprime le chemin associé à une couleur.
     * @param color L’index de la couleur à supprimer
     */
    public void removePath(int color) {
        paths.remove(color);
    }
}