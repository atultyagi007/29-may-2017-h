/**************************************************************************
* Copyright (c) 2014 Dell Inc. All rights reserved. *
* *
* DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may *
* only be supplied under the terms of a license agreement or *
* nondisclosure agreement with Dell Inc. and may not be copied or *
* disclosed except in accordance with the terms of such agreement. *
**************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.perfmonitoring;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import com.google.common.base.Objects;

public class PerformanceMetricSummary {
List<String> all_time_peak;
double average;
long device_first_seen;
long device_last_seen;
List<String> max;
List<String> min;
public double getAverage() {
return average;
}
public void setAverage(double average) {
this.average = average;
}
public List<String> getAll_time_peak() {
    return all_time_peak;
}
public void setAll_time_peak(List<String> all_time_peak) {
    this.all_time_peak = all_time_peak;
}
public long getDevice_first_seen() {
    return device_first_seen;
}
public void setDevice_first_seen(long device_first_seen) {
    this.device_first_seen = device_first_seen;
}
public long getDevice_last_seen() {
    return device_last_seen;
}
public void setDevice_last_seen(long device_last_seen) {
    this.device_last_seen = device_last_seen;
}
public List<String> getMax() {
return max;
}
public void setMax(List<String> max) {
this.max = max;
}
public List<String> getMin() {
return min;
}
public void setMin(List<String> min) {
this.min = min;
}
@Override
public String toString() {
return Objects.toStringHelper(this)
.add("average", average)
.add("all_time_peak", all_time_peak)
.add("device_first_seen", device_first_seen)
.add("device_last_seen", device_last_seen)
.add("max", max)
.add("min", min)
.toString();
}
}
