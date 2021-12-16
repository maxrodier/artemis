package ca.artemis.engine.xml.adpters;

public class LongAdapter extends ValueAdapter {

    protected LongAdapter(String value) {
        super(value);
    }

    @Override
    public Object getValue() {
        return Long.parseLong(value);
    }
}
