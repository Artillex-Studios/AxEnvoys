package me.bencex100.rivalsenvoy.crateconfig;

import me.bencex100.rivalsenvoy.RivalsEnvoy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;

public class Crates {
    private final static HashMap<String, Crate> crates = new HashMap<>();

    @NotNull
    public static HashMap<String, Crate> getCrates() {
        return crates;
    }

    @Nullable
    public static Crate getCrate(@NotNull String name) {
        return crates.getOrDefault(name, null);
    }

    public static void loadCrates() {
        crates.clear();
        File path = new File(RivalsEnvoy.getInstance().getDataFolder(), "crates");
        for (File file : path.listFiles()) {
            Crate cr = new Crate(file.getName().replace(".yml", ""));
            crates.put(file.getName().replace(".yml", ""), cr);
        }
    }
}
