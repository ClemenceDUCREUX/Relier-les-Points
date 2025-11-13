package com.example.sae41_2024;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Représente la vue du jeu : affichage de la grille, des points, des chemins, et gestion des interactions tactiles.
 */
public class GameView extends View {
    private Paint gridPaint, circlePaint, pathPaint;
    private int cellSize;
    private GameLogic gameLogic;
    private List<int[]> currentPath = new ArrayList<>();
    private List<int[]> animatedPath = new ArrayList<>();
    private int currentColor = 0;
    private int currentColorIndex = -1;
    private boolean isDrawing = false;
    private Context context;
    private boolean isAchromateMode = false;
    private int lastX = -1, lastY = -1;
    private int startX = 0;
    private int startY = 0;
    private boolean isGameWon = false;

    /**
     * Constructeur de la vue du jeu.
     *
     * @param context    Le contexte Android
     * @param gameLogic  L'instance de la logique du jeu à utiliser
     */
    public GameView(Context context, GameLogic gameLogic) {
        super(context);
        this.context = context;
        this.gameLogic = gameLogic;
        loadAchromateMode();
        init();
    }

    /**
     * Initialise les pinceaux de dessin.
     */
    private void init() {
        gridPaint = new Paint();
        gridPaint.setColor(Color.BLACK);
        gridPaint.setStrokeWidth(5);

        circlePaint = new Paint();

        pathPaint = new Paint();
        pathPaint.setStrokeWidth(15);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    /**
     * Charge l'état du mode achromate depuis les préférences.
     */
    private void loadAchromateMode() {
        SharedPreferences prefs = context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);
        isAchromateMode = prefs.getBoolean("achromateMode", false);
    }

    /**
     * Active ou désactive le mode achromate.
     *
     * @param isAchromate true pour activer, false pour désactiver
     */
    public void setAchromateMode(boolean isAchromate) {
        this.isAchromateMode = isAchromate;
        invalidate();
    }

    /**
     * Réinitialise la grille du jeu, sauf si la partie est gagnée.
     */
    public void resetGame() {
        if (isGameWon) {
            Toast.makeText(context, "La partie est terminée ! Vous ne pouvez plus réinitialiser.", Toast.LENGTH_SHORT).show();
            return;
        }
        gameLogic.resetGrid();
        currentPath.clear();
        currentColor = 0;
        currentColorIndex = -1;
        isDrawing = false;
        invalidate();
    }

    /**
     * Dessine la grille, les points, les chemins enregistrés et le tracé en cours.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);

        int width = getWidth();
        int height = getHeight();
        int gridSize = gameLogic.getGridSize();
        int gridSizePx = Math.min(width, height) * 90 / 100;
        cellSize = gridSizePx / gridSize;

        startX = (width - gridSizePx) / 2;
        startY = (height - gridSizePx) / 2;

        for (int i = 0; i <= gridSize; i++) {
            canvas.drawLine(startX + i * cellSize, startY, startX + i * cellSize, startY + gridSizePx, gridPaint);
            canvas.drawLine(startX, startY + i * cellSize, startX + gridSizePx, startY + i * cellSize, gridPaint);
        }

        for (FlowPoint point : gameLogic.getPoints()) {
            int color;
            try {
                color = gameLogic.getColorByIndex(point.getColorIndex(), isAchromateMode);
            } catch (Exception e) {
                Log.e("DEBUG_FLOW", "Erreur couleur: " + e.getMessage());
                continue;
            }

            circlePaint.setColor(color);
            float centerX = startX + point.getX() * cellSize + cellSize / 2;
            float centerY = startY + point.getY() * cellSize + cellSize / 2;
            float radius = cellSize / 3.5f;
            canvas.drawCircle(centerX, centerY, radius, circlePaint);
        }

        for (Map.Entry<Integer, List<int[]>> entry : gameLogic.getPaths().entrySet()) {
            int colorIndex = entry.getKey();
            int color = gameLogic.getColorByIndex(colorIndex, isAchromateMode);
            pathPaint.setColor(color);
            List<int[]> path = entry.getValue();
            for (int i = 1; i < path.size(); i++) {
                int[] start = path.get(i - 1);
                int[] end = path.get(i);
                float startXPos = startX + start[0] * cellSize + cellSize / 2;
                float startYPos = startY + start[1] * cellSize + cellSize / 2;
                float endXPos = startX + end[0] * cellSize + cellSize / 2;
                float endYPos = startY + end[1] * cellSize + cellSize / 2;
                canvas.drawLine(startXPos, startYPos, endXPos, endYPos, pathPaint);
            }
        }

        if (!currentPath.isEmpty()) {
            pathPaint.setColor(gameLogic.getColorByIndex(currentColorIndex, isAchromateMode));
            for (int i = 1; i < currentPath.size(); i++) {
                int[] start = currentPath.get(i - 1);
                int[] end = currentPath.get(i);
                float startXPos = startX + start[0] * cellSize + cellSize / 2;
                float startYPos = startY + start[1] * cellSize + cellSize / 2;
                float endXPos = startX + end[0] * cellSize + cellSize / 2;
                float endYPos = startY + end[1] * cellSize + cellSize / 2;
                canvas.drawLine(startXPos, startYPos, endXPos, endYPos, pathPaint);
            }
        }
    }

    /**
     * Gère les interactions tactiles du joueur (début, tracé, relâchement).
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isGameWon) return false;

        int x = (int) ((event.getX() - startX) / cellSize);
        int y = (int) ((event.getY() - startY) / cellSize);

        if (x < 0 || x >= gameLogic.getGridSize() || y < 0 || y >= gameLogic.getGridSize()) {
            currentPath.clear();
            gameLogic.removePath(currentColorIndex);
            isDrawing = false;
            invalidate();
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            FlowPoint startPoint = gameLogic.getPointAt(x, y);
            if (startPoint != null) {
                currentColorIndex = startPoint.getColorIndex();
                currentColor = gameLogic.getColorByIndex(currentColorIndex, isAchromateMode);

                if (gameLogic.isPathExists(currentColorIndex)) {
                    gameLogic.removePath(currentColorIndex);
                }

                isDrawing = true;
                currentPath.clear();
                currentPath.add(new int[]{x, y});
                lastX = x;
                lastY = y;
                invalidate();
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (isDrawing) {
                if ((x != lastX || y != lastY) && isValidMove(lastX, lastY, x, y)) {
                    if (isAlreadyInCurrentPath(x, y) || isPathOccupied(x, y)) return true;
                    if (isPointOnCell(x, y) && !isCorrectEndPoint(x, y)) {
                        resetCurrentPath();
                        return true;
                    }

                    currentPath.add(new int[]{x, y});
                    lastX = x;
                    lastY = y;

                    if (isCorrectEndPoint(x, y)) {
                        gameLogic.savePath(currentColorIndex, currentPath);
                        isDrawing = false;
                        if (gameLogic.checkWin()) {
                            isGameWon = true;
                            showWinMessage();
                        }
                    }
                    invalidate();
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isDrawing) {
                int[] lastPoint = currentPath.get(currentPath.size() - 1);
                int lastX = lastPoint[0];
                int lastY = lastPoint[1];

                if (!isCorrectEndPoint(lastX, lastY)) {
                    currentPath.clear();
                    gameLogic.removePath(currentColorIndex);
                    isDrawing = false;
                } else {
                    gameLogic.savePath(currentColorIndex, currentPath);
                    if (gameLogic.checkWin()) {
                        isGameWon = true;
                        showWinMessage();
                    }
                }
            }
            invalidate();
        }
        return true;
    }

    /**
     * Réinitialise le tracé en cours.
     */
    private void resetCurrentPath() {
        currentPath.clear();
        animatedPath.clear();
        isDrawing = false;
        invalidate();
    }

    /**
     * Vérifie si le mouvement entre deux cellules est valide (adjacentes).
     */
    private boolean isValidMove(int startX, int startY, int endX, int endY) {
        return (startX == endX && Math.abs(startY - endY) == 1) ||
                (startY == endY && Math.abs(startX - endX) == 1);
    }

    /**
     * Vérifie si une cellule est déjà dans le tracé en cours.
     */
    private boolean isAlreadyInCurrentPath(int x, int y) {
        for (int[] pos : currentPath) {
            if (pos[0] == x && pos[1] == y) return true;
        }
        return false;
    }

    /**
     * Vérifie si une cellule est déjà utilisée dans un autre chemin.
     */
    private boolean isPathOccupied(int x, int y) {
        for (Map.Entry<Integer, List<int[]>> entry : gameLogic.getPaths().entrySet()) {
            for (int[] pos : entry.getValue()) {
                if (pos[0] == x && pos[1] == y) return true;
            }
        }
        return false;
    }

    /**
     * Vérifie si une cellule correspond à un point de départ ou d'arrivée.
     */
    private boolean isPointOnCell(int x, int y) {
        for (FlowPoint point : gameLogic.getPoints()) {
            if (point.getX() == x && point.getY() == y) return true;
        }
        return false;
    }

    /**
     * Vérifie si une cellule est bien le bon point final du chemin (même couleur).
     */
    private boolean isCorrectEndPoint(int x, int y) {
        for (FlowPoint point : gameLogic.getPoints()) {
            if (point.getX() == x && point.getY() == y && point.getColorIndex() == currentColorIndex) {
                return true;
            }
        }
        return false;
    }

    /**
     * Affiche un message de victoire à l'utilisateur.
     */
    private void showWinMessage() {
        Toast.makeText(context, "Bravo ! Vous avez gagné !", Toast.LENGTH_LONG).show();
    }
}