package ca.artemis.engine.xml.adpters;

public class LongCAdapter extends ValueAdapter {

    protected LongCAdapter(String value) {
        super(value);
    }

    @Override
    public Object getValue() {
        return Long.valueOf(value);
    }
}
