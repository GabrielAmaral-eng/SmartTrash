package com.smarttrash.service;

import com.smarttrash.model.BinStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BinStatusClassifierTest {

    private final BinStatusClassifier classifier = new BinStatusClassifier();

    @Test
    void classifiesEmptyFromZeroToFortyNinePercent() {
        assertThat(classifier.classify(0)).isEqualTo(BinStatus.EMPTY);
        assertThat(classifier.classify(49)).isEqualTo(BinStatus.EMPTY);
    }

    @Test
    void classifiesAttentionFromFiftyToSeventyNinePercent() {
        assertThat(classifier.classify(50)).isEqualTo(BinStatus.ATTENTION);
        assertThat(classifier.classify(79)).isEqualTo(BinStatus.ATTENTION);
    }

    @Test
    void classifiesFullFromEightyToOneHundredPercent() {
        assertThat(classifier.classify(80)).isEqualTo(BinStatus.FULL);
        assertThat(classifier.classify(100)).isEqualTo(BinStatus.FULL);
    }

    @Test
    void rejectsInvalidPercentages() {
        assertThatThrownBy(() -> classifier.classify(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> classifier.classify(101)).isInstanceOf(IllegalArgumentException.class);
    }
}
