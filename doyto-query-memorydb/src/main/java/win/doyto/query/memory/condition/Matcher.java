package win.doyto.query.memory.condition;

import java.util.Collection;

/**
 * Matcher
 *
 * @author f0rb on 2021-12-10
 */
interface Matcher {

    /**
     * Filter entity objects by comparing qv and ev
     *
     * @param efv The field value of the entity object
     * @param qfv The field value of the query object
     * @return true The field value matches the filter criteria
     */
    boolean doMatch(Object efv, Object qfv);

    default boolean match(Object efv, Object qfv) {
        return isComparable(efv, qfv) && doMatch(efv, qfv);
    }

    default boolean isComparable(Object efv, Object qfv) {
        return qfv instanceof Collection || (qfv instanceof Comparable && efv instanceof Comparable);
    }
}
