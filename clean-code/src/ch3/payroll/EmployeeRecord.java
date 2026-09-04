package ch3.payroll;

public class EmployeeRecord {
    public EmployeeType type;
    public String name;

    // 각 유형별 계산에 필요한 원시 데이터들
    public String baseSalary;
    public double hoursWorked;
    public String hourlyRate;

    public EmployeeRecord(EmployeeType type, String name) {
        this.type = type;
        this.name = name;
    }
}
