/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2010-2011 Wireless Galicia, S.L.
 * Copyright (C) 2011-2012 Igalia, S.L.

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

package org.libreplan.web.orders;

import static org.libreplan.web.I18nHelper._;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.templates.entities.BudgetTemplate;
import org.libreplan.web.common.ConstraintChecker;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.libreplan.web.planner.consolidations.AdvanceConsolidationController;
import org.libreplan.web.planner.tabs.MultipleTabsPlannerController;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Controller for the creation of an {@link order} with its principal
 * properties.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgailicia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */

public class ProjectDetailsController extends GenericForwardComposer {

    private static final Log LOG = LogFactory
            .getLog(AdvanceConsolidationController.class);

    private OrderCRUDController orderController;

    private Grid gridProjectDetails;

    private BaseCalendar defaultCalendar;

    private boolean isCodeAutogeneratedInit;

    private MultipleTabsPlannerController tabs;

    private Window window;

    private Datebox initDate;

    private BandboxSearch bdProjectTemplate;

    private Textbox txtName;

    private Textbox txtDescription;

    private Datebox deadline;

    private Checkbox generateCode;

    @Autowired
    private IOrderDAO orderDAO;

    private BudgetTemplate template;

    public ProjectDetailsController() {
        Window window = (Window) Executions.createComponents(
                "/orders/_projectDetails.zul", null,
                new HashMap<String, String>());
        try {
            doAfterCompose(window);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        window = (Window) comp;
        window.setVariable("projectController", this, true);
    }

    public void showWindow(OrderCRUDController orderController,
            MultipleTabsPlannerController tabs) {
        this.tabs = tabs;
        this.orderController = orderController;
        this.defaultCalendar = orderController.getOrder().getCalendar();
        this.isCodeAutogeneratedInit = orderController.getOrder()
                .isCodeAutogenerated();
        try {
            Util.reloadBindings(window);
            Util.createBindingsFor(gridProjectDetails);
            Util.reloadBindings(gridProjectDetails);
            window.doModal();
        } catch (SuspendNotAllowedException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void cancel() {
        clearProperties();
        close();
    }

    public void accept() {
        if (validate()) {
            if (tabs != null) {
                tabs.goToOrdersList();
            }
            if (bdProjectTemplate.getSelectedElement() != null) {
                BudgetTemplate template = (BudgetTemplate) bdProjectTemplate
                        .getSelectedElement();
                orderController.createFromTemplate(template);
            }
            orderController.editNewCreatedOrder(window);
        }
    }

    private boolean validate() {
        ConstraintChecker.isValid(window);
        if (initDate.getValue() == null) {
            showWrongValue();
            return false;
        }
        if (orderDAO.existsByNameAnotherTransaction(txtName.getValue())) {
            showWrongName();
            return false;
        }
        if (bdProjectTemplate.getSelectedElement() == null) {
            showWrongTemplate();
            return false;
        }
        return true;
    }

    private void showWrongValue() {
        throw new WrongValueException(initDate, _("cannot be empty"));
    }

    private void showWrongName() {
        throw new WrongValueException(txtName,
                _("project name already being used"));
    }

    private void showWrongTemplate() {
        throw new WrongValueException(bdProjectTemplate,
                _("it is neccesary to specify a template"));
    }

    private void close() {
        window.setVisible(false);
    }

    public Order getOrder() {
        return orderController.getOrder();
    }

    public boolean isCodeAutogenerated() {
        return orderController.isCodeAutogenerated();
    }

    public void setCodeAutogenerated(boolean codeAutogenerated) {
        orderController.setCodeAutogeneratedInModel(codeAutogenerated);
        Util.reloadBindings(gridProjectDetails);
    }

    public List<ExternalCompany> getExternalCompaniesAreClient() {
        return orderController.getExternalCompaniesAreClient();
    }

    public List<BaseCalendar> getBaseCalendars() {
        return orderController.getBaseCalendars();
    }

    public ComboitemRenderer getBaseCalendarsComboitemRenderer() {
        return orderController.getBaseCalendarsComboitemRenderer();
    }

    public void setBaseCalendar(BaseCalendar calendar) {
        orderController.setBaseCalendar(calendar);
    }

    private void clearProperties() {
        Order order = orderController.getOrder();
        order.setName(null);
        order.setDescription(null);
        // reset the code autogenerated property
        if (isCodeAutogeneratedInit) {
            order.setCodeAutogenerated(true);

        } else {
            order.setCodeAutogenerated(false);
            order.setCode("");
        }
        order.setCustomer(null);
        order.setDeadline(null);
        order.setInitDate(new Date());
        order.setCalendar(defaultCalendar);
    }

    public Constraint checkConstraintFinishDate() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                Date finishDate = (Date) value;
                if (finishDate == null) {
                    throw new WrongValueException(comp, _("cannot be empty"));
                }
                if ((initDate.getRawValue() != null)
                        && (finishDate.compareTo((Date) initDate.getRawValue()) < 0)) {
                    getOrder().setDeadline(null);
                    throw new WrongValueException(comp,
                            _("must be after start date"));
                }
            }
        };
    }

    public Constraint checkConstraintStartDate() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                Date startDate = (Date) value;
                if (startDate == null) {
                    throw new WrongValueException(comp, _("cannot be empty"));
                }
                if ((deadline.getRawValue() != null)
                        && (startDate.compareTo((Date) deadline.getRawValue()) > 0)) {
                    getOrder().setInitDate(null);
                    throw new WrongValueException(comp,
                            _("must be lower than end date"));
                }
            }
        };
    }

    public Constraint checkConstraintTemplate() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                BandboxSearch template = (BandboxSearch) comp;
                if (template.getSelectedElement() == null) {
                    throw new WrongValueException(comp, _("cannot be empty"));
                }
            }
        };
    }

    public BudgetTemplate getTemplate() {
        return template;
    }

    public void setTemplate(BudgetTemplate template) {
        this.template = template;
        if (template == null) {
            generateCode.setDisabled(false);
            generateCode.setTooltiptext("");
        } else {
            if (!isCodeAutogenerated()) {
                setCodeAutogenerated(true);
            }
            generateCode.setDisabled(true);
            generateCode
                    .setTooltiptext(_("Set Code as autogenerated to create a new project from templates"));
            generateCode.setChecked(true);
        }
    }

}
