package com.sorin.grecu.ledstrip.ledStrip.Effects;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Sorin on 7/26/2016.
 */
@Slf4j
public class LedEffectRegistry {

    private static List<LedEffect> effectsRegistry = new ArrayList<>();
    private static int currentEffectPosition = 0;

    public static void putEffect(LedEffect ledEffect) {
        if (ledEffect != null) {
            effectsRegistry.add(ledEffect);
            log.info("Registered {} effect", ledEffect.getClass().getSimpleName());
        } else {
            log.warn("Led effect is null");
        }
    }

    public static LedEffect getNext() {
        currentEffectPosition = currentEffectPosition + 1 >= effectsRegistry.size() ? 0 : currentEffectPosition + 1;
        LedEffect next=effectsRegistry.get(currentEffectPosition);
        log.info("Switching to {}",next.getClass().getSimpleName());
        return next;
    }

    public static LedEffect getCurrent() {
        if (currentEffectPosition == -1) {
            return getByName(ManualEffect.class.getSimpleName()).get();
        }
        return effectsRegistry.get(currentEffectPosition);
    }

    public static Optional<LedEffect> getByName(String effectName) {
        return effectsRegistry.stream()
                .filter(effect -> effect.getClass().getSimpleName().toUpperCase().contains(effectName.toUpperCase()))
                .findFirst();
    }
}
