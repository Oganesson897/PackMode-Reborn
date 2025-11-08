package io.sommers.packmode.core;

import io.sommers.packmode.PMConfig;
import net.minecraftforge.fml.relauncher.IFMLCallHook;

import java.io.File;
import java.util.Map;

import static io.sommers.packmode.core.SelectUI.overrideConfig;

public class PreLauncher implements IFMLCallHook {

    private File mcLocation;

    @Override
    public void injectData(Map<String, Object> data) {
        mcLocation = (File) data.get("mcLocation");
    }

    @Override
    public Void call() {
        if (PMConfig.enabledTipScreen()) {
            new SelectUI(mcLocation);
        } else {
            overrideConfig(mcLocation);
        }
        return null;
    }
}
