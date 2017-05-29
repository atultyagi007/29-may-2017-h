package com.dell.asm.asmcore.asmmanager.client.perfmonitoring;

import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;

public class PerformanceMetric {

    String target;
    List<List<String>> datapoints;

    public List<List<String>> getDatapoints() {
        return datapoints;
    }

    public void setDatapoints(List<List<String>> datapoints) {
        this.datapoints = datapoints;
    }

    Map<Object, Object> thresholds;
    PerformanceMetricSummary summary;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Map<Object, Object> getThresholds() {
        return thresholds;
    }

    public void setThresholds(Map<Object, Object> thresholds) {
        this.thresholds = thresholds;
    }

    public PerformanceMetricSummary getSummary() {
        return summary;
    }

    public void setSummary(PerformanceMetricSummary summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("target", target)
                .toString();
    }

}
