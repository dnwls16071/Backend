package ch3.payroll;

public abstract class Employee {

    protected EmployeeType type;
    protected String name;

    public Employee(EmployeeRecord record) {
        this.name = record.name;
    }

    public abstract boolean isPayday();
    public abstract Money calculatePay();
    public abstract void deliverPay(Money pay);
}