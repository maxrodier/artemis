package ca.artemis.engine.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ca.artemis.engine.api.vulkan.commands.CommandPool;
import ca.artemis.engine.api.vulkan.core.VulkanDevice;
import ca.artemis.engine.api.vulkan.core.VulkanMemoryAllocator;
import ca.artemis.engine.core.math.Vector2f;
import ca.artemis.engine.core.math.Vector3f;
import ca.artemis.engine.rendering.resources.Mesh;
import ca.artemis.engine.rendering.resources.Mesh.Vertex;

public class MeshUtils {
    
    public static Mesh loadMesh(VulkanDevice device, VulkanMemoryAllocator allocator, CommandPool commandPool, String meshName) {
        List<Vector3f> positions = new ArrayList<>();
        List<Vector2f> textures = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<String> vertices = new ArrayList<>();

        try {
            for(String line : FileUtils.readLines("models/" + meshName + ".obj")) {
                String[] tokens = line.split(" ");
                if(tokens.length == 0) {
                    continue;
                } 
                    
                String type = tokens[0];
                if(type.equals("v")) {
                    positions.add(new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
                } else if(type.equals("vt")) {
                    textures.add(new Vector2f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2])));
                } else if(type.equals("vn")) {
                    normals.add(new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
                } else if(type.equals("f")) {
                    vertices.add(tokens[1]);
                    vertices.add(tokens[2]);
                    vertices.add(tokens[3]);
                }
            }

            HashMap<Vertex, Integer> resultIndexMap = new HashMap<>();
            List<Vertex> resultVertices = new ArrayList<>();
            List<Integer> resultIndices = new ArrayList<>();

            for(int i = 0; i < vertices.size(); i++) {
                String[] tokens = vertices.get(i).split("/");

                Vector3f position = positions.get(Integer.parseInt(tokens[0]) - 1);
                Vector2f texture = textures.get(Integer.parseInt(tokens[1]) - 1);
                Vector3f normal = normals.get(Integer.parseInt(tokens[2]) - 1);

                Vertex vertex = new Vertex(position, texture, normal);
                Integer index = resultIndexMap.get(vertex);
                if(index == null) {
                    index = resultVertices.size();
                    resultIndexMap.put(vertex, index);
                    resultVertices.add(vertex);
                }

                resultIndices.add(index);
            }

            Vertex[] resultVerticesArray = resultVertices.toArray(new Vertex[resultVertices.size()]);
            Integer[] resultIndicesArray = resultIndices.toArray(new Integer[resultIndices.size()]);

            return new Mesh(device, allocator, commandPool, resultVerticesArray, resultIndicesArray);
        } catch (Exception e) {
            throw new RuntimeException("Could not load mesh: " + meshName, e);
        }
    }
}
