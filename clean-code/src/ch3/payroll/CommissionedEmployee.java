package ch3.payroll;

public class CommissionedEmployee extends Employee {
    private final Money baseSalary;

    public CommissionedEmployee(EmployeeRecord record) {
        super(record);
        this.baseSalary = new Money(record.baseSalary != null ? record.baseSalary : "0");
    }

    @Override
    public boolean isPayday() {
        return true;
    }

    @Override
    public Money calculatePay() {
        // 기본급 + 인센티브 계산 로직 (여기서는 기본급만 대충 처리)
        return baseSalary.multiply(1.2);
    }

    @Override
    public void deliverPay(Money pay) {
        System.out.println("[수수료제] " + name + "님에게 " + pay + "이 계좌이체 되었습니다.");
    }
}