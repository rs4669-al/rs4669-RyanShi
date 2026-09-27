package com.taxapi.model;

/**
 * Request body for updating an item's base price.
 */
public final class UpdatePriceRequest {

    /** The new base price. */
    private double basePrice;

    /** Default constructor. */
    public UpdatePriceRequest() {
    }

    /**
     * Gets the new base price.
     *
     * @return the base price
     */
    public double getBasePrice() {
        return basePrice;
    }

    /**
     * Sets the new base price.
     *
     * @param basePrice the base price
     */
    public void setBasePrice(final double basePrice) {
        this.basePrice = basePrice;
    }
}

