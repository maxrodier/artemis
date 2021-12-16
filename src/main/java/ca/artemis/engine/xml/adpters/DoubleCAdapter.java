package ca.artemis.engine.xml.adpters;

public class DoubleCAdapter extends ValueAdapter {

    protected DoubleCAdapter(String value) {
        super(value);
    }

    @Override
    public Object getValue() {
        return Double.valueOf(value);
    }
}
