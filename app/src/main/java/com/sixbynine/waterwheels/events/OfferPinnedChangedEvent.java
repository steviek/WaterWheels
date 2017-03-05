package com.sixbynine.waterwheels.events;

import com.sixbynine.waterwheels.model.Offer;

public final class OfferPinnedChangedEvent {

    private final Offer offer;

    public OfferPinnedChangedEvent(Offer offer) {
        this.offer = offer;
    }

    public Offer getOffer() {
        return offer;
    }
}
