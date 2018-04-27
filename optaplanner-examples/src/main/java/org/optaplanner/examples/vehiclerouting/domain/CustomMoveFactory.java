package org.optaplanner.examples.vehiclerouting.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class CustomMoveFactory implements MoveListFactory<VehicleRoutingSolution> {

    public static class CustomMove extends AbstractMove<VehicleRoutingSolution> {

        public enum Type {
            ADD, REMOVE;
        }

        private Type type;
        private Customer customer;
        private Standstill pivot;

        public CustomMove(Type type, Customer customer, Standstill pivot) {
            this.type = type;
            this.customer = customer;
            this.pivot = pivot;
        }

        @Override
        public boolean isMoveDoable(ScoreDirector<VehicleRoutingSolution> director) {
            return true;
        }

        @Override
        protected void doMoveOnGenuineVariables(ScoreDirector<VehicleRoutingSolution> director) {

            if (type == Type.ADD) {
                System.out.println("DEBUG ADD: " + pivot + " -> " + customer + " -> " + pivot.getNextCustomer());
                System.out.flush();

                Customer afterPivot = pivot.getNextCustomer();

                director.beforeEntityAdded(customer);
                director.getWorkingSolution().getCustomerList().add(customer);
                director.afterEntityAdded(customer);

                director.beforeVariableChanged(customer, "previousStandstill");
                customer.setPreviousStandstill(pivot);
                director.afterVariableChanged(customer, "previousStandstill");

                if (afterPivot != null) {
                    director.beforeVariableChanged(afterPivot, "previousStandstill");
                    afterPivot.setPreviousStandstill(customer);
                    director.afterVariableChanged(afterPivot, "previousStandstill");
                }
            }
            else if (type == Type.REMOVE) {
                System.out.println("DEBUG REMOVE: " + customer.getPreviousStandstill() + " -> " +
                        customer + " -> " + customer.getNextCustomer());
                System.out.flush();

                Customer afterCustomer = customer.getNextCustomer();

                if (afterCustomer != null) {
                    director.beforeVariableChanged(afterCustomer, "previousStandstill");
                    afterCustomer.setPreviousStandstill(pivot);
                    director.afterVariableChanged(afterCustomer, "previousStandstill");
                }

                director.beforeEntityRemoved(customer);
                director.getWorkingSolution().getCustomerList().remove(customer);
                director.afterEntityRemoved(customer);
            }
            else
                throw new IllegalArgumentException("invalid type: " + type);
        }

        @Override
        protected CustomMove createUndoMove(ScoreDirector<VehicleRoutingSolution> director) {
            if (type == Type.ADD)
                return new CustomMove(Type.REMOVE, customer, pivot);

            if (type == Type.REMOVE)
                return new CustomMove(Type.ADD, customer, pivot);

            throw new IllegalArgumentException("invalid type: " + type);
        }

        @Override
        public Collection<? extends Object> getPlanningEntities() {
            return Collections.singletonList(null);
        }

        @Override
        public Collection<? extends Object> getPlanningValues() {
            return Collections.singletonList(null);
        }
    }

    @Override
    public List<CustomMove> createMoveList(VehicleRoutingSolution solution) {
        List<CustomMove> moves = new ArrayList<>();

        for (Vehicle vehicle : solution.getVehicleList()) {

            for (Customer customer = vehicle.getNextCustomer(); customer != null; customer = customer.getNextCustomer()) {

                Customer customerCopy = new Customer();

                customerCopy.setId(customer.getId() + 1000000000);
                customerCopy.setLocation(customer.getLocation());
                customerCopy.setDemand(customer.getDemand());

                moves.add(new CustomMove(CustomMove.Type.ADD, customerCopy, customer));
            }
        }

        return moves;
    }
}
