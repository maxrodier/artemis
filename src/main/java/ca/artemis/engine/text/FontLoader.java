package ca.artemis.engine.text;

import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import ca.artemis.engine.utils.FileUtils;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.api.memory.VulkanTexture;

public class FontLoader {
    
    public static Font load(VulkanContext context, String name) {
        Gson gson = new Gson();
        FontMetadata fontMetadata = null;
        try {
            fontMetadata = gson.fromJson(FileUtils.readString("fonts/" + name + ".json"), FontMetadata.class);
        } catch (JsonSyntaxException | IOException e) {
            e.printStackTrace();
        } 
        
        VulkanTexture texture = new VulkanTexture(context, "fonts/" + name +".png", false);
        Font font = new Font(texture);
        for(CharacterMetadata characterMetadata : fontMetadata.characters) {

            float xMinTexCoord = (characterMetadata.x) / fontMetadata.width;
            float yMinTexCoord = (characterMetadata.y) / fontMetadata.height;

            float xMaxTexCoord = ((characterMetadata.x + characterMetadata.width)) / fontMetadata.width;
            float yMaxTexCoord = ((characterMetadata.y + characterMetadata.height)) / fontMetadata.height;

            float width = characterMetadata.width / fontMetadata.size;
            float height = characterMetadata.height / fontMetadata.size;

            float xOffset = characterMetadata.xoffset / fontMetadata.size;
            float yOffset = characterMetadata.yoffset / fontMetadata.size;

            float xAdvance = characterMetadata.xadvance / fontMetadata.size;

            Character character = new Character(xMinTexCoord, xMaxTexCoord, yMinTexCoord, yMaxTexCoord, width, height, xOffset, yOffset, xAdvance);
            font.characters.put((int)characterMetadata.id, character);
        }
        return font;
    }

    private class FontMetadata {

        private float size;
        private float width;
        private float height;
        private List<CharacterMetadata> characters;
    }

    private class CharacterMetadata {

        private char id;
        private float x;
        private float y;
        private float width;
        private float height;
        private float xoffset;
        private float yoffset;
        private float xadvance;
    }
}
