package ca.artemis.engine.xml.adpters;

public class FloatCAdapter extends ValueAdapter {

    protected FloatCAdapter(String value) {
        super(value);
    }

    @Override
    public Object getValue() {
        return Float.valueOf(value);
    }
}
