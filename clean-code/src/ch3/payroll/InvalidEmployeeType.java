package ch3.payroll;

public class InvalidEmployeeType extends Exception {
    public InvalidEmployeeType(EmployeeType type) {
        super("알 수 없는 직원 유형입니다: " + type);
    }
}