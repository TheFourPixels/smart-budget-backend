package com.teamfourpixels.service.classification;

import com.teamfourpixels.entity.Transaction;
import java.util.Optional;


public interface ClassificationStrategy {

    Optional<Long> classify(Transaction transaction);

    int getPriority();
}