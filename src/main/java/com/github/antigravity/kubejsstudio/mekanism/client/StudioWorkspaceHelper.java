package com.github.antigravity.kubejsstudio.mekanism.client;

import com.github.antigravity.kubejsstudio.mekanism.KubeJSStudioMekanismCompat;
import com.github.antigravity.kubejsstudio.mekanism.gas.MekanismGasDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Safe reflection helper to interface with KubeJS Studio's Workspace and Operation system.
 */
public final class StudioWorkspaceHelper {
    private StudioWorkspaceHelper() {}

    /**
     * Queues a gas definition into KubeJS Studio's Workspace pending list as a KubeScript operation.
     */
    public static boolean queueGasIntoWorkspace(MekanismGasDefinition gas) {
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
            return true;
        } catch (Throwable t) {
            KubeJSStudioMekanismCompat.LOGGER.warn("Could not queue via KubeJS Studio Workspace (falling back to internal queue): {}", t.getMessage());
            return false;
        }
    }
}
