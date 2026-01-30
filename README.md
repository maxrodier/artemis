# Artemis - Vulkan Game Engine

A Java-based 3D game engine built on Vulkan, using LWJGL bindings for low-level graphics programming.

## Overview

Artemis is a modern game engine written in Java that leverages the Vulkan graphics API for high-performance rendering. The engine features a deferred rendering pipeline with multiple render passes, a scene management system, and cross-platform window management supporting both X11 and Wayland.

## Features

- **Vulkan-based rendering pipeline** with multi-pass architecture
- **Deferred rendering** with entity, UI, and swapchain renderers
- **Scene management** with session-based architecture
- **Cross-platform support** (Linux with X11/Wayland, Windows, macOS)
- **Hot-reloading** window resize support with automatic pipeline regeneration
- **Memory management** using Vulkan Memory Allocator (VMA)
- **Shader compilation** at runtime using shaderc

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Application Layer                        │
│  ┌─────────────┐    ┌──────────────┐    ┌──────────────────┐   │
│  │ GameManager │───▶│SessionManager│───▶│   Scene System   │   │
│  └─────────────┘    └──────────────┘    └──────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Rendering Engine Layer                      │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │          LowPolyRenderingEngine (Frame Control)          │   │
│  └──────────────────────────────────────────────────────────┘   │
│           │                 │                  │                 │
│           ▼                 ▼                  ▼                 │
│  ┌───────────────┐ ┌───────────────┐ ┌──────────────────┐      │
│  │    Entity     │ │      UI       │ │   Swapchain      │      │
│  │   Renderer    │ │   Renderer    │ │   Renderer       │      │
│  │               │ │               │ │                  │      │
│  │ - Secondary   │ │ - Primary     │ │ - Primary        │      │
│  │   Commands    │ │   Commands    │ │   Commands       │      │
│  │ - Culling     │ │ - Compositing │ │ - Presentation   │      │
│  └───────────────┘ └───────────────┘ └──────────────────┘      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Vulkan Abstraction Layer                      │
│  ┌─────────────┐  ┌──────────────┐  ┌───────────────────┐      │
│  │   Context   │  │   Pipeline   │  │   Framebuffer     │      │
│  │  - Instance │  │  - Shaders   │  │  - RenderPass     │      │
│  │  - Device   │  │  - Graphics  │  │  - Swapchain      │      │
│  │  - Surface  │  │    Pipeline  │  │  - Attachments    │      │
│  └─────────────┘  └──────────────┘  └───────────────────┘      │
│                                                                  │
│  ┌─────────────┐  ┌──────────────┐  ┌───────────────────┐      │
│  │   Memory    │  │  Commands    │  │  Synchronization  │      │
│  │  - Buffers  │  │  - Pools     │  │  - Semaphores     │      │
│  │  - Images   │  │  - Primary   │  │  - Fences         │      │
│  │  - VMA      │  │  - Secondary │  │                   │      │
│  └─────────────┘  └──────────────┘  └───────────────────┘      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         LWJGL Bindings                           │
│              Vulkan 1.1 │ GLFW │ VMA │ Shaderc                  │
└─────────────────────────────────────────────────────────────────┘
```

### Rendering Pipeline

The engine uses a **three-pass deferred rendering pipeline**:

```
Frame Start
    │
    ▼
┌─────────────────────────┐
│  Pass 1: Entity Pass    │
│  ─────────────────────  │
│  • Render 3D geometry   │
│  • Secondary commands   │
│  • Output: HDR buffer   │
│    (R16G16B16A16_SFLOAT)│
└─────────────────────────┘
    │
    ▼ [Wait: Entity Complete Semaphore]
    │
┌─────────────────────────┐
│  Pass 2: UI Pass        │
│  ─────────────────────  │
│  • Composite UI         │
│  • Sample entity buffer │
│  • Output: HDR buffer   │
│    (R16G16B16A16_SFLOAT)│
└─────────────────────────┘
    │
    ▼ [Wait: UI Complete Semaphore]
    │
┌─────────────────────────┐
│  Pass 3: Swapchain Pass │
│  ─────────────────────  │
│  • Final composition    │
│  • Sample UI buffer     │
│  • Output: Swapchain    │
│    (B8G8R8A8_SRGB)      │
└─────────────────────────┘
    │
    ▼
Present to Screen
```

### Key Components

**Core Systems:**
- `LowPolyEngine` - Main engine instance managing window, input, and Vulkan context
- `GameManager` - Game loop and frame timing
- `SessionManager` - Scene lifecycle management
- `LowPolyRenderingEngine` - Renderer orchestration and frame synchronization

**Vulkan Subsystems:**
- `VulkanContext` - Instance, device, surface, and memory allocator
- `RenderPass` - Configurable render pass builder
- `GraphicsPipeline` - Shader compilation and pipeline state
- `FramebufferObject` - Render targets and image management
- `CommandBuffer` - Primary and secondary command recording

**Rendering:**
- `EntityRenderer` - 3D geometry rendering with secondary command buffers
- `UiRenderer` - UI composition pass
- `SwapchainRenderer` - Final presentation pass

## Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **Vulkan SDK** installed and configured
- **Graphics drivers** with Vulkan 1.1+ support
- Platform-specific:
  - Linux: X11 or Wayland development libraries
  - Windows: Visual C++ Redistributable
  - macOS: MoltenVK

## Building

Build the project using Maven:

```bash
mvn clean package
```

This will create two JAR files in the `target` directory:
- `artemis-0.0.1-SNAPSHOT.jar` - Basic JAR
- `artemis-0.0.1-SNAPSHOT-jar-with-dependencies.jar` - Standalone JAR with all dependencies

## Running

Execute the application using the standalone JAR:

```bash
java -jar target/artemis-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

### Runtime Controls

- **F11** - Toggle fullscreen mode
- **Window resize** - Automatically regenerates rendering pipeline
- **Close window** - Graceful shutdown

## Project Structure

```
src/main/java/ca/artemis/
├── engine/                    # Core engine systems
│   ├── core/                  # Window, input management
│   ├── rendering/             # Rendering abstractions
│   │   ├── entity/           # Entity renderer
│   │   ├── ui/               # UI renderer
│   │   └── swapchain/        # Swapchain renderer
│   ├── vulkan/               # Vulkan API wrappers
│   │   ├── api/              
│   │   │   ├── commands/     # Command buffers
│   │   │   ├── context/      # Instance, device, surface
│   │   │   ├── descriptor/   # Descriptor sets
│   │   │   ├── framebuffer/  # Render passes, framebuffers
│   │   │   ├── memory/       # Buffers, images, VMA
│   │   │   ├── pipeline/     # Graphics pipelines, shaders
│   │   │   └── synchronization/ # Fences, semaphores
│   │   └── core/             # Shader programs, meshes
│   ├── scenes/               # Scene management
│   ├── sessions/             # Session management
│   └── maths/                # Math utilities
├── game/                     # Game-specific code
│   ├── rendering/            # Game rendering engine
│   └── scenes/               # Game scenes
└── UniformBufferObject.java  # Shader uniform data

src/main/resources/
├── shaders/                  # GLSL shader sources
│   ├── simple.vert          # Entity vertex shader
│   ├── simple.frag          # Entity fragment shader
│   ├── swapchain.vert       # Final pass vertex shader
│   └── swapchain.frag       # Final pass fragment shader
└── icons/                    # Application icons
```

## Configuration

Window configuration can be modified in `LowPolyEngine.Builder`:

```java
this.window = new Window.Builder(800, 600, "Artemis").build();
```

Rendering configuration:
- `MAX_FRAMES_IN_FLIGHT = 2` - Number of frames to process concurrently
- HDR format: `VK_FORMAT_R16G16B16A16_SFLOAT`
- Swapchain format: `VK_FORMAT_B8G8R8A8_SRGB`

## Memory Management

The engine uses Vulkan Memory Allocator (VMA) for efficient GPU memory management:
- Automatic memory pooling
- Defragmentation support
- CPU-to-GPU staging buffers
- GPU-only memory for vertex/index buffers

## Synchronization Model

Frame synchronization uses a dual semaphore chain:

```
Frame N:
  Entity Pass → [Entity Semaphore] → UI Pass → [UI Semaphore] → Swapchain Pass
                                                                       │
Frame N+1:                                                             │
  Wait on Fence ←────────────────────────────────────────────────────┘
```

Each renderer maintains:
- **Wait Semaphores** - From previous stage
- **Signal Semaphores** - For next stage
- **Fences** - For CPU-GPU synchronization

## Platform Support

### Linux
- X11 (primary)
- Wayland (experimental, libdecor disabled)
- Auto-detection via `XDG_SESSION_TYPE` environment variable

### Windows
- Win32 native windowing

### macOS
- MoltenVK required for Vulkan support

## Known Limitations

- No depth buffer in current implementation
- UI shaders are placeholder (empty)
- Single queue family usage
- Fixed viewport (matches window size)
- No texture loading system yet

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## Dependencies

- **LWJGL 3.3.4** - Java bindings for native libraries
  - Vulkan
  - GLFW (windowing)
  - VMA (memory allocation)
  - Shaderc (shader compilation)
- **GLM Java** - OpenGL Mathematics
- **Gson 2.8.8** - JSON serialization

## Performance Notes

The engine supports automatic pipeline regeneration on window resize, which includes:
- Waiting for device idle
- Recreating framebuffers and render passes
- Recompiling graphics pipelines
- Window size debouncing (500ms) to avoid excessive regeneration

Frame timing is calculated using nanosecond precision with FPS counter output to console.
