package com.core.fullstack.Beans;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Student {
	private String stuName;
	private String stuId;
	private String stuEmail;
	private String mobile;

    private Student(StudentBuilder builder) {
        this.stuName = builder.stuName;
        this.stuId = builder.stuId;
        this.stuEmail = builder.stuEmail;
        this.mobile = builder.mobile;
    }

    public static class StudentBuilder {
        public String stuName;
        public String stuId;
        public String stuEmail;
        public String mobile;

        public StudentBuilder setStuName(String stuName) {
            this.stuName = stuName;
            return this;
        }

        public StudentBuilder setStuId(String stuId) {
            this.stuId = stuId;
            return this;
        }

        public StudentBuilder setStuEmail(String stuEmail) {
            this.stuEmail = stuEmail;
            return this;
        }

        public StudentBuilder setMobile(String mobile) {
            this.mobile = mobile;
            return this;
        }

        public Student build() {
            return new Student(this);
        }
    }
}
