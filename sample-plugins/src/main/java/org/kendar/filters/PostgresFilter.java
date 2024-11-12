package org.kendar.filters;

import org.kendar.proxy.FilterContext;
import org.kendar.settings.GlobalSettings;
import org.kendar.settings.PluginSettings;
import org.kendar.settings.ProtocolSettings;
import org.kendar.sql.jdbc.SelectResult;
import org.kendar.sql.jdbc.proxy.JdbcCall;
import org.pf4j.Extension;

import java.util.List;

@Extension
public class PostgresFilter extends ProtocolPluginDescriptor<JdbcCall, SelectResult> implements AlwaysActivePlugin {
    @Override
    public boolean handle(FilterContext filterContext, ProtocolPhase phase, JdbcCall in, SelectResult out) {
        return false;
    }

    /**
     * Only PRE_CALL and POST_CALL for things different from http
     *
     * @return
     */
    @Override
    public List<ProtocolPhase> getPhases() {
        return List.of();
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public String getProtocol() {
        return "postgres";
    }

    @Override
    public PluginDescriptor initialize(GlobalSettings global, ProtocolSettings protocol) {
        return null;
    }


    @Override
    public void terminate() {

    }

    @Override
    public Class<?> getSettingClass() {
        return PostgresFilterSettings.class;
    }

    @Override
    public void setSettings(PluginSettings plugin) {
        super.setSettings(plugin);

    }
}