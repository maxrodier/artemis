package ca.artemis.engine.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.vulkan.VkQueue;

import ca.artemis.FileUtils;
import ca.artemis.Mesh;
import ca.artemis.Util;
import ca.artemis.Vector2f;
import ca.artemis.Vector3f;
import ca.artemis.Vertex;
import ca.artemis.Vertex.VertexKind;
import ca.artemis.vulkan.api.commands.CommandPool;
import ca.artemis.vulkan.api.context.VulkanDevice;
import ca.artemis.vulkan.api.context.VulkanMemoryAllocator;

public class MeshLoader {
    
    public static Mesh loadModel(VulkanDevice device, VulkanMemoryAllocator allocator, VkQueue graphicsQueue, CommandPool commandPool, String filePath) {
	    List<Vector3f> positions = new ArrayList<>();
	    List<Vector2f> texCoords = new ArrayList<>();
	    List<Vector3f> normals = new ArrayList<>();
	    List<OBJIndex> indices = new ArrayList<>();;

		try {
            for(String line : FileUtils.readLines(filePath)) {
				String[] tokens = line.split(" ");
				tokens = Util.RemoveEmptyStrings(tokens);

				if(tokens.length == 0 || tokens[0].equals("#"))
					continue;
				else if(tokens[0].equals("v"))
				{
					positions.add(new Vector3f(Float.valueOf(tokens[1]),
							Float.valueOf(tokens[2]),
							Float.valueOf(tokens[3])));
				}
				else if(tokens[0].equals("vt"))
				{
					texCoords.add(new Vector2f(Float.valueOf(tokens[1]),
							1.0f - Float.valueOf(tokens[2])));
				}
                else if(tokens[0].equals("vn"))
				{
					normals.add(new Vector3f(Float.valueOf(tokens[1]),
							Float.valueOf(tokens[2]),
							Float.valueOf(tokens[3])));
				}
				else if(tokens[0].equals("f"))
				{
					indices.add(OBJIndex.parseOBJIndex(tokens[1]));
					indices.add(OBJIndex.parseOBJIndex(tokens[2]));
					indices.add(OBJIndex.parseOBJIndex(tokens[3]));
				}
			}

            return toIndexedModel(device, allocator, graphicsQueue, commandPool, positions, texCoords, normals, indices);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException("Failed to load model");
		}
	}

    private static Mesh toIndexedModel(VulkanDevice device, VulkanMemoryAllocator allocator, VkQueue graphicsQueue, CommandPool commandPool, List<Vector3f> positions, List<Vector2f> texCoords, List<Vector3f> normals, List<OBJIndex> indices) {
		HashMap<OBJIndex, Integer> resultIndexMap = new HashMap<OBJIndex, Integer>();
        List<Integer> result = new ArrayList<>();
        List<Vertex> vertices = new ArrayList<>();

		for(int i = 0; i < indices.size(); i++) {
			OBJIndex currentIndex = indices.get(i);

			Vector3f currentPosition = positions.get(currentIndex.GetVertexIndex());
			Vector2f currentTexCoord = texCoords.get(currentIndex.GetTexCoordIndex());
			Vector3f currentNormal = normals.get(currentIndex.GetNormalIndex());

			Integer modelVertexIndex = resultIndexMap.get(currentIndex);

			if(modelVertexIndex == null) {
				modelVertexIndex = vertices.size();
				resultIndexMap.put(currentIndex, modelVertexIndex);

                vertices.add(new Vertex(currentPosition, currentTexCoord, currentNormal));
			}

			result.add(modelVertexIndex);
		}

		return new Mesh(device, allocator, graphicsQueue, commandPool,vertices.toArray(new Vertex[vertices.size()]), result.toArray(new Integer[result.size()]), VertexKind.POS_UV_NORMAL);
	}

    private static class OBJIndex {
        private int vertexIndex;
        private int texCoordIndex;
        private int normalIndex;

        public int GetVertexIndex()   { return vertexIndex; }
        public int GetTexCoordIndex() { return texCoordIndex; }
        public int GetNormalIndex() { return normalIndex; }

        public void SetVertexIndex(int val)   { vertexIndex = val; }
        public void SetTexCoordIndex(int val) { texCoordIndex = val; }
        public void SetNormalIndex(int val) { normalIndex = val; }

        public static OBJIndex parseOBJIndex(String token){
            String[] values = token.split("/");

            OBJIndex result = new OBJIndex();
            result.SetVertexIndex(Integer.parseInt(values[0]) - 1);
            result.SetTexCoordIndex(Integer.parseInt(values[1]) - 1);
            result.SetNormalIndex(Integer.parseInt(values[2]) - 1);

            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            OBJIndex index = (OBJIndex)obj;

            return vertexIndex == index.vertexIndex
                    && texCoordIndex == index.texCoordIndex;
        }

        @Override
        public int hashCode() {
            final int BASE = 17;
            final int MULTIPLIER = 31;

            int result = BASE;

            result = MULTIPLIER * result + vertexIndex;
            result = MULTIPLIER * result + texCoordIndex;
            result = MULTIPLIER * result + normalIndex;

            return result;
        }
    }
}
