package domain;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import domain.exceptions.EnrollmentRulesViolationException;

public class EnrollCtrl {
	public void enroll(Student s, List<CSE> courses) throws EnrollmentRulesViolationException {
        checkForAlreadyPassedCourses(courses, s.transcript());
        checkForPrerequisiteRequirements(courses, s.transcript());
        checkForDuplicateEnrollRequest(courses);
        checkForConfilictingExamTimes(courses);
        checkForGPALimit(courses, s.transcript());
        for (CSE o : courses)
			s.takeCourse(o.getCourse(), o.getSection());
	}

    private void checkForPrerequisiteRequirements(List<CSE> courses, Map<Term, Map<Course, Double>> transcript) throws EnrollmentRulesViolationException {
        for (CSE o : courses) {
            List<Course> prereqs = o.getCourse().getPrerequisites();
            nextPre:
            for (Course pre : prereqs) {
                for (Map.Entry<Term, Map<Course, Double>> tr : transcript.entrySet()) {
                    if(hasPassed(tr , pre)){
                        continue nextPre;
                    }
                }
                throw new EnrollmentRulesViolationException(String.format("The student has not passed %s as a prerequisite of %s", pre.getName(), o.getCourse().getName()));
            }
        }
    }

    private void checkForAlreadyPassedCourses(List<CSE> courses, Map<Term, Map<Course, Double>> transcript) throws EnrollmentRulesViolationException {
        for (CSE o : courses) {
            for (Iterator<Map.Entry<Term, Map<Course, Double>>> iterator = transcript.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<Term, Map<Course, Double>> tr = iterator.next();
                if(hasPassed(tr , o.getCourse())){
                    throw new EnrollmentRulesViolationException(String.format("The student has already passed %s", o.getCourse().getName()));
                }
            }
        }
    }

    private void checkForDuplicateEnrollRequest(List<CSE> courses) throws EnrollmentRulesViolationException {
        for (CSE o : courses) {
            for (CSE o2 : courses) {
                if (o == o2)
                    continue;
                if (o.getCourse().equals(o2.getCourse()))
                    throw new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice", o.getCourse().getName()));
            }
        }
    }

    private void checkForConfilictingExamTimes(List<CSE> courses) throws EnrollmentRulesViolationException {
        for (CSE o : courses) {
            for (CSE o2 : courses) {
                if (o == o2)
                    continue;
                if (o.getExamTime().equals(o2.getExamTime()))
                    throw new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam time", o, o2));
            }
		}
    }

    private void checkForGPALimit(List<CSE> courses, Map<Term, Map<Course, Double>> transcript) throws EnrollmentRulesViolationException {
        int unitsRequested = courses.stream().mapToInt(o -> o.getCourse().getUnits()).sum();
        if ((Student.getGpa(transcript) < 12 && unitsRequested > 14) ||
                (Student.getGpa(transcript) < 16 && unitsRequested > 16) ||
                (unitsRequested > 20))
            throw new EnrollmentRulesViolationException(String.format("Number of units (%d) requested does not match GPA of %f", unitsRequested, Student.getGpa(transcript)));
    }

    public boolean hasPassed(Map.Entry<Term, Map<Course, Double>> tr , Course course){
        for (Map.Entry<Course, Double> r : tr.getValue().entrySet()) {
            if (r.getKey().equals(course) && r.getValue() >= 10)
                return true;
        }
        return false;
    }
}
