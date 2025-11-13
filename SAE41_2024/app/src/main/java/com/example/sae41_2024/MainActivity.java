package com.example.sae41_2024;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activité principale qui affiche le jeu "Relier les points".
 * Elle gère le chargement de niveau, l'affichage de la grille,
 * la réinitialisation et la persistance du chemin dessiné.
 */
public class MainActivity extends Activity {
    private GameView gameView;
    private GameLogic gameLogic;
    private String fileName;
    private boolean isQuitting = false;

    /**
     * Initialise l'activité et le jeu à partir du fichier de niveau reçu par Intent.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle("Relier les points");
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#361F5F")));
            getActionBar().setDisplayShowHomeEnabled(false);
            getActionBar().setIcon(null);
        }

        try {
            String[] fileList = getAssets().list("puzzles");
            for (String file : fileList) {
                System.out.println("Fichier trouvé : " + file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String levelName = getIntent().getStringExtra("LEVEL_NAME");
        TextView title = findViewById(R.id.title);
        title.setText(levelName);

        fileName = getIntent().getStringExtra("FILE_NAME");
        if (levelName == null || levelName.trim().isEmpty()) {
            levelName = fileName;
        }

        gameLogic = new GameLogic(5);
        if (fileName != null) {
            gameLogic.loadLevelFromAssets(this, "puzzles/" + fileName + ".xml", fileName);
        } else {
            Toast.makeText(this, "Erreur : Impossible de charger le fichier du niveau", Toast.LENGTH_SHORT).show();
        }

        FrameLayout gameContainer = findViewById(R.id.game_container);
        gameView = new GameView(this, gameLogic);
        gameContainer.addView(gameView);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isAchromateMode = prefs.getBoolean("achromateMode", false);
        gameView.setAchromateMode(isAchromateMode);

        Button resetButton = findViewById(R.id.reset_button);
        resetButton.setOnClickListener(v -> gameView.resetGame());

        if (savedInstanceState != null) {
            restoreGameState(savedInstanceState);
        }
    }

    /**
     * Désactive l'inflation du menu (aucune option dans la barre d'action).
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    /**
     * Gère le retour arrière via la flèche de l'action bar.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            isQuitting = true;
            clearSavedPath();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Gère le bouton retour physique du téléphone.
     */
    @Override
    public void onBackPressed() {
        isQuitting = true;
        clearSavedPath();
        super.onBackPressed();
    }

    /**
     * Supprime les chemins enregistrés du niveau courant.
     */
    private void clearSavedPath() {
        if (fileName != null) {
            SharedPreferences prefs = getSharedPreferences("GameState", MODE_PRIVATE);
            prefs.edit().remove("paths_" + fileName).apply();
            Log.d("DEBUG_FLOW", "Chemins supprimés pour : " + fileName);
        }
    }

    /**
     * Recharge le niveau et restaure les chemins enregistrés (si existants) à la reprise de l'activité.
     */
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isAchromateMode = prefs.getBoolean("achromateMode", false);
        Log.d("DEBUG_FLOW", "Mode achromate depuis les préférences : " + isAchromateMode);

        gameLogic.setAchromateMode(isAchromateMode);
        gameView.setAchromateMode(isAchromateMode);

        if (fileName != null) {
            Log.d("DEBUG_FLOW", "Rechargement du niveau : " + fileName);
            gameLogic.loadLevelFromAssets(this, "puzzles/" + fileName + ".xml", fileName);

            SharedPreferences prefsState = getSharedPreferences("GameState", MODE_PRIVATE);
            String savedPaths = prefsState.getString("paths_" + fileName, null);

            if (savedPaths != null) {
                Map<Integer, List<int[]>> restoredPaths = new HashMap<>();
                for (String colorBlock : savedPaths.split("\\|")) {
                    if (colorBlock.trim().isEmpty()) continue;
                    String[] parts = colorBlock.split(":");
                    int color = Integer.parseInt(parts[0]);
                    List<int[]> path = new ArrayList<>();
                    for (String coord : parts[1].split(";")) {
                        if (coord.trim().isEmpty()) continue;
                        String[] xy = coord.split(",");
                        path.add(new int[]{Integer.parseInt(xy[0]), Integer.parseInt(xy[1])});
                    }
                    restoredPaths.put(color, path);
                }
                for (Map.Entry<Integer, List<int[]>> entry : restoredPaths.entrySet()) {
                    gameLogic.savePath(entry.getKey(), entry.getValue());
                }
            }
            gameView.invalidate();
        } else {
            Log.w("DEBUG_FLOW", "Aucun nom de fichier reçu dans l'intent !");
        }
    }

    /**
     * Sauvegarde automatique des chemins lors du passage en arrière-plan, sauf si l'utilisateur quitte.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (isQuitting || fileName == null) return;

        SharedPreferences prefs = getSharedPreferences("GameState", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Map<Integer, List<int[]>> paths = gameLogic.getPaths();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, List<int[]>> entry : paths.entrySet()) {
            int color = entry.getKey();
            sb.append(color).append(":");
            for (int[] point : entry.getValue()) {
                sb.append(point[0]).append(",").append(point[1]).append(";");
            }
            sb.append("|");
        }
        editor.putString("paths_" + fileName, sb.toString());
        editor.apply();
    }

    /**
     * Sauvegarde temporaire de l'état de l'activité (rotation de l'écran, etc.).
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Map<Integer, List<int[]>> paths = gameLogic.getPaths();
        outState.putSerializable("paths", (Serializable) new HashMap<>(paths));
        outState.putString("fileName", fileName);
    }

    /**
     * Restaure l'état temporaire lors d'une recréation d'activité (ex: rotation).
     *
     * @param savedInstanceState Le Bundle contenant les données sauvegardées
     */
    private void restoreGameState(Bundle savedInstanceState) {
        Map<Integer, List<int[]>> restoredPaths = (Map<Integer, List<int[]>>) savedInstanceState.getSerializable("paths");
        if (restoredPaths != null) {
            for (Map.Entry<Integer, List<int[]>> entry : restoredPaths.entrySet()) {
                gameLogic.savePath(entry.getKey(), entry.getValue());
            }
            gameView.invalidate();
        }
    }
}