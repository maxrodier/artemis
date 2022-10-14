package ca.artemis.engine.master;

public class EngineConfigs {
    
    public int languageId = 0;
	
	public int windowWidth = 1280;
	public int windowHeight = 720;
	
	public int windowMinWidth = 600;
	public int windowMinHeight = 350;
	public float UiSize = 1;
	
	public boolean fullscreen = true;
	public String windowTitle = "The Game";
	public int fps = 100;
	public boolean vsync = true;
	public boolean msaa = true;
	
	//public UiResources uiResources = new UiResources();
	
	//public Resources resources = new Resources();
	
	//public State initialState = new EmptyState();
	//public State defaultState = new EmptyState();
	
	//public DebugFormat debugger = null;
	
	public static EngineConfigs getDefaultConfigs(){
		return new EngineConfigs();
	}
	
	protected EngineConfigs() {}
}
