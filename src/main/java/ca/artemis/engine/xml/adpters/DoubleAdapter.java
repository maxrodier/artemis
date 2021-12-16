package ca.artemis.engine.xml.adpters;

public class DoubleAdapter extends ValueAdapter {

    protected DoubleAdapter(String value) {
        super(value);
    }

    @Override
    public Object getValue() {
        return Double.parseDouble(value);
    }
}
