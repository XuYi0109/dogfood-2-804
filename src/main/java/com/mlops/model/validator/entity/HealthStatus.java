package com.mlops.model.validator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

public class HealthStatus {

    @JsonProperty("status")
    private String status;

    @JsonProperty("service")
    private String service;

    @JsonProperty("version")
    private String version;

    @JsonProperty("timestamp")
    private Instant timestamp;

    @JsonProperty("uptime_seconds")
    private long uptimeSeconds;

    @JsonProperty("checks")
    private Map<String, Object> checks;

    public HealthStatus() {
        this.timestamp = Instant.now();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public long getUptimeSeconds() {
        return uptimeSeconds;
    }

    public void setUptimeSeconds(long uptimeSeconds) {
        this.uptimeSeconds = uptimeSeconds;
    }

    public Map<String, Object> getChecks() {
        return checks;
    }

    public void setChecks(Map<String, Object> checks) {
        this.checks = checks;
    }

    public static HealthStatus up(String service, String version, long uptimeSeconds, Map<String, Object> checks) {
        HealthStatus status = new HealthStatus();
        status.setStatus("UP");
        status.setService(service);
        status.setVersion(version);
        status.setUptimeSeconds(uptimeSeconds);
        status.setChecks(checks);
        return status;
    }

    public static HealthStatus down(String service, String version, Map<String, Object> checks) {
        HealthStatus status = new HealthStatus();
        status.setStatus("DOWN");
        status.setService(service);
        status.setVersion(version);
        status.setChecks(checks);
        return status;
    }
}
