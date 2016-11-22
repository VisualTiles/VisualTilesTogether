package com.javierarboleda.visualtilestogether.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chris on 11/18/16.
 */

public class TileEffect {
    public static final String START_TIME_ID = "startTimeMillis";
    // {"effectType": "FADE_HALF", "startTimeMillis": 1, "effectOffsetPct": 0.0,
    // "effectDurationPct": 1.0}
    // Do not re-use names.
    public enum EffectType {
        NONE,
        FADE_HALF,
        FLIP_HORIZONTAL,
        FLY_AWAY,
        FREEZE,
        ROTATE_LEFT,
        ROTATE_RIGHT,
    }
    private String  effectType;
    /**
     * A generic start time of the effect.. is used as a unique identifier when effect is triggered.
     */
    private Long startTimeMillis;
    /** Delay, as % of a loop from the time of the 'start' of the master loop duration.
    /*  Offset + effectDurationMs must be less than 1 (the entire loop duration), or the effect
    /*  will be cancelled and reset early.
     **/
    private Double effectOffsetPct;
    private Double effectDurationPct;
    // Custom parameters used by effects.
    private Map<String, String> parameters;

    // Test effect.
    public static TileEffect buildBasicEffect(EffectType type, double offset, double width) {
        TileEffect tileEffect = new TileEffect();
        tileEffect.effectType = type.name();
        tileEffect.startTimeMillis = System.currentTimeMillis();

        tileEffect.effectOffsetPct = offset;
        tileEffect.effectDurationPct = width;
        return tileEffect;
    }

    public TileEffect() {}

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("effectType", effectType);
        map.put(START_TIME_ID, startTimeMillis);
        map.put("effectOffsetPct", effectOffsetPct);
        map.put("effectDurationPct", effectDurationPct);
        map.put("parameters", parameters);
        return map;
    }

    public static TileEffect fromMap(Map<String, Object> fieldMap) {
        if (fieldMap == null)
            return null;
        TileEffect tileEffect = new TileEffect();
        tileEffect.effectType = (String) fieldMap.get("effectType");
        tileEffect.startTimeMillis = (Long) fieldMap.get(START_TIME_ID);
        tileEffect.effectOffsetPct = (Double) fieldMap.get("effectOffsetPct");
        tileEffect.effectDurationPct = (Double) fieldMap.get("effectDurationPct");
        Object param = fieldMap.get("parameters");
        if (param != null && tileEffect.getClass().isInstance(param)) {
            @SuppressWarnings("unchecked")
            Map<String, String> paramMap = (Map<String, String>) param;
            tileEffect.parameters = paramMap;
        }
        return tileEffect;
    }

    public Double getEffectDurationPct() {
        return effectDurationPct;
    }

    public void setEffectDurationPct(Double effectDurationPct) {
        this.effectDurationPct = effectDurationPct;
    }

    public Double getEffectOffsetPct() {
        return effectOffsetPct;
    }

    public void setEffectOffsetPct(Double effectOffsetPct) {
        this.effectOffsetPct = effectOffsetPct;
    }

    public String getEffectType() {
        return effectType;
    }

    public void setEffectType(String effectType) {
        this.effectType = effectType;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Long getStartTimeMillis() {
        return startTimeMillis;
    }

    public void setStartTimeMillis(Long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }
}
