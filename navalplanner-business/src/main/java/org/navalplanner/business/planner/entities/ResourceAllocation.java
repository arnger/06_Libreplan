/**
 *
 */
package org.navalplanner.business.planner.entities;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.NotNull;
import org.navalplanner.business.common.BaseEntity;

/**
 * Resources are allocated to planner tasks.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public abstract class ResourceAllocation extends BaseEntity {

    @NotNull
    private Task task;

    private Set<AssigmentFunction> assigmentFunction = new HashSet<AssigmentFunction>();

    /**
     * Allocation percentage of the resource.
     *
     * It's one based, instead of one hundred based.
     */
    private BigDecimal percentage = new BigDecimal(0).setScale(2);

    /**
     * Constructor for hibernate. Do not use!
     */
    public ResourceAllocation() {

    }

    public ResourceAllocation(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    /**
     * @param proportion
     *            It's one based, instead of one hundred based.
     */
    public void setPercentage(BigDecimal proportion) {
        this.percentage = proportion;
    }

    public Set<AssigmentFunction> getAssigmentFunction() {
        return assigmentFunction;
    }

    public void setAssigmentFunction(Set<AssigmentFunction> assigmentFunction) {
        this.assigmentFunction = assigmentFunction;
    }
}
