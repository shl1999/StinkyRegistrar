package domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Student {
    private String id;
    private String name;
    private Map<Term, Map<Course, Double>> transcript;
    private List<CourseSection> currentTerm;

    public Student(String id, String name) {
        this.id = id;
        this.name = name;
        this.transcript = new HashMap<>();
        this.currentTerm = new ArrayList<>();
    }

    public Map<Term, Map<Course, Double>> getTranscript() {
        return transcript;
    }

    public Map<Term, Map<Course, Double>> transcript() {
        return getTranscript();
    }

    public List<CourseSection> getCurrentTerm() {
        return currentTerm;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void takeCourse(Course c, int section) {
        currentTerm.add(new CourseSection(c, section));
    }

    public void addTranscriptRecord(Course course, Term term, double grade) {
        if (!transcript.containsKey(term))
            transcript.put(term, new HashMap<>());
        transcript.get(term).put(course, grade);
    }

}
