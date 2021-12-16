package ca.artemis.engine.xml.adpters;

public class StringAdapter extends ValueAdapter {

    protected StringAdapter(String value) {
        super(value);
    }

    @Override
    public Object getValue() {
        return value;
    }
}
