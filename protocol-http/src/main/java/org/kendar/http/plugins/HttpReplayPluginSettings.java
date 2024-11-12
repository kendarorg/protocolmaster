package org.kendar.http.plugins;

import org.kendar.filters.settings.BasicReplayPluginSettings;

import java.util.ArrayList;
import java.util.List;

public class HttpReplayPluginSettings extends BasicReplayPluginSettings {
    private boolean replay;
    private String replayId;
    private boolean respectCallDuration;
    private boolean blockExternal;
    private List<String> matchSites = new ArrayList<>();

    public List<String> getMatchSites() {
        return matchSites;
    }

    public void setMatchSites(List<String> matchSites) {
        this.matchSites = matchSites;
    }

    public boolean isReplay() {
        return replay;
    }

    public void setReplay(boolean replay) {
        this.replay = replay;
    }

    public String getReplayId() {
        return replayId;
    }

    public void setReplayId(String replayId) {
        this.replayId = replayId;
    }

    public boolean isRespectCallDuration() {
        return respectCallDuration;
    }

    public void setRespectCallDuration(boolean respectCallDuration) {
        this.respectCallDuration = respectCallDuration;
    }

    public boolean isBlockExternal() {
        return blockExternal;
    }

    public void setBlockExternal(boolean blockExternal) {
        this.blockExternal = blockExternal;
    }
}
