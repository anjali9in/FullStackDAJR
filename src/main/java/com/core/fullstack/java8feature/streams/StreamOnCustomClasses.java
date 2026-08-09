package com.core.fullstack.java8feature.streams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.core.fullstack.Beans.Employee;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StreamOnCustomClasses {

	static Logger log = org.slf4j.LoggerFactory.getLogger(StreamOnCustomClasses.class);

	public static void main(String[] args) {
		StreamOnCustomClasses val = new StreamOnCustomClasses();
//		Employee emp = new Employee();
//		List<Employee> dd = val.getData(emp);
		streamOverEmpDep();
	}

	public static void streamOverEmpDep() {

		List<EmpDep> empDepList = Arrays.asList(new EmpDep("John", "E001", "john@tb", "1234567890", "IT", 50000.0),
				new EmpDep("Alice", "E002", "alice@tb", "9876543210", "HR", 60000.0),
				new EmpDep("Bob", "E003", "bob@tb", "5555555555", "Finance", 55000.0),
				new EmpDep("Eve", "E004", "eve@tb", "1111111111", "IT", 70000.0),
				new EmpDep("Charlie", "E005", "charlie@tb", "2222222222", "Finance", 55000.0),
				new EmpDep("David", "E006", "david@tb", "3333333333", "HR", 60000.0),
				new EmpDep("Frank", "E007", "frank@tb", "4444444444", "IT", 65000.0),
				new EmpDep("Grace", "E008", "grace@tb", "5555555555", "Finance", 58000.0));

		List<EmpDep> sortByDeparSal = empDepList.stream()
				.sorted(Comparator.comparing(EmpDep::depName).thenComparing(EmpDep::salary).reversed()).toList();
		sortByDeparSal.forEach(System.out::println);

		Map<String, List<EmpDep>> top2SalEmpByDep = sortByDeparSal.stream()
				.collect(Collectors.groupingBy(EmpDep::depName, Collectors.toList()));

		top2SalEmpByDep.forEach((dep, emp) -> {
			System.out.println("Department: " + dep);
			emp.forEach(System.out::println);
			emp.stream().limit(1).forEach(System.out::println);
		});

		// top n salary employee in each department using limit
		List<EmpDep> sortByDeparSalList = empDepList.stream()
				.sorted(Comparator.comparing(EmpDep::depName).thenComparing(EmpDep::salary).reversed())
				.collect(Collectors.groupingBy(EmpDep::depName, Collectors.toList())).values().stream()
				.map(empList -> empList.stream().limit(1).toList())
				.flatMap(List::stream)
				.toList();

		sortByDeparSalList.forEach(System.out::println);

	}

	public static void streamOverEmp() {

		List<Emp> empList = Arrays.asList(new Emp("John", "E001", "john@tb", "1234567890", 50000.0),
				new Emp("Alice", "E002", "alice@tb", "9876543210", 60000.0),
				new Emp("Bob", "E003", "bob@tb", "5555555555", 55000.0),
				new Emp("Eve", "E004", "eve@tb", "1111111111", 70000.0),
				new Emp("Charlie", "E005", "charlie@tb", "2222222222", 55000.0));

		// transform
		List<Emp> modifiedList = empList.stream()
				.map(empObj -> new Emp(empObj.empName(), empObj.empId(), empObj.empEmail(), empObj.mobile(),
						empObj.salary() * 0.2)) // Increase salary by 20%
				.toList();

		modifiedList.forEach(empObj -> log.info("Modified Employee: Name={}, ID={}, Email={}, Mobile={}, Salary={}",
				empObj.empName(), empObj.empId(), empObj.empEmail(), empObj.mobile(), empObj.salary()));

		// filter
		List<Emp> filteredList = empList.stream().filter(empObj -> empObj.salary() > 55000.0).toList();
		filteredList.forEach(System.out::println);

		// sort
		List<Emp> sortedList = empList.stream().sorted((e1, e2) -> Double.compare(e1.salary(), e2.salary())).toList();
		System.out.println("Sorted List by Salary:");
		sortedList.forEach(System.out::println);

		// sort using comparator
		List<Emp> sortedList2 = empList.stream().sorted(Comparator.comparing(Emp::salary).reversed()).toList();
		System.out.println("Sorted List by Salary using comparator:");
		sortedList2.forEach(System.out::println);

		// top 2 salary employees
		List<Emp> top2SalEmp = empList.stream().sorted(Comparator.comparing(Emp::salary).reversed()).limit(2).toList();
		System.out.println("top2SalEmp List by Salary using comparator:");
		top2SalEmp.forEach(System.out::println);

		// top 2 salary
		List<Double> top2Sal = empList.stream().sorted(Comparator.comparing(Emp::salary).reversed()).limit(2)
				.map(Emp::salary).toList();
		System.out.println("top2Sal List by Salary using comparator:");
		top2Sal.forEach(System.out::println);

	}

	public List<Employee> getData(Employee emp) {
		log.info("inside creating bean :");
		List<Employee> list = new ArrayList<>();
		Employee emp1 = new Employee();
		emp1.setEmpId(emp.getEmpId());
		emp1.setEmpEmail(emp.getEmpEmail());
		emp1.setMobile(emp.getMobile());
		emp1.setEmpName(emp.getEmpName());
		list.add(emp1);

		return list;
	}
}

record Emp(String empName, String empId, String empEmail, String mobile, Double salary) {
}

record EmpDep(String empName, String empId, String empEmail, String mobile, String depName, Double salary) {
}

record Dep(String depName, String depId, String empId) {
}