package uk.ac.ed.inf.mandelbrotmaps.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import uk.ac.ed.inf.mandelbrotmaps.R;
import uk.ac.ed.inf.mandelbrotmaps.colouring.DefaultColourStrategy;
import uk.ac.ed.inf.mandelbrotmaps.colouring.IColourStrategy;
import uk.ac.ed.inf.mandelbrotmaps.colouring.JuliaColourStrategy;
import uk.ac.ed.inf.mandelbrotmaps.colouring.PsychadelicColourStrategy;
import uk.ac.ed.inf.mandelbrotmaps.colouring.RGBWalkColourStrategy;
import uk.ac.ed.inf.mandelbrotmaps.IFractalSceneDelegate;
import uk.ac.ed.inf.mandelbrotmaps.overlay.PinColour;
import uk.ac.ed.inf.mandelbrotmaps.settings.saved_state.SavedGraphArea;
import uk.ac.ed.inf.mandelbrotmaps.settings.saved_state.SavedJuliaGraph;

public class SettingsManager implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Context context;
    private IFractalSceneDelegate sceneDelegate;

    public SettingsManager(Context context) {
        this.context = context.getApplicationContext();
        PreferenceManager.setDefaultValues(this.context, R.xml.settings, false);
    }

    public void setFractalSceneDelegate(IFractalSceneDelegate sceneDelegate) {
        this.sceneDelegate = sceneDelegate;
    }

    // Preference keys

    private static final String PREFERENCE_KEY_CRUDE_FIRST = "CRUDE";
    private static final boolean PREFERENCE_CRUDE_FIRST_DEFAULT = true;

    private static final String PREFERENCE_KEY_SHOW_TIMES = "SHOW_TIMES";
    private static final boolean PREFERENCE_SHOW_TIMES_DEFAULT = false;

    private static final String PREFERENCE_KEY_PIN_COLOUR = "PIN_COLOUR";
    private static final String PREFERENCE_PIN_COLOUR_DEFAULT = "blue";

    private static final String PREFERENCE_KEY_MANDELBROT_COLOUR = "MANDELBROT_COLOURS";
    private static final String PREFERENCE_MANDELBROT_COLOUR_DEFAULT = "MandelbrotDefault";

    private static final String PREFERENCE_KEY_JULIA_COLOUR = "JULIA_COLOURS";
    private static final String PREFERENCE_JULIA_COLOUR_DEFAULT = "JuliaDefault";

    public static final String PREVIOUS_MAIN_GRAPH_AREA = "prevMainGraphArea";
    public static final String PREVIOUS_LITTLE_GRAPH_AREA = "prevLittleGraphArea";
    public static final String PREVIOUS_JULIA_PARAMS = "prevJuliaParams";
    public final String PREVIOUS_JULIA_GRAPH = "prevJuliaGraph";

    private SharedPreferences getDefaultSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this.context.getApplicationContext());
    }
    // Specific settings

    public boolean performCrudeFirst() {
        boolean result = this.getDefaultSharedPreferences().getBoolean(PREFERENCE_KEY_CRUDE_FIRST, PREFERENCE_CRUDE_FIRST_DEFAULT);
        Log.i("SM", "Perform crude first: " + result);
        return result;
    }

    public boolean showTimes() {
        return this.getDefaultSharedPreferences().getBoolean(PREFERENCE_KEY_CRUDE_FIRST, PREFERENCE_SHOW_TIMES_DEFAULT);
    }

    public void refreshPinSettings() {
        this.onSharedPreferenceChanged(this.getDefaultSharedPreferences(), PREFERENCE_KEY_PIN_COLOUR);
    }

    public void refreshColourSettings() {
        IColourStrategy colourStrategy = strategyFromPreference(this.getDefaultSharedPreferences().getString(PREFERENCE_KEY_MANDELBROT_COLOUR, PREFERENCE_MANDELBROT_COLOUR_DEFAULT));
        if (colourStrategy != null)
            this.sceneDelegate.onMandelbrotColourSchemeChanged(colourStrategy, false);

        colourStrategy = strategyFromPreference(this.getDefaultSharedPreferences().getString(PREFERENCE_KEY_JULIA_COLOUR, PREFERENCE_JULIA_COLOUR_DEFAULT));
        if (colourStrategy != null)
            this.sceneDelegate.onJuliaColourSchemeChanged(colourStrategy, false);
    }

    private IColourStrategy strategyFromPreference(String preference) {
        if (preference.equals("MandelbrotDefault"))
            return new DefaultColourStrategy();
        else if (preference.equals("JuliaDefault"))
            return new JuliaColourStrategy();
        else if (preference.equals("RGBWalk"))
            return new RGBWalkColourStrategy();
        else if (preference.equals("Psychadelic"))
            return new PsychadelicColourStrategy();

        return null;
    }

    public SavedGraphArea getPreviousMandelbrotGraph() {
        String storedArea = this.getDefaultSharedPreferences().getString(PREVIOUS_MAIN_GRAPH_AREA, "");
        if (storedArea.isEmpty())
            return null;
        else
            return new Gson().fromJson(storedArea, SavedGraphArea.class);
    }

    public void savePreviousMandelbrotGraph(SavedGraphArea graphArea) {
        SharedPreferences.Editor editor = this.getDefaultSharedPreferences().edit();
        editor.putString(PREVIOUS_MAIN_GRAPH_AREA, new Gson().toJson(graphArea));
        editor.commit();
    }

    public SavedJuliaGraph getPreviousJuliaGraph() {
        String storedArea = this.getDefaultSharedPreferences().getString(PREVIOUS_JULIA_GRAPH, "");
        if (storedArea.isEmpty())
            return null;
        else
            return new Gson().fromJson(storedArea, SavedJuliaGraph.class);
    }

    public void savePreviousJuliaGraph(SavedJuliaGraph juliaGraph) {
        SharedPreferences.Editor editor = this.getDefaultSharedPreferences().edit();
        String gsonString = new Gson().toJson(juliaGraph);
        editor.putString(PREVIOUS_JULIA_GRAPH, gsonString);
        editor.commit();
    }


    // OnSharedPreferenceChangeListener

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equalsIgnoreCase(PREFERENCE_KEY_PIN_COLOUR)) {
            PinColour pinColour;
            try {
                pinColour = PinColour.valueOf(sharedPreferences.getString(key, PREFERENCE_PIN_COLOUR_DEFAULT).toUpperCase());
            } catch (IllegalArgumentException e) {
                pinColour = PinColour.valueOf(PREFERENCE_PIN_COLOUR_DEFAULT);
            }

            this.sceneDelegate.onPinColourChanged(pinColour);
        } else if (key.equalsIgnoreCase(PREFERENCE_KEY_MANDELBROT_COLOUR)) {
            IColourStrategy colourStrategy = strategyFromPreference(sharedPreferences.getString(key, PREFERENCE_MANDELBROT_COLOUR_DEFAULT));
            if (colourStrategy != null)
                this.sceneDelegate.onMandelbrotColourSchemeChanged(colourStrategy, true);
        } else if (key.equalsIgnoreCase(PREFERENCE_KEY_JULIA_COLOUR)) {
            IColourStrategy colourStrategy = strategyFromPreference(sharedPreferences.getString(key, PREFERENCE_JULIA_COLOUR_DEFAULT));
            if (colourStrategy != null)
                this.sceneDelegate.onJuliaColourSchemeChanged(colourStrategy, true);
        }
    }
}
