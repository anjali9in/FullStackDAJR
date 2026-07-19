package com.core.fullstack.designpatterns.creational;

import com.core.fullstack.Beans.Student;

public class BuilderCaller {

    public static void main(String[] args) {
        Builder order = new Builder.CustomOrderBuilder("ORD-1001", "Anjali")
                .deliveryAddress("Bengaluru, India")
                .expressDelivery(true)
                .build();

        Student stud = new Student.StudentBuilder()
                .setStuEmail("Ere")
                .setStuId("123")
                .setStuName("Anjali")
                .setMobile("1234567890")
                .build();

        System.out.println(stud);
    }
}