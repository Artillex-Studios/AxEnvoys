package com.artillexstudios.axenvoy.user;

import com.artillexstudios.axenvoy.envoy.CrateType;
import com.artillexstudios.axenvoy.envoy.Envoy;

public final class CrateCooldown {
    public Envoy envoy;
    public CrateType crateType;
    public long end;

    public CrateCooldown(Envoy envoy, CrateType crateType, long end) {
        this.envoy = envoy;
        this.crateType = crateType;
        this.end = end;
    }
}
