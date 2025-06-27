package com.jinzo.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WeaponKillData {
    public Set<UUID> uniqueKills = Collections.synchronizedSet(new HashSet<>());
    public UUID lastKilled = null;
}