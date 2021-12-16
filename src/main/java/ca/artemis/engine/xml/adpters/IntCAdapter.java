package ca.artemis.engine.xml.adpters;

public class IntCAdapter extends ValueAdapter {

    protected IntCAdapter(String value) {
        super(value);
    }

    @Override
    public Object getValue() {
        return Integer.valueOf(value);
    }
}
