package com.dolacraft.loyalty.managers;

import com.dolacraft.loyalty.config.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PayoutManager {
    private static PayoutManager instance;

    private final Map<UUID, Long> payoutTimes = new HashMap<>();

    public final int DEFAULT_PAYOUT_TIME = Config.getInstance().getPayoutDelay();

    public static PayoutManager getInstance () {
        if (instance == null) {
            instance = new PayoutManager();
        }

        return instance;
    }

    public void addPlayerPayout (UUID player) {
        if (!payoutTimes.containsKey(player)) {
            payoutTimes.put(player, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(DEFAULT_PAYOUT_TIME));
        }
    }

    public void updatePlayerPayout (UUID player) {
        if (payoutTimes.containsKey(player)) {
            payoutTimes.replace(player, getPayoutTime(player) + TimeUnit.SECONDS.toMillis(DEFAULT_PAYOUT_TIME));
        } else {
            addPlayerPayout(player);
        }
    }

    public void removePlayerPayout (UUID player) {
        payoutTimes.remove(player);
    }

    public Long getPayoutTime (UUID player) {
        return payoutTimes.getOrDefault(player, System.currentTimeMillis());
    }

    public Map<UUID, Long> getPayoutList () {
        return payoutTimes;
    }
}
