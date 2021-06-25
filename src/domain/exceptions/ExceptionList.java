package domain.exceptions;

import java.util.ArrayList;
import java.util.List;

public class ExceptionList extends Exception{
    private List<EnrollmentRulesViolationException> errors;
    public ExceptionList(List<EnrollmentRulesViolationException> errorList) {
        this.errors = errorList;
    }

    public List<EnrollmentRulesViolationException> getErrors() {
        return errors;
    }
}
