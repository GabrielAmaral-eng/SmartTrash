package com.smarttrash.service;

import com.smarttrash.model.BinStatus;
import org.springframework.stereotype.Component;

@Component
public class BinStatusClassifier {

    public BinStatus classify(double fillLevelPercent) {
        if (fillLevelPercent < 0 || fillLevelPercent > 100) {
            throw new IllegalArgumentException("fillLevelPercent must be between 0 and 100");
        }
        if (fillLevelPercent < 50) {
            return BinStatus.EMPTY;
        }
        if (fillLevelPercent < 80) {
            return BinStatus.ATTENTION;
        }
        return BinStatus.FULL;
    }
}
