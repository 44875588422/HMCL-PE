package com.tungsten.hmclpe.launcher.game;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.gson.JsonParseException;
import com.tungsten.hmclpe.utils.Lang;
import com.tungsten.hmclpe.utils.Logging;
import com.tungsten.hmclpe.utils.gson.JsonMap;
import com.tungsten.hmclpe.utils.gson.tools.Validation;
import com.tungsten.hmclpe.utils.string.StringUtils;
import com.tungsten.hmclpe.utils.string.ToStringBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 *
 * @author huangyuhui
 */
public class Version implements Comparable<Version>, Validation {

    public static final String DEFAULT_LIBRARY_URL = "https://libraries.minecraft.net/";
    public static final String DEFAULT_VERSION_DOWNLOAD_URL = "https://s3.amazonaws.com/Minecraft.Download/versions/";
    public static final String DEFAULT_INDEX_URL = "https://s3.amazonaws.com/Minecraft.Download/indexes/";

    private final String id;
    private final String version;
    private final Integer priority;
    private final String minecraftArguments;
    private final Arguments arguments;
    private final String mainClass;
    private final String inheritsFrom;
    private final String jar;
    private final AssetIndexInfo assetIndex;
    private final String assets;
    private final Integer complianceLevel;
    private final GameJavaVersion javaVersion;
    private final List<Library> libraries;
    private final List<CompatibilityRule> compatibilityRules;
    private final JsonMap<DownloadType, DownloadInfo> downloads;
    private final JsonMap<DownloadType, LoggingInfo> logging;
    private final ReleaseType type;
    private final Date time;
    private final Date releaseTime;
    private final Integer minimumLauncherVersion;
    private final Boolean root;
    private final Boolean hidden;
    private final List<Version> patches;

    private transient final boolean resolved;

    public Version(String id) {
        this(false, id, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, false, true, null);
    }

    /**
     * Constructor for patch
     *
     * @param id        patch id
     * @param version   patch version
     * @param priority  patch priority
     * @param arguments patch additional arguments
     * @param mainClass main class to override
     * @param libraries additional libraries
     */
    public Version(String id, String version, int priority, Arguments arguments, String mainClass, List<Library> libraries) {
        this(false, id, version, priority, null, arguments, mainClass, null, null, null, null, null, null, libraries, null, null, null, null, null, null, null, null, null, null);
    }

    public Version(boolean resolved, String id, String version, Integer priority, String minecraftArguments, Arguments arguments, String mainClass, String inheritsFrom, String jar, AssetIndexInfo assetIndex, String assets, Integer complianceLevel, GameJavaVersion javaVersion, List<Library> libraries, List<CompatibilityRule> compatibilityRules, Map<DownloadType, DownloadInfo> downloads, Map<DownloadType, LoggingInfo> logging, ReleaseType type, Date time, Date releaseTime, Integer minimumLauncherVersion, Boolean hidden, Boolean root, List<Version> patches) {
        this.resolved = resolved;
        this.id = id;
        this.version = version;
        this.priority = priority;
        this.minecraftArguments = minecraftArguments;
        this.arguments = arguments;
        this.mainClass = mainClass;
        this.inheritsFrom = inheritsFrom;
        this.jar = jar;
        this.assetIndex = assetIndex;
        this.assets = assets;
        this.complianceLevel = complianceLevel;
        this.javaVersion = javaVersion;
        this.libraries = Lang.copyList(libraries);
        this.compatibilityRules = Lang.copyList(compatibilityRules);
        this.downloads = downloads == null ? null : new JsonMap<>(downloads);
        this.logging = logging == null ? null : new JsonMap<>(logging);
        this.type = type;
        this.time = time == null ? null : (Date) time.clone();
        this.releaseTime = releaseTime == null ? null : (Date) releaseTime.clone();
        this.minimumLauncherVersion = minimumLauncherVersion;
        this.hidden = hidden;
        this.root = root;
        this.patches = Lang.copyList(patches);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Optional<String> getMinecraftArguments() {
        return Optional.ofNullable(minecraftArguments);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Optional<Arguments> getArguments() {
        return Optional.ofNullable(arguments);
    }

    public String getMainClass() {
        return mainClass;
    }

    public Date getTime() {
        return time;
    }

    public String getId() {
        return id;
    }

    /**
     * Version of the patch.
     * Exists only when this version object represents a patch.
     * Example: 0.5.0.33 for fabric-loader, 28.0.46 for minecraft-forge.
     */
    @Nullable
    public String getVersion() {
        return version;
    }

    public int getPriority() {
        return priority == null ? Integer.MIN_VALUE : priority;
    }

    public ReleaseType getType() {
        return type == null ? ReleaseType.UNKNOWN : type;
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    public String getJar() {
        return jar;
    }

    public String getInheritsFrom() {
        return inheritsFrom;
    }

    public int getMinimumLauncherVersion() {
        return minimumLauncherVersion == null ? 0 : minimumLauncherVersion;
    }

    public Integer getComplianceLevel() {
        return complianceLevel;
    }

    public GameJavaVersion getJavaVersion() {
        return javaVersion;
    }

    public boolean isHidden() {
        return hidden == null ? false : hidden;
    }

    public boolean isRoot() {
        return root == null ? false : root;
    }

    public boolean isResolved() {
        return resolved;
    }

    public boolean isResolvedPreservingPatches() {
        return inheritsFrom == null && !resolved;
    }

    public List<Version> getPatches() {
        return patches == null ? Collections.emptyList() : patches;
    }

    public Map<DownloadType, LoggingInfo> getLogging() {
        return logging == null ? Collections.emptyMap() : Collections.unmodifiableMap(logging);
    }

    public List<Library> getLibraries() {
        return libraries == null ? Collections.emptyList() : Collections.unmodifiableList(libraries);
    }

    public List<CompatibilityRule> getCompatibilityRules() {
        return compatibilityRules == null ? Collections.emptyList() : Collections.unmodifiableList(compatibilityRules);
    }

    public Map<DownloadType, DownloadInfo> getDownloads() {
        return downloads == null ? Collections.emptyMap() : Collections.unmodifiableMap(downloads);
    }

    public DownloadInfo getDownloadInfo() {
        DownloadInfo client = downloads == null ? null : downloads.get(DownloadType.CLIENT);
        String jarName = jar == null ? id : jar;
        if (client == null)
            return new DownloadInfo(String.format("%s%s/%s.jar", DEFAULT_VERSION_DOWNLOAD_URL, jarName, jarName));
        else
            return client;
    }

    public AssetIndexInfo getAssetIndex() {
        String assetsId = assets == null ? "legacy" : assets;
        return assetIndex == null ? new AssetIndexInfo(assetsId, DEFAULT_INDEX_URL + assetsId + ".json") : assetIndex;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean appliesToCurrentEnvironment() {
        return CompatibilityRule.appliesToCurrentEnvironment(compatibilityRules);
    }

    /**
     * Resolve given version.
     * Resolving version will list all patches within this version and its parents,
     * which is for analysis.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Version resolve(VersionProvider provider) throws VersionNotFoundException {
        if (isResolved()) return this;
        return resolve(provider, new HashSet<>()).markAsResolved();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected Version merge(Version parent, boolean isPatch) {
        return new Version(
                true,
                id,
                null,
                null,
                minecraftArguments == null ? parent.minecraftArguments : minecraftArguments,
                Arguments.merge(parent.arguments, arguments),
                mainClass == null ? parent.mainClass : mainClass,
                null, // inheritsFrom
                jar == null ? parent.jar : jar,
                assetIndex == null ? parent.assetIndex : assetIndex,
                assets == null ? parent.assets : assets,
                complianceLevel,
                javaVersion == null ? parent.javaVersion : javaVersion,
                Lang.merge(this.libraries, parent.libraries),
                Lang.merge(parent.compatibilityRules, this.compatibilityRules),
                downloads == null ? parent.downloads : downloads,
                logging == null ? parent.logging : logging,
                type == null ? parent.type : type,
                time == null ? parent.time : time,
                releaseTime == null ? parent.releaseTime : releaseTime,
                Lang.merge(minimumLauncherVersion, parent.minimumLauncherVersion, (BinaryOperator<Integer>) Math::max),
                hidden,
                true,
                isPatch ? parent.patches : Lang.merge(Lang.merge(parent.patches, Collections.singleton(toPatch())), patches));
    }

    protected Version resolve(VersionProvider provider, Set<String> resolvedSoFar) throws VersionNotFoundException {
        Version thisVersion;

        if (inheritsFrom == null) {
            if (isRoot()) {
                thisVersion = new Version(id).setPatches(patches);
            } else {
                thisVersion = this;
            }
            thisVersion = this.jar == null ? thisVersion.setJar(id) : thisVersion.setJar(this.jar);
        } else {
            // To maximize the compatibility.
            if (!resolvedSoFar.add(id)) {
                Logging.LOG.log(Level.WARNING, "Found circular dependency versions: " + resolvedSoFar);
                thisVersion = this.jar == null ? this.setJar(id) : this;
            } else {
                // It is supposed to auto install an version in getVersion.
                thisVersion = merge(provider.getVersion(inheritsFrom).resolve(provider, resolvedSoFar), false);
            }
        }

        if (patches != null && !patches.isEmpty()) {
            // Assume patches themselves do not have patches recursively.
            List<Version> sortedPatches = patches.stream()
                    .sorted(Comparator.comparing(Version::getPriority))
                    .collect(Collectors.toList());
            for (Version patch : sortedPatches) {
                thisVersion = patch.setJar(null).merge(thisVersion, true);
            }
        }

        return thisVersion.setId(id);
    }

    private Version toPatch() {
        return this.clearPatches().setHidden(true).setId("resolved." + getId());
    }

    /**
     * Resolve the version preserving all dependencies and patches.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Version resolvePreservingPatches(VersionProvider provider) throws VersionNotFoundException {
        return resolvePreservingPatches(provider, new HashSet<>());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected Version mergePreservingPatches(Version parent) {
        return parent.addPatch(toPatch()).addPatches(patches);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected Version resolvePreservingPatches(VersionProvider provider, Set<String> resolvedSoFar) throws VersionNotFoundException {
        Version thisVersion = isRoot() ? this : new Version(id).addPatch(toPatch()).addPatches(getPatches());

        if (inheritsFrom == null) {
            // keep thisVersion
        } else {
            // To maximize the compatibility.
            if (!resolvedSoFar.add(id)) {
                Logging.LOG.log(Level.WARNING, "Found circular dependency versions: " + resolvedSoFar);
                // keep thisVersion
            } else {
                // It is supposed to auto install an version in getVersion.
                thisVersion = mergePreservingPatches(provider.getVersion(inheritsFrom).resolvePreservingPatches(provider, resolvedSoFar));
            }
        }

        return thisVersion.setId(id).setJar(resolve(provider).getJar());
    }

    private Version markAsResolved() {
        return new Version(true, id, version, priority, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, complianceLevel, javaVersion, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden, root, patches);
    }

    public Version markAsUnresolved() {
        return new Version(false, id, version, priority, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, complianceLevel, javaVersion, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden, root, patches);
    }

    private Version setHidden(Boolean hidden) {
        return new Version(true, id, version, priority, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, complianceLevel, javaVersion, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden, root, patches);
    }

    public Version setId(String id) {
        return new Version(resolved, id, version, priority, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, complianceLevel, javaVersion, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden, root, patches);
    }

    public Version setVersion(String version) {
        return new Version(resolved, id, version, priority, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, complianceLevel, javaVersion, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden, root, patches);
    }

    public Version setPriority(Integer priority) {
        return new Version(resolved, id, version, priority, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, complianceLevel, javaVersion, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden, root, patches);
    }

    public Version setMinecraftArguments(String minecraftArguments) {
        return new Version(resolved, id, version, priority, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, complianceLevel, javaVersion, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden, root, patches);
    }

    public Version setArguments(Arguments arguments) {
        return new Version(resolved, id, version, priority, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, complianceLevel, javaVersion, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden, root, patches);
    }

    public Version setMainClass(String mainClass) {
        return new Version(resolved, id, version, priority, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, complianceLevel, javaVersion, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden, root, patches);
    }

    public Version setInheritsFrom(String inheritsFrom) {
        return new Version(resolved, id, version, priority, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, complianceLevel, javaVersion, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden, root, patches);
    }

    public Version setJar(String jar) {
        return new Version(resolved, id, version, priority, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, complianceLevel, javaVersion, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden, root, patches);
    }

    public Version setLibraries(List<Library> libraries) {
        return new Version(resolved, id, version, priority, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, complianceLevel, javaVersion, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden, root, patches);
    }

    public Version setLogging(Map<DownloadType, LoggingInfo> logging) {
        return new Version(resolved, id, version, priority, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, complianceLevel, javaVersion, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden, root, patches);
    }

    public Version setPatches(List<Version> patches) {
        return new Version(resolved, id, version, priority, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, complianceLevel, javaVersion, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden, root, patches);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Version addPatch(Version... additional) {
        return addPatches(Arrays.asList(additional));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Version addPatches(@Nullable List<Version> additional) {
        Set<String> patchIds = additional == null ? Collections.emptySet() : additional.stream().map(Version::getId).collect(Collectors.toSet());
        List<Version> patches = Lang.merge(this.patches == null ? null : this.patches.stream().filter(patch -> !patchIds.contains(patch.getId())).collect(Collectors.toList()), additional);
        return new Version(resolved, id, version, priority, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, complianceLevel, javaVersion, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden, root, patches);
    }

    public Version clearPatches() {
        return new Version(resolved, id, version, priority, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, complianceLevel, javaVersion, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden, root, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Version removePatchById(String patchId) {
        return new Version(resolved, id, version, priority, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, complianceLevel, javaVersion, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden, root,
                patches == null ? null : patches.stream().filter(patch -> !patchId.equals(patch.getId())).collect(Collectors.toList()));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean hasPatch(String patchId) {
        return patches != null && patches.stream().anyMatch(patch -> patchId.equals(patch.getId()));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Version && Objects.equals(id, ((Version) obj).id);
    }

    @Override
    public int compareTo(Version o) {
        return id.compareTo(o.id);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).toString();
    }

    @Override
    public void validate() throws JsonParseException {
        if (StringUtils.isBlank(id))
            throw new JsonParseException("Version ID cannot be blank");
        if (downloads != null)
            for (Map.Entry<DownloadType, DownloadInfo> entry : downloads.entrySet()) {
                if (!(entry.getKey() instanceof DownloadType))
                    throw new JsonParseException("Version downloads key must be DownloadType");
                if (!(entry.getValue() instanceof DownloadInfo))
                    throw new JsonParseException("Version downloads value must be DownloadInfo");
            }
        if (logging != null)
            for (Map.Entry<DownloadType, LoggingInfo> entry : logging.entrySet()) {
                if (!(entry.getKey() instanceof DownloadType))
                    throw new JsonParseException("Version logging key must be DownloadType");
                if (!(entry.getValue() instanceof LoggingInfo))
                    throw new JsonParseException("Version logging value must be LoggingInfo");
            }
    }

}

