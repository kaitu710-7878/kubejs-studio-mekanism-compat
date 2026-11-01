package com.github.antigravity.kubejsstudio.mekanism.client;

import com.github.antigravity.kubejsstudio.mekanism.KubeJSStudioMekanismCompat;
import com.github.antigravity.kubejsstudio.mekanism.gas.GasScriptSerializer;
import com.github.antigravity.kubejsstudio.mekanism.gas.MekanismGasDefinition;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Safe helper to interface with KubeJS Studio's Workspace and Operation system,
 * with guaranteed physical file synchronization to `kubejs/startup_scripts/`.
 */
public final class StudioWorkspaceHelper {
    private StudioWorkspaceHelper() {}

    /**
     * Queues a gas definition into KubeJS Studio's Workspace pending list as a KubeScript operation,
     * and guarantees persistent export to kubejs/startup_scripts/mekanism_chemicals.js.
     */
    public static boolean queueGasIntoWorkspace(MekanismGasDefinition gas) {
        boolean workspaceQueued = false;
        try {
            Class<?> workspaceCls = Class.forName("dev.tysontheember.kubejsstudio.client.Workspace");
            Field instanceField = workspaceCls.getField("INSTANCE");
            Object workspace = instanceField.get(null);

            Class<?> backendCls = Class.forName("dev.tysontheember.kubejsstudio.core.op.Backend");
            Field kubejsBackendField = backendCls.getField("KUBEJS");
            Object kubejsBackend = kubejsBackendField.get(null);

            Class<?> kubeScriptCls = Class.forName("dev.tysontheember.kubejsstudio.core.op.KubeScript");
            // Constructor: KubeScript(String targetId, String body, boolean override, String label, Backend backend)
            Constructor<?> ctor = kubeScriptCls.getConstructor(String.class, String.class, boolean.class, String.class, backendCls);

            String type = gas.chemicalType() != null ? gas.chemicalType() : "gas";
            String scriptSnippet = "StartupEvents.registry('mekanism:" + type + "', event => {\n" + gas.toKubeJsStatement() + "\n});";
            String label = "Chemical (" + type + "): " + gas.id();
            Object op = ctor.newInstance(gas.id(), scriptSnippet, false, label, kubejsBackend);

            Method addMethod = workspaceCls.getMethod("add", Class.forName("dev.tysontheember.kubejsstudio.core.op.Operation"));
            addMethod.invoke(workspace, op);

            KubeJSStudioMekanismCompat.LOGGER.info("Successfully queued {} into KubeJS Studio Workspace!", gas.id());
            workspaceQueued = true;
        } catch (Throwable t) {
            KubeJSStudioMekanismCompat.LOGGER.warn("Could not queue via KubeJS Studio Workspace: {}", t.getMessage());
        }

        // Always also write directly to disk to guarantee instant persistence
        saveChemicalToStartupScript(gas);

        return workspaceQueued;
    }

    /**
     * Safely writes or appends the custom chemical to `kubejs/startup_scripts/mekanism_chemicals.js`.
     */
    public static boolean saveChemicalToStartupScript(MekanismGasDefinition gas) {
        try {
            Path gameDir = null;
            try {
                gameDir = FMLPaths.GAMEDIR.get();
            } catch (Throwable ignored) {
                gameDir = Path.of(".");
            }

            Path scriptDir = gameDir.resolve("kubejs").resolve("startup_scripts");
            Files.createDirectories(scriptDir);
            Path scriptFile = scriptDir.resolve("mekanism_chemicals.js");

            List<MekanismGasDefinition> gases = new ArrayList<>();
            if (Files.exists(scriptFile)) {
                String existing = Files.readString(scriptFile, StandardCharsets.UTF_8);
                gases.addAll(GasScriptSerializer.parseScript(existing));
            }

            // Remove existing by ID if already present, then add updated
            gases.removeIf(g -> g.id().equalsIgnoreCase(gas.id()));
            gases.add(gas);

            String fullScript = GasScriptSerializer.buildStartupScript(gases);
            Files.writeString(scriptFile, fullScript, StandardCharsets.UTF_8);
            KubeJSStudioMekanismCompat.LOGGER.info("Successfully saved chemical {} to {}", gas.id(), scriptFile);
            return true;
        } catch (IOException e) {
            KubeJSStudioMekanismCompat.LOGGER.error("Failed to write chemical script to startup_scripts: {}", e.getMessage(), e);
            return false;
        }
    }
}
