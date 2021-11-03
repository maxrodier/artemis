package ca.artemis.engine.text;

public class Character {
    
    public final float xMinTexCoord;
    public final float xMaxTexCoord;
    public final float yMinTexCoord;
    public final float yMaxTexCoord;
    public final float width;
    public final float height;
    public final float xOffset;
    public final float yOffset;
    public final float xAdvance;
    
    public Character(float xMinTexCoord, float xMaxTexCoord, float yMinTexCoord, float yMaxTexCoord, float width, float height, float xOffset, float yOffset, float xAdvance) {
        this.xMinTexCoord = xMinTexCoord;
        this.xMaxTexCoord = xMaxTexCoord;
        this.yMinTexCoord = yMinTexCoord;
        this.yMaxTexCoord = yMaxTexCoord;
        this.width = width;
        this.height = height;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.xAdvance = xAdvance;
    }
}
