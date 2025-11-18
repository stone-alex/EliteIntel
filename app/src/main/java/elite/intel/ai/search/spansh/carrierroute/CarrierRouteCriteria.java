package elite.intel.ai.search.spansh.carrierroute;

/**
 * Record for carrier route search criteria.
 */
public record CarrierRouteCriteria(
        String sourceSystem,
        String destinationSystem,
        int capacity,
        int capacityUsed,
        int calculateStartingFuel
) {}
