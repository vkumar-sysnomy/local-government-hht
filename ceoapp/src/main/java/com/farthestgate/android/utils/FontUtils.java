package com.farthestgate.android.utils;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FontUtils
{

    public static final Map<String, String> TAXONOMY_FONT_MAPPING;
//    public static final Map<String, Integer> TAXONOMY_EVENT_BG_MAPPING;  // REUSE !!

    static {
        Map<String, String> map = new HashMap<String, String>();
        map.put("basketball", "\uE000");
        map.put("baseball", "\uE001");
        map.put("aussie rules", "\uE002");
        map.put("athletics", "\uE003");
        map.put("american football", "\uE004");
        map.put("road cycling", "\uE005");
        map.put("rugby league", "\uE006");
        map.put("rugby union", "\uE007");
        map.put("snooker", "\uE008");
        map.put("squash", "\uE009");
        map.put("tennis", "\uE00A");
        map.put("cycling", "\uE00B");
        map.put("wrestling", "\uE00C");
        map.put("judo", "\uE00D");
        map.put("ice hockey", "\uE00E");
        map.put("horse racing", "\uE00F");
        map.put("golf", "\uE010");
        map.put("cricket", "\uE011");
        map.put("boxing", "\uE012");
        map.put("volleyball", "\uE013");
        map.put("polo", "\uE014");
        map.put("ping pong", "\uE015");
        map.put("formula 1", "\uE016");
        map.put("nascar", "\uE017");
        map.put("triathlon", "\uE018");
        map.put("moto gp", "\uE019");
        map.put("beach volleyball", "\uE01A");
        map.put("swimming", "\uE01B");
        map.put("sailing", "\uE01C");
        map.put("mma", "\uE01D");
        map.put("badminton", "\uE01E");
        map.put("extreme sports", "\uE01F");
        map.put("rowing", "\uE020");
        map.put("british gt", "\uE021");
        map.put("hockey", "\uE022");
        map.put("motor racing", "\uE023");
        map.put("football", "\uE024");
        TAXONOMY_FONT_MAPPING = Collections.unmodifiableMap(map);

        //Method to display icons - need to reuse

     /*   Map<String, Integer> eventBgMap = new HashMap<String, Integer>();
        eventBgMap.put("basketball", R.drawable.basketball_event);
        eventBgMap.put("baseball", R.drawable.profile_cover_placeholder);
        eventBgMap.put("aussie rules", R.drawable.rugby_event);
        eventBgMap.put("athletics", R.drawable.profile_cover_placeholder);
        eventBgMap.put("american football", R.drawable.american_football_event);
        eventBgMap.put("road cycling", R.drawable.profile_cover_placeholder);
        eventBgMap.put("rugby league", R.drawable.rugby_event);
        eventBgMap.put("rugby union", R.drawable.rugby_event);
        eventBgMap.put("snooker", R.drawable.profile_cover_placeholder);
        eventBgMap.put("squash", R.drawable.profile_cover_placeholder);
        eventBgMap.put("tennis", R.drawable.tennis_event);
        eventBgMap.put("cycling", R.drawable.profile_cover_placeholder);
        eventBgMap.put("wrestling", R.drawable.profile_cover_placeholder);
        eventBgMap.put("judo", R.drawable.profile_cover_placeholder);
        eventBgMap.put("ice hockey", R.drawable.ice_hockey_event);
        eventBgMap.put("horse racing", R.drawable.profile_cover_placeholder);
        eventBgMap.put("golf", R.drawable.golf_event);
        eventBgMap.put("cricket", R.drawable.cricket_event);
        eventBgMap.put("boxing", R.drawable.profile_cover_placeholder);
        eventBgMap.put("volleyball", R.drawable.profile_cover_placeholder);
        eventBgMap.put("polo", R.drawable.profile_cover_placeholder);
        eventBgMap.put("ping pong", R.drawable.profile_cover_placeholder);
        eventBgMap.put("formula 1", R.drawable.formula_event);
        eventBgMap.put("nascar", R.drawable.profile_cover_placeholder);
        eventBgMap.put("triathlon", R.drawable.profile_cover_placeholder);
        eventBgMap.put("moto gp", R.drawable.profile_cover_placeholder);
        eventBgMap.put("beach volleyball", R.drawable.profile_cover_placeholder);
        eventBgMap.put("swimming", R.drawable.profile_cover_placeholder);
        eventBgMap.put("sailing", R.drawable.profile_cover_placeholder);
        eventBgMap.put("mma", R.drawable.profile_cover_placeholder);
        eventBgMap.put("badminton", R.drawable.profile_cover_placeholder);
        eventBgMap.put("extreme sports", R.drawable.profile_cover_placeholder);
        eventBgMap.put("rowing", R.drawable.profile_cover_placeholder);
        eventBgMap.put("british gt", R.drawable.profile_cover_placeholder);
        eventBgMap.put("hockey", R.drawable.profile_cover_placeholder);
        eventBgMap.put("motor racing", R.drawable.profile_cover_placeholder);
        eventBgMap.put("football", R.drawable.football_event);
        TAXONOMY_EVENT_BG_MAPPING = Collections.unmodifiableMap(eventBgMap);*/
    }

}


