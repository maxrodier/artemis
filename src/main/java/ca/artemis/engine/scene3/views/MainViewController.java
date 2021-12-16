package ca.artemis.engine.scene3.views;

import ca.artemis.engine.scene3.Controller;
import ca.artemis.engine.scene3.annotations.Autowired;
import ca.artemis.engine.scene3.base.Container;
import ca.artemis.engine.scene3.components.Block;

public class MainViewController extends Controller<MainView> {

    @Autowired
    public Container container1;

    @Autowired
    public Container container2;

    @Autowired
    public Block block1;

    public MainViewController(MainView view) {
        super(view);
    }

    @Override
    public MainView getView() {
        return view;
    }
}
