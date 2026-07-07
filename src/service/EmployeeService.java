package service;

import model.Employee;
import model.enums.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeService {
    private final List<Employee> employees;

    public EmployeeService() {
        this.employees = new ArrayList<>();
    }

    public void registerEmployee(Employee employee){
        employees.add(employee);
    }

    public Optional<Employee> authenticateByPin(String enteredPin){
        return employees.stream()
                .filter(emp -> emp.isActive() && emp.verifyPin(enteredPin))
                .findFirst();
    }

    /**
     * Helper method to verify if an employee has managerial rights.
     */
    public boolean requireManagerAccess(Employee actingEmployee) {
        if (actingEmployee == null || actingEmployee.getRole() != Role.MANAGER) {
            throw new SecurityException("Access Denied: Manager role required.");
        }
        return true;
    }

    public List<Employee> getEmployees() {
        return this.employees;
    }

    public void setEmployees(List<Employee> loadedEmployees) {
        this.employees.clear();
        this.employees.addAll(loadedEmployees);
    }
}


