package com.nubi.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class KeyMappings {
    /** Mantén pulsada esta tecla (por defecto G) para flotar con la armadura de nube. */
    public static final KeyMapping FLOAT =
            new KeyMapping("key.nubi.float", GLFW.GLFW_KEY_G, "key.categories.nubi");

    private KeyMappings() {}
}
