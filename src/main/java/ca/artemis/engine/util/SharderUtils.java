package ca.artemis.engine.util;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.vulkan.VK11;

import ca.artemis.engine.rendering.programs.ShaderProgram;
import ca.artemis.engine.rendering.resources.Shader;

public class SharderUtils {

    public static Shader loadShader(String shaderName) {
        
    }

    public static Spirv compileShaderFile(String path, ShaderStageKind shaderStageKind) {
        try {
            String source = FileUtils.readString(path);
            return compileShader(path, source, shaderStageKind);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Spirv compileShader(String path, String source, ShaderStageKind shaderStageKind) {
        long compiler = Shaderc.shaderc_compiler_initialize();
        if(compiler == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create shader compiler");
        }

        long result = Shaderc.shaderc_compile_into_spv(compiler, source, shaderStageKind.getKind(), path, "main", MemoryUtil.NULL);
        if(result == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to compile shader " + path + " into SPIR-V");
        }
        if(Shaderc.shaderc_result_get_compilation_status(result) != Shaderc.shaderc_compilation_status_success) {
            throw new RuntimeException("Failed to compile shader " + path + " into SPIR-V:\n " + Shaderc.shaderc_result_get_error_message(result));
        }

        Shaderc.shaderc_compiler_release(compiler);

        return new Spirv(result, Shaderc.shaderc_result_get_bytes(result), shaderStageKind);
    }

    public enum ShaderStageKind {

        VERTEX_SHADER(VK11.VK_SHADER_STAGE_VERTEX_BIT, Shaderc.shaderc_glsl_vertex_shader),
        GEOMETRY_SHADER(VK11.VK_SHADER_STAGE_GEOMETRY_BIT, Shaderc.shaderc_glsl_geometry_shader),
        FRAGMENT_SHADER(VK11.VK_SHADER_STAGE_FRAGMENT_BIT, Shaderc.shaderc_glsl_fragment_shader);

        private final int stage;
        private final int kind;

        ShaderStageKind(int stage, int kind) {
            this.stage = stage;
            this.kind = kind;
        }

        public int getStage() {
            return stage;
        }

        public int getKind() {
            return kind;
        }
    }

    public static final class Spirv {

        private final long handle;
        private final ByteBuffer bytecode;
        private final ShaderStageKind shaderStageKind;

        public Spirv(long handle, ByteBuffer bytecode, ShaderStageKind shaderStageKind) {
            this.handle = handle;
            this.bytecode = bytecode;
            this.shaderStageKind = shaderStageKind;
        }

        public ByteBuffer getBytecode() {
            return bytecode;
        }

        public ShaderStageKind getShaderStageKind() {
            return shaderStageKind;
        }

        public void destroy() {
            Shaderc.shaderc_result_release(handle);
        }
    }
}