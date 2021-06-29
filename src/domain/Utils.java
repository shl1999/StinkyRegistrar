package domain;

import domain.Course;
import domain.Term;

import java.util.Map;

public class Utils {
    public static double getGpa(Map<Term, Map<Course, Double>> transcript) {
        double points = 0;
        int totalUnits = 0;
        for (Map.Entry<Term, Map<Course, Double>> tr : transcript.entrySet()) {
            for (Map.Entry<Course, Double> r : tr.getValue().entrySet()) {
                points += r.getValue() * r.getKey().getUnits();
                totalUnits += r.getKey().getUnits();
            }
        }
        double gpa = points / totalUnits;
        return gpa;
    }
}
