package domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import domain.exceptions.EnrollmentRulesViolationException;
import domain.exceptions.ExceptionList;

public class EnrollCtrl {
    private List<EnrollmentRulesViolationException> errors = new ArrayList<>();

    public void enroll(Student s, List<CSE> courses) throws ExceptionList {
        checkForAlreadyPassedCourses(courses, s.transcript());
        checkForPrerequisiteRequirements(courses, s.transcript());
        checkForDuplicateEnrollRequest(courses);
        checkForConfilictingExamTimes(courses);
        checkForGPALimit(courses, s.transcript());

        if (errors.size() > 0) {
            throw new ExceptionList(errors);
        }

        for (CSE o : courses)
            s.takeCourse(o.getCourse(), o.getSection());
    }

    private void checkForPrerequisiteRequirements(List<CSE> courses, Map<Term, Map<Course, Double>> transcript) {
        for (CSE o : courses) {
            List<Course> prereqs = o.getCourse().getPrerequisites();
            nextPre:
            for (Course pre : prereqs) {
                for (Map.Entry<Term, Map<Course, Double>> tr : transcript.entrySet()) {
                    if (hasPassed(tr, pre)) {
                        continue nextPre;
                    }
                }
                errors.add(new EnrollmentRulesViolationException(String.format("The student has not passed %s as a prerequisite of %s", pre.getName(), o.getCourse().getName())));
            }
        }
    }

    private void checkForAlreadyPassedCourses(List<CSE> courses, Map<Term, Map<Course, Double>> transcript) {
        for (CSE o : courses) {
            for (Iterator<Map.Entry<Term, Map<Course, Double>>> iterator = transcript.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<Term, Map<Course, Double>> tr = iterator.next();
                if (hasPassed(tr, o.getCourse())) {
                    errors.add(new EnrollmentRulesViolationException(String.format("The student has already passed %s", o.getCourse().getName())));
                }
            }
        }
    }

    private void checkForDuplicateEnrollRequest(List<CSE> courses) {
        for (CSE o : courses) {
            for (CSE o2 : courses) {
                if (o == o2)
                    continue;
                if (o.getCourse().equals(o2.getCourse()))
                    errors.add(new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice", o.getCourse().getName())));
            }
        }
    }

    private void checkForConfilictingExamTimes(List<CSE> courses) {
        for (CSE o : courses) {
            for (CSE o2 : courses) {
                if (o == o2)
                    continue;
                if (o.getExamTime().equals(o2.getExamTime()))
                    errors.add(new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam time", o, o2)));
            }
        }
    }

    private void checkForGPALimit(List<CSE> courses, Map<Term, Map<Course, Double>> transcript) {
        int unitsRequested = courses.stream().mapToInt(o -> o.getCourse().getUnits()).sum();
        if ((Student.getGpa(transcript) < 12 && unitsRequested > 14) ||
                (Student.getGpa(transcript) < 16 && unitsRequested > 16) ||
                (unitsRequested > 20))
            errors.add(new EnrollmentRulesViolationException(String.format("Number of units (%d) requested does not match GPA of %f", unitsRequested, Student.getGpa(transcript))));
    }

    public boolean hasPassed(Map.Entry<Term, Map<Course, Double>> tr, Course course) {
        for (Map.Entry<Course, Double> r : tr.getValue().entrySet()) {
            if (r.getKey().equals(course) && r.getValue() >= 10)
                return true;
        }
        return false;
    }
}
