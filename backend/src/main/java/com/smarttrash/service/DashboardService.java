package com.smarttrash.service;

import com.smarttrash.dto.DashboardDtos.DashboardHistoryPoint;
import com.smarttrash.dto.DashboardDtos.DashboardHistoryResponse;
import com.smarttrash.dto.DashboardDtos.DashboardRegionsResponse;
import com.smarttrash.dto.DashboardDtos.DashboardSummaryResponse;
import com.smarttrash.dto.DashboardDtos.RegionSummary;
import com.smarttrash.model.BinStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final SensorService sensorService;

    public DashboardService(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    public DashboardSummaryResponse getSummary() {
        var sensors = sensorService.listSensors().sensors();
        var byStatusEnum = new EnumMap<BinStatus, Integer>(BinStatus.class);
        for (var status : BinStatus.values()) {
            byStatusEnum.put(status, 0);
        }
        sensors.forEach(sensor -> byStatusEnum.merge(sensor.status(), 1, Integer::sum));

        Map<String, Integer> byStatus = new LinkedHashMap<>();
        byStatusEnum.forEach((status, count) -> byStatus.put(status.name(), count));
        var average = sensors.stream()
                .mapToDouble(sensor -> sensor.fillLevelPercent())
                .average()
                .orElse(0);
        var alerts = byStatus.get("ATTENTION") + byStatus.get("FULL");

        return new DashboardSummaryResponse(sensors.size(), byStatus, round(average), alerts);
    }

    public DashboardHistoryResponse getHistory() {
        var details = sensorService.listSensors().sensors().stream()
                .map(sensor -> sensorService.getSensor(sensor.id()))
                .toList();

        var points = details.stream()
                .flatMap(sensor -> sensor.history().stream())
                .collect(Collectors.groupingBy(
                        reading -> reading.timestamp(),
                        Collectors.averagingDouble(reading -> reading.fillLevelPercent())
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new DashboardHistoryPoint(entry.getKey(), round(entry.getValue())))
                .toList();

        return new DashboardHistoryResponse(points);
    }

    public DashboardRegionsResponse getRegions() {
        var sensors = sensorService.listSensors().sensors();
        var regions = sensors.stream()
                .collect(Collectors.groupingBy(sensor -> sensor.region()))
                .entrySet().stream()
                .map(entry -> {
                    var regionSensors = entry.getValue();
                    var average = regionSensors.stream()
                            .mapToDouble(sensor -> sensor.fillLevelPercent())
                            .average()
                            .orElse(0);
                    var alerts = (int) regionSensors.stream()
                            .filter(sensor -> sensor.status() != BinStatus.EMPTY)
                            .count();
                    return new RegionSummary(entry.getKey(), regionSensors.size(), alerts, round(average));
                })
                .sorted(Comparator.comparing(RegionSummary::region))
                .toList();

        return new DashboardRegionsResponse(regions);
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
