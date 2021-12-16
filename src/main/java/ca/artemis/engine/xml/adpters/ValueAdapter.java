package ca.artemis.engine.xml.adpters;

import ca.artemis.engine.xml.LoadException;

public abstract class ValueAdapter {
    
    protected final String value;

    protected ValueAdapter(String value) {
        this.value = value;
    }

    public abstract Object getValue();

    public static ValueAdapter createAdpapter(Class<?> cls, Object value) throws LoadException {
        if(!String.class.isInstance(value))
            throw new LoadException("Value must but of String type, type is " + value.getClass() + ".");
        String sValue = String.class.cast(value);

        //String
        if(cls == String.class) 
            return new StringAdapter(sValue);

        //Integer
        if(cls == int.class) 
            return new IntAdapter(sValue);
        if(cls == Integer.class) 
            return new IntCAdapter(sValue);

        //Float
        if(cls == float.class) 
            return new FloatAdapter(sValue);
        if(cls == Float.class) 
            return new FloatCAdapter(sValue);

        //Double
        if(cls == double.class) 
            return new DoubleAdapter(sValue);
        if(cls == Double.class) 
            return new DoubleCAdapter(sValue);    

        //Long
        if(cls == long.class) 
            return new LongAdapter(sValue);
        if(cls == Long.class) 
            return new LongCAdapter(sValue);    

        throw new LoadException("Unsupported property type: " + cls + ".");
    }
}
