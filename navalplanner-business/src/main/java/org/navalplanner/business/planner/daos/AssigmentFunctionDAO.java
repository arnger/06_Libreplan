package org.navalplanner.business.planner.daos;

import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.planner.entities.AssigmentFunction;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * DAO for {@AssigmentFunction}
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class AssigmentFunctionDAO extends
        GenericDAOHibernate<AssigmentFunction, Long> implements
        IAssigmentFunctionDAO {

}
