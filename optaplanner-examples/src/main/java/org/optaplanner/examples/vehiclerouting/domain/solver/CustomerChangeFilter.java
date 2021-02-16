package org.optaplanner.examples.vehiclerouting.domain.solver;

import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.examples.vehiclerouting.domain.Customer;
import org.optaplanner.examples.vehiclerouting.domain.Vehicle;
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution;

public class CustomerChangeFilter implements SelectionFilter<VehicleRoutingSolution, Customer> {
    @Override
    public boolean accept(ScoreDirector<VehicleRoutingSolution> director, Customer customer) {
        boolean result = !(customer.getPreviousStandstill() instanceof Vehicle);
        System.out.println("CustomerChangeFilter.accept(director, " + customer + ") -> " + result);
        return result;
    }
}
