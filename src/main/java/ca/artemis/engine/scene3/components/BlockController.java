package ca.artemis.engine.scene3.components;

import ca.artemis.engine.scene3.Controller;
import ca.artemis.engine.scene3.annotations.Autowired;
import ca.artemis.engine.scene3.base.Container;

public class BlockController extends Controller<Block> {

    @Autowired
    public Container container1;

    public BlockController(Block view) {
        super(view);
    }
}
