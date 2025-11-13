package com.example.sae41_2024;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.util.Xml;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Activité principale du menu permettant à l'utilisateur de :
 * - voir la liste des niveaux disponibles
 * - lancer un niveau
 * - accéder aux options
 * - quitter le jeu.
 */
public class MenuActivity extends Activity {

    /**
     * Liste des noms de niveaux à afficher.
     */
    private List<String> levels;

    /**
     * Tableau indiquant quels puzzles sont valides.
     */
    private boolean[] puzzleEnabled;

    /**
     * Association entre le nom affiché du niveau et son nom de fichier.
     */
    private Map<String, String> levelToFileMap = new HashMap<>();

    /**
     * Méthode appelée à la création de l'activité.
     * Initialise l'interface utilisateur et charge les niveaux disponibles.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        if (getActionBar() != null) {
            getActionBar().setTitle("Menu");
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#361F5F")));
            getActionBar().setDisplayShowHomeEnabled(false);
            getActionBar().setIcon(null);
        }

        ListView levelListView = findViewById(R.id.level_list);
        levels = getLevelsFromAssets();
        puzzleEnabled = new boolean[levels.size()];

        // Vérifie la validité de chaque puzzle
        for (int i = 0; i < levels.size(); i++) {
            puzzleEnabled[i] = isPuzzleValid(levels.get(i));
        }

        // Adapte la liste pour afficher les niveaux
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, levels) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextSize(18);

                if (!puzzleEnabled[position]) {
                    textView.setTextColor(Color.GRAY);
                    textView.setAlpha(0.5f);
                    textView.setText(levels.get(position) + " (Erreur)");
                } else {
                    textView.setTextColor(Color.WHITE);
                    textView.setAlpha(1.0f);
                }
                return view;
            }
        };

        levelListView.setAdapter(adapter);

        // Action lorsqu’un niveau est sélectionné
        levelListView.setOnItemClickListener((parent, view, position, id) -> {
            if (puzzleEnabled[position]) {
                String levelName = levels.get(position);
                String fileName = levelToFileMap.get(levelName);

                if (fileName == null) {
                    Toast.makeText(this, "Erreur : fichier introuvable pour " + levelName, Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                intent.putExtra("LEVEL_NAME", levelName);
                intent.putExtra("FILE_NAME", fileName);
                startActivity(intent);
                System.out.println("Chargement du fichier de niveau : " + fileName);
            } else {
                Toast.makeText(MenuActivity.this, "Ce puzzle est invalide et ne peut pas être joué.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Crée le menu dans la barre d’action.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_menu_main, menu);

        // Met en violet le texte des options du menu
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString s = new SpannableString(item.getTitle());
            s.setSpan(new ForegroundColorSpan(Color.parseColor("#361F5F")), 0, s.length(), 0);
            item.setTitle(s);
        }

        return true;
    }

    /**
     * Gère les actions sélectionnées dans le menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.finish_button) {
            closeApplication();
            return true;
        } else if (item.getItemId() == R.id.options_button) {
            Intent intent = new Intent(MenuActivity.this, OptionsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Ferme complètement l'application.
     */
    private void closeApplication() {
        finishAffinity();
        System.exit(0);
    }

    /**
     * Extrait le nom du niveau à partir du fichier XML du puzzle.
     *
     * @param fileName Le nom du fichier XML
     * @return Le nom du niveau, ou null s’il n’est pas trouvé
     */
    private String getLevelNameFromFile(String fileName) {
        try {
            InputStream inputStream = getAssets().open("puzzles/" + fileName);
            XmlPullParser parser = android.util.Xml.newPullParser();
            parser.setInput(inputStream, "UTF-8");

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.getName().equals("puzzle")) {
                    return parser.getAttributeValue(null, "nom");
                }
                eventType = parser.next();
            }
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupère les niveaux depuis le dossier assets.
     *
     * @return Liste des noms de niveaux à afficher
     */
    private List<String> getLevelsFromAssets() {
        List<String> levelNames = new ArrayList<>();
        levelToFileMap.clear();

        try {
            String[] files = getAssets().list("puzzles");
            if (files != null) {
                for (String file : files) {
                    if (file.endsWith(".xml")) {
                        String levelName = getLevelNameFromFile(file);
                        levelName = (levelName != null) ? levelName : file.replace(".xml", "");
                        levelNames.add(levelName);
                        levelToFileMap.put(levelName, file.replace(".xml", ""));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        puzzleEnabled = new boolean[levelNames.size()];
        for (int i = 0; i < levelNames.size(); i++) {
            String fileName = levelToFileMap.get(levelNames.get(i));
            puzzleEnabled[i] = (fileName != null) && isPuzzleValid(fileName);
        }
        return levelNames;
    }

    /**
     * Vérifie que la structure XML du fichier est correcte.
     *
     * @param fileName Le nom du fichier XML (sans .xml)
     * @return true si la syntaxe est correcte, sinon false
     */
    private boolean isSyntaxValid(String fileName) {
        try {
            InputStream inputStream = getAssets().open("puzzles/" + fileName + ".xml");
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream, "UTF-8");

            boolean puzzleFound = false;
            boolean paireFound = false;
            boolean pointFound = false;
            int gridSize = -1;

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();

                    switch (tagName) {
                        case "puzzle":
                            if (puzzleFound) return false;
                            puzzleFound = true;

                            String sizeValue = parser.getAttributeValue(null, "size");
                            if (sizeValue == null) return false;

                            try {
                                gridSize = Integer.parseInt(sizeValue);
                                if (gridSize < 5 || gridSize > 14) return false;
                            } catch (NumberFormatException e) {
                                return false;
                            }

                            for (int i = 0; i < parser.getAttributeCount(); i++) {
                                String attrName = parser.getAttributeName(i);
                                if (!attrName.equals("size") && !attrName.equals("nom")) return false;
                            }

                            String nameValue = parser.getAttributeValue(null, "nom");
                            if (nameValue != null && nameValue.trim().isEmpty()) return false;
                            break;

                        case "paire":
                            paireFound = true;
                            break;

                        case "point":
                            pointFound = true;

                            String colValue = parser.getAttributeValue(null, "colonne");
                            String rowValue = parser.getAttributeValue(null, "ligne");

                            if (colValue == null || rowValue == null) return false;

                            if (parser.getAttributeCount() != 2) return false;

                            for (int i = 0; i < parser.getAttributeCount(); i++) {
                                String attrName = parser.getAttributeName(i);
                                if (!attrName.equals("colonne") && !attrName.equals("ligne")) return false;
                            }

                            try {
                                int col = Integer.parseInt(colValue);
                                int row = Integer.parseInt(rowValue);
                                if (col < 0 || col >= gridSize || row < 0 || row >= gridSize) return false;
                            } catch (NumberFormatException e) {
                                return false;
                            }
                            break;

                        default:
                            return false;
                    }
                }
                eventType = parser.next();
            }
            inputStream.close();
            return puzzleFound && paireFound && pointFound;

        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Vérifie que le puzzle est valide en structure et logique.
     *
     * @param levelName Nom du niveau
     * @return true si le niveau est jouable, false sinon
     */
    private boolean isPuzzleValid(String levelName) {
        String fileName = levelToFileMap.get(levelName);
        if (fileName == null || !isSyntaxValid(fileName)) return false;

        try (InputStream inputStream = getAssets().open("puzzles/" + fileName + ".xml")) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream, null);

            int eventType = parser.getEventType();
            int paireCount = 0;
            boolean insidePaire = false;
            int pointCount = 0;
            HashSet<String> occupiedPositions = new HashSet<>();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if ("paire".equals(parser.getName())) {
                        pointCount = 0;
                        insidePaire = true;
                        paireCount++;
                    } else if ("point".equals(parser.getName()) && insidePaire) {
                        pointCount++;

                        String colValue = parser.getAttributeValue(null, "colonne");
                        String rowValue = parser.getAttributeValue(null, "ligne");

                        if (colValue == null || rowValue == null) return false;

                        String key = colValue + ":" + rowValue;
                        if (occupiedPositions.contains(key)) return false;
                        else occupiedPositions.add(key);
                    }
                } else if (eventType == XmlPullParser.END_TAG && "paire".equals(parser.getName())) {
                    insidePaire = false;
                    if (pointCount != 2) return false;
                }
                eventType = parser.next();
            }
            return paireCount > 0;

        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            return false;
        }
    }
}