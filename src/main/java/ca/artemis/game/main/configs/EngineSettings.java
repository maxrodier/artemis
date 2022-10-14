package ca.artemis.game.main.configs;

import ca.artemis.engine.master.EngineConfigs;

public class EngineSettings extends EngineConfigs {
    
    public EngineSettings() {
		windowTitle = "City Building Game";
		vsync = true;
		msaa = true;
		fps = 100;
		UiSize = 1;
		fullscreen = true;
		windowMinHeight = 0;
		windowMinWidth = 0;
		windowWidth = 1280;
		windowHeight = 720;
		//defaultState = GameState.NORMAL;
		//initialState = GameState.SPLASH_SCREEN;
		languageId = 0;
		//debugger = new DebuggerConfigs();
		//resources.colours.addPalette(loadColourPalette());
	}

    /*
    private static ColourPalette loadColourPalette() {
		try {
			return ColourPalette.load(new File(EngineFiles.RES_FOLDER, "palette.png"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
    */
}
