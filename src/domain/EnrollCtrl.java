package domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import domain.exceptions.EnrollmentRulesViolationException;
import domain.exceptions.ExceptionList;

public class EnrollCtrl {
    private List<EnrollmentRulesViolationException> errors = new ArrayList<>();

    public void enroll(Student s, List<Offering> courses) throws ExceptionList {
        checkForAlreadyPassedCourses(courses, s.transcript());
        checkForPrerequisiteRequirements(courses, s.transcript());
        checkForDuplicateEnrollRequest(courses);
        checkForConfilictingExamTimes(courses);
        checkForGPALimit(courses, s.transcript());

        if (errors.size() > 0) {
            throw new ExceptionList(errors);
        }

        for (Offering o : courses)
            s.takeCourse(o.getCourse(), o.getSection());
    }

    private void checkForPrerequisiteRequirements(List<Offering> courses, Map<Term, Map<Course, Double>> transcript) {
        for (Offering o : courses) {
            List<Course> prerequisites = o.getCourse().getPrerequisites();
            nextPre:
            for (Course pre : prerequisites) {
                for (Map.Entry<Term, Map<Course, Double>> tr : transcript.entrySet()) {
                    if (hasPassed(tr, pre)) {
                        continue nextPre;
                    }
                }
                errors.add(new EnrollmentRulesViolationException(String.format("The student has not passed %s as a prerequisite of %s", pre.getName(), o.getCourse().getName())));
            }
        }
    }

    private void checkForAlreadyPassedCourses(List<Offering> offerings, Map<Term, Map<Course, Double>> transcript) {
        for (Offering offering : offerings) {
            for (Iterator<Map.Entry<Term, Map<Course, Double>>> iterator = transcript.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<Term, Map<Course, Double>> tr = iterator.next();
                if (hasPassed(tr, offering.getCourse())) {
                    errors.add(new EnrollmentRulesViolationException(String.format("The student has already passed %s", offering.getCourse().getName())));
                }
            }
        }
    }

    private void checkForDuplicateEnrollRequest(List<Offering> offerings) {
        for (Offering offering : offerings) {
            for (Offering offering2 : offerings) {
                if (offering == offering2)
                    continue;
                if (offering.getCourse().equals(offering2.getCourse()))
                    errors.add(new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice", offering.getCourse().getName())));
            }
        }
    }

    private void checkForConfilictingExamTimes(List<Offering> offerings) {
        for (Offering offering : offerings) {
            for (Offering offering2 : offerings) {
                if (offering == offering2)
                    continue;
                if (offering.getExamDate().equals(offering2.getExamDate()))
                    errors.add(new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam time", offering, offering2)));
            }
        }
    }

    private void checkForGPALimit(List<Offering> courses, Map<Term, Map<Course, Double>> transcript) {
        int unitsRequested = courses.stream().mapToInt(o -> o.getCourse().getUnits()).sum();
        if ((Utils.getGpa(transcript) < 12 && unitsRequested > 14) ||
                (Utils.getGpa(transcript) < 16 && unitsRequested > 16) ||
                (unitsRequested > 20))
            errors.add(new EnrollmentRulesViolationException(String.format("Number of units (%d) requested does not match GPA of %f", unitsRequested, Utils.getGpa(transcript))));
    }

    public boolean hasPassed(Map.Entry<Term, Map<Course, Double>> tr, Course course) {
        for (Map.Entry<Course, Double> r : tr.getValue().entrySet()) {
            if (r.getKey().equals(course) && r.getValue() >= 10)
                return true;
        }
        return false;
    }
}
