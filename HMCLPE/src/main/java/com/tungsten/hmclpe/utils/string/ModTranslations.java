package com.tungsten.hmclpe.utils.string;

import static com.tungsten.hmclpe.utils.Pair.pair;
import static com.tungsten.hmclpe.utils.Logging.LOG;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.tungsten.hmclpe.utils.Pair;
import com.tungsten.hmclpe.utils.io.IOUtils;

/**
 * Parser for mod_data.txt
 *
 * @see <a href="https://www.mcmod.cn">mcmod.cn</a>
 */
public final class ModTranslations {
    public static List<Mod> mods;
    private static Map<String, Mod> modIdMap; // mod id -> mod
    private static Map<String, Mod> curseForgeMap; // curseforge id -> mod
    private static List<Pair<String, Mod>> keywords;
    private static int maxKeywordLength = -1;

    private ModTranslations(){}

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Mod getModByCurseForgeId(String id) {
        if (StringUtils.isBlank(id) || !loadCurseForgeMap()) return null;

        return curseForgeMap.get(id);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Mod getModById(String id) {
        if (StringUtils.isBlank(id) || !loadModIdMap()) return null;

        return modIdMap.get(id);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static List<Mod> searchMod(String query) {
        if (!loadKeywords()) return Collections.emptyList();

        StringBuilder newQuery = query.chars()
                .filter(ch -> !Character.isSpaceChar(ch))
                .collect(StringBuilder::new, (sb, value) -> sb.append((char)value), StringBuilder::append);
        query = newQuery.toString();

        StringUtils.LongestCommonSubsequence lcs = new StringUtils.LongestCommonSubsequence(query.length(), maxKeywordLength);
        List<Pair<Integer, Mod>> modList = new ArrayList<>();
        for (Pair<String, Mod> keyword : keywords) {
            int value = lcs.calc(query, keyword.getKey());
            if (value >= Math.max(1, query.length() - 3)) {
                modList.add(pair(value, keyword.getValue()));
            }
        }
        return modList.stream()
                .sorted((a, b) -> -a.getKey().compareTo(b.getKey()))
                .map(Pair::getValue)
                .collect(Collectors.toList());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static boolean loadFromResource() {
        if (mods != null) return true;
        try {
            String modData = IOUtils.readFullyAsString(ModTranslations.class.getResourceAsStream("/assets/mod_data.txt"), StandardCharsets.UTF_8);
            mods = Arrays.stream(modData.split("\n")).filter(line -> !line.startsWith("#")).map(Mod::new).collect(Collectors.toList());
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to load /assets/mod_data.txt", e);
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static boolean loadCurseForgeMap() {
        if (curseForgeMap != null) {
            return true;
        }

        if (mods == null) {
            if (!loadFromResource()) return false;
        }

        curseForgeMap = new HashMap<>();
        for (Mod mod : mods) {
            if (StringUtils.isNotBlank(mod.getCurseforge())) {
                curseForgeMap.put(mod.getCurseforge(), mod);
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static boolean loadModIdMap() {
        if (modIdMap != null) {
            return true;
        }

        if (mods == null) {
            if (loadFromResource()) {
            } else {
                return false;
            }
        }

        modIdMap = new HashMap<>();
        for (Mod mod : mods) {
            for (String id : mod.getModIds()) {
                modIdMap.put(id, mod);
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static boolean loadKeywords() {
        if (keywords != null) {
            return true;
        }

        if (mods == null) {
            if (!loadFromResource()) return false;
        }

        keywords = new ArrayList<>();
        maxKeywordLength = -1;
        for (Mod mod : mods) {
            if (StringUtils.isNotBlank(mod.getName())) {
                keywords.add(pair(mod.getName(), mod));
                maxKeywordLength = Math.max(maxKeywordLength, mod.getName().length());
            }
            if (StringUtils.isNotBlank(mod.getSubname())) {
                keywords.add(pair(mod.getSubname(), mod));
                maxKeywordLength = Math.max(maxKeywordLength, mod.getSubname().length());
            }
            if (StringUtils.isNotBlank(mod.getAbbr())) {
                keywords.add(pair(mod.getAbbr(), mod));
                maxKeywordLength = Math.max(maxKeywordLength, mod.getAbbr().length());
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getDisplayName(String title,String slug) {
        String name = title;
        loadFromResource();
        for (int i = 0;i < mods.size();i++){
            if (mods.get(i).getCurseforge().equals(slug)){
                String first = "";
                if (!mods.get(i).getAbbr().equals("") && mods.get(i).getAbbr() != null){
                    first = "[" + mods.get(i).getAbbr() + "] ";
                }
                String second = title;
                if (!mods.get(i).getName().equals("") && mods.get(i).getName() != null){
                    second = mods.get(i).getName();
                }
                String third = "";
                if (!mods.get(i).getSubname().equals("") && mods.get(i).getSubname() != null){
                    third = " ("  + mods.get(i).getSubname() + ")";
                }
                name = first + second + third;
            }
        }
        return name;
    }

    public static class Mod {
        private final String curseforge;
        private final String mcmod;
        private final String mcbbs;
        private final List<String> modIds;
        private final String name;
        private final String subname;
        private final String abbr;

        public Mod(String line) {
            String[] items = line.split(";", -1);
            if (items.length != 7) {
                throw new IllegalArgumentException("Illegal mod data line, 7 items expected " + line);
            }

            curseforge = items[0];
            mcmod = items[1];
            mcbbs = items[2];
            modIds = Collections.unmodifiableList(Arrays.asList(items[3].split(",")));
            name = items[4];
            subname = items[5];
            abbr = items[6];
        }

        public Mod(String curseforge, String mcmod, String mcbbs, List<String> modIds, String name, String subname, String abbr) {
            this.curseforge = curseforge;
            this.mcmod = mcmod;
            this.mcbbs = mcbbs;
            this.modIds = modIds;
            this.name = name;
            this.subname = subname;
            this.abbr = abbr;
        }

        public String getDisplayName() {
            StringBuilder builder = new StringBuilder();
            if (StringUtils.isNotBlank(abbr)) {
                builder.append("[").append(abbr.trim()).append("] ");
            }
            builder.append(name);
            if (StringUtils.isNotBlank(subname)) {
                builder.append(" (").append(subname).append(")");
            }
            return builder.toString();
        }

        public String getCurseforge() {
            return curseforge;
        }

        public String getMcmod() {
            return mcmod;
        }

        public String getMcbbs() {
            return mcbbs;
        }

        public List<String> getModIds() {
            return modIds;
        }

        public String getName() {
            return name;
        }

        public String getSubname() {
            return subname;
        }

        public String getAbbr() {
            return abbr;
        }
    }
}
