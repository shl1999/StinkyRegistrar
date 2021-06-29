package domain;

public class CourseSection {
    Course course;
    int section;

    CourseSection(Course course, int section) {
        this.course = course;
        this.section = section;
    }

    public Course getCourse() {
        return course;
    }

    public int getSection() {
        return section;
    }

}
