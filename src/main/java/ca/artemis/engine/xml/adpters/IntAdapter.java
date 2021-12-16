package ca.artemis.engine.xml.adpters;

public class IntAdapter extends ValueAdapter {

    protected IntAdapter(String value) {
        super(value);
    }

    @Override
    public Object getValue() {
        return Integer.parseInt(value);
    }
}
