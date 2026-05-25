package com.aicloud.gateway.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
@ConfigurationProperties(prefix = "aicloud.gateway")
/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class RoutePermissionProperties {

    private List<Rule> permissionRules = new ArrayList<>();

    public List<Rule> getPermissionRules() {
        return permissionRules;
    }

    public void setPermissionRules(List<Rule> permissionRules) {
        this.permissionRules = permissionRules;
    }

    public static class Rule {
        private String name;
        private String method;
        private String path;
        private String permission;
        private List<String> terminals = new ArrayList<>();
        private List<String> userTypes = new ArrayList<>();
        private String effect = "ALLOW";
        private Integer priority = 0;
        private Boolean enabled = true;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getPermission() {
            return permission;
        }

        public void setPermission(String permission) {
            this.permission = permission;
        }

        public List<String> getTerminals() {
            return terminals;
        }

        public void setTerminals(List<String> terminals) {
            this.terminals = terminals;
        }

        public List<String> getUserTypes() {
            return userTypes;
        }

        public void setUserTypes(List<String> userTypes) {
            this.userTypes = userTypes;
        }

        public String getEffect() {
            return effect;
        }

        public void setEffect(String effect) {
            this.effect = effect;
        }

        public Integer getPriority() {
            return priority;
        }

        public void setPriority(Integer priority) {
            this.priority = priority;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
    }
}
