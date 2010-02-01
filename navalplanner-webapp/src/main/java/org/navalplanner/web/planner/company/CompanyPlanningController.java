/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.web.planner.company;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.web.common.components.bandboxsearch.BandboxMultipleSearch;
import org.navalplanner.web.common.components.finders.FilterPair;
import org.navalplanner.web.orders.OrderPredicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.extensions.ICommandOnTask;
import org.zkoss.ganttz.resourceload.ScriptsRequiredByResourceLoadPanel;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.OnZKDesktopRegistry;
import org.zkoss.ganttz.util.script.IScriptsRegister;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Vbox;

/**
 * Controller for company planning view. Representation of company orders in the
 * planner.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CompanyPlanningController implements Composer{

    @Autowired
    private ICompanyPlanningModel model;

    private List<ICommandOnTask<TaskElement>> additional = new ArrayList<ICommandOnTask<TaskElement>>();

    private Planner planner;

    private Vbox orderFilter;
    private Vbox filter;
    private Datebox filterStartDate;
    private Datebox filterFinishDate;
    private BandboxMultipleSearch bdFilters;
    private Checkbox checkIncludeOrderElements;

    private ICommandOnTask<TaskElement> doubleClickCommand;

    private Map<String, String[]> parameters;

    public CompanyPlanningController() {
        getScriptsRegister().register(ScriptsRequiredByResourceLoadPanel.class);
    }

    private IScriptsRegister getScriptsRegister() {
        return OnZKDesktopRegistry.getLocatorFor(IScriptsRegister.class)
                .retrieve();
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) {
        planner = (Planner) comp;
        String zoomLevelParameter = null;
        if ((parameters != null) && (parameters.get("zoom") != null)
                && !(parameters.isEmpty())) {
            zoomLevelParameter = parameters.get("zoom")[0];
        }
        planner
                .setInitialZoomLevel(ZoomLevel
                        .getFromString(zoomLevelParameter));
        planner.setAreContainersExpandedByDefault(Planner
                .guessContainersExpandedByDefault(parameters));

        orderFilter = (Vbox) planner.getFellow("orderFilter");
        // Configuration of the order filter
        Component filterComponent = Executions.createComponents(
                "/orders/_orderFilter.zul", orderFilter,
                new HashMap<String, String>());
        filterComponent.setVariable("controller", this, true);
        filterStartDate = (Datebox) filterComponent
                .getFellow("filterStartDate");
        filterFinishDate = (Datebox) filterComponent
                .getFellow("filterFinishDate");
        bdFilters = (BandboxMultipleSearch) filterComponent
                .getFellow("bdFilters");
        checkIncludeOrderElements = (Checkbox) filterComponent
                .getFellow("checkIncludeOrderElements");
        filterComponent.setVisible(true);

    }

    public void setConfigurationForPlanner() {
        // Added predicate
        model
                .setConfigurationToPlanner(planner, additional,
                doubleClickCommand, createPredicate());
    }

    public void setAdditional(List<ICommandOnTask<TaskElement>> additional) {
        Validate.notNull(additional);
        Validate.noNullElements(additional);
        this.additional = additional;
    }

    public void setDoubleClickCommand(
            ICommandOnTask<TaskElement> doubleClickCommand) {
        this.doubleClickCommand = doubleClickCommand;
    }

    public void setURLParameters(Map<String, String[]> parameters) {
        this.parameters = parameters;
    }

    public void onApplyFilter() {
        filterByPredicate(createPredicate());
    }

    private OrderPredicate createPredicate() {
        List<FilterPair> listFilters = (List<FilterPair>) bdFilters
                .getSelectedElements();
        Date startDate = filterStartDate.getValue();
        Date finishDate = filterFinishDate.getValue();
        Boolean includeOrderElements = checkIncludeOrderElements.isChecked();

        if (listFilters.isEmpty() && startDate == null && finishDate == null) {
            return null;
        }
        return new OrderPredicate(listFilters, startDate, finishDate,
                includeOrderElements);
    }

    private void filterByPredicate(OrderPredicate predicate) {
        // Recalculate predicate
        model.setConfigurationToPlanner(planner, additional,
                doubleClickCommand, predicate);
        planner.invalidate();
    }

}
