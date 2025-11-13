package com.example.sae41_2024;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Activité représentant l'écran des options du jeu.
 * Elle permet à l'utilisateur d'activer ou désactiver le mode achromate (niveaux en nuances de gris).
 */
public class OptionsActivity extends PreferenceActivity {

    /**
     * Méthode appelée à la création de l'activité.
     * Elle initialise l'interface utilisateur avec les préférences définies dans le fichier XML
     * et configure la barre d'action avec le titre et la couleur.
     *
     * @param savedInstanceState L'état sauvegardé de l'activité (s'il existe)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // Configure la barre d'action (titre, retour arrière, couleur)
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle("Paramètres");
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#361F5F")));
            getActionBar().setDisplayShowHomeEnabled(false);
            getActionBar().setIcon(null);
        }

        // Récupère le switch pour le mode achromate et ajoute un listener
        CheckBoxPreference achroSwitch = (CheckBoxPreference) findPreference("achromateMode");
        achroSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            /**
             * Appelé lorsque l'utilisateur modifie l'état du switch.
             *
             * @param preference La préférence modifiée
             * @param newValue   La nouvelle valeur du switch (true ou false)
             * @return true pour accepter le changement
             */
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean isAchromateEnabled = (Boolean) newValue;

                Toast.makeText(OptionsActivity.this,
                        isAchromateEnabled ? "Mode Achromate activé" : "Mode Achromate désactivé",
                        Toast.LENGTH_SHORT).show();

                return true;
            }
        });
    }

    /**
     * Méthode appelée pour créer le menu d’options.
     * Elle est ici désactivée (aucun menu à afficher).
     *
     * @param menu Le menu dans lequel ajouter les éléments
     * @return false car aucun menu n'est affiché
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    /**
     * Gère les actions de la barre de menu (non utilisée ici).
     *
     * @param item Élément sélectionné dans le menu
     * @return true si l'action est traitée, sinon false
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}