package com.example.sae41_2024;

/**
 * Représente un point coloré dans la grille du jeu.
 * Chaque point possède des coordonnées (x, y) et un identifiant de couleur.
 */
public class FlowPoint {
    private int colorIndex;
    private int x, y, color;

    /**
     * Crée un point à une position donnée avec une couleur spécifique.
     *
     * @param x          La position en colonne (horizontalement).
     * @param y          La position en ligne (verticalement).
     * @param colorIndex L'indice de la couleur (utilisé pour relier les bons points entre eux).
     */
    public FlowPoint(int x, int y, int colorIndex) {
        this.x = x;
        this.y = y;
        this.colorIndex = colorIndex;
    }

    /**
     * Retourne la position horizontale (colonne) du point.
     *
     * @return L'abscisse du point.
     */
    public int getX() {
        return x;
    }

    /**
     * Retourne la position verticale (ligne) du point.
     *
     * @return L'ordonnée du point.
     */
    public int getY() {
        return y;
    }

    /**
     * Retourne l'indice de couleur associé à ce point.
     *
     * @return L'indice de la couleur.
     */
    public int getColorIndex() {
        return colorIndex;
    }
}