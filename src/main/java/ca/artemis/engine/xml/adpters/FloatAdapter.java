package ca.artemis.engine.xml.adpters;

public class FloatAdapter extends ValueAdapter {

    protected FloatAdapter(String value) {
        super(value);
    }

    @Override
    public Object getValue() {
        return Float.parseFloat(value);
    }
}
