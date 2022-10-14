package ca.artemis.engine.game;

import ca.artemis.engine.master.Engine;
import ca.artemis.engine.master.EngineConfigs;
import ca.artemis.engine.master.EngineCreator;

public class GameManager {// TODO only very high level systems in here please
    
	private static Engine engine;

    /* 
	private static MasterSceneRenderer renderer;
	private static CityBuilderGuis gameUi;
	private static ResourceRepository resourceRepos;
	private static SessionManager sessionManager;

	private static Session currentSession;
    */

    public static void init(EngineConfigs configs) {// TODO take in game configs
		initSystems(configs);
		initFirstSession();
    }

    public static void update() {
        /* 
		if (engine.keyboard.keyPressEvent(GLFW.GLFW_KEY_Y) && sessionManager.hasSessionReady()) {
			sessionManager.save();
		}
		Debugger.update(engine.getDeltaSeconds());
		// TODO don't update session when in main menu
		if (sessionManager.hasSessionReady()) {
			currentSession = sessionManager.getActiveSession();
			currentSession.update(engine.getDeltaSeconds());
			renderer.render(currentSession.scene);
		}
		gameUi.update();
        */
		engine.update();
	}

	public static void cleanUp() {
        /* 
		if (sessionManager.hasSessionReady() && !sessionManager.isCurrentlySaving())
			sessionManager.save();
		BackgroundLoader.completeAllRequests();
		sessionManager.cleanUp();
		renderer.cleanUp();
        */
		engine.cleanUp();
	}

    public static boolean readyToClose() {
		return engine.isCloseRequested();
	}

	private static void initSystems(EngineConfigs configs) {

		engine = EngineCreator.init(configs);
        /*
		renderer = new MasterSceneRenderer();
		resourceRepos = ResourceRepoLoader.loadResources();
		sessionManager = SessionManager.init(engine, new File(FileUtils.getRootFolder(), "saves"), 1);// configs
        */
	}

	private static void initFirstSession() {// TODO temp stuff, replace when splash screen implemented
        /* 
		ProgressReport report = sessionManager.loadInitialSave();
		while (!report.isComplete()) {
			engine.update();
			sleep(10);
		}
		gameUi = new CityBuilderGuis(engine);
        */
	}
}
