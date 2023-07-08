package com.artillexstudios.axenvoy.user;

import com.artillexstudios.axenvoy.envoy.Crate;
import com.artillexstudios.axenvoy.envoy.Envoy;

public final class CrateCooldown {
    public Envoy envoy;
    public Crate crate;
    public long end;

    public CrateCooldown(Envoy envoy, Crate crate, long end) {
        this.envoy = envoy;
        this.crate = crate;
        this.end = end;
    }
}
