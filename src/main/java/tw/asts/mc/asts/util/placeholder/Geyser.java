package tw.asts.mc.asts.util.placeholder;

import org.geysermc.geyser.api.GeyserApi;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
final public class Geyser {
    private List<String> supportedVersions = null;

    public List<String> getSupportedVersions() {
        if (supportedVersions == null) {
            supportedVersions = Arrays.stream(GeyserApi.api().supportedBedrockVersions().getLast().versionString().split("/")).toList();
        }
        return supportedVersions;
    }

    public String getLastSupportedVersion() {
        return getSupportedVersions().getLast();
    }
}
